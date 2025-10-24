package com.fii.targ.gdlpda;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fii.targ.gdlpda.model.QueryProductResponseBody;
import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import com.fii.targ.gdlpda.service.QueryProduct;

import androidx.appcompat.app.AppCompatActivity;

public class QueryActivity extends AppCompatActivity {
    private static final String TAG = "QueryActivity";
    ImageButton qrScanButton;
    Button queryButton;
    EditText productSnEditText;
    ProgressBar progressBar;
    TextView resultStation;

    ScannerManager scannerManager;
    private TextView scanMessageTextView;
    private Dialog scanDialog;

    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_PRODUCT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_product);

        productSnEditText = findViewById(R.id.qr_code_input);
        qrScanButton = findViewById(R.id.qr_code_scan);
        queryButton = findViewById(R.id.submit_button);
        progressBar = findViewById(R.id.progress_bar);
        resultStation = findViewById(R.id.query_result);

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
                        productSnEditText.setText(barcodeData);
                        productSnEditText.requestFocus();
//                        productSnEditText.post(() -> {
//                            productSnEditText.setText(barcodeData);
//                        });
                        productSnEditText.setText(barcodeData);
                        Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " Barcode data: " + barcodeData);
                        productSnEditText.clearFocus();
                        queryProductStation(barcodeData);
                    }
                });

            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    Toast.makeText(QueryActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultStation.setText("");
                String productSN = productSnEditText.getText().toString();
                if (productSN.isEmpty()) {
                    Toast.makeText(QueryActivity.this, "产品码不能为空 Mã sản phẩm không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                queryProductStation(productSN);
            }
        });

        qrScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productSnEditText.setText("");
                resultStation.setText("");
                scanMessageTextView.setText("Scan the product SN");

                // Show the scan dialog
                scanDialog.show();

                // Start the scan
                scannerManager.requestScan(SCAN_REQUEST_ID);
            }
        });

    }

    private void queryProductStation(String productSN) {
        progressBar.setVisibility(View.VISIBLE);
        queryButton.setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                QueryProductResponseBody response = QueryProduct.queryProductStation(productSN, QueryActivity.this);
                boolean success = response.getCode().equals("0");
                String message = response.getMessage();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        queryButton.setEnabled(true);

                        if (success) {
                            resultStation.setText(response.getStation());
                            Toast.makeText(QueryActivity.this, "OK", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(QueryActivity.this, "查询成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(QueryActivity.this, "Failed" + message, Toast.LENGTH_SHORT).show();
//                            Toast.makeText(QueryActivity.this, "查询失败" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
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