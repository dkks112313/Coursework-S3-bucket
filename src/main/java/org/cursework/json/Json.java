package org.cursework.json;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Json implements JsonOperation {
    private String jsonFilePath;

    public Json(String jsonFile) {
        this.jsonFilePath = jsonFile + File.separator + "meta.json";
    }

    @Override
    public Map<String, String> readJson(List<String> listProperties)
            throws IOException, ParseException {
        Map<String, String> map = new HashMap<>();
        Object obj = new JSONParser().parse(new FileReader(jsonFilePath));
        JSONObject j = (JSONObject) obj;

        for (String string : listProperties) {
            map.put(string, (String) j.get(string));
        }

        return map;
    }

    @Override
    public void writeToJson(Map<String, String> map) {
        JSONObject jsonObject = new JSONObject();

        for (Map.Entry<String, String> entity : map.entrySet()) {
            jsonObject.put(entity.getKey(), entity.getValue());
        }

        try {
            FileWriter file = new FileWriter(jsonFilePath);
            file.write(jsonObject.toJSONString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
