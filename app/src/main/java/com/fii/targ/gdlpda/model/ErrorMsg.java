package com.fii.targ.gdlpda.model;

public class ErrorMsg {
    

    private String errorSource;
    private String errorType;
    private String errorLevel;
    private String errorMessage;
    private String recoverySteps;
    private String notes;

    // Getters and setters...

    public String getErrorSource() {
        return errorSource;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorLevel() {
        return errorLevel;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getRecoverySteps() {
        return recoverySteps;
    }

    public String getNotes() {
        return notes;
    }

    public String toString() {
        return "\tError Source: " + errorSource + "\n" +
                "\tError Type: " + errorType + "\n" +
                "\tError Level: " + errorLevel + "\n" +
                "\tError Message: " + errorMessage + "\n" +
                "\tRecovery Steps: " + recoverySteps + "\n" +
                "\tNotes: " + notes + "\n";
    }
}