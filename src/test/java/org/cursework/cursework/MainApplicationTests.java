package org.cursework.cursework;

import org.cursework.MainApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MainApplicationTests {
    @Test
    void contextLoads() {
        new SpringApplicationBuilder(MainApplication.class)
                .web(org.springframework.boot.WebApplicationType.NONE)
                .run();
    }

    @Test
    void testInitCreatesDirectoriesAndWritesKey() {
        MainApplication.init(new String[]{});

        File tempDir = new File(Utils.pathToStorage, "temp");
        File dataDir = new File(Utils.pathToStorage, "data");
        File keysDir = new File(Utils.pathToStorage, "keys");
        File dataMainDir = new File(Utils.pathToStorage + "/data", "main");

        assertTrue(tempDir.exists() && tempDir.isDirectory(), "temp directory should exist");
        assertTrue(dataDir.exists() && dataDir.isDirectory(), "data directory should exist");
        assertTrue(keysDir.exists() && keysDir.isDirectory(), "keys directory should exist");
        assertTrue(dataMainDir.exists() && dataMainDir.isDirectory(), "data/main directory should exist");

        File keysFile = new File(keysDir, "keys.txt");
        assertTrue(keysFile.exists(), "keys.txt file should exist");
        assertTrue(keysFile.length() > 0, "keys.txt file should not be empty");
    }

    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
