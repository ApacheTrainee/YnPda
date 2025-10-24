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

import com.fii.targ.gdlpda.model.AutoTransportResponseBody;
import com.fii.targ.gdlpda.model.CartBindingInfo;
import com.fii.targ.gdlpda.model.CartProduct;
import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import com.fii.targ.gdlpda.service.BindOperation;
import com.fii.targ.gdlpda.service.TrolleyTransporter;
import com.fii.targ.gdlpda.ui.TrolleyProduct;
import com.fii.targ.gdlpda.ui.TrolleyProductAdapter;
import com.google.gson.Gson;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class UnbindProductActivity extends AppCompatActivity {
    private static final String TAG = "UnbindProductActivity";
    private EditText qrCodeInput;
    private TextView groundCodeText;
    private TrolleyProductAdapter productAdapter;
    private List<TrolleyProduct> productList;
    private Button unbindButton;

    private ScannerManager scannerManager;
    private TextView scanMessageTextView;
    private Dialog scanDialog;
    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_CART;

    private String boundCellID = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unbind_product);

        // 初始化控件
        qrCodeInput = findViewById(R.id.qr_code_input);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        groundCodeText = findViewById(R.id.ground_code_text);
        unbindButton = findViewById(R.id.unbind_button);
        ImageButton qrCodeScan = findViewById(R.id.qr_code_scan);

        productList = new ArrayList<>();
        productAdapter = new TrolleyProductAdapter(this, productList, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);

        // 解绑按钮点击事件
        unbindButton.setOnClickListener(v -> unbindProducts());

        // 扫描按钮点击事件
        qrCodeScan.setOnClickListener(v -> {
            // 清空输入框并显示扫描提示
            qrCodeInput.setText("");
            groundCodeText.setText("");
            productList.clear();
            Toast.makeText(this, "Scanning ground code...", Toast.LENGTH_SHORT).show();
            scanMessageTextView.setText("Scan the ground code");

            // Show the scan dialog
            scanDialog.show();
    
            // Start the scan
            scannerManager.requestScan(SCAN_REQUEST_ID);

        });

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
                        qrCodeInput.requestFocus();
//                        qrCodeInput.post(() -> {
//                            qrCodeInput.setText(barcodeData);
//                        });
                        qrCodeInput.setText(barcodeData);
                        Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " Barcode data: " + qrCodeInput.getText().toString());
                        qrCodeInput.clearFocus();
                        queryBindingInfo();
                    }
                });
            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    Toast.makeText(UnbindProductActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void queryBindingInfo() {
        String scannedCartCode = qrCodeInput.getText().toString().trim();
        Log.d("DEBUG", "scannedCartCode: " + scannedCartCode);
        if (scannedCartCode.isEmpty()) {
            Toast.makeText(this, "货架码不能为空 Mã kệ không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // 模拟查询绑定关系
        new Thread(() -> {
            CartBindingInfo bindingInfo = BindOperation.queryCartBindingInfo(scannedCartCode);
            runOnUiThread(() -> {
                if (bindingInfo != null) {
                    List<CartProduct> cartProducts = bindingInfo.getCartProducts();
                    groundCodeText.setText(bindingInfo.getGroundCode());
                    boundCellID = bindingInfo.getGroundCode();
                    Log.d("GroundUnBindActivity", "products: " + new Gson().toJson(cartProducts));
                    productList.clear();
                    if (cartProducts != null && !cartProducts.isEmpty()) {
                        productList.addAll(bindingInfo.convertToTrolleyProductList(bindingInfo.getCartProducts()));
                    }
                    productAdapter.notifyDataSetChanged();
                } else {
//                    Toast.makeText(UnbindProductActivity.this, "查询绑定关系失败", Toast.LENGTH_SHORT).show();
                    Toast.makeText(UnbindProductActivity.this, "query Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void unbindProducts() {
        String cartCode = qrCodeInput.getText().toString();
        String groundCode = "";
        String productCode = "";
        Log.d("GroundUnBindActivity", "Unbinding cart code: " + cartCode);
        if (cartCode.isEmpty()) {
            Toast.makeText(this, "地码不能为空 Mã mặt đất không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // 模拟解绑操作
        new Thread(() -> {
            queryBindingInfo();
            boolean isSuccess = BindOperation.unbindProducts(cartCode);
            runOnUiThread(() -> {
                if (isSuccess) {
                    Toast.makeText(UnbindProductActivity.this, "OK", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(UnbindProductActivity.this, "解绑产品成功", Toast.LENGTH_SHORT).show();
                    Log.d("Dialog", "解绑完成，准备显示对话框");
                    showFirstDialog(cartCode, boundCellID, productCode);
                } else {
                    Toast.makeText(UnbindProductActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(UnbindProductActivity.this, "解绑产品成功", Toast.LENGTH_SHORT).show();
                }

            });
        }).start();
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
                                    Toast.makeText(UnbindProductActivity.this, "AGV task OK", Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(UnbindProductActivity.this, "立即生成AGV任务", Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "立即生成AGV任务");
                                } else {
                                    Toast.makeText(UnbindProductActivity.this, "AGV task Failed" + finalMessage, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(UnbindProductActivity.this, "生成AGV任务失败！" + finalMessage, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(UnbindProductActivity.this, "AGF task OK", Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(UnbindProductActivity.this, "立即生成AGF任务", Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "立即生成AGF任务");
                                } else {
                                    Toast.makeText(UnbindProductActivity.this, "AGF task Failed" + finalMessage, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(UnbindProductActivity.this, "生成AGF任务失败！" + finalMessage, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(UnbindProductActivity.this, "cancel", Toast.LENGTH_SHORT).show();
//                Toast.makeText(UnbindProductActivity.this, "不生成任务", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("Dialog", "显示第二级对话框");
        secondDialog.show(getSupportFragmentManager(), "SecondDialog");
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
        scannerManager.registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在Activity暂停时取消扫描
        cancelCurrentScan();
        scannerManager.unregisterReceiver();
    }
}