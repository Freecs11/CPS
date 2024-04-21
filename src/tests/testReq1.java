package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class testReq1 {

    public String readFile(String path) {
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

    public void parseFile(String path) {
        String wholeFile = readFile(path);
        String[] lines = wholeFile.split("Final Query result");
        for (String line : lines) {
            System.out.println(line);
        }
    }

    public static void main(String[] args) {
        testReq1 test = new testReq1();
        test.parseFile("logRegistry.log");
    }

}
