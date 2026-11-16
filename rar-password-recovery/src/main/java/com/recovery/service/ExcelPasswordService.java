package com.recovery.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.recovery.model.PasswordEntry;

public class ExcelPasswordService {

    /**
     * Lee la primera columna de la primera hoja.
     * @param excelPath archivo XLSX
     * @param firstDataRowIndex 0 para comenzar en fila 1; 1 si la fila 1 es encabezado
     */
    public List<PasswordEntry> readPasswords(Path excelPath, int firstDataRowIndex)
            throws Exception {
        if (excelPath == null || !Files.isRegularFile(excelPath)) {
            throw new IllegalArgumentException("No se encontró el archivo Excel.");
        }

        List<PasswordEntry> entries = new ArrayList<PasswordEntry>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream input = Files.newInputStream(excelPath);
             Workbook workbook = WorkbookFactory.create(input)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("El Excel no contiene hojas.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = firstDataRowIndex;
                 rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                Cell cell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell == null) {
                    continue;
                }

                // No aplicar trim: los espacios pueden formar parte de la contraseña.
                String password = formatter.formatCellValue(cell);
                if (password != null && !password.isEmpty()) {
                    entries.add(new PasswordEntry(rowIndex + 1, password));
                }
            }
        }

        return entries;
    }
}
