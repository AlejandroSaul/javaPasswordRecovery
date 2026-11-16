package com.recovery.model;

public class RecoveryResult {
    public enum Status {
        FOUND,
        NOT_FOUND,
        CANCELLED,
        ERROR
    }

    private final Status status;
    private final String password;
    private final int excelRow;
    private final int tested;
    private final String message;

    public RecoveryResult(Status status, String password, int excelRow,
                          int tested, String message) {
        this.status = status;
        this.password = password;
        this.excelRow = excelRow;
        this.tested = tested;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getPassword() {
        return password;
    }

    public int getExcelRow() {
        return excelRow;
    }

    public int getTested() {
        return tested;
    }

    public String getMessage() {
        return message;
    }
}
