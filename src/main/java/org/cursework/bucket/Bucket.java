package org.cursework.bucket;

import java.util.List;

public class Bucket {
    private String name;
    private List<FileObject> files;

    public Bucket(String name, List<FileObject> files) {
        this.name = name;
        this.files = files;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FileObject> getFiles() {
        return files;
    }

    public void setFiles(List<FileObject> files) {
        this.files = files;
    }
}
