package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserRequest {

    public static String readFile(String path) {
        String wholeFile = "";
        try {
            File file = new File(path);
            // Return the whole content of the file
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                wholeFile += myReader.nextLine();
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return wholeFile;
    }

    public static Map<String, Map<String, Map<String, Double>>> parseFile (String path) {
        String wholeFile = readFile(path);
        String[] lines = wholeFile.split("URI : ");
        Map<String, Map<String, Map<String, Double>>> dataMap = new HashMap<>();
        

        for (int i = 1; i < lines.length; i++) {
            String uri =  lines[i].split(":")[0].replace(" ", ""); // Assuming URI ends at the first newline
            Map<String, Map<String, Double>> nodeMap = new HashMap<>();

            Pattern nodePattern = Pattern.compile("nodeIdentifier=([^\\s]+) sensorIdentifier=([^\\s]+) value=([0-9.]+)");
            Matcher matcher = nodePattern.matcher(lines[i]);

            while (matcher.find()) {
                String nodeIdentifier = matcher.group(1);
                String sensorIdentifier = matcher.group(2);
                Double value = Double.parseDouble(matcher.group(3));

                nodeMap.putIfAbsent(nodeIdentifier, new HashMap<>());
                nodeMap.get(nodeIdentifier).put(sensorIdentifier, value);
            }

            dataMap.put(uri, nodeMap);
        }

        // Debug output to check the structure
        dataMap.forEach((uri, nodes) -> {
            System.out.println("URI: " + uri);
            nodes.forEach((node, sensors) -> {
                System.out.println(" Node: " + node);
                sensors.forEach((sensor, value) -> {
                    System.out.println("  Sensor: " + sensor + ", Value: " + value);
                });
            });
        });

        System.out.println(dataMap.keySet());
        System.out.println(dataMap.get("req1").keySet());
        // System.out.println(dataMap.get("req1").keySet());
        return dataMap;
    }

    public static void main(String[] args) {
        ParserRequest test = new ParserRequest();
        test.parseFile("logRegistry.log");
    }

}
