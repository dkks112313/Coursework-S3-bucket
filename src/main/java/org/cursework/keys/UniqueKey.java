package org.cursework.keys;

import java.util.UUID;

public class UniqueKey {
    private String key;

    public UniqueKey() {
        key = UUID.randomUUID().toString();
    }

    public UniqueKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
