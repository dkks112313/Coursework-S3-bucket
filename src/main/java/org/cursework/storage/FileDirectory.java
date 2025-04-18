package org.cursework.storage;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class FileDirectory {
    @Value("${path.storage}")
    private String path;

    public String createDirectory(String originalFilename) {
        File theDir = new File(path + File.separator + originalFilename);

        if (!theDir.exists()){
            theDir.mkdirs();
        }

        return theDir.getAbsolutePath();
    }
}
