package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OutputGenerator {
    public static void generateCSV(List<String> violations, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.append("Employee,Violation Type\n");

            if (violations.isEmpty()) {
                writer.append("No violations found\n");
            } else {
                for (String violation : violations) {
                    writer.append(violation).append("\n");
                }
            }

            System.out.println("✅ Output successfully written to: " + fileName);
        } catch (IOException e) {
            System.err.println("❌ Error writing to file: " + fileName);
            e.printStackTrace();
        }
    }
}
