package org.cursework.storage;

import java.io.File;
import java.nio.file.Paths;

public class FileDirectory {
    public static String createDirectory(String fullPath, String directoryName) {
        try {
            if (directoryName == null || directoryName.isEmpty()
                || fullPath == null) {
                throw new NullPointerException("Exception is null");
            }

            String newPath = Paths.get(fullPath, directoryName).toString();
            File theDir = new File(newPath);

            if (!theDir.exists()){
                theDir.mkdirs();
            }

            return theDir.getAbsolutePath();
        } catch (NullPointerException e) {
            System.err.println(e);
        }

        return null;
    }
}
