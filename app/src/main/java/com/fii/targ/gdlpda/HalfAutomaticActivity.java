// HalfAutomaticActivity.java
package com.fii.targ.gdlpda;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fii.targ.gdlpda.model.TolleryTransportResponseBody;
import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import com.fii.targ.gdlpda.service.TrolleyTransporter;

import java.util.ArrayList;
import java.util.List;


public class HalfAutomaticActivity extends AppCompatActivity {
    private static final String TAG = "HalfAutomaticActivity";
    TextView groundCodeEditText;
    ImageButton groundScanButton;
    Button sendButton;
    Button callButton;
    Button backButton;

    ScannerManager scannerManager;
    private TextView scanMessageTextView;
    private Dialog scanDialog;
    private Spinner agvType_spinner;
    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_GROUND;
    // 存储从MCS获取的数据
    private List<String> agvTypes = new ArrayList<>();

    // 当前选择的值
    private String selectedGroundCode = "";
    private String selectedAgvTypes = "";
    private String selectedAction = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_half_automatic);
        groundCodeEditText = findViewById(R.id.ground_code_input);
        groundScanButton = findViewById(R.id.ground_code_scan);
        sendButton = findViewById(R.id.send_button);
        callButton = findViewById(R.id.call_button);
        backButton = findViewById(R.id.back_button);

        // 初始化下拉框
        agvType_spinner = findViewById(R.id.agvType_spinner);
        ArrayAdapter<String> agvTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, agvTypes);
        agvTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        agvType_spinner.setAdapter(agvTypeAdapter);
        // 启用下拉框
        enableAgvTypeSpinner();

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
//                        trolleyCodeEditText.post(() -> {
//                            trolleyCodeEditText.setText(barcodeData);
//                        });
                        groundCodeEditText.setText(barcodeData);
                        selectedGroundCode = barcodeData;
                        Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " Barcode data: " + barcodeData);
                        groundCodeEditText.clearFocus();
                    }
                });
            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    Toast.makeText(HalfAutomaticActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // 小车类型选择监听
        agvType_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 如果是第0项（空选项），则不执行任何有效操作
                if (position != 0) {
                    selectedAgvTypes = agvTypes.get(position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedAction = "send";
                callButton.setEnabled(false);
                backButton.setEnabled(false);
                actionRequest(selectedAction);
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedAction = "call";
                sendButton.setEnabled(false);
                backButton.setEnabled(false);
                actionRequest(selectedAction);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedAction = "back";
                callButton.setEnabled(false);
                sendButton.setEnabled(false);
                actionRequest(selectedAction);
            }
        });

        groundScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groundCodeEditText.setText("");
                selectedGroundCode = "";
                scanMessageTextView.setText("Scan the trolley code");
                scanDialog.show();
                scannerManager.requestScan(SCAN_REQUEST_ID);
            }
        });
    }

    private void setAllButtons(boolean enabled) {
        sendButton.setEnabled(enabled);
        callButton.setEnabled(enabled);
        backButton.setEnabled(enabled);
    }

    private void enableAgvTypeSpinner() {
        // 启用小车类型下拉框并加载数据
        agvType_spinner.setEnabled(true);
        // 加载小车类型
        agvTypes.clear();
        agvTypes.add(""); // 空字符串，或者 agvTypes.add("请选择");
        agvTypes.add("AGV");
        agvTypes.add("AGF");
//        agvTypes.add("AGF1");

        ((ArrayAdapter) agvType_spinner.getAdapter()).notifyDataSetChanged();
        // 关键：设置默认选中第一项（即空选项）
        agvType_spinner.setSelection(0);
    }

    private void actionRequest(String action) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    selectedGroundCode = groundCodeEditText.getText().toString();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (selectedGroundCode.isEmpty() || selectedAgvTypes.isEmpty()) {
                                Toast.makeText(HalfAutomaticActivity.this, "地码和小车类型不能为空 Mã mặt đất không được để trống", Toast.LENGTH_SHORT).show();
                                sendButton.setEnabled(true); // 重新启用按钮
                                return;
                            }
                        }
                    });
                    TolleryTransportResponseBody agf_response = TrolleyTransporter.actionrequest(selectedGroundCode,selectedAgvTypes,action);
                    boolean agf_success = false;
                    String agf_message = "";

                    if (agf_response != null) {
                        agf_success = agf_response.getCode().equals("0");
                        agf_message = agf_response.getMessage();
                    } else {
                        Log.e("Dialog", "API 返回 null 对象");
                        agf_message = "API返回空响应";
                    }

                    final boolean finalSuccess = agf_success;
                    final String finalMessage = agf_message;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (action) {
                                case "send":
                                    if (finalSuccess) {
                                        Toast.makeText(HalfAutomaticActivity.this, "send request OK", Toast.LENGTH_SHORT).show();
                                        Log.d("Dialog", "生成下料请求成功");
                                    } else {
                                        Toast.makeText(HalfAutomaticActivity.this, "send request Failed" + finalMessage, Toast.LENGTH_SHORT).show();
                                        Log.d("Dialog", "生成下料请求失败！");
                                    }
                                    break;
                                case "call":
                                    if (finalSuccess) {
                                        Toast.makeText(HalfAutomaticActivity.this, "call request OK", Toast.LENGTH_SHORT).show();
                                        Log.d("Dialog", "生成上料请求成功");
                                    } else {
                                        Toast.makeText(HalfAutomaticActivity.this, "call request Failed" + finalMessage, Toast.LENGTH_SHORT).show();
                                        Log.d("Dialog", "生成上料请求失败！");
                                    }
                                    break;
                                case "back":
                                    if (finalSuccess) {
                                        Toast.makeText(HalfAutomaticActivity.this, "back request OK", Toast.LENGTH_SHORT).show();
                                        Log.d("Dialog", "生成退库请求成功");
                                    } else {
                                        Toast.makeText(HalfAutomaticActivity.this, "back request Failed" + finalMessage, Toast.LENGTH_SHORT).show();
                                        Log.d("Dialog", "生成退库请求失败！");
                                    }
                                    break;
                                default:
                                    Toast.makeText(HalfAutomaticActivity.this, "Unknown action", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    });
                } finally {
                    // 无论成功失败，最终都在UI线程中重新启用所有按钮并重置界面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 重新启用所有按钮
                            setAllButtons(true);
                            // 原有的重置界面逻辑
                            groundCodeEditText.setText("");
                            selectedGroundCode = "";
                            enableAgvTypeSpinner();
                        }
                    });
                }

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