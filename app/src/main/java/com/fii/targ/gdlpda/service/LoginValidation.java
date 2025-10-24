package com.fii.targ.gdlpda.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fii.targ.gdlpda.conn.ApiResponse;
import com.fii.targ.gdlpda.conn.HttpClient;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LoginValidation {

    private static final String TAG = "LoginValidation";
    private static final String KEY_PREFS_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_DEPARTMENT_ID = "department_id";
    private static final String KEY_EMPLOYEE_ID = "employee_id";



    public static boolean validateLogin(String username, String password, Context context) {
        try {
            LoginRequestBody requestBody = new LoginRequestBody(username, password);
            Log.d("DEBUG", "Constants.BASE_URL: " + Constants.getBaseUrl());
            ApiResponse<LoginResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.LOGIN, requestBody, LoginResponseBody.class);
            if (response.isSuccess()) {
                saveLoginState(true, response.getData().getData(), context);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    

    private static void saveLoginState(boolean isLoggedIn, LoginResponseData data, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.LOGIN_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_PREFS_NAME, data.getUsername());
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_DEPARTMENT_ID, data.getDeptName());
        editor.putString(KEY_EMPLOYEE_ID, data.getEmployeeId());
        editor.apply();
    }


}

class LoginRequestBody {
    private String username;
    private String password;

    public LoginRequestBody(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

class LoginResponseBody {
    private String code;
    private String message;
    private LoginResponseData data;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public LoginResponseData getData() {
        return data;
    }
}

class LoginResponseData {
    private String username;
    private String nickName;
    private String deptName;
    private String employeeId;

    public String getUsername() {
        return username;
    }

    public String getNickName() {
        return nickName;
    }

    public String getDeptName() {
        return deptName;
    }

    public String getEmployeeId() {
        return employeeId;
    }
}