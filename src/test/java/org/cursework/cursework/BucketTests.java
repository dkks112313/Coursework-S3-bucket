package org.cursework.cursework;

import org.cursework.bucket.Bucket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BucketTests {
    private static Bucket bucket;

    @BeforeAll
    public static void init() {
        bucket = new Bucket("name");
    }

    @Test
    public void testGetBucketName() {
        Assertions.assertEquals(bucket.getName(), "name");
    }

    @Test
    public void testSetBucketName() {
        String name = "name";
        bucket.setName(name);
        Assertions.assertEquals(bucket.getName(), "name");
    }
}
