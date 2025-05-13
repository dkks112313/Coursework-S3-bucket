package org.cursework.cursework;

import org.cursework.bucket.MetaData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MetaDataTests {
    private static MetaData metaData;

    @BeforeAll
    public static void init() {
        metaData = new MetaData("file", "12");
    }

    @Test
    public void testCreateMetaFile() {
        metaData.writeMetaFile();
    }
}
