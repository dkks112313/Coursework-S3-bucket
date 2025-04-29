package org.cursework;

import org.cursework.keys.UniqueKey;
import org.cursework.storage.FileDirectory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class MainApplication {

    private static void init(String[] args) {
        Environment env = SpringApplication.run(MainApplication.class, args).getEnvironment();
        String getStoragePath = env.getProperty("path.storage");
        FileDirectory.createDirectory(getStoragePath, "data");
        FileDirectory.createDirectory(getStoragePath, "keys");
        FileDirectory.createDirectory(getStoragePath, "data/main");

        UniqueKey key = new UniqueKey();
        FileDirectory.createAndWriteToKeys(getStoragePath, key.getKey());
    }

    public static void main(String[] args) {
        init(args);
    }

}
