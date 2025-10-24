package com.fii.targ.gdlpda.model;


import java.util.List;

public class FetchErrorMsgResponse {
    private String code;
    private String message;
    private List<SysErrorMsg> data;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<SysErrorMsg> getData() {
        return data;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
