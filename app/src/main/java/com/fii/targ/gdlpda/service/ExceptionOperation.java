package com.fii.targ.gdlpda.service;

import com.fii.targ.gdlpda.model.ExceptionItem;
import com.fii.targ.gdlpda.conn.ApiResponse;
import com.fii.targ.gdlpda.conn.HttpClient;
import com.fii.targ.gdlpda.model.FetchErrorMsgResponse;
import com.fii.targ.gdlpda.model.SysErrorMsg;

import java.util.ArrayList;
import java.util.List;

public class ExceptionOperation {

    public static boolean clear(String errorUid, boolean solved) {
        try {
            ClearErrorMsgRequest requestBody = new ClearErrorMsgRequest(errorUid, solved);
            ApiResponse<ClearErrorMsgResponse> response = HttpClient.post(Constants.getBaseUrl() + Constants.CLEAR_ERROR, requestBody, ClearErrorMsgResponse.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static FetchErrorMsgResponse getExceptionsFromMCS() {
        try {
            FetchErrorMsgRequest requestBody = new FetchErrorMsgRequest("");
            ApiResponse<FetchErrorMsgResponse> response = HttpClient.post(Constants.getBaseUrl() + Constants.FETCH_ERROR_MSG, requestBody, FetchErrorMsgResponse.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            FetchErrorMsgResponse errorResponse = new FetchErrorMsgResponse();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;       
        }
    }

    public static List<ExceptionItem> convertToExceptionItemList(List<SysErrorMsg> sysErrorMsgs) {
        List<ExceptionItem> exceptionItems = new ArrayList<>();
        if (sysErrorMsgs == null) return exceptionItems;
        for (SysErrorMsg sysErrorMsg : sysErrorMsgs) {
            exceptionItems.add(new ExceptionItem(sysErrorMsg.getMsgID(), sysErrorMsg.getErrorID(), sysErrorMsg.getErrorMsg().getErrorSource(), sysErrorMsg.getErrorMsg().getErrorMessage(), sysErrorMsg.getErrorDeviceID(), sysErrorMsg.getErrorTime(), sysErrorMsg.getErrorMsg().getRecoverySteps(), sysErrorMsg.getErrorMsg().getErrorLevel()));    
        }
        return exceptionItems;
    }
}

class ClearErrorMsgRequest {
    private String errorUid;
    private boolean Solved;

    public ClearErrorMsgRequest(String errorUid, boolean Solved) {
        this.errorUid = errorUid;
        this.Solved = Solved;
    }

    public String getErrorUid() {
        return errorUid;
    }

    public boolean isSolved() {
        return Solved;
    }
}

class ClearErrorMsgResponse {
    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

class FetchErrorMsgRequest {
    private String errorSource;

    public FetchErrorMsgRequest(String errorSource) {
        this.errorSource = errorSource;
    }

    public String getErrorSource() {
        return errorSource;
    }
}

