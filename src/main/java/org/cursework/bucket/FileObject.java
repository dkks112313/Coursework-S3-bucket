package org.cursework.bucket;

import java.io.IOException;

public final class FileObject {
    private String key;
    private MetaData metaData;

    public FileObject(String fileName, String size) throws IOException {
        this.key = fileName;
        this.metaData = new MetaData(fileName, size);
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
