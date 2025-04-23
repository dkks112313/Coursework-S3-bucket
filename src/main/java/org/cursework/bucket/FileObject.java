package org.cursework.bucket;

import java.io.IOException;

public class FileObject {
    private String key;
    private Byte[] data;
    private MetaData metaData;

    public FileObject(String fileName, Byte[] data, MetaData metaData) throws IOException {
        this.key = fileName;
        this.data = data;
        this.metaData = metaData;
    }
}
