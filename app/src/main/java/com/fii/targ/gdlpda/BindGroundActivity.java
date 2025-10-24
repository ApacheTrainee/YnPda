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

import com.fii.targ.gdlpda.model.AutoTransportResponseBody;
import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import com.fii.targ.gdlpda.service.BindOperation;

import androidx.appcompat.app.AppCompatActivity;

import com.fii.targ.gdlpda.service.TrolleyTransporter;


public class BindGroundActivity extends AppCompatActivity {
    private static String TAG = "BindGroundActivity";
    private EditText cartCodeEditText;
    private EditText groundCodeEditText;
    private EditText productCodeEditText;
    private EditText boxSizeEditText;
    private ImageButton cartScanButton;
    private ImageButton groundScanButton;
    private ImageButton productScanButton;
    private Button bindButton;

    private ScannerManager scannerManager;
    private TextView scanMessageTextView;
    private Dialog scanDialog;
    private volatile boolean isActive = true;

    private static final int SCAN_REQUEST_ID_GROUND = Constants.SCAN_TYPE_VN_AGV_GROUND;
    private static final int SCAN_REQUEST_ID_CART = Constants.SCAN_TYPE_VN_AGV_CART;
    private static final int SCAN_REQUEST_ID_PRODUCT = Constants.SCAN_TYPE_VN_AGV_PRODUCT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_ground);

        cartCodeEditText = findViewById(R.id.qr_code_input);
        cartScanButton = findViewById(R.id.qr_code_scan);
        groundCodeEditText = findViewById(R.id.ground_code_input);
        groundScanButton = findViewById(R.id.ground_code_scan);
        productCodeEditText = findViewById(R.id.product_code_input);
        productScanButton = findViewById(R.id.product_code_scan);
        boxSizeEditText = findViewById(R.id.box_size_input);
        bindButton = findViewById(R.id.submit_button);
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
                    String request = barcodeData;
                    onScanComplete();
                    if (!isActive || isFinishing()) return;
                    switch (requestId) {
                        case SCAN_REQUEST_ID_CART:
                            cartCodeEditText.requestFocus();
//                            cartCodeEditText.post(() -> {
//                                cartCodeEditText.setText(request);
//                            });
                            cartCodeEditText.setText(request);
                            Log.d("Scan_request", "Scan back cartCode:" + request);
                            cartCodeEditText.clearFocus();
                            break;
                        case SCAN_REQUEST_ID_GROUND:
                            groundCodeEditText.requestFocus();
//                            groundCodeEditText.post(() -> {
//                                groundCodeEditText.setText(request);
//                            });
                            groundCodeEditText.setText(request);
                            Log.d("Scan_request", "Scan back groundCode:" + request);
                            groundCodeEditText.clearFocus();
                            break;
                        case SCAN_REQUEST_ID_PRODUCT:
                            productCodeEditText.requestFocus();
//                            productCodeEditText.post(() -> {
//                                productCodeEditText.setText(request);
//                            });
                            productCodeEditText.setText(request);
                            Log.d("Scan_request", "Scan back productCode:" + request);
                            productCodeEditText.clearFocus();
                            break;
                    }
                });
            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    Toast.makeText(BindGroundActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
        cartScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCartCodeEditText();
                scanMessageTextView.setText("Scan the cart code");

                // Show the scan dialog
                scanDialog.show();

                // Start the scan
                scannerManager.requestScan(SCAN_REQUEST_ID_CART);
            }
        });

        groundScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearGroundCodeEditText();
                scanMessageTextView.setText("Scan the ground code");

                // Show the scan dialog
                scanDialog.show();

                // Start the scan
                scannerManager.requestScan(SCAN_REQUEST_ID_GROUND);
            }
        });
        productScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearProductCodeEditText();
                scanMessageTextView.setText("Scan the product code");

                // Show the scan dialog
                scanDialog.show();

                // Start the scan
                scannerManager.requestScan(SCAN_REQUEST_ID_PRODUCT);
            }
        });

        bindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cartCode = cartCodeEditText.getText().toString();
                String groundCode = groundCodeEditText.getText().toString();
                String productCode = productCodeEditText.getText().toString();
                String boxSize = boxSizeEditText.getText().toString();

                if (cartCode.isEmpty() || groundCode.isEmpty()|| productCode.isEmpty()) {
                    Toast.makeText(BindGroundActivity.this, "货架码、地码、产品码不能为空 Mã không được để trống",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform the bind operation in a background thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isSuccess = BindOperation.bindGround(cartCode, groundCode, productCode, boxSize);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isSuccess) {
//                                    Toast.makeText(BindGroundActivity.this, "绑定成功", Toast.LENGTH_LONG).show();
                                    Toast.makeText(BindGroundActivity.this, "OK", Toast.LENGTH_LONG).show();
                                    Log.d("Dialog", "绑定完成，准备显示对话框");
                                    showFirstDialog(cartCode, groundCode, productCode);
                                } else {
//                                    Toast.makeText(BindGroundActivity.this, "绑定失败", Toast.LENGTH_LONG).show();
                                    Toast.makeText(BindGroundActivity.this, "Failed", Toast.LENGTH_LONG).show();
                                }
                                clearCartCodeEditText();
                                clearGroundCodeEditText();
                                clearProductCodeEditText();
                                clearBoxSizeEditText();
                                cartCodeEditText.requestFocus();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    // 显示第一个对话框的方法
    private void showFirstDialog(final String cartCode, final String groundCode, final String productCode) {
        FirstDialogFragment firstDialog = new FirstDialogFragment();
        firstDialog.setFirstDialogListener(new FirstDialogFragment.FirstDialogListener() {
            @Override
            public void onOptionBSelected() {
                // 当选择B时显示第二个对话框并传递参数
                Log.d("Dialog", "自动生成任务，准备显示第二级对话框");
                showSecondDialog(cartCode, groundCode, productCode);
            }
        });
        Log.d("Dialog", "显示第一级对话框");
        firstDialog.show(getSupportFragmentManager(), "FirstDialog");
    }

    // 显示第二个对话框的方法
    private void showSecondDialog(final String cartCode, final String groundCode, final String productCode) {
        // 创建带参数的对话框实例
        SecondDialogFragment secondDialog = SecondDialogFragment.newInstance(cartCode, groundCode, productCode);

        secondDialog.setSecondDialogListener(new SecondDialogFragment.SecondDialogListener() {
            @Override
            public void onOptionASelected(String cartCode, String groundCode, String productCode) {
                Log.d("Dialog", "选择AGV，准备生成AGV任务");
                String agvType = "AGV";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AutoTransportResponseBody agv_response = TrolleyTransporter.sendAGV(cartCode, groundCode, productCode, agvType);
                        boolean agv_success = false;
                        String agv_message = "";

                        if (agv_response != null) {
                            agv_success = agv_response.getCode().equals("0");
                            agv_message = agv_response.getMessage();
                        } else {
                            Log.e("Dialog", "API 返回 null 对象");
                            agv_message = "API返回空响应";
                        }

                        final boolean finalSuccess = agv_success;
                        final String finalMessage = agv_message;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalSuccess) {
//                                    Toast.makeText(BindGroundActivity.this, "立即生成AGV任务", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(BindGroundActivity.this, "AGV task OK", Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "立即生成AGV任务");
                                } else {
//                                    Toast.makeText(BindGroundActivity.this, "生成AGV任务失败！" + finalMessage, Toast.LENGTH_SHORT).show();
                                    Toast.makeText(BindGroundActivity.this, "AGV task Failed" + finalMessage, Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "生成AGV任务失败！");
                                }
                            }
                        });
                    }
                }).start();
            }
            @Override
            public void onOptionBSelected(String cartCode, String groundCode, String productCode) {
                Log.d("Dialog", "选择AGF，准备生成AGF任务");
                String agvType = "AGF1";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AutoTransportResponseBody agv_response = TrolleyTransporter.sendAGV(cartCode, groundCode, productCode, agvType);
                        boolean agv_success = false;
                        String agv_message = "";

                        if (agv_response != null) {
                            agv_success = agv_response.getCode().equals("0");
                            agv_message = agv_response.getMessage();
                        } else {
                            Log.e("Dialog", "API 返回 null 对象");
                            agv_message = "API返回空响应";
                        }

                        final boolean finalSuccess = agv_success;
                        final String finalMessage = agv_message;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalSuccess) {
//                                    Toast.makeText(BindGroundActivity.this, "立即生成AGF任务", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(BindGroundActivity.this, "AGF task OK", Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "立即生成AGF任务");
                                } else {
//                                    Toast.makeText(BindGroundActivity.this, "生成AGF任务失败！" + finalMessage, Toast.LENGTH_SHORT).show();
                                    Toast.makeText(BindGroundActivity.this, "AGF task Failed" + finalMessage, Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "生成AGF任务失败！");
                                }
                            }
                        });
                    }
                }).start();
            }
            @Override
            public void onCancelSelected() {
                Log.d("Dialog", "选择取消，不生成任务");
//                Toast.makeText(BindGroundActivity.this, "不生成任务", Toast.LENGTH_SHORT).show();
                Toast.makeText(BindGroundActivity.this, "cancel", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("Dialog", "显示第二级对话框");
        secondDialog.show(getSupportFragmentManager(), "SecondDialog");
    }

    private void clearCartCodeEditText() {
        cartCodeEditText.setText("");
    }

    private void clearGroundCodeEditText() {
        groundCodeEditText.setText("");
    }

    private void clearProductCodeEditText() {
        productCodeEditText.setText("");
    }

    private void clearBoxSizeEditText() {
        boxSizeEditText.setText("");
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
    protected void onResume() {
        super.onResume();
        isActive = true;
        scannerManager.registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        // 在Activity暂停时取消扫描
        cancelCurrentScan();
        scannerManager.unregisterReceiver();
    }

}