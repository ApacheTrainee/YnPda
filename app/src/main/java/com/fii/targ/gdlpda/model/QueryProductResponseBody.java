
package com.fii.targ.gdlpda.model;

public class QueryProductResponseBody {
    private String code;
    private String message;
    private String station;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(String code) { this.code = code; }

    public void setMessage(String message) { this.message = message; }

    public String getStation() {
        return station;
    }
}