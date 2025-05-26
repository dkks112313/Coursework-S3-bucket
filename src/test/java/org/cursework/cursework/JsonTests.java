package org.cursework.cursework;

import org.cursework.json.Json;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class JsonTests {

    private static Json json;
    private static File tempDir;

    @BeforeAll
    public static void init() throws IOException {
        tempDir = Files.createTempDirectory("json-test-storage").toFile();

        json = new Json(tempDir.getAbsolutePath());
    }

    @AfterAll
    public static void cleanUp() {
        for (File file : Objects.requireNonNull(tempDir.listFiles())) {
            file.delete();
        }
        tempDir.delete();
    }

    @Test
    public void testOnWriteJson() {
        Map<String, String> map = new HashMap<>();
        map.put("Anna", "bebra");
        map.put("Bebra", "bum");

        json.writeToJson(map);

        List<String> list = Arrays.asList("Anna", "Bebra");

        Map<String, String> map1 = new HashMap<>();
        try {
            map1 = json.readJson(list);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during readJson");
        }

        Assertions.assertEquals(map, map1);
    }

    @Test
    public void testOnReadJson() {
        List<String> list = Arrays.asList("Anna", "Bebra");

        Map<String, String> result = new HashMap<>();
        try {
            result = json.readJson(list);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during readJson");
        }

        Map<String, String> expected = new HashMap<>();
        expected.put("Anna", "bebra");
        expected.put("Bebra", "bum");

        Assertions.assertEquals(expected, result);
    }
}
