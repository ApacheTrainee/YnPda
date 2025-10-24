package com.fii.targ.gdlpda.service;

import android.util.Log;

public class Constants {
    public static final String IP = "10.81.175.67";  // 生产环境：10.81.175.67
//    public static final String IP = "10.81.134.19";
//    public static final String IP = "10.81.175.67";
    public static final String PORT = "8003";   // 生产环境：8003
//    public static final String PORT = "9090";
    public static final String SERVICE_PORT = "9999";
    public static final String BASE_URL = "http://" + IP + ":" + PORT;

    public static String SettingIP = "";
    public static String SettingPort = "";

    public static final String LOGIN_PREFS = "LoginPrefs";
    public static final String LOGIN = "/api/pdaToMcs/login";
    public static final String UNBIND_PRODUCTS = "/api/pdaToMcs/unbindCartProducts";
    public static final String UNBIND_GROUND = "/api/pdaToMcs/unbindCartPosition";
    public static final String BIND_PRODUCTS = "/api/pdaToMcs/bindCartProducts";
    public static final String BIND_GROUND = "/api/pdaToMcs/bindCartPosition";

    public static final String BIND_FL_PRODUCT = "/api/pdaToMcs/bindForkPosition";
    public static final String UNBIND_FL_PRODUCT = "/api/pdaToMcs/unbindForkPosition";

    public static final String CLEAR_ERROR = "/api/pdaToMcs/notifyErrorClear";

    public static final String QUERY_STATION = "/api/pdaToMcs/queryProductStation";
    public static final String QUERY_BINDING = "/api/pdaToMcs/queryCartBindingInfo";
    public static final String QUERY_GROUND_BINDING = "/api/pdaToMcs/queryGroundBindingInfo";
    public static final String QUERY_FL_BINDING = "/api/pdaToMcs/queryFlProductBindingInfo";
    public static final String SEND_TROLLEY = "/api/pdaToMcs/sendTrolleyByAGV";
    public static final String CALL_TROLLEY = "/api/pdaToMcs/callTrolleyByAGV";
    public static final String SEND_Fl_TROLLEY = "/api/pdaToMcs/sendTrolleyByAGF";
    public static final String CALL_Fl_TROLLEY = "/api/pdaToMcs/callTrolleyByAGF";
    public static final String FETCH_AVAILABLE_TROLLEY = "/api/pdaToMcs/fetchAvailableTrolley";

    public static final String HEART_BEART = "/api/pdaToMcs/heartBeat";
    public static final String FETCH_ERROR_MSG = "/api/pdaToMcs/fetchErrorMsg";

    public static final String QUERY_TASK = "/api/pdaToMcs/taskQuery";
    public static final String CANCEL_TASK = "/api/pdaToMcs/taskCancel";

    // call和send自选库区
    public static final String SEND_FETCH_STORAGE = "/api/pdaToMcs/sendfetchStorage";
    public static final String SEND_FETCH_LOCATION = "/api/pdaToMcs/sendfetchLocation";
    public static final String CALL_FIND_STORAGE = "/api/pdaToMcs/callfindStorage";
    public static final String CALL_FIND_LOCATION = "/api/pdaToMcs/callfindLocation";
    public static final String SEND_SELECTED = "/api/pdaToMcs/sendSelected";
    public static final String CALL_SELECTED = "/api/pdaToMcs/callSelected";
    public static final String SEND_FETCH_STORAGE_AGF = "/api/pdaToMcs/sendfetchStorageAGF";
    public static final String SEND_FETCH_LOCATION_AGF = "/api/pdaToMcs/sendfetchLocationAGF";
    public static final String CALL_FIND_STORAGE_AGF = "/api/pdaToMcs/callfindStorageAGF";
    public static final String CALL_FIND_LOCATION_AGF = "/api/pdaToMcs/callfindLocationAGF";
    public static final String SEND_SELECTED_AGF = "/api/pdaToMcs/sendSelectedAGF";
    public static final String CALL_SELECTED_AGF = "/api/pdaToMcs/callSelectedAGF";
    // 半自动请求
    public static final String ACTION_REQUEST = "/api/pdaToMcs/actionRequest";

    public static final String CONTENT_TYPE = "application/json";
    public static final String TOKEN = "Bearer pda-fixed-token";


    public static void setSettingIP(String ip) {
        Log.d("Constants", "SettingIP: " + ip);
        SettingIP = ip;
        getBaseUrl();
    }

    public static void setSettingPort(String port) {
        Log.d("Constants", "SettingPort: " + port);
        SettingPort = port;
    }

    private static String getIP() {
        if (SettingIP == "") {
            return IP;
        }
        return SettingIP;
    }

    private static String getPort() {
        if (SettingPort == "") {
            return PORT;
        }
        return SettingPort;
    }
    
    public static String getBaseUrl() {
        String url =  "http://" + getIP() + ":" + getPort();
        Log.d("Constants", "getBaseUrl: " + url);
        return url;
    }
}
