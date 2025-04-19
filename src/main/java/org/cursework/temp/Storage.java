package org.cursework.temp;

import java.util.List;

public class Storage {
    private String name;
    private List<Bucket> buckets;

    public Storage(String name, List<Bucket> buckets) {
        this.name = name;
        this.buckets = buckets;
    }

    public String getStorageName() {
        return name;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }
}
