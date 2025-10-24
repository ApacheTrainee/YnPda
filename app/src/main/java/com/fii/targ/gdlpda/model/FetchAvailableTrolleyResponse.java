package com.fii.targ.gdlpda.model;

import com.fii.targ.gdlpda.ui.TrolleyProduct;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FetchAvailableTrolleyResponse {

    private String code;
    private String message;
    private List<CartInfo> data;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<CartInfo> getData() {
        return data;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}