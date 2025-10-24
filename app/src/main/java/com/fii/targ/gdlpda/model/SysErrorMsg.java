package com.fii.targ.gdlpda.model;

public class SysErrorMsg {
    
   private String msgID;
    private String errorID;
    private String errorDeviceID;
    private ErrorMsg errorMsg;
    private String errorTime;
    private String resolvedTime;
    private String errorStatus;
    private boolean errorResolved;

    // Getters and setters...

    public String getMsgID() {
        return msgID;
    }

    public String getErrorID() {
        return errorID;
    }

    public String getErrorDeviceID() {
        return errorDeviceID;
    }

    public ErrorMsg getErrorMsg() {
        return errorMsg;
    }

    public String getErrorTime() {
        return errorTime;
    }

    public String getResolvedTime() {
        return resolvedTime;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public boolean isErrorResolved() {
        return errorResolved;
    }

    public String toString() {
        return "msgID: " + msgID + "\n" +
                "Error ID: " + errorID + "\n" +
                "Error Device ID: " + errorDeviceID + "\n" +
                "Error Message: \n" + errorMsg.toString() + "\n" +
                "Error Time: " + errorTime + "\n" +
                "Resolved Time: " + resolvedTime + "\n" +
                "Error Status: " + errorStatus + "\n" +
                "Error Resolved: " + errorResolved;
    }

}
