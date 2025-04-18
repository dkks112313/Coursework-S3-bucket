package org.cursework.temp;

import java.util.List;

public class Bucket {
    private String bucketName;
    private List<FileObject> fileObjects;

    public Bucket(String bucketName, List<FileObject> fileObjects) {
        this.bucketName = bucketName;
        this.fileObjects = fileObjects;
    }

    public String getBucketName() {
        return bucketName;
    }

    public List<FileObject> getFileObjects() {
        return fileObjects;
    }
}
