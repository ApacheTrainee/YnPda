package com.fii.targ.gdlpda.service;

import com.fii.targ.gdlpda.conn.ApiResponse;
import com.fii.targ.gdlpda.conn.HttpClient;
import com.fii.targ.gdlpda.model.CartInfo;
import com.fii.targ.gdlpda.model.FetchAvailableTrolleyResponse;
import com.fii.targ.gdlpda.model.TolleryTransportResponseBody;
import com.fii.targ.gdlpda.ui.TrolleyInfo;

import java.util.ArrayList;
import java.util.List;

public class TrolleyFlTransporter {

    public static boolean call(String cellId, String cartCode) {
        try {
            TolleryTransportRequestBody requestBody = new TolleryTransportRequestBody(cellId, cartCode);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_Fl_TROLLEY, requestBody, TolleryTransportResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean call(String cellId) {
        try {
            TolleryTransportRequestBody requestBody = new TolleryTransportRequestBody(cellId);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_Fl_TROLLEY, requestBody, TolleryTransportResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static TolleryTransportResponseBody send(String cellId) {
        try {
            TolleryTransportSendRequestBody requestBody = new TolleryTransportSendRequestBody(cellId);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_Fl_TROLLEY, requestBody, TolleryTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            TolleryTransportResponseBody errorResponse = new TolleryTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }


    public static FetchAvailableTrolleyResponse fetchAvailableTrolleys(String cellId) {
        try {
            FetchAvailableTrolleyRequestBody requestBody = new FetchAvailableTrolleyRequestBody(cellId);
            ApiResponse<FetchAvailableTrolleyResponse> response = HttpClient.post(Constants.getBaseUrl() + Constants.FETCH_AVAILABLE_TROLLEY, requestBody, FetchAvailableTrolleyResponse.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            FetchAvailableTrolleyResponse errorResponse = new FetchAvailableTrolleyResponse();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static List<TrolleyInfo> convertToTrolleyInfoList(List<CartInfo> cartInfos) {
        List<TrolleyInfo> trolleyInfos = new ArrayList<>();
        if (cartInfos == null) return trolleyInfos;
        for (CartInfo cartInfo : cartInfos) {
            trolleyInfos.add(new TrolleyInfo(cartInfo.getCartCode(), cartInfo.getCellId()));
        }
        return trolleyInfos;
    }
}
