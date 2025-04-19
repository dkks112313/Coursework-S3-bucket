package org.cursework.temp;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetaData {
    private String name;
    private double size;
    private String type;

    private List<File> parts;

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
}
