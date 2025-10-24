package com.fii.targ.gdlpda;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fii.targ.gdlpda.model.AutoTransportResponseBody;
import com.fii.targ.gdlpda.model.CartProduct;
import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.service.BindOperation;
import com.fii.targ.gdlpda.service.TrolleyTransporter;
import com.fii.targ.gdlpda.ui.TrolleyProduct;
import com.fii.targ.gdlpda.ui.TrolleyProductAdapter;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import java.util.ArrayList;
import java.util.List;

public class BindProductActivity extends AppCompatActivity {

    private static final String TAG = "BindProductActivity";
    private TrolleyProductAdapter productAdapter;

    private EditText groundCodeEditText;


    RecyclerView recyclerView;
    private BindTask bindTask;

    private ScannerManager scannerManager;
    private TextView scanMessageTextView;
    private EditText cartCodeEditText;
    private Dialog scanDialog;
    private volatile boolean isActive = true;
    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_CART;
    private static final int SCAN_REQUEST_ID_GROUND = Constants.SCAN_TYPE_VN_AGV_GROUND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_product);
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Scanning...");
//        progressDialog.setCancelable(false);

        ImageButton scanButton = findViewById(R.id.qr_code_scan);
        recyclerView = findViewById(R.id.recyclerView);
        Button bindButton = findViewById(R.id.submit_button);

        cartCodeEditText = findViewById(R.id.qr_code_input);
        groundCodeEditText = findViewById(R.id.ground_code_input);
        ImageButton groundScanButton = findViewById(R.id.ground_code_scan);
        // cartCodeEditText.setShowSoftInputOnFocus(false);

        List<TrolleyProduct> productList = new ArrayList<>();
        for (int i = 1; i <= 8; i++) { // 8 是一台车上的最大产品数量
            productList.add(new TrolleyProduct(String.valueOf(i), ""));
        }
        productAdapter = new TrolleyProductAdapter(this, productList, false);
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
                if (requestId == SCAN_REQUEST_ID) {
                    runOnUiThread(() -> {
                        onScanComplete();
                        // progressDialog.dismiss();
                        cartCodeEditText.requestFocus();
//                        cartCodeEditText.post(() -> {
//                            cartCodeEditText.setText(barcodeData);
//                        });
                        cartCodeEditText.setText(barcodeData);
                        Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " Barcode data: " + barcodeData);
                        cartCodeEditText.clearFocus();

                    });
                    // startScanning();
                }
                if (requestId == SCAN_REQUEST_ID_GROUND) {
                    runOnUiThread(() -> {
                        onScanComplete();
                        // progressDialog.dismiss();
                        Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID_GROUND + " Barcode data: " + barcodeData);
                        groundCodeEditText.requestFocus();
//                        groundCodeEditText.post(() -> {
//                            groundCodeEditText.setText(barcodeData);
//                        });
                        groundCodeEditText.setText(barcodeData);
                        groundCodeEditText.clearFocus();
                    });
                }

            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " Scan timed out");
                    Toast.makeText(BindProductActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });

//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(cartCodeEditText.getWindowToken(), 0);

        // 设置地面扫描按钮点击事件
        groundScanButton.setOnClickListener(v -> {
            groundCodeEditText.setText("");
            scanMessageTextView.setText("Scan the ground code");

            // Show the scan dialog
            scanDialog.show();
    
            // Start the scan
            scannerManager.requestScan(SCAN_REQUEST_ID_GROUND);
        });

        // backButton.setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // Intent intent = new Intent(BindProductActivity.this, HomeActivity.class);
        // startActivity(intent);
        // finish(); // Optional: Call finish() if you want to close the current
        // activity
        // }
        // });

        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // progressDialog.show();
                cartCodeEditText.setText("");
                startScan("Scan the cart code");
            }
        });
        bindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cartCode = cartCodeEditText.getText().toString();
                String groundCode = groundCodeEditText.getText().toString();
                List<TrolleyProduct> latestProductList = getItems();

                if (cartCode.isEmpty() || groundCode.isEmpty()) {
                    Toast.makeText(BindProductActivity.this, "货架码、地码不能为空 Mã không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isProductListEmpty = true;
                for (TrolleyProduct product : latestProductList) {
                    if (!product.getProductSN().isEmpty()) {
                        isProductListEmpty = false;
                        break;
                    }
                }
            
                if (isProductListEmpty) {
                    // 提示用户确认是否为空台车
                    new android.app.AlertDialog.Builder(BindProductActivity.this)
                        .setTitle("是空货架,要绑定吗 Đây là kệ trống. Có muốn gán không")
                        .setMessage("NO / YES")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // 执行绑定产品和地码
                            bindProductsAndGround(cartCode, groundCode, latestProductList);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // 中断操作
                            Toast.makeText(BindProductActivity.this, "cancel", Toast.LENGTH_SHORT).show();
                        })
                        .show();
                } else {
                    // 先绑定产品，再绑定地码
                    bindProductsAndGround(cartCode, groundCode, latestProductList);
                }
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
//                                    Toast.makeText(BindProductActivity.this, "立即生成AGV任务", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(BindProductActivity.this, "AGV task OK", Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "立即生成AGV任务");
                                } else {
//                                    Toast.makeText(BindProductActivity.this, "生成AGV任务失败！" + finalMessage, Toast.LENGTH_SHORT).show();
                                    Toast.makeText(BindProductActivity.this, "AGV task Failed" + finalMessage, Toast.LENGTH_SHORT).show();
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
//                                    Toast.makeText(BindProductActivity.this, "立即生成AGF任务", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(BindProductActivity.this, "AGF task OK", Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "立即生成AGF任务");
                                } else {
//                                    Toast.makeText(BindProductActivity.this, "生成AGF任务失败！" + finalMessage, Toast.LENGTH_SHORT).show();
                                    Toast.makeText(BindProductActivity.this, "AGF task Failed" + finalMessage, Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(BindProductActivity.this, "不生成任务", Toast.LENGTH_SHORT).show();
                Toast.makeText(BindProductActivity.this, "cancel", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("Dialog", "显示第二级对话框");
        secondDialog.show(getSupportFragmentManager(), "SecondDialog");
    }

    @SuppressLint("StaticFieldLeak")
    private void bindProductsAndGround(String cartCode, String groundCode, List<TrolleyProduct> productList) {
        // 绑定产品
        if (bindTask != null) {
            bindTask.cancel(true);
        }
    
        bindTask = new BindTask(cartCode, BindOperation.convertToCartProductList(productList), BindProductActivity.this) {
            @Override
            protected void onPostExecute(Boolean isSuccess) {
                
                if (isSuccess) {
                    // 产品绑定成功后绑定地码
                    bindGround(cartCode, groundCode);
                } else {
//                    Toast.makeText(BindProductActivity.this, "产品绑定失败", Toast.LENGTH_SHORT).show();
                    Toast.makeText(BindProductActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
        bindTask.execute();
    }

    private void bindGround(String cartCode, String groundCode) {
        new Thread(() -> {
            boolean isSuccess = BindOperation.bindGround(cartCode, groundCode);
            runOnUiThread(() -> {
                if (isSuccess) {
//                    Toast.makeText(BindProductActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                    Toast.makeText(BindProductActivity.this, "OK", Toast.LENGTH_SHORT).show();
                    String productCode = "";
                    // Show the select dialog
                    Log.d("Dialog", "绑定成功，准备显示第一级对话框");
                    showFirstDialog(cartCode, groundCode, productCode);
                } else {
//                    Toast.makeText(BindProductActivity.this, "绑定失败", Toast.LENGTH_SHORT).show();
                    Toast.makeText(BindProductActivity.this, "Failed", Toast.LENGTH_SHORT).show();
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

    private void startScan(String message) {
        // Set the scan message
        scanMessageTextView.setText(message);

        // Show the scan dialog
        scanDialog.show();

        // Start the scan
        scannerManager.requestScan(SCAN_REQUEST_ID);
    }


    private void onScanComplete() {
        // Dismiss the scan dialog
        if (scanDialog.isShowing()) {
            Log.d("onScanComplete", "dismiss");
            scanDialog.dismiss();
        }
    }

    public List<TrolleyProduct> getItems() {
        for (int i = 0; i < productAdapter.getItemCount(); i++) {
            TrolleyProductAdapter.TrolleyProductViewHolder holder = (TrolleyProductAdapter.TrolleyProductViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                String productSnValue = holder.getProductSnValue();
                productAdapter.setItem(i, productSnValue);
            }
        }
        return productAdapter.getItems();
    }

    private void startScanning() {

        productAdapter.requestStartScan();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the bind task if the activity is destroyed
        if (bindTask != null) {
            bindTask.cancel(true);
        }
    }

    private static class BindTask extends AsyncTask<Void, Void, Boolean> {

        private String cartCode;
        private List<CartProduct> cartProducts;
        private Context context;

        public BindTask(String cartCode, List<CartProduct> cartProducts, Context context) {
            this.cartCode = cartCode;
            this.cartProducts = cartProducts;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            return BindOperation.bindProducts(cartCode, cartProducts);
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {

            if (isSuccess) {
                Toast.makeText(context, "绑定成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "绑定失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            // Handle task cancellation if needed
            Toast.makeText(context, "Bind task was cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}
