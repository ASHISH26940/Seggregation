package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelReader {
    public static List<String[]> readExcelFile(String filePath) {
        List<String[]> dataList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
    
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                boolean isEmptyRow = true;
    
                for (Cell cell : row) {
                    String value = cell.toString().trim();
                    rowData.add(value);
                    if (!value.isEmpty()) isEmptyRow = false;
                }
    
                if (!isEmptyRow) {
                    dataList.add(rowData.toArray(new String[0]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
    }
    
    
}
