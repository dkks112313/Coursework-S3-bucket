package org.cursework.service;

import org.cursework.bucket.Bucket;
import org.cursework.bucket.FileObject;
import org.cursework.storage.FileDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
        if (Files.exists(path) && !Files.isDirectory(path)) {
            return true;
        } else {
            return false;
        }
    }

    public void saveFileObject(String bucketName, MultipartFile fileToSave) throws IOException {
        String fileName = fileToSave.getOriginalFilename();

        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }

        if (!checkBucketIsExist(bucketName)) {
            throw new IllegalArgumentException("Bucket does not exist");
        }

        String absolute = FileDirectory.createDirectory(Paths.get(storageDirectory, "data", bucketName).toString(), fileName);

        Path mail = Paths.get(absolute, fileName);
        Path targetPath = mail.normalize();

        if (!targetPath.startsWith(Paths.get(absolute, fileName).normalize())) {
            throw new SecurityException("Unsupported filename!");
        }

        Files.copy(fileToSave.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public File getDownloadFileObject(String bucketName, String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        if (!checkBucketIsExist(bucketName)) {
            throw new IllegalArgumentException("Bucket does not exist");
        }

        var fileToDownload = new File(Path.of(getStorageDataDirectory(), bucketName, fileName, fileName).toString());
        System.out.println(fileToDownload.getAbsolutePath());

        if (!Objects.equals(fileToDownload.getParent(), Path.of(getStorageDataDirectory(), bucketName, fileName).toString())) {
            throw new SecurityException("Unsupported filename!");
        }

        if (!fileToDownload.exists()) {
            throw new FileNotFoundException("No file named: " + fileName);
        }

        return fileToDownload;
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
