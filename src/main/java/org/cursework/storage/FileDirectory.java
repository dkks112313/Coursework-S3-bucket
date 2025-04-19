package org.cursework.storage;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class FileDirectory {
    public static String createDirectory(String fullPath, String originalFilename) {
        File theDir = new File(fullPath + File.separator + originalFilename);

        if (!theDir.exists()){
            theDir.mkdirs();
        }

        return theDir.getAbsolutePath();
    }
}
