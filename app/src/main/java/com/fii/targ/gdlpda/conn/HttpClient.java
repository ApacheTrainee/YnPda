package com.fii.targ.gdlpda.conn;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


import com.fii.targ.gdlpda.service.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class HttpClient {

    public static <T> ApiResponse<T> post(String urlString, Object requestBody, Class<T> responseClass) {
        return post(urlString, requestBody, responseClass, Constants.TOKEN);
    }
/*
    public static <T> ApiResponse<T> post(String urlString, Object requestBody, Class<T> responseClass, String token) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            setPostDefaultHeader(connection, token);

            Gson gson = new Gson();
            String jsonRequestBody = gson.toJson(requestBody);
             Log.d("DEBUG", "post request: " + jsonRequestBody);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
             Log.d("DEBUG", "get resp: " + responseCode);
            try (Scanner scanner = new Scanner(
                    responseCode == HttpURLConnection.HTTP_OK ? connection.getInputStream() : connection.getErrorStream(),
                    "UTF-8")) {
                String responseBody = scanner.useDelimiter("\\A").next();
                T data = gson.fromJson(responseBody, responseClass);
                return new ApiResponse<>(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("500", e.getMessage());
        }
    }
*/
public static <T> ApiResponse<T> post(String urlString, Object requestBody, Class<T> responseClass, String token) {
    HttpURLConnection connection = null;
    try {
        URL url = new URL(urlString);
        connection = (HttpURLConnection) url.openConnection();

        // 设置请求属性

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");
        if (token != null) {
//            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Authorization",  token);
        }

        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        Log.d("DEBUG", "POST设置请求属性");
        // 写入请求体
        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(requestBody);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonRequestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            Log.d("DEBUG", "post request: " + jsonRequestBody);
        }

        // 读取响应
        int responseCode = connection.getResponseCode();
        Log.d("DEBUG", "POST读取响应: " + responseCode);
        InputStream inputStream = responseCode >= 200 && responseCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();

        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
            // ✅ 关键：打印原始响应内容
            Log.d("DEBUG", "Raw Response: " + responseBody.toString());

            // 解析响应
            if (responseCode >= 200 && responseCode < 300) {
                // 成功响应，解析为数据类型
                T data = gson.fromJson(responseBody.toString(), responseClass); // 打印解析后的对象
                Log.d("DEBUG", "POST响应解析为数据类型" + data.toString());
                return new ApiResponse<>(data);
            } else {
                // 错误响应，解析错误信息
                ErrorResponse error = gson.fromJson(responseBody.toString(), ErrorResponse.class);
                String errorMsg = (error != null) ? error.getMessage() : "Unknown error";
                Log.e("ERROR", "POST错误响应: " + responseCode + " - " + errorMsg); // 错误日志
                return new ApiResponse<>(String.valueOf(responseCode), error != null ? error.getMessage() : "Unknown error");
            }
        }
    } catch (MalformedURLException e) {
        Log.e("ERROR", "POST错误：无效URL" + urlString, e);
        return new ApiResponse<>("INVALID_URL", e.getMessage());
    } catch (IOException e) {
        Log.e("ERROR", "POST错误：网络错误", e);
        return new ApiResponse<>("NETWORK_ERROR", e.getMessage());
    } catch (JsonSyntaxException e) {
        Log.e("ERROR", "POST错误：解析错误", e);
        return new ApiResponse<>("PARSE_ERROR", e.getMessage());
    } catch (Exception e) {
        Log.e("ERROR", "POST错误：内部错误" + e.getMessage(), e);
        return new ApiResponse<>("INTERNAL_ERROR", e.getMessage());
    } finally {
        if (connection != null) {
            connection.disconnect();
        }
    }
}


    public static void setPostDefaultHeader(HttpURLConnection connection, String token) {
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
        int TIMEOUT_MILLIS = 10000; // 10 seconds
        connection.setRequestProperty("Content-Type", Constants.CONTENT_TYPE);
        if (token != "") {
            connection.setRequestProperty("Authorization", token);
        }
        
        connection.setDoOutput(true);
        connection.setConnectTimeout(TIMEOUT_MILLIS);
        connection.setReadTimeout(TIMEOUT_MILLIS);
    }

}