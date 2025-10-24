// SendTrolleyActivity.java
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;

public class UnbindActivity extends AppCompatActivity {
    private static final String TAG = "UnbindActivity";
    private TrolleyProductAdapter productAdapter;
    private List<TrolleyProduct> productList;
    EditText qrCodeInput;
    TextView groundCodeText;

    private ScannerManager scannerManager;
    private TextView scanMessageTextView;
    private Dialog scanDialog;
    
    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_CART;
    private String boundCellID = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unbind);

        qrCodeInput = findViewById(R.id.qr_code_input);
        /*
        qrCodeInput.setInputType(InputType.TYPE_NULL);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(qrCodeInput.getWindowToken(), 0);
        */

        groundCodeText = findViewById(R.id.ground_code_text);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Button unbindGroundButton = findViewById(R.id.button_unbind_ground);
        Button unbindProductButton = findViewById(R.id.button_unbind_product);
        ImageButton qrCodeScanButton = findViewById(R.id.qr_code_scan);

        productList = new ArrayList<>();
        productAdapter = new TrolleyProductAdapter(this, productList, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);

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
                    Toast.makeText(UnbindActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
        qrCodeScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                qrCodeInput.setText("");
                groundCodeText.setText("");
                productList.clear();
                scanMessageTextView.setText("Scan the cart code");

                // Show the scan dialog
                scanDialog.show();
        
                // Start the scan
                scannerManager.requestScan(SCAN_REQUEST_ID);
            }
        });

/*
        qrCodeInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 允许光标移动和文本选择，但不弹出键盘
                int inType = qrCodeInput.getInputType();
                qrCodeInput.setInputType(InputType.TYPE_NULL);
                qrCodeInput.onTouchEvent(event);
                qrCodeInput.setInputType(inType);
                return true;
            }
        });

 */
        unbindGroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cartCode = qrCodeInput.getText().toString();
                if (cartCode.isEmpty()) {
                    Toast.makeText(UnbindActivity.this, "货架码不能为空 Mã kệ không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(() -> {
                    queryBindingInfo();
                    boolean isSuccess = BindOperation.unbindGround(cartCode);
                    runOnUiThread(() -> {
                        if (isSuccess) {
                            groundCodeText.setText("");
                            Toast.makeText(UnbindActivity.this, "OK", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(UnbindActivity.this, "解绑地码成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UnbindActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(UnbindActivity.this, "解绑地码失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
        });

        unbindProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cartCode = qrCodeInput.getText().toString();
                //String productCode = "";
                if (cartCode.isEmpty()) {
                    Toast.makeText(UnbindActivity.this, "货架码不能为空 Mã kệ không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(() -> {
                    queryBindingInfo();
                    boolean isSuccess = BindOperation.unbindProducts(cartCode);
                    // we need to unbind ground code too, otherwise it will be recognized as a empty cart
                    boolean isSuccess2 = BindOperation.unbindGround(cartCode);
                    runOnUiThread(() -> {
                        if (isSuccess && isSuccess2) {
                            Toast.makeText(UnbindActivity.this, "OK", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(UnbindActivity.this, "解绑产品和地码成功", Toast.LENGTH_SHORT).show();
                            Log.d("Dialog", "解绑完成，准备显示对话框");
                            //showFirstDialog(cartCode, boundCellID, productCode);
                        } else {
                            Toast.makeText(UnbindActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(UnbindActivity.this, "解绑产品和地码成功", Toast.LENGTH_SHORT).show();
                        }

                    });
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
                                    Toast.makeText(UnbindActivity.this, "AGV task OK", Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(UnbindActivity.this, "立即生成AGV任务", Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "立即生成AGV任务");
                                } else {
                                    Toast.makeText(UnbindActivity.this, "AGV task Failed" + finalMessage, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(UnbindActivity.this, "生成AGV任务失败！" + finalMessage, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(UnbindActivity.this, "AGF task OK", Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(UnbindActivity.this, "立即生成AGF任务", Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "立即生成AGF任务");
                                } else {
                                    Toast.makeText(UnbindActivity.this, "AGF task Failed" + finalMessage, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(UnbindActivity.this, "生成AGF任务失败！" + finalMessage, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(UnbindActivity.this, "cancel", Toast.LENGTH_SHORT).show();
//                Toast.makeText(UnbindActivity.this, "不生成任务", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("Dialog", "显示第二级对话框");
        secondDialog.show(getSupportFragmentManager(), "SecondDialog");
    }

    public void queryBindingInfo() {
        // Simulate QR code scanning
        String scannedCartCode = qrCodeInput.getText().toString();
        Log.d("DEBUG", "scannedCartCode: " + scannedCartCode);
        if (scannedCartCode.isEmpty()) {
            Toast.makeText(UnbindActivity.this, "货架码不能是空的 Mã kệ không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            CartBindingInfo bindingInfo = BindOperation.queryCartBindingInfo(scannedCartCode);
            runOnUiThread(() -> {
                if (bindingInfo != null) {
                    List<CartProduct> cartProducts = bindingInfo.getCartProducts();
                    groundCodeText.setText(bindingInfo.getGroundCode());
                    boundCellID = bindingInfo.getGroundCode();
                    Log.d("UnbindActivity", "products: " + new Gson().toJson(cartProducts));
                    productList.clear();
                    if (cartProducts != null && !cartProducts.isEmpty()) {
                        productList.addAll(bindingInfo.convertToTrolleyProductList(bindingInfo.getCartProducts()));
                    }
                    productAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UnbindActivity.this, "query Failed", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(UnbindActivity.this, "查询绑定关系失败", Toast.LENGTH_SHORT).show();
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
