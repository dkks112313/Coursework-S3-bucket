package org.cursework.bucket;

import org.cursework.json.Json;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetaData extends Json {
    private String name;
    private double size;
    private String type;
    private List<String> parts;

    public MetaData(String name, double size) {
        this.name = name;
        this.size = size;
        this.parts = new ArrayList<>();
    }

    public void createJsonFile() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("ID", "1");
        jsonObject.put("First_Name", "Shikhar");
        jsonObject.put("Last_Name", "Dhawan");
        jsonObject.put("Date_Of_Birth", "1981-12-05");
        jsonObject.put("Place_Of_Birth", "Delhi");
        jsonObject.put("Country", "India");

        try {
            FileWriter file = new FileWriter("E:/output.json");
            file.write(jsonObject.toJSONString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("JSON file created: "+jsonObject);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
