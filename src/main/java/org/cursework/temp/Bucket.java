package org.cursework.temp;

import java.util.List;

public class Bucket {
    private String name;
    private List<FileObject> objects;

    public Bucket(String name, List<FileObject> objects) {
        this.name = name;
        this.objects = objects;
    }

    public String getBucketName() {
        return name;
    }

    public List<FileObject> getFileObjects() {
        return objects;
    }
}
