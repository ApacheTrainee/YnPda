package com.fii.targ.gdlpda.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.fii.targ.gdlpda.conn.ApiResponse;
import com.fii.targ.gdlpda.conn.HttpClient;
import com.fii.targ.gdlpda.model.HeartbeatResponse;
import com.fii.targ.gdlpda.utils.DeviceUtils;

public class HeartbeatService extends Service {
    private Handler handler;
    private Runnable heartbeatRunnable;
    private static final long HEARTBEAT_INTERVAL = 30000; // 30 seconds
    private boolean showToast = true;
    private static String deviceID;
    private static String username;
    private static final String KEY_PREFS_NAME = "user_name";

    @Override
    public void onCreate() {
        super.onCreate();
        deviceID = DeviceUtils.getMacAddress(this);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_PREFS, Context.MODE_PRIVATE);

        username = sharedPreferences.getString(KEY_PREFS_NAME, "");
        handler = new Handler(Looper.getMainLooper());
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                postHeartbeat(true);
                handler.postDelayed(this, HEARTBEAT_INTERVAL);
            }
        };

        // Start sending heartbeat periodically
        handler.post(heartbeatRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop sending heartbeat
        handler.removeCallbacks(heartbeatRunnable);

        // Send heartbeat with online status false
        postHeartbeat(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    // sendHeartbeat
    public static HeartbeatResponse sendHeartbeat(boolean onlineStatus) {
        try {
            Log.d("HeartbeatService", "onlineStatus: " + onlineStatus );
            HeartbeatRequest requestBody = new HeartbeatRequest(deviceID, onlineStatus, username);
            ApiResponse<HeartbeatResponse> response = HttpClient.post(Constants.getBaseUrl() + Constants.HEART_BEART, requestBody, HeartbeatResponse.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            HeartbeatResponse errorResponse = new HeartbeatResponse();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public void postHeartbeat(boolean onlineStatus) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HeartbeatResponse response = sendHeartbeat(onlineStatus);
                if (response == null) {
                    Log.d("HeartbeatService", "Failed to send heartbeat");
                    return;
                }
                boolean success = response.getCode().equals("0");
                if (success) {
                    Log.d("HeartbeatService", "Heartbeat sent successfully");
                } else {
                    Log.d("HeartbeatService", "Failed to send heartbeat");
                }
            }
        }).start();
        
    }
}




class HeartbeatRequest {
    private String deviceID;
    private boolean onlineStatus;
    private String user;

    public HeartbeatRequest(String deviceID, boolean onlineStatus, String user) {
        this.deviceID = deviceID;
        this.onlineStatus = onlineStatus;
        this.user = user;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public boolean isOnlineStatus() {
        return onlineStatus;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "HeartbeatRequest{" +
                "deviceID='" + deviceID + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}

