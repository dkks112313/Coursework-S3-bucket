package org.cursework.service;

import org.cursework.bucket.Bucket;
import org.cursework.bucket.FileObject;
import org.cursework.storage.FileDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class BucketService {
    @Value("${path.storage}")
    public String storageDirectory;

    private static final Logger log = Logger.getLogger(BucketService.class.getName());

    private static final int UPLOAD_BUFFER_SIZE = 10 * 1024 * 1024;
    private static final int DOWNLOAD_BUFFER_SIZE = 10 * 1024 * 1024;
    private static final AtomicInteger activeOperations = new AtomicInteger(0);

    private Bucket bucket;
    private FileObject fileObject;
    private String bucketName;

    public void performOperationForBucket(String bucketName) {
        bucket = new Bucket(bucketName);
        this.bucketName = bucket.getName();
        log.info("Operating on bucket: " + bucket.getName());
    }

    public String getStorageDataDirectory() {
        return Path.of(storageDirectory, "data").toString();
    }

    public boolean checkBucketIsExist() {
        Path path = Path.of(getStorageDataDirectory(), bucketName);
        return Files.exists(path) && Files.isDirectory(path);
    }

    public List<String> getListFileObjects() {
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

    public void saveFileObject(MultipartFile fileToSave) throws IOException {
        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }

        String fileName = fileToSave.getOriginalFilename();
        if (fileName == null) {
            fileName = "unnamed_file_" + System.currentTimeMillis();
        }

        long size = fileToSave.getSize();
        log.info("Starting upload of file: " + fileName + " (" + size + " bytes) to bucket: " + bucketName);

        if (!checkBucketIsExist()) {
            throw new IllegalArgumentException("Bucket does not exist: " + bucketName);
        }

        String pathToBucket = Paths.get(storageDirectory, "data", bucketName).toString();
        String fileDir = FileDirectory.createDirectory(pathToBucket, fileName);
        String partsDir = FileDirectory.createDirectory(fileDir, "parts");

        fileObject = new FileObject(Paths.get(storageDirectory, "data", bucketName, fileName).toString(), String.valueOf(size));
        fileObject.getMetaData().writeMetaFile();

        activeOperations.incrementAndGet();

        try {
            int partNumber = 0;
            long remainingBytes = fileToSave.getSize();

            ByteBuffer buffer = ByteBuffer.allocateDirect(UPLOAD_BUFFER_SIZE);

            try (InputStream is = fileToSave.getInputStream();
                 ReadableByteChannel inChannel = Channels.newChannel(is)) {

                while (remainingBytes > 0) {
                    Path partPath = Paths.get(partsDir, "part." + partNumber);

                    try (FileOutputStream fos = new FileOutputStream(partPath.toFile());
                         FileChannel outChannel = fos.getChannel()) {

                        buffer.clear();

                        if (buffer.limit() > remainingBytes) {
                            buffer.limit((int)remainingBytes);
                        }

                        int bytesRead = inChannel.read(buffer);
                        if (bytesRead <= 0) break;

                        buffer.flip();
                        outChannel.write(buffer);

                        remainingBytes -= bytesRead;
                        partNumber++;
                    }
                }
            }

            log.info("Successfully uploaded file: " + fileName + " in " + partNumber + " parts");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error during file upload: " + fileName, e);
            cleanupFiles(fileDir);
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        } finally {
            activeOperations.decrementAndGet();
        }
    }

    private void cleanupFiles(String directory) {
        try {
            Files.walkFileTree(Paths.get(directory), new SimpleFileVisitor<>() {
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
            log.log(Level.WARNING, "Failed to clean up directory: " + directory, cleanupEx);
        }
    }

    public File getDownloadFileObject(String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        if (!checkBucketIsExist()) {
            throw new IllegalArgumentException("Bucket does not exist: " + bucketName);
        }

        Path partsDir = Paths.get(getStorageDataDirectory(), bucketName, fileName, "parts");
        if (!Files.exists(partsDir) || !Files.isDirectory(partsDir)) {
            throw new FileNotFoundException("Parts directory not found for file: " + fileName);
        }

        log.info("Starting download assembly of file: " + fileName + " from bucket: " + bucketName);

        cleanupTempFiles();

        File assembledFile = File.createTempFile("download-", "-" + fileName);
        assembledFile.deleteOnExit();

        activeOperations.incrementAndGet();

        try {
            int bufferSize = DOWNLOAD_BUFFER_SIZE;

            try (FileOutputStream fos = new FileOutputStream(assembledFile);
                 FileChannel outputChannel = fos.getChannel()) {
                int maxPartNumber = -1;
                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(partsDir, "part.*")) {
                    for (Path path : dirStream) {
                        String name = path.getFileName().toString().replace("part.", "");
                        try {
                            int partNumber = Integer.parseInt(name);
                            maxPartNumber = Math.max(maxPartNumber, partNumber);
                        } catch (NumberFormatException e) {
                            log.warning("Skipping invalid part file: " + path);
                        }
                    }
                }

                long position = 0;
                for (int partNum = 0; partNum <= maxPartNumber; partNum++) {
                    Path partPath = Paths.get(partsDir.toString(), "part." + partNum);
                    if (!Files.exists(partPath)) {
                        log.warning("Missing part file: " + partPath + ", skipping");
                        continue;
                    }

                    try (FileInputStream fis = new FileInputStream(partPath.toFile());
                         FileChannel inputChannel = fis.getChannel()) {

                        long size = inputChannel.size();
                        if (size == 0) continue;

                        long transferredBytes = 0;
                        long segmentSize = Math.min(bufferSize, 1024 * 1024);

                        while (transferredBytes < size) {
                            long bytesToTransfer = Math.min(segmentSize, size - transferredBytes);
                            long bytes = outputChannel.transferFrom(
                                    inputChannel,
                                    position + transferredBytes,
                                    bytesToTransfer
                            );

                            if (bytes == 0) break;
                            transferredBytes += bytes;
                        }

                        position += transferredBytes;
                    }
                }
                outputChannel.force(true);
            }

            log.info("Successfully assembled file for download: " + fileName +
                    " (size: " + assembledFile.length() + " bytes)");

            return assembledFile;

        } catch (Exception e) {
            log.log(Level.SEVERE, "Error during file download assembly: " + fileName, e);
            if (assembledFile.exists()) {
                assembledFile.delete();
            }
            throw new IOException("Failed to prepare file for download: " + e.getMessage(), e);
        } finally {
            activeOperations.decrementAndGet();
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
                                    log.fine("Deleted old temp file: " + path);
                                }
                            }
                        } catch (IOException e) {
                            log.warning("Error cleaning up temp file: " + path + ", " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.warning("Error during temp files cleanup: " + e.getMessage());
        }
    }

    public void deleteFileObject(String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        if (!checkBucketIsExist()) {
            throw new IllegalArgumentException("Bucket does not exist: " + bucketName);
        }

        Path fileToDelete = Path.of(getStorageDataDirectory(), bucketName, fileName);
        if (!Files.exists(fileToDelete)) {
            throw new FileNotFoundException("No file named: " + fileName);
        }

        log.info("Deleting file: " + fileName + " from bucket: " + bucketName);

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

        log.info("Successfully deleted file: " + fileName);
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

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public String getBucketName() {
        return bucket != null ? bucket.getName() : null;
    }

    public void setBucketName(String bucketName) {
        if (bucket != null) {
            bucket.setName(bucketName);
        }
    }
}
