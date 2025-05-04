package org.cursework.storage;

import org.cursework.bucket.Bucket;

import java.util.List;

public final class Storage {
    private final String name;

    private final List<Bucket> buckets;

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
