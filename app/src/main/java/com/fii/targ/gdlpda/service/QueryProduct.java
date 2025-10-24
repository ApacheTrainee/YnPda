package com.fii.targ.gdlpda.service;


import android.content.Context;
import android.content.SharedPreferences;

import com.fii.targ.gdlpda.conn.ApiResponse;
import com.fii.targ.gdlpda.conn.HttpClient;
import com.fii.targ.gdlpda.model.QueryProductResponseBody;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import com.google.gson.Gson;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class QueryProduct {

    private static final String PREFS_NAME = "QueryPrefs";
    private static final String KEY_STATION = "station";

    public static QueryProductResponseBody queryProductStation(String productSN, Context context) {
        try {
            QueryProductRequestBody requestBody = new QueryProductRequestBody(productSN);
            ApiResponse<QueryProductResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.QUERY_STATION, requestBody, QueryProductResponseBody.class);
            
            saveStation(response.getData().getStation(), context);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            QueryProductResponseBody errorResponse = new QueryProductResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }


    private static void saveStation(String station, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_STATION, station);
        editor.apply();
    }
}

class QueryProductRequestBody {
    private String productSN;

    public QueryProductRequestBody(String productSN) {
        this.productSN = productSN;
    }
}
