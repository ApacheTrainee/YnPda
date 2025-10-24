package com.fii.targ.gdlpda.service;

public class QueryGroundBindingInfoResponse {
    private String code;
    private String message;
    private String station;
    //private GroundBindingInfo data;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

//    public GroundBindingInfo getData() {
//        return data;
//    }

    public void setCode(String code) { this.code = code; }

    public void setMessage(String message) { this.message = message; }
    public String getStation() {
        return station;
    }

}
