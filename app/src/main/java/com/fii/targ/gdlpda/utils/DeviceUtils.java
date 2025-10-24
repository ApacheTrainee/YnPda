package com.fii.targ.gdlpda.utils;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class DeviceUtils {

    private static final String TAG = "DeviceUtils";

    public static String getMacAddress(Context context) {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                Log.d(TAG, "Found wlan0 interface");
                if (!nif.isUp()) {
                    Log.e(TAG, "Network interface wlan0 is not up");
                    return "";
                }

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    Log.e(TAG, "MAC address is null");
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error getting MAC address", ex);
        }
        return "02:00:00:00:00:00"; // Default MAC address
    }
}