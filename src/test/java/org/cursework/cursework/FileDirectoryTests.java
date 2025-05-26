package org.cursework.cursework;

import org.cursework.storage.FileDirectory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileDirectoryTests {
    @Test
    public void checkFullPathOnNull() {
        String res = FileDirectory.createDirectory(null, "file");

        Assertions.assertEquals(res, null);
    }

    @Test
    public void checkFullPathOnEmpty() {
        String res = FileDirectory.createDirectory("", "file");
        Assertions.assertEquals(res, null);
    }

    @Test
    public void checkFileDirectoryOnNull() {
        String res = FileDirectory.createDirectory(Utils.path, null);

        Assertions.assertEquals(res, null);
    }

    @Test
    public void checkFileDirectoryOnEmpty() {
        String res = FileDirectory.createDirectory(Utils.path, "");

        Assertions.assertEquals(res, null);
    }
}
