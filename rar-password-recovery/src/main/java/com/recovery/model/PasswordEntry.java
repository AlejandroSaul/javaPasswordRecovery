package com.recovery.model;

public class PasswordEntry {
    private final int excelRow;
    private final String password;

    public PasswordEntry(int excelRow, String password) {
        this.excelRow = excelRow;
        this.password = password;
    }

    public int getExcelRow() {
        return excelRow;
    }

    public String getPassword() {
        return password;
    }
}
