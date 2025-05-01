package org.cursework.json;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface JsonOperation {
    Map<String, String> readJson(List<String> listProperties) throws IOException, ParseException;
    void writeToJson(Map<String, String> map);
}
