package com.fii.targ.gdlpda.conn;

import java.lang.reflect.Method;

public class ApiResponse<T> {
    private T data;
    private String errorCode;
    private String errorMessage;

    public ApiResponse(T data) {
        this.data = data;
    }

    public ApiResponse(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public T getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccess() {
        if (data != null) {
            try {
                Method getErrorCodeMethod = data.getClass().getMethod("getCode");
                Object errorCodeValue = getErrorCodeMethod.invoke(data);
                return "0".equals(errorCodeValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

