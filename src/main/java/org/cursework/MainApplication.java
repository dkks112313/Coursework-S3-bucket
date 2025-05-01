package org.cursework;

import org.cursework.keys.UniqueKey;
import org.cursework.storage.FileDirectory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.File;

@SpringBootApplication
public class MainApplication {
    public static void main(String[] args) {
        init(args);

        SpringApplication.run(MainApplication.class, args);
    }

    private static void init(String[] args) {
        ConfigurableApplicationContext initContext = new SpringApplicationBuilder(MainApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);

        try {
            Environment env = initContext.getEnvironment();
            String storagePath = env.getProperty("path.storage");

            FileDirectory.createDirectory(storagePath, "data");
            FileDirectory.createDirectory(storagePath, "keys");
            FileDirectory.createDirectory(storagePath, "data/main");

            UniqueKey key = new UniqueKey();
            FileDirectory.createAndWriteKeysFile(storagePath, key.getKey());
        } finally {
            initContext.close();
        }
    }
}
