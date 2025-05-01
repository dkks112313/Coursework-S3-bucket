package org.cursework.bucket;

import org.cursework.json.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaData extends Json {
    private String name;
    private String size;

    public MetaData(String name, String size) {
        super(name);

        this.name = name;
        this.size = size;
    }

    public void writeMetaFile() {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("size", size);

        super.writeToJson(map);
    }

    public void readMetaFile() {
        List<String> files = new ArrayList<>();

        try {
            super.readJson(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

}
