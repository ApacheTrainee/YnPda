package com.fii.targ.gdlpda;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

public class BindFlProductActivity extends AppCompatActivity {

    private EditText groundCodeEditText;
    private EditText productBatchEditText;
    private ScannerManager scannerManager;
    // private Spinner workstationSpinner;
    // private Spinner lineSpinner;
    private Button submitButton;
    private Button number_1;
    private Button number_2;
    private Button number_3;
    private int selectedNumber = 0;
    private Button lastSelectedButton = null; // 上一个被选中的按钮
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable numberInputRunnable;
    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_GROUND;
    private Dialog scanDialog;
    private TextView scanMessageTextView;
    private ImageButton qrCodeScanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fl_bind_ground);

        groundCodeEditText = findViewById(R.id.qr_code_input);
        productBatchEditText = findViewById(R.id.product_batch_input);
//        workstationSpinner = findViewById(R.id.workstation_spinner);
//        lineSpinner = findViewById(R.id.line_spinner);
        submitButton = findViewById(R.id.submit_button);
        number_1 = findViewById(R.id.number_1_button);
        number_2 = findViewById(R.id.number_2_button);
        number_3 = findViewById(R.id.number_3_button);
        number_1.setEnabled(false);
        number_2.setEnabled(false);
        number_3.setEnabled(false);
        qrCodeScanButton = findViewById(R.id.qr_code_scan);

        loadSpinnerData();
        // 设置数字按钮的点击监听
        setupNumberButtons();

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
                    if (requestId == SCAN_REQUEST_ID) {
                        groundCodeEditText.requestFocus();
//                        groundCodeEditText.post(() -> {
//                            groundCodeEditText.setText(barcodeData);
//                        });
                        groundCodeEditText.setText(barcodeData);
                        Log.d("Scan_request", "Scan back groundCode:" + barcodeData);
                        groundCodeEditText.clearFocus();
                    }
                });
            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    Toast.makeText(BindFlProductActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // 产品批次输入监听 - 使用延时检测
        numberInputRunnable = new Runnable() {
            @Override
            public void run() {
                String productBatch = productBatchEditText.getText().toString();
                if (!productBatch.isEmpty()) {
                    number_1.setEnabled(true);
                    number_2.setEnabled(true);
                    number_3.setEnabled(true);
                } else {
                    number_1.setEnabled(false);
                    number_2.setEnabled(false);
                    number_3.setEnabled(false);
                }
            }
        };
        productBatchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // 先移除之前的回调
                handler.removeCallbacks(numberInputRunnable);
                // 延迟3000毫秒执行，如果用户持续输入，会一直移除回调
                handler.postDelayed(numberInputRunnable, 3000);
            }
        });

        qrCodeScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Simulate QR code scanning
                clearInputText();
                scanMessageTextView.setText("Scan the ground code");
                scanDialog.show();
                scannerManager.requestScan(SCAN_REQUEST_ID);
            }
        });

//        number_1.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                selectedNumber = 0;
//                // 恢复上一个选中按钮的状态
//                if (lastSelectedButton != null) {
//                    lastSelectedButton.setSelected(false);
//                }
//                // 设置当前按钮为选中状态
//                number_1.setSelected(true);
//                lastSelectedButton = number_1;
//
//                // 根据按钮设置type值
//                selectedNumber = 1;
//
//                Log.d("LayerSelection", "当前选择的层数: " + selectedNumber);
//            }
//        });
//
//        number_2.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                selectedNumber = 0;
//                // 恢复上一个选中按钮的状态
//                if (lastSelectedButton != null) {
//                    lastSelectedButton.setSelected(false);
//                }
//                // 设置当前按钮为选中状态
//                number_2.setSelected(true);
//                lastSelectedButton = number_2;
//
//                // 根据按钮设置type值
//                selectedNumber = 2;
//
//                Log.d("LayerSelection", "当前选择的层数: " + selectedNumber);
//            }
//        });
//
//        number_3.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                selectedNumber = 0;
//                // 恢复上一个选中按钮的状态
//                if (lastSelectedButton != null) {
//                    lastSelectedButton.setSelected(false);
//                }
//                // 设置当前按钮为选中状态
//                number_3.setSelected(true);
//                lastSelectedButton = number_3;
//
//                // 根据按钮设置type值
//                selectedNumber = 3;
//
//                Log.d("LayerSelection", "当前选择的层数: " + selectedNumber);
//            }
//        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groundCode = groundCodeEditText.getText().toString();
                String productSn = "";
                String materialNum = productBatchEditText.getText().toString();
//
//                String workstation = workstationSpinner.getSelectedItem().toString();
//                String line = lineSpinner.getSelectedItem().toString();

                if (groundCode.isEmpty()) {
                    Toast.makeText(BindFlProductActivity.this, "地码不能是空 Mã mặt đất không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!materialNum.isEmpty()) {
                    if (selectedNumber == 0) {
                        Toast.makeText(BindFlProductActivity.this, "堆叠栈板的个数不能是空 Số lượng pallet xếp chồng không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                new Thread(() -> {
                    boolean isSuccess = BindOperation.bindFlProduct(groundCode, productSn, materialNum, selectedNumber);
                    runOnUiThread(() -> {
                        if (isSuccess) {
//                            Toast.makeText(BindFlProductActivity.this, "绑定地码成功", Toast.LENGTH_SHORT).show();
                            Toast.makeText(BindFlProductActivity.this, "OK", Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(BindFlProductActivity.this, "绑定地码失败", Toast.LENGTH_SHORT).show();
                            Toast.makeText(BindFlProductActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                        clearInputText();
                    });
                }).start();
            }
        });
    }

    private void setupNumberButtons() {
        // 统一的按钮点击监听器
        View.OnClickListener numberButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNumberButtonClick((Button) v);
            }
        };

        number_1.setOnClickListener(numberButtonClickListener);
        number_2.setOnClickListener(numberButtonClickListener);
        number_3.setOnClickListener(numberButtonClickListener);
    }

    private void handleNumberButtonClick(Button clickedButton) {
        // 恢复上一个选中按钮的状态
        if (lastSelectedButton != null && lastSelectedButton != clickedButton) {
            lastSelectedButton.setSelected(false);
        }

        // 设置当前按钮为选中状态
        clickedButton.setSelected(true);
        lastSelectedButton = clickedButton;

        // 根据按钮设置selectedNumber值
        if (clickedButton == number_1) {
            selectedNumber = 1;
        } else if (clickedButton == number_2) {
            selectedNumber = 2;
        } else if (clickedButton == number_3) {
            selectedNumber = 3;
        }

        Log.d("LayerSelection", "当前选择的层数: " + selectedNumber);
    }

    private void resetNumberButtons() {
        selectedNumber = 0;
        if (lastSelectedButton != null) {
            lastSelectedButton.setSelected(false);
            lastSelectedButton = null;
        }
        number_1.setEnabled(false);
        number_2.setEnabled(false);
        number_3.setEnabled(false);
    }

    private void clearInputText() {
        groundCodeEditText.setText("");
        productBatchEditText.setText("");
        resetNumberButtons();
        // workstationSpinner.setSelection(0);
        // lineSpinner.setSelection(0);
    }

    private void loadSpinnerData() {
        // 获取工站位选项
//        String[] workstationArray = getResources().getStringArray(R.array.workstation_array);
//
//        // 获取线体选项
//        String[] lineArray = getResources().getStringArray(R.array.line_array);
//
//        // 设置工站位 Spinner
//        ArrayAdapter<String> workstationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
//                workstationArray);
//        workstationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        workstationSpinner.setAdapter(workstationAdapter);
//
//        // 设置线体 Spinner
//        ArrayAdapter<String> lineAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lineArray);
//        lineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        lineSpinner.setAdapter(lineAdapter);
    }

    public void cancelCurrentScan() {
        scannerManager.cancelScan();
        scanDialog.dismiss();
        scannerManager.cancelScan();
    }

    private void onScanComplete() {
        if (scanDialog.isShowing()) {
            scanDialog.dismiss();
        }
    }
}
