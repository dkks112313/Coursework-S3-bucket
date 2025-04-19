package org.cursework.authentication;

import java.util.UUID;

public class UniqueKey {
    private String key;

    public UniqueKey() {
        key = UUID.randomUUID().toString();
    }

    public UniqueKey(String key) {
        this.key = key;
    }
}
