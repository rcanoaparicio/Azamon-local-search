package localsearchclasses;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CsvWriter {
    public void writeFile(String fileName, ArrayList<ArrayList<String>> data) {
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(fileName);
            for (ArrayList<String> row : data) {
                fileWriter.append(String.join(",", row));
                fileWriter.append("\n");
            }
            System.out.println("CSV file was created");
        } catch (Exception e) {
            System.out.println("CSV ERROR !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }
}
