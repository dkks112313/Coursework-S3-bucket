package org.cursework.storage;

import java.io.File;

public class FileDirectory {
    public static String createDirectory(String fullPath, String directoryName) {
        try {
            if (directoryName == null || directoryName.isEmpty()
                || fullPath == null || fullPath.isEmpty()) {
                throw new NullPointerException("Exception is null");
            }

            String newPath = fullPath + File.separator + directoryName;
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
