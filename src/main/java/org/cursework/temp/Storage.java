package org.cursework.temp;

import java.util.List;

public class Storage {
    private String storageName;
    private List<Bucket> buckets;

    public Storage(String storageName, List<Bucket> buckets) {
        this.storageName = storageName;
        this.buckets = buckets;
    }

    public String getStorageName() {
        return storageName;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }
}
