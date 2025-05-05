package org.cursework.service;

import org.cursework.bucket.Bucket;
import org.cursework.bucket.FileObject;
import org.cursework.bucket.MetaData;
import org.cursework.storage.FileDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class BucketService {
    private static final Logger LOGGER = Logger.getLogger(BucketService.class.getName());

    private static final int UPLOAD_BUFFER_SIZE = 8 * 1024 * 1024;
    private static final int DOWNLOAD_BUFFER_SIZE = 4 * 1024 * 1024;
    private static final AtomicInteger activeOperations = new AtomicInteger(0);

    @Value("${path.storage}")
    private String storageDirectory;

    private Bucket bucket;

    public String getStorageDataDirectory() {
        return Path.of(storageDirectory, "data").toString();
    }

    private boolean checkBucketIsExist(String bucketName) {
        Path path = Path.of(getStorageDataDirectory(), bucketName);
        return Files.exists(path) && Files.isDirectory(path);
    }

    public List<String> getListFileObjects(String bucketName) {
        List<String> fileObjects = new ArrayList<>();
        File files = new File(getStorageDataDirectory(), bucketName);

        if (!files.exists() || !files.isDirectory()) {
            return fileObjects;
        }

        File[] filesList = files.listFiles();
        if (filesList != null) {
            for(File file : filesList) {
                if (file.isDirectory()) {
                    fileObjects.add(file.getName());
                }
            }
        }

        return fileObjects;
    }

    public void saveFileObject(String bucketName, MultipartFile fileToSave) throws IOException {
        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }

        String fileName = fileToSave.getOriginalFilename();
        if (fileName == null) {
            fileName = "unnamed_file_" + System.currentTimeMillis();
        }

        long size = fileToSave.getSize();
        LOGGER.info("Starting upload of file: " + fileName + " (" + size + " bytes) to bucket: " + bucketName);

        if (!checkBucketIsExist(bucketName)) {
            throw new IllegalArgumentException("Bucket does not exist: " + bucketName);
        }

        String pathToBucket = Paths.get(storageDirectory, "data", bucketName).toString();
        String fileDir = FileDirectory.createDirectory(pathToBucket, fileName);
        String partsDir = FileDirectory.createDirectory(fileDir, "parts");

        File storage = new File(pathToBucket);
        long freeSpace = storage.getFreeSpace();
        if (freeSpace < size * 1.1) {
            throw new IOException("Not enough disk space. Required: " + size + " bytes, available: " + freeSpace + " bytes");
        }

        MetaData meta = new MetaData(Paths.get(storageDirectory, "data", bucketName, fileName).toString(), String.valueOf(size));
        meta.writeMetaFile();

        activeOperations.incrementAndGet();

        byte[] buffer = new byte[UPLOAD_BUFFER_SIZE];
        try (InputStream inputStream = new BufferedInputStream(fileToSave.getInputStream(), UPLOAD_BUFFER_SIZE)) {
            int partNumber = 0;
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                Path partPath = Paths.get(partsDir, "part." + partNumber);
                try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(partPath), UPLOAD_BUFFER_SIZE)) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
                partNumber++;

                Runtime runtime = Runtime.getRuntime();
                long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                long maxMemory = runtime.maxMemory();
                if ((double) usedMemory / maxMemory > 0.7) {
                    LOGGER.info("Memory usage high (" + (usedMemory/1024/1024) + "MB), suggesting GC run");
                    System.gc();
                }
            }
            LOGGER.info("Successfully uploaded file: " + fileName + " in " + partNumber + " parts");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during file upload: " + fileName, e);
            try {
                Files.walkFileTree(Paths.get(fileDir), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (Exception cleanupEx) {
                LOGGER.log(Level.WARNING, "Failed to clean up after failed upload", cleanupEx);
            }
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        } finally {
            activeOperations.decrementAndGet();
            buffer = null;
            System.gc();
        }
    }

    public File getDownloadFileObject(String bucketName, String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        if (!checkBucketIsExist(bucketName)) {
            throw new IllegalArgumentException("Bucket does not exist: " + bucketName);
        }

        Path partsDir = Paths.get(getStorageDataDirectory(), bucketName, fileName, "parts");
        if (!Files.exists(partsDir) || !Files.isDirectory(partsDir)) {
            throw new FileNotFoundException("Parts directory not found for file: " + fileName);
        }

        LOGGER.info("Starting download assembly of file: " + fileName + " from bucket: " + bucketName);

        cleanupTempFiles();

        File assembledFile = File.createTempFile("download-", "-" + fileName);
        assembledFile.deleteOnExit();

        activeOperations.incrementAndGet();

        try {
            List<Path> sortedParts = Files.list(partsDir)
                    .filter(path -> path.getFileName().toString().startsWith("part."))
                    .sorted(Comparator.comparingInt(path -> {
                        String name = path.getFileName().toString().replace("part.", "");
                        return Integer.parseInt(name);
                    }))
                    .toList();

            long requiredSize = 0;
            for (Path part : sortedParts) {
                requiredSize += Files.size(part);
            }

            File tempDir = assembledFile.getParentFile();
            long availableSpace = tempDir.getUsableSpace();
            if (availableSpace < requiredSize * 1.1) {
                throw new IOException("Not enough disk space for download. Required: " + requiredSize +
                        " bytes, available: " + availableSpace + " bytes");
            }

            try (BufferedOutputStream outputStream = new BufferedOutputStream(
                    new FileOutputStream(assembledFile), DOWNLOAD_BUFFER_SIZE)) {

                byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];

                for (Path partPath : sortedParts) {
                    try (BufferedInputStream inputStream = new BufferedInputStream(
                            Files.newInputStream(partPath), DOWNLOAD_BUFFER_SIZE)) {

                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                    }

                    Runtime runtime = Runtime.getRuntime();
                    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                    long maxMemory = runtime.maxMemory();
                    if ((double) usedMemory / maxMemory > 0.7) {
                        LOGGER.info("Memory usage high during download (" + (usedMemory/1024/1024) +
                                "MB), suggesting GC run");
                        buffer = null;
                        System.gc();
                        buffer = new byte[DOWNLOAD_BUFFER_SIZE];
                    }
                }
            }

            LOGGER.info("Successfully assembled file for download: " + fileName +
                    " (size: " + assembledFile.length() + " bytes)");

            return assembledFile;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during file download assembly: " + fileName, e);
            if (assembledFile.exists()) {
                assembledFile.delete();
            }
            throw new IOException("Failed to prepare file for download: " + e.getMessage(), e);
        } finally {
            activeOperations.decrementAndGet();
            System.gc();
        }
    }

    private void cleanupTempFiles() {
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            final long MAX_AGE = 3600000;

            Files.list(tempDir)
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.startsWith("download-") && Files.isRegularFile(path);
                    })
                    .forEach(path -> {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                            long fileAge = System.currentTimeMillis() - attrs.creationTime().toMillis();

                            if (fileAge > MAX_AGE) {
                                boolean deleted = Files.deleteIfExists(path);
                                if (deleted) {
                                    LOGGER.fine("Deleted old temp file: " + path);
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.warning("Error cleaning up temp file: " + path + ", " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            LOGGER.warning("Error during temp files cleanup: " + e.getMessage());
        }
    }

    public void deleteFileObject(String bucketName, String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        if (!checkBucketIsExist(bucketName)) {
            throw new IllegalArgumentException("Bucket does not exist: " + bucketName);
        }

        Path fileToDelete = Path.of(getStorageDataDirectory(), bucketName, fileName);
        if (!Files.exists(fileToDelete)) {
            throw new FileNotFoundException("No file named: " + fileName);
        }

        LOGGER.info("Deleting file: " + fileName + " from bucket: " + bucketName);

        Files.walkFileTree(fileToDelete, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });

        LOGGER.info("Successfully deleted file: " + fileName);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long allocatedMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = allocatedMemory - freeMemory;

        stats.put("activeOperations", activeOperations.get());
        stats.put("maxMemoryMB", maxMemory);
        stats.put("allocatedMemoryMB", allocatedMemory);
        stats.put("freeMemoryMB", freeMemory);
        stats.put("usedMemoryMB", usedMemory);
        stats.put("memoryUsagePercent", (double) usedMemory / maxMemory * 100);

        return stats;
    }

    // Геттеры и сеттеры для работы с объектом Bucket
    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public List<FileObject> getBucketFileObjects() {
        return bucket != null ? bucket.getFiles() : Collections.emptyList();
    }

    public String getBuckedName() {
        return bucket != null ? bucket.getName() : null;
    }

    public void setBucketName(String bucketName) {
        if (bucket != null) {
            bucket.setName(bucketName);
        }
    }

    public void setBucketFileObjects(List<FileObject> fileObjects) {
        if (bucket != null) {
            bucket.setFiles(fileObjects);
        }
    }
}
