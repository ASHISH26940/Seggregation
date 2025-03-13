package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputGenerator {

    // Method to generate CSV output
    public static void generateCSV(List<String[]> violations, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.append("SoD_Rule_Name,User_Name,SoD_Leg_1,Role(Leg_1),Access_Point(Leg_1),")
                  .append("Incident_Path(Leg_1),Conflicting_Role(Leg_2),Conflicting_Access_Point(Leg_2),")
                  .append("Incident_Path(Leg_2),Type_of_Conflict,Department,User_Full_Name,Email_ID,Type_of_User\n");

            if (violations.isEmpty()) {
                writer.append("No violations found\n");
            } else {
                for (String[] violation : violations) {
                    writer.append(String.join(",", violation)).append("\n");
                }
            }

            System.out.println("✅ CSV output successfully written to: " + fileName);
        } catch (IOException e) {
            System.err.println("❌ Error writing CSV to file: " + fileName);
            e.printStackTrace();
        }
    }

    // Method to generate memory-efficient Excel output
    public static void generateExcel(List<String[]> violations, String outputPath) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) { // Keep 100 rows in memory
            Sheet sheet = workbook.createSheet("SoD Violations");

            String[] headers = {
                    "SoD_Rule_Name", "User_Name", "SoD_Leg_1", "Role(Leg_1)", "Access_Point(Leg_1)",
                    "Incident_Path(Leg_1)", "Conflicting_Role(Leg_2)", "Conflicting_Access_Point(Leg_2)",
                    "Incident_Path(Leg_2)", "Type_of_Conflict", "Department", "User_Full_Name",
                    "Email_ID", "Type_of_User"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (String[] violation : violations) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < violation.length; i++) {
                    row.createCell(i).setCellValue(violation[i]);
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }

            System.out.println("✅ Excel output successfully written to: " + outputPath);
        } catch (IOException e) {
            System.err.println("❌ Error writing Excel to file: " + outputPath);
            e.printStackTrace();
        }
    }

    // Method to parse and format raw input data
    public static List<String[]> parseAndFormatData(List<String> rawViolations) {
        List<String[]> formattedViolations = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(\\w+)\\s+(\\S+)\\s+(Privilege Conflict|Cycle Detected)\\s+(.+)"
        );

        for (String violation : rawViolations) {
            Matcher matcher = pattern.matcher(violation);

            if (matcher.find()) {
                String sodRuleName = "Conflict Rule";
                String userName = matcher.group(2);
                String conflictType = matcher.group(3);
                String incidentPath = matcher.group(4);

                String[] incidentPathSegments = incidentPath.split("<->");

                String sodLeg1 = "Leg_1";
                String roleLeg1 = "Role_1";
                String accessPointLeg1 = "AP1";
                String incidentPathLeg1 = incidentPathSegments.length > 0 ? incidentPathSegments[0].trim() : "N/A";
                String conflictingRoleLeg2 = "Role_2";
                String conflictingAccessPointLeg2 = "AP2";
                String incidentPathLeg2 = incidentPathSegments.length > 1 ? incidentPathSegments[1].trim() : "N/A";
                String department = "HR";
                String userFullName = userName.replace(".", " ");
                String emailID = userName.toLowerCase() + "@example.com";
                String typeOfUser = "Employee";

                formattedViolations.add(new String[]{
                        sodRuleName, userName, sodLeg1, roleLeg1, accessPointLeg1,
                        incidentPathLeg1, conflictingRoleLeg2, conflictingAccessPointLeg2,
                        incidentPathLeg2, conflictType, department, userFullName, emailID, typeOfUser
                });
            }
        }
        return formattedViolations;
    }
}
