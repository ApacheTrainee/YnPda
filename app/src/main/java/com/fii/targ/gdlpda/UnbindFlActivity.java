package com.fii.targ.gdlpda;

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

import com.fii.targ.gdlpda.model.FlProductBindingInfo;
import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import com.fii.targ.gdlpda.service.BindOperation;

public class UnbindFlActivity extends AppCompatActivity {
    private static final String TAG = "UnbindFlActivity";
    private EditText EditText;
    private Button unbindButton;
    private ScannerManager scannerManager;
    private Dialog scanDialog;
    private TextView scanMessageTextView;

    private TextView bindingInfoTextView;
    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_GROUND;
    private ImageButton qrCodeScanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fl_unbind);

        EditText = findViewById(R.id.qr_code_input);
        unbindButton = findViewById(R.id.button_unbind_product);
        bindingInfoTextView = findViewById(R.id.info_label);
        qrCodeScanButton = findViewById(R.id.qr_code_scan);

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
                        EditText.requestFocus();
//                        EditText.post(() -> {
//                            EditText.setText(barcodeData);
//                        });
                        EditText.setText(barcodeData);
                        Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " Barcode data: " + barcodeData);
                        EditText.clearFocus();
                        //queryBindingInfo();
                    }
                });
            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    Toast.makeText(UnbindFlActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });

        qrCodeScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText.setText("");
                scanMessageTextView.setText("Scanning the ground Code");
                scanDialog.show();
                scannerManager.requestScan(SCAN_REQUEST_ID);
            }
        });
        EditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scannerManager.requestScan(SCAN_REQUEST_ID);
            }
        });

        unbindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groundCode = EditText.getText().toString();

                if (groundCode.isEmpty()) {
                    Toast.makeText(UnbindFlActivity.this, "地码不能为空 Mã mặt đất không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(() -> {
                    boolean isSuccess = BindOperation.unbindFlProduct(groundCode);
                    runOnUiThread(() -> {
                        if (isSuccess) {
                            Toast.makeText(UnbindFlActivity.this, "OK", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(UnbindFlActivity.this, "解绑地码成功", Toast.LENGTH_SHORT).show();
                            queryBindingInfo();
                        } else {
                            Toast.makeText(UnbindFlActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(UnbindFlActivity.this, "解绑地码失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();

            }
        });
    }

    public void queryBindingInfo() {
        // Simulate QR code scanning
        String scannedCartCode = EditText.getText().toString();
        if (scannedCartCode.isEmpty()) {
            Toast.makeText(UnbindFlActivity.this, "产品码不能是空的 Mã sản phẩm không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            FlProductBindingInfo bindingInfo = BindOperation.queryFlProductBindingInfo(scannedCartCode);

            runOnUiThread(() -> {
                if (bindingInfo != null) {
                    String info = "Rack SN: " + bindingInfo.getFlProduct() + "\n";
                    bindingInfoTextView.setText(info);
                } else {
                    bindingInfoTextView.setText("");
                    Toast.makeText(UnbindFlActivity.this, "query Failed", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(UnbindFlActivity.this, "查询绑定信息失败", Toast.LENGTH_SHORT).show();
                }
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

    @Override
    protected void onPause() {
        super.onPause();
        // 在Activity暂停时取消扫描
        cancelCurrentScan();
        scannerManager.unregisterReceiver();
    }
}