package org.cursework.cursework;

import org.cursework.json.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonTests {
    private static Json json;

    @BeforeAll
    public static void init() {
        json = new Json("C:\\Users\\ovcha\\IdeaProjects\\curse-work");
    }

    @Test
    public void testOnWriteJson() {
        Map<String, String> map = new HashMap<>();
        map.put("Anna", "bebra");
        map.put("Bebra", "bum");

        json.writeToJson(map);

        List<String> list = new ArrayList<>();
        list.add("Anna");
        list.add("Bebra");

        Map<String, String> map1 = new HashMap<>();
        try {
            map1 = json.readJson(list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(map, map1);
    }

    @Test
    public void testOnReadJsom() {
        List<String> list = new ArrayList<>();
        list.add("Anna");
        list.add("Bebra");

        Map<String, String> map = new HashMap<>();
        try {
            map = json.readJson(list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> st = new HashMap<>();
        st.put("Anna", "bebra");
        st.put("Bebra", "bum");

        Assertions.assertEquals(map, st);
    }
}
