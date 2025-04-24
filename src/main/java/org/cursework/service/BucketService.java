package org.cursework.service;

import org.cursework.bucket.Bucket;
import org.cursework.bucket.FileObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BucketService {
    private Bucket bucket;

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
