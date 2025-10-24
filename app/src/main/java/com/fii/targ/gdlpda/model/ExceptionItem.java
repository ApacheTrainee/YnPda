package com.fii.targ.gdlpda.model;

public class ExceptionItem {
    private String errorUid;
    private String errorCode;
    private String errorSource;
    private String errorMessage;
    private String errorLevel;
    private String errorDeviceID;
    private String errorTime;

    private String notes;

    public ExceptionItem(String errorUid, String errorCode, String errorSource, String errorMessage, String errorDeviceID, String errorTime, String notes, String errorLevel) {
        this.errorUid = errorUid;
        this.errorCode = errorCode;
        this.errorSource = errorSource;
        this.errorMessage = errorMessage;
        this.errorTime = errorTime;
        this.errorDeviceID = errorDeviceID;
        this.errorLevel = errorLevel;
        this.notes = notes;
    }


    public String getErrorLevel() {
        return errorLevel;
    }
    public String getNotes() {
        return notes;
    }
    public String getErrorDeviceID() {
        return errorDeviceID;
    }

    public String getErrorSource() {
        return errorSource;
    }

    public String getErrorUid() {
        return errorUid;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorTime() {
        return errorTime;
    }

    @Override
    public String toString() {
        return "Error UID: " + errorUid + "\n" +
               "Error Code: " + errorCode + "\n" +
               "Error Message: " + errorMessage + "\n" +
               "Error Device ID: " + errorDeviceID + "\n" +
               "Error Time: " + errorTime + "\n" +
               "Notes: " + notes;
    }

}