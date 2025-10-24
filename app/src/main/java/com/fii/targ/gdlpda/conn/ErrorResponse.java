package com.fii.targ.gdlpda.conn;

public class ErrorResponse {
    private String code;
    private String message;
    private String details;

    // 无参构造函数（供Gson使用）
    public ErrorResponse() {}

    // Getter方法
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    // Setter方法（可选，根据需要添加）
    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
