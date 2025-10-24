package com.fii.targ.gdlpda;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import com.fii.targ.gdlpda.service.BindOperation;
import com.fii.targ.gdlpda.service.QueryGroundBindingInfoResponse;

public class QueryGroundActivity extends AppCompatActivity {
    private static final String TAG = "QueryGroundActivity";
    ImageButton qrScanButton;
    Button queryButton;
    EditText groundCodeEditText;
    TextView resultStation;

    ScannerManager scannerManager;
    private TextView scanMessageTextView;
    private Dialog scanDialog;

    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_GROUND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_ground);

        groundCodeEditText = findViewById(R.id.ground_code_input);
        qrScanButton = findViewById(R.id.ground_code_scan);
        queryButton = findViewById(R.id.query_button);
        resultStation = findViewById(R.id.bound_cart_text);

        scanDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_scan, null);
        scanDialog.setContentView(dialogView);
        scanDialog.setCancelable(false);
        scanMessageTextView = dialogView.findViewById(R.id.scan_message);

        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> cancelCurrentScan()); 
        
        scannerManager = new ScannerManager(this, new ScannerManager.ScanListener() {
            @Override
            public void onScanResult(int requestId, String barcodeData) {
                runOnUiThread(() -> {
                    onScanComplete();
                    if (requestId == SCAN_REQUEST_ID) {
                        groundCodeEditText.requestFocus();
//                        groundCodeEditText.post(() -> {
//                            groundCodeEditText.setText(barcodeData);
//                        });
                        groundCodeEditText.setText(barcodeData);
                        Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " Barcode data: " + groundCodeEditText.getText().toString());
                        groundCodeEditText.clearFocus();
                        queryBindingInfo();
                    }
                });

            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    Toast.makeText(QueryGroundActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // 查询按钮点击事件
        queryButton.setOnClickListener(v -> queryBindingInfo());
        // 扫描按钮点击事件
        qrScanButton.setOnClickListener(v -> {
            groundCodeEditText.setText("");
            resultStation.setText("");
            Toast.makeText(this, "Scanning ground code...", Toast.LENGTH_SHORT).show();
            scanMessageTextView.setText("Scan the product SN");

            // Show the scan dialog
            scanDialog.show();

            // Start the scan
            scannerManager.requestScan(SCAN_REQUEST_ID);
        });

    }

//    @SuppressLint("SetTextI18n")
    @SuppressLint("SetTextI18n")
    private void queryBindingInfo() {
        resultStation.setText("");
        String groundCode = groundCodeEditText.getText().toString().trim();
        Log.d("DEBUG", "groundCode: " + groundCode);
        if (groundCode.isEmpty()) {
            Toast.makeText(this, "地码不能为空 Mã mặt đất không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // 模拟查询绑定关系
        new Thread(() -> {
            QueryGroundBindingInfoResponse response = BindOperation.queryGroundBindingInfo(groundCode,QueryGroundActivity.this);
            boolean success = response.getCode().equals("0");
            String message = response.getMessage();

//            if (boundInfo == null) {
//                runOnUiThread(() -> {
//                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
////                    Toast.makeText(this, "检索绑定信息失败", Toast.LENGTH_SHORT).show();
//                });
//                return;
//            }

//            String boundCart = boundInfo.cartCode;
            runOnUiThread(() -> {
                queryButton.setEnabled(true);
                if (success) {
                    resultStation.setText(response.getStation());
                    Toast.makeText(QueryGroundActivity.this, "OK", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(QueryActivity.this, "查询成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(QueryGroundActivity.this, "Failed" + message, Toast.LENGTH_SHORT).show();
//                            Toast.makeText(QueryActivity.this, "地码没有找到绑定关系" + message, Toast.LENGTH_SHORT).show();
                }
//                if (boundCart != null && !boundCart.isEmpty()) {
//                    Log.d("DEBUG", "cartCode: " + boundCart);
//                    resultStation.setText("Bound Cart: " + boundCart);
//                    Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
////                    Toast.makeText(this, "查询成功", Toast.LENGTH_SHORT).show();
//                } else {
//                    resultStation.setText("Bound Cart: None");
//                    Toast.makeText(this, "None", Toast.LENGTH_SHORT).show();
////                    Toast.makeText(this, "地码没有找到绑定关系", Toast.LENGTH_SHORT).show();
//                }
            });
        }).start();
    }

    public void cancelCurrentScan() {
        Log.d("DEBUG", "cancel button");
        scannerManager.cancelScan();
        if (scanDialog.isShowing()) {
            Log.d("cancelCurrentScan", "dismiss");
            scanDialog.dismiss();
        }
    }

    private void onScanComplete() {
        // Dismiss the scan dialog
        if (scanDialog.isShowing()) {
            Log.d("onScanComplete", "dismiss");
            scanDialog.dismiss();
        }
    }

}