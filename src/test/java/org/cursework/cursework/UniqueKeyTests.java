package org.cursework.cursework;

import org.cursework.keys.UniqueKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UniqueKeyTests {
    private static UniqueKey uniqueKey;

    @BeforeAll
    public static void init() {
        uniqueKey = new UniqueKey();
    }

    @Test
    public void createUniqueKeyLength() {
        int keyLength = uniqueKey.getKey().length();
        Assertions.assertEquals(keyLength, 43);
    }

    @Test
    public void setUniqueKeyTest() {
        String key = "asdasdq23asdas324";
        uniqueKey.setKey(key);
        String keyGetter = uniqueKey.getKey();
        Assertions.assertEquals(keyGetter, key);
    }
}
