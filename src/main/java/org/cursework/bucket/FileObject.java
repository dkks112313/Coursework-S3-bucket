package org.cursework.bucket;

import java.io.IOException;

public class FileObject {
    private String key;

    private MetaData metaData;

    public FileObject(String fileName, MetaData metaData) throws IOException {
        this.key = fileName;
        this.metaData = metaData;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }
}
