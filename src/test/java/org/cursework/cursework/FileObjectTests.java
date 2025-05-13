package org.cursework.cursework;

import org.cursework.bucket.FileObject;
import org.cursework.bucket.MetaData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FileObjectTests {
    private static FileObject fileObject;

    @BeforeAll
    public static void init() throws IOException {
        fileObject = new FileObject("file", "12");
    }

    @Test
    public void testCheckNameFileObject() {
        Assertions.assertEquals(fileObject.getKey(), "file");
    }
}
