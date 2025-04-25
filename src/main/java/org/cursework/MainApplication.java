package org.cursework;

import org.cursework.storage.FileDirectory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        Environment env = SpringApplication.run(MainApplication.class, args).getEnvironment();
        String getStoragePath = env.getProperty("path.storage");
        FileDirectory.createDirectory(getStoragePath, "data");
        FileDirectory.createDirectory(getStoragePath, "keys");
        FileDirectory.createDirectory(getStoragePath, "data/main");
    }

}
