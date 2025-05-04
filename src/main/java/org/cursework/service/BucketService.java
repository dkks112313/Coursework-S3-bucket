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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class BucketService {
    @Value("${path.storage}")
    private String storageDirectory;

    private Bucket bucket;

    public String getStorageDataDirectory() {
        return Path.of(storageDirectory, "data").toString();
    }

    private boolean checkBucketIsExist(String bucketName) {
        Path path = Path.of(getStorageDataDirectory(), bucketName);
        if (Files.exists(path) && Files.isDirectory(path)) {
            return true;
        } else {
            return false;
        }
    }

    public List<String> getListFileObjects(String bucketName) {
        List<String> fileObjects = new ArrayList<>();
        File files = new File(getStorageDataDirectory(), bucketName);

        for(File file : Objects.requireNonNull(files.listFiles())) {
            fileObjects.add(file.getName());
        }

        return fileObjects;
    }

    public void saveFileObject(String bucketName, MultipartFile fileToSave) throws IOException {
        String fileName = fileToSave.getOriginalFilename();
        long size = fileToSave.getSize();

        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }

        if (!checkBucketIsExist(bucketName)) {
            throw new IllegalArgumentException("Bucket does not exist");
        }

        String pathToBucket = Paths.get(storageDirectory, "data", bucketName).toString();

        String abs = FileDirectory.createDirectory(pathToBucket, fileName);
        String absolute = FileDirectory.createDirectory(abs, "parts");

        MetaData meta = new MetaData(Paths.get(storageDirectory, "data", bucketName, fileName).toString(), String.valueOf(size));
        meta.writeMetaFile();

        try (var i = fileToSave.getInputStream()) {
            byte[] buffer = new byte[1024 * 1024 * 1024];
            int partNumber = 0;
            int bytesRead;

            while ((bytesRead = i.read(buffer)) != -1) {
                Path partPath = Paths.get(absolute, "part." + partNumber);
                try (OutputStream out = Files.newOutputStream(partPath)) {
                    out.write(buffer, 0, bytesRead);
                }
                partNumber++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getDownloadFileObject(String bucketName, String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        if (!checkBucketIsExist(bucketName)) {
            throw new IllegalArgumentException("Bucket does not exist");
        }

        Path partsDir = Paths.get(getStorageDataDirectory(), bucketName, fileName, "parts");
        if (!Files.exists(partsDir) || !Files.isDirectory(partsDir)) {
            throw new FileNotFoundException("Parts directory not found for file: " + fileName);
        }

        File assembledFile = File.createTempFile("download-", "-" + fileName);
        assembledFile.deleteOnExit();

        System.out.println("DEBUG: bucket = " + bucketName + ", file = " + fileName);
        System.out.println("DEBUG: Looking in path: " + Paths.get(getStorageDataDirectory(), bucketName, fileName, "parts"));

        try (OutputStream outputStream = new FileOutputStream(assembledFile)) {
            Files.list(partsDir)
                    .filter(path -> path.getFileName().toString().startsWith("part."))
                    .sorted(Comparator.comparingInt(path -> {
                        String name = path.getFileName().toString().replace("part.", "");
                        return Integer.parseInt(name);
                    }))
                    .forEach(partPath -> {
                        try (InputStream inputStream = Files.newInputStream(partPath)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }

        return assembledFile;
    }

    public void deleteFileObject(String bucketName, String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        if (!checkBucketIsExist(bucketName)) {
            throw new IllegalArgumentException("Bucket does not exist");
        }

        var fileToDelete = Path.of(getStorageDataDirectory(), bucketName, fileName);
        if (!Files.exists(fileToDelete)) {
            throw new FileNotFoundException("No file named: " + fileName);
        }

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
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public List<FileObject> getBucketFileObjects() {
        return bucket.getFiles();
    }

    public String getBuckedName() {
        return bucket.getName();
    }

    public void setBucketName(String bucketName) {
        bucket.setName(bucketName);
    }

    public void setBucketFileObjects(List<FileObject> fileObjects) {
        bucket.setFiles(fileObjects);
    }
}
