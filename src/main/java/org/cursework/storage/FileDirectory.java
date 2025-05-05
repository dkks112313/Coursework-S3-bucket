package org.cursework.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class FileDirectory {
    public static String createDirectory(String fullPath, String directoryName) {
        try {
            if (directoryName == null || directoryName.isEmpty()
                || fullPath == null || fullPath.isEmpty()) {
                throw new NullPointerException("Exception is null");
            }

            String newPath = Paths.get(fullPath, directoryName).toString();
            File theDir = new File(newPath);

            if (!theDir.exists()){
                theDir.mkdirs();
            }

            return theDir.getAbsolutePath();
        } catch (NullPointerException e) {
            System.err.println(e);
        }

        return null;
    }

    public static void createAndWriteKeysFile(String fullPath, String data) {
        String path = Path.of(fullPath, "keys", "keys.txt").toString();

        try {
            File myObj = new File(path);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        writeToKeysFile(path, data);
    }

    private static void writeToKeysFile(String fullPath, String data) {
        try {
            FileWriter myWriter = new FileWriter(fullPath);
            myWriter.write(data);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static String readFileKey(String fullPath) {
        String path = Path.of(fullPath, "keys", "keys.txt").toString();

        try {
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            String data = myReader.nextLine();
            myReader.close();

            return data;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return "";
    }
}
