package org.cursework.service;

import org.cursework.storage.FileDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

@Service
public class StorageService {

    @Value("${path.storage}")
    private String storageDirectory;

    public String getStorageDirectory() {
        return Path.of(storageDirectory, "data").toString();
    }

    public boolean createBucket(String bucketName) {
        File folder = new File(getStorageDirectory());

        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().equals(bucketName)) {
                    throw new IllegalArgumentException("Bucket name already exists");
                }
            }
        }

        FileDirectory.createDirectory(getStorageDirectory(), bucketName);
        return true;
    }

    public void deleteBucket(String bucketName) {
        File folder = new File(getStorageDirectory(), bucketName);
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
           throw new IllegalArgumentException("Bucket does not exist");
        }

        Arrays.stream(files).forEach((file) -> {
            if(file.getName().equals(bucketName)) file.delete();
        });
    }

}
