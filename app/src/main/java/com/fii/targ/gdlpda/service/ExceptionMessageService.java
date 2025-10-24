package com.fii.targ.gdlpda.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.fii.targ.gdlpda.model.ExceptionItem;
import com.fii.targ.gdlpda.model.SysErrorMsg;
import com.google.gson.Gson;

import org.json.JSONObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

import java.io.IOException;
import java.util.Map;

public class ExceptionMessageService extends Service {

    private static final String TAG = "ExceptionMsgService";
    private static final int PORT = Integer.parseInt(Constants.SERVICE_PORT);
    private MyHTTPD server;

    @Override
    public void onCreate() {
        super.onCreate();
        server = new MyHTTPD(PORT);
        try {
            server.start();
            Log.d(TAG, "server.start");
            Log.d(TAG, "HTTP server started on port " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
            Log.d(TAG, "server.stop");
            Log.d(TAG, "HTTP server stopped");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class MyHTTPD extends NanoHTTPD {

        public MyHTTPD(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            if (Method.POST.equals(session.getMethod()) && "/api/mcsToPda/notifyErrorMsg".equals(session.getUri())) {
                try {
                    Log.d(TAG, "Response serve");
                    Map<String, String> files = new java.util.HashMap<>();
                    session.parseBody(files);
                    String requestBody = files.get("postData");
                    Gson gson = new Gson();
                    NotifyErrorMsgRequest notifyErrorMsgRequest = gson.fromJson(requestBody, NotifyErrorMsgRequest.class);
                    SysErrorMsg sysErrorMsg = notifyErrorMsgRequest.getSysErrorMsg();

                    // Convert SysErrorMsg to ExceptionItem
                    ExceptionItem exceptionItem = new ExceptionItem(
                            sysErrorMsg.getMsgID(),
                            sysErrorMsg.getErrorID(),
                            sysErrorMsg.getErrorMsg().getErrorSource(),
                            sysErrorMsg.getErrorMsg().getErrorMessage(),
                            sysErrorMsg.getErrorDeviceID(),
                            sysErrorMsg.getErrorTime(),
                            sysErrorMsg.getErrorMsg().getNotes(),
                            sysErrorMsg.getErrorMsg().getErrorLevel() 
                    );

                    Log.d(TAG, "exceptionItem: " + exceptionItem.toString());
                    // Send a broadcast to update the RecyclerView
                    Intent intent = new Intent("com.fii.targ.gdlpda.UPDATE_EXCEPTION");
                    intent.putExtra("errorUid", exceptionItem.getErrorUid());
                    intent.putExtra("errorSource", exceptionItem.getErrorSource());
                    intent.putExtra("errorCode", exceptionItem.getErrorCode());
                    intent.putExtra("errorMessage", exceptionItem.getErrorMessage());
                    intent.putExtra("errorDeviceID", exceptionItem.getErrorDeviceID());
                    intent.putExtra("errorTime", exceptionItem.getErrorTime());
                    intent.putExtra("notes", exceptionItem.getNotes());
                    intent.putExtra("errorLevel", exceptionItem.getErrorLevel());
                    sendBroadcast(intent);

                    return NanoHTTPD.newFixedLengthResponse(Status.OK, "application/json", "{\"code\":\"0\",\"message\":\"Success\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                    return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "application/json", "{\"code\":\"-1\",\"message\":\"Error processing request\"}");
                }
            } else if (Method.POST.equals(session.getMethod()) && "/api/mcsToPda/notifyErrorMsgClear".equals(session.getUri())) {
                try {
                    Log.d(TAG, "Response serve");
                    Map<String, String> files = new java.util.HashMap<>();
                    session.parseBody(files);
                    String requestBody = files.get("postData");
                    Gson gson = new Gson();
                    NotifyErrorMsgClearRequest notifyErrorMsgClearRequest = gson.fromJson(requestBody, NotifyErrorMsgClearRequest.class);
                    String sysErrorMsgID = notifyErrorMsgClearRequest.getSysErrorMsgID();

                    // Send a broadcast to update the RecyclerView
                    Intent intent = new Intent("com.fii.targ.gdlpda.CLEAR_EXCEPTION");
                    intent.putExtra("errorUid", sysErrorMsgID);
                    Log.d(TAG, "Clear sysErrorMsgID: " + sysErrorMsgID);
                    sendBroadcast(intent);

                    return NanoHTTPD.newFixedLengthResponse(Status.OK, "application/json", "{\"code\":\"0\",\"message\":\"Success\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                    return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "application/json", "{\"code\":\"-1\",\"message\":\"Error processing request\"}");
                }
            }else {
                return NanoHTTPD.newFixedLengthResponse(Status.NOT_FOUND, "application/json", "{\"code\":\"-1\",\"message\":\"Not Found\"}");
            }
        }
    }
}



class NotifyErrorMsgRequest {
    private SysErrorMsg sysErrorMsg;

    public SysErrorMsg getSysErrorMsg() {
        return sysErrorMsg;
    }

    public void setSysErrorMsg(SysErrorMsg sysErrorMsg) {
        this.sysErrorMsg = sysErrorMsg;
    }
}

class NotifyErrorMsgClearRequest {
    private String sysErrorMsgID;

    public String getSysErrorMsgID() {
        return sysErrorMsgID;
    }

    public void setSysErrorMsgID(String sysErrorMsgID) {
        this.sysErrorMsgID = sysErrorMsgID;
    }
}