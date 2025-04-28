package org.cursework.keys;

import java.security.SecureRandom;
import java.util.Base64;

public class UniqueKey {
    private static final int KEY_LENGTH = 32;
    private String key;

    public UniqueKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[KEY_LENGTH];
        secureRandom.nextBytes(keyBytes);

        key = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
