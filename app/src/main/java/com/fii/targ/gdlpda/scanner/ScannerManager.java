package com.fii.targ.gdlpda.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_L10_GROUND;
import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_L10_PRODUCT;
import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_L11_CART;
import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_L11_GROUND;
import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_L11_PRODUCT;
import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_VN_AGF_CART;
import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_VN_AGF_GROUND;
import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_VN_AGF_PRODUCT;
import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_VN_AGV_CART;
import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_VN_AGV_GROUND;
import static com.fii.targ.gdlpda.scanner.Constants.SCAN_TYPE_VN_AGV_PRODUCT;
import static com.fii.targ.gdlpda.scanner.ScannerParser.GroundCodeParser;
import static com.fii.targ.gdlpda.scanner.ScannerParser.L10CartBarcodeParser;
import static com.fii.targ.gdlpda.scanner.ScannerParser.L10ProductBarcodeParser;
import static com.fii.targ.gdlpda.scanner.ScannerParser.L11ProductBarcodeParser;
import static com.fii.targ.gdlpda.scanner.ScannerParser.ProductBarcodeParser;
import static com.fii.targ.gdlpda.scanner.ScannerParser.StdBarcodeParser;
import static com.fii.targ.gdlpda.scanner.ScannerParser.vnCartBarcodeParser;
import static com.fii.targ.gdlpda.scanner.ScannerParser.vnGroundCodeParser;
import static com.fii.targ.gdlpda.scanner.ScannerParser.vnProductBarcodeParser;

import com.action.scanenginesdk.ScanEngineApi;
import com.action.scanengine.api.IScanDataListener;
import com.action.scanengine.api.ScanData;
import com.action.scanengine.api.SdkConnectCallback;
import com.fii.targ.gdlpda.BindProductActivity;


public class ScannerManager {

    private static final String TAG = "ScannerManager";
    private static final String ACTION_BARCODE_DATA = "com.honeywell.decode.intent.action.BARCODE_DATA";
    private static final String EXTRA_BARCODE_DATA = "data";

    /**
     * Honeywell DataCollection Intent API
     * Claim scanner
     * Permissions:
     * "com.honeywell.decode.permission.DECODE"
     */
    private static final String ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER";

    /**
     * Honeywell DataCollection Intent API
     * Release scanner claim
     * Permissions:
     * "com.honeywell.decode.permission.DECODE"
     */
    private static final String ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER";

    /**
     * Honeywell DataCollection Intent API
     * Optional. Sets the scanner to claim. If scanner is not available or if extra
     * is not used,
     * DataCollection will choose an available scanner.
     * Values : String
     * "dcs.scanner.imager" : Uses the internal scanner
     * "dcs.scanner.ring" : Uses the external ring scanner
     */
    private static final String EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER";
    /**
     * Honeywell DataCollection Intent API
     * Optional. Sets the profile to use. If profile is not available or if extra is
     * not used,
     * the scanner will use factory default properties (not "DEFAULT" profile
     * properties).
     * Values : String
     */
    private static final String EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE";
    /**
     * Honeywell DataCollection Intent API
     * Optional. Overrides the profile properties (non-persistent) until the next
     * scanner claim.
     * Values : Bundle
     */
    private static final String EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES";

    private Context context;
    private ScanListener scanListener;
    private BroadcastReceiver scanReceiver;

    private CountDownLatch latch;
    private String scanResult;

    private Queue<Integer> scanRequestQueue = new LinkedList<>();
    private boolean isScanning = false;
    private static final long SCAN_TIMEOUT = 10; // Timeout in seconds

    private ScanEngineApi scanEngineApi;

    // 添加取消标志
    private volatile boolean isCancelled = false;

    // 添加线程池
    //private final ExecutorService scanExecutor = Executors.newSingleThreadExecutor();

    public ScannerManager(Context context, ScanListener scanListener) {
        this.context = context;
        this.scanListener = scanListener;
        latch = new CountDownLatch(1);
        initReceiver();
        initScanner();
    }

    private void initScanner(){
        IScanDataListener scanDatalistener= new IScanDataListener.Stub(){
            @Override
            public void onScanResult(List<ScanData> list)throws RemoteException {
                String result = "";
                StringBuilder resultText = new StringBuilder();
                int i = 0;
                resultText.append("解析结果:\n");
                if (list != null && !list.isEmpty()) {
                    for(ScanData data :list) {
                        //这里获取到扫描头解到的码，ScanData是一个对象，包含条码内容,码质，坐标...
                        //System.out.println(data.getData());
                        result = data.getData();
                        i = i + 1;
                        Log.d(TAG, "Scan result"+ i +":" + result);
                        // 解析条码数据
                        //                       Map<String, String> parsedData = BarcodeParser.parseBarcode(result);
                        // 显示解析结果
//                        for (Map.Entry<String, String> entry : parsedData.entrySet()) {
//                            // 格式化日期显示
//                            String value = entry.getKey().contains("Date") ?
//                                    BarcodeParser.formatDate(entry.getValue()) :
//                                    entry.getValue();
//
//                            resultText.append(entry.getKey()).append(": ").append(value).append("\n");
//                        }
//                        resultText.append("\n-----------------\n");

                    }
                    scanResult = result;
                    latch.countDown();
                    scanEngineApi.stopScan();
                    //showToast("扫描成功，共识别 " + list.size() + " 个条码");
                }
            }
        };

        SdkConnectCallback connectCallback = new SdkConnectCallback(){
            @Override
            public void onConnected() {
                scanEngineApi.registerScanDataListener(scanDatalistener);
            }

            @Override
            public void onDisconnected() {

            }
        };
        scanEngineApi = new ScanEngineApi(this.context,connectCallback);
        // 设置基本扫描参数
        scanEngineApi.setScannerEnable(true);
        scanEngineApi.setSoundEnable(true);
        scanEngineApi.setAimEnable(true);
        scanEngineApi.setIllBrightnessLevel(5);
    }

    private void showToast(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
    }
    private String getErrorMsg(int errorCode) {
        switch (errorCode) {
            case -2: return "SDK未初始化";
            case -1: return "参数错误";
            case 0: return "IPC未知错误";
            default: return "未知错误 (" + errorCode + ")";
        }
    }

    private void initReceiver() {
        scanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "[Debug] onReceive: " + intent.getAction());
                if (ACTION_BARCODE_DATA.equals(intent.getAction())) {
                    /*
                     * These extras are available:
                     * "version" (int) = Data Intent Api version
                     * "aimId" (String) = The AIM IdentifierAPI DOCUMENTATION
                     * Honeywell Android Data Collection Intent API
                     * "charset" (String) = The charset used to convert "dataBytes" to "data" string
                     * "codeId" (String) = The Honeywell Symbology Identifier
                     * "data" (String) = The barcode data as a string
                     * "dataBytes" (byte[]) = The barcode data as a byte array
                     * "timestamp" (String) = The barcode timestamp
                     */
                    int version = intent.getIntExtra("version", 0);
                    if (version >= 1) {
                        String aimId = intent.getStringExtra("aimId");
                        String charset = intent.getStringExtra("charset");
                        String codeId = intent.getStringExtra("codeId");
                        String data = intent.getStringExtra("data");
                        // DEBUG. Trace value
                        // String dataBytesStr = "";
                        // byte[] dataBytes = intent.getByteArrayExtra("dataBytes");
                        // if (dataBytes == null) {
                        // dataBytesStr = new String(dataBytes);
                        // }
                        // String dataBytesStr = bytesToHexString(dataBytes);
                        Object data2 = intent.getExtras().get("dataBytes");
                        if (data2 instanceof byte[]) {
                            byte[] dataBytes = (byte[]) data2;
                            Log.d(TAG, "Data bytes: " + bytesToHexString(dataBytes));
                            // Handle byte[] data
                        } else if (data2 instanceof String) {
                            String dataString = (String) data2;
                            Log.d(TAG, "Data string: " + dataString);
                            // Handle String data
                        } else {
                            Log.d(TAG, "[DEBUG] Unexpected data type: "
                                    + (data2 != null ? data2.getClass().getName() : "null"));
                        }
                        // String dataBytesStr = intent.getStringExtra("dataBytes");
                        String timestamp = intent.getStringExtra("timestamp");
                        String text = String.format(
                                "Data: %s\n" +
                                        "Charset: %s\n" +
                        // "Bytes:%s\n" +
                                        "AimId: %s\n" +
                                        "CodeId: %s\n" +
                                        "Timestamp: %s\n",
                                data, charset, aimId, codeId, timestamp);
                        Log.d(TAG, text);

                        scanResult = data;
                        latch.countDown();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(ACTION_BARCODE_DATA);
        context.registerReceiver(scanReceiver, filter);
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_BARCODE_DATA);
        context.registerReceiver(scanReceiver, filter);
        claimScanner();
    }

    public void unregisterReceiver() {
        context.unregisterReceiver(scanReceiver);
        releaseScanner();
    }

    public interface ScanListener {
        void onScanResult(int requestId, String barcodeData);

        void onScanError(int requestId, String error);
    }

    private void claimScanner() {
        Bundle properties = new Bundle();
        properties.putBoolean("DPR_DATA_INTENT", true);
        properties.putString("DPR_DATA_INTENT_ACTION", ACTION_BARCODE_DATA);
        context.sendBroadcast(new Intent(ACTION_CLAIM_SCANNER)
                .setPackage("com.intermec.datacollectionservice")
                .putExtra(EXTRA_SCANNER, "dcs.scanner.imager")
                .putExtra(EXTRA_PROFILE, "MyProfile1")
                .putExtra(EXTRA_PROPERTIES, properties));
    }

    private void releaseScanner() {
        context.sendBroadcast(new Intent(ACTION_RELEASE_SCANNER).setPackage("com.intermec.datacollectionservice"));
    }

    private String bytesToHexString(byte[] arr) {
        String s = "[]";
        if (arr != null) {
            s = "[";
            for (int i = 0; i < arr.length; i++) {
                s += "0x" + Integer.toHexString(arr[i]) + ", ";
            }
            s = s.substring(0, s.length() - 2) + "]";
        }
        return s;
    }

    public void startScanning(Context context) {
        Intent intent = new Intent("com.honeywell.aidc.action.ACTION_CONTROL_SCANNER");
        intent.putExtra("com.honeywell.aidc.extra.EXTRA_SCAN", true);
        context.sendBroadcast(intent);
    }

    public void stopScanning(Context context) {
        Intent intent = new Intent("com.honeywell.aidc.action.ACTION_CONTROL_SCANNER");
        intent.putExtra("com.honeywell.aidc.extra.EXTRA_SCAN", false);
        context.sendBroadcast(intent);
    }

//    public void cancelScan() {
//        latch.countDown();
//    }

    // 添加取消扫描方法
    public void cancelScan() {
        synchronized (this) {
            isCancelled = true;
            Log.d(TAG, "cancelScan()|取消标志设一： " + isCancelled);
            // 中断等待
            if (latch != null && latch.getCount() > 0) {
                latch.countDown(); // 中断等待
                Log.d(TAG, "中断扫描");
            }

            // 停止扫描引擎
            if (scanEngineApi != null) {
                scanEngineApi.stopScan();
            }

        }
    }

    private String scan(int requestId) throws InterruptedException {
        int result = scanEngineApi.startScan();
        if (result == 1) {
            Log.d(TAG, "开始扫描");
        } else {
            Log.d(TAG, "启动扫描失败");
        }

        scanResult = "";
        latch = new CountDownLatch(1);
//        synchronized (this) {
//            scanResult = "";
//            latch = new CountDownLatch(1);
//            isCancelled = false; // 重置取消标志
//            Log.d(TAG, "processNextScanRequest()|重置取消标志：" + isCancelled);
//        }

        // 使用带超时的等待，但添加中断检查
        final long startTime = System.currentTimeMillis();
        final long timeoutMillis = SCAN_TIMEOUT * 1000;

        while (true) {
            // 检查取消请求
            if (isCancelled) {
                Log.d(TAG, "扫描被用户取消");
                throw new InterruptedException("Scan cancelled by user");
            }

            // 检查是否超时
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= timeoutMillis) {
                Log.d(TAG, "扫描超时");
                throw new InterruptedException("Scan timed out");
            }

            // 尝试等待一小段时间
            try {
                if (latch.await(200, TimeUnit.MILLISECONDS)) {
                    // 正常获取到结果，但是要检查是否是取消导致的
                    if (isCancelled) {
                        Log.d(TAG, "扫描被用户取消");
                        throw new InterruptedException("Scan cancelled by user");
                    }
                    Log.d(TAG, "[DEBUG] scan: " + requestId);
                    return scanResult;
                }
            } catch (InterruptedException e) {
                if (isCancelled) {
                    Log.d(TAG, "扫描被用户取消");
                    throw new InterruptedException("Scan cancelled by user");
                }
                // 如果不是因为取消，重新抛出异常（虽然不太可能发生）
                throw e;
            }
        }
    }

    public synchronized void requestScan(int requestId) {
        Log.d(TAG, "requestScan: " + requestId);
        scanRequestQueue.add(requestId);
        if (!isScanning) {
            processNextScanRequest();
        }
    }

    public synchronized void clearScanRequestQueue() {
        scanRequestQueue.clear();
    }

    private synchronized void processNextScanRequest() {
        if (!scanRequestQueue.isEmpty()) {
            int requestId = scanRequestQueue.poll();
            //Log.d(TAG, "processNextScanRequest: " + requestId);
            isScanning = true;
            isCancelled = false; // 重置取消标志
            Log.d(TAG, "processNextScanRequest()|重置取消标志：" + isCancelled);
            new Thread(() -> {
                try {
                    // 检查是否已被取消
                    if (isCancelled) {
                        throw new InterruptedException("Scan cancelled by user");
                    }
                    String result = scan(requestId);
                    Log.d(TAG, "processNextScanRequest|result: " + result);
                    String scanresult = "";
                    int scanType = filterScanType(requestId);
                    Log.d(TAG, "requestId: " + requestId + " scanType: " + scanType);
                    scanresult = filterResult(scanType, result);
                    // 再次检查是否已被取消
                    if (!isCancelled) {
                        scanListener.onScanResult(requestId, scanresult);
                    }
                }  catch (InterruptedException e) {
                    if ("Scan cancelled by user".equals(e.getMessage())) {
                        // 用户取消，不报告错误
                        Log.d(TAG, "Scan cancelled by user,类型： " + requestId);
                    } else {
                        Log.d(TAG, "InterruptedException " + e.getMessage());
                        scanListener.onScanError(requestId, "扫描超时 Hết thời gian quét");
//                        scanListener.onScanError(requestId, "Scan timed out");
                    }
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "IllegalArgumentException " + e.getMessage());
//                    scanListener.onScanError(requestId, "Invalid code");
                    scanListener.onScanError(requestId, "无效的码 Mã không hợp lệ");
                } finally {
                    synchronized (this) {
                        isScanning = false;
                        // 只有未被取消时才处理下一个请求
                        if (!isCancelled) {
                            processNextScanRequest();
                        }
                    }
                }
            }).start();
        }
    }

    public static int filterScanType(int scanType) throws IllegalArgumentException {
        // Remove the last two digits
        int baseType = (scanType / 100) * 100;
        switch (baseType) {
            case Constants.SCAN_TYPE_L10_CART:
            case SCAN_TYPE_L10_GROUND:
                return baseType;
            case SCAN_TYPE_L10_PRODUCT:
                return baseType;
            case SCAN_TYPE_L11_CART:
                return baseType;
            case SCAN_TYPE_L11_GROUND:
                return baseType;
            case SCAN_TYPE_L11_PRODUCT:
                return baseType;
            case SCAN_TYPE_VN_AGV_GROUND:
                return baseType;
            case SCAN_TYPE_VN_AGV_CART:
                return baseType;
            case SCAN_TYPE_VN_AGV_PRODUCT:
                return baseType;
            case SCAN_TYPE_VN_AGF_GROUND:
                return baseType;
            case SCAN_TYPE_VN_AGF_CART:
                return baseType;
            case SCAN_TYPE_VN_AGF_PRODUCT:
                return baseType;
            default:
                Log.d(TAG, "无效扫描类型: " + scanType);
                throw new IllegalArgumentException("无效扫描类型: " + scanType);
        }
    }

    private String filterResult(int scanType, String rawResult) throws IllegalArgumentException {
        String result = rawResult;
        try {

            switch (scanType) {

                case Constants.SCAN_TYPE_L10_CART:
                    result = L10CartBarcodeParser(rawResult);
                    return result;
                case Constants.SCAN_TYPE_L11_CART:
                    return result;
                case Constants.SCAN_TYPE_L10_GROUND:
                    result = GroundCodeParser(rawResult);
                    return result;
                case Constants.SCAN_TYPE_L11_GROUND:
                    result = GroundCodeParser(rawResult);
                    return result;
                case Constants.SCAN_TYPE_L10_PRODUCT:
                    // result = L10ProductBarcodeParser(rawResult);
                    result = ProductBarcodeParser(rawResult);
                    return result;
                case Constants.SCAN_TYPE_L11_PRODUCT:
                    // result = L11ProductBarcodeParser(rawResult);
                    result = ProductBarcodeParser(rawResult);
                    return result;
                case Constants.SCAN_TYPE_VN_AGV_CART:
                    result = vnCartBarcodeParser(rawResult);
                    return result;
                case Constants.SCAN_TYPE_VN_AGV_GROUND:
                    result = vnGroundCodeParser(rawResult);
                    return result;
                case Constants.SCAN_TYPE_VN_AGV_PRODUCT:
                    result = vnProductBarcodeParser(rawResult);
                    return result;

                default:
                    Log.d(TAG, "无效扫描类型: " + scanType);
                    return "";
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException in resultFilter: " + e.getMessage(), e);
            throw new IllegalArgumentException("Invalid code");
        }
    }

//    public void shutdown() {
//        synchronized (this) {
//            scanExecutor.shutdownNow();
//
//            // 停止扫描引擎
//            if (scanEngineApi != null) {
//                scanEngineApi.stopScan();
//            }
//
//            // 清空队列
//            scanRequestQueue.clear();
//
//            Log.d(TAG, "ScannerManager 已关闭");
//        }
//    }
}
