// SendTrolleyActivity.java
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fii.targ.gdlpda.model.TolleryTransportResponseBody;
import com.fii.targ.gdlpda.model.locationResponseBody;
import com.fii.targ.gdlpda.model.storageResponseBody;
import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import com.fii.targ.gdlpda.service.TrolleyTransporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SendFlTrolleyActivity extends AppCompatActivity {
    private static final String TAG = "SendFlTrolleyActivity";
    TextView groundCodeEditText;
    ImageButton groundScanButton;
    Button sendButton;
//    ProgressBar progressBar;

    ScannerManager scannerManager;
    private TextView scanMessageTextView;
    private Dialog scanDialog;
    private Spinner agvType_spinner;
    private Spinner storage_spinner;
    private Spinner location_spinner;
    private Spinner height_spinner;
    // 存储从MCS获取的数据
    private List<String> agvTypes = new ArrayList<>();
    private List<String> storage = new ArrayList<>();
    private List<String> location = new ArrayList<>();
    private List<String> height = new ArrayList<>();

    // 当前选择的值
    private String selectedCellId = "";
    private String selectedAgvTypes = "";
    private String selectedStorage = "";
    private String selectedLocation = "";
    private String selectedHeight = "";
    private boolean isProgrammaticallyClearing = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable trolleyInputRunnable;
    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_GROUND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_fl_trolley);
        groundCodeEditText = findViewById(R.id.ground_code_input);
        groundScanButton = findViewById(R.id.ground_code_scan);
//        progressBar = findViewById(R.id.progress_bar);
        sendButton = findViewById(R.id.submit_button);

        // 初始化下拉框
        agvType_spinner = findViewById(R.id.agvType_spinner);
        storage_spinner = findViewById(R.id.storage_spinner);
        location_spinner = findViewById(R.id.location_spinner);
        height_spinner = findViewById(R.id.height_spinner);
        ArrayAdapter<String> agvTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, agvTypes);
        agvTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        agvType_spinner.setAdapter(agvTypeAdapter);
        ArrayAdapter<String> storageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, storage);
        storageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storage_spinner.setAdapter(storageAdapter);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, location);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        location_spinner.setAdapter(locationAdapter);
        ArrayAdapter<String> heightAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, height);
        heightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        height_spinner.setAdapter(heightAdapter);
        // 禁用下拉框
        disableAllSpinners();

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
                        selectedCellId = barcodeData;
                        //enableAgvTypeSpinner();
                        Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " Barcode data: " + barcodeData);
                        groundCodeEditText.clearFocus();
                    }
                });
            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    Toast.makeText(SendFlTrolleyActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // 地码输入监听 - 使用延时检测
        trolleyInputRunnable = new Runnable() {
            @Override
            public void run() {
                selectedCellId = groundCodeEditText.getText().toString();
                if (!selectedCellId.isEmpty()) {
                    enableAgvTypeSpinner();
                } else if(isProgrammaticallyClearing){
                    disableAllSpinners();
                    isProgrammaticallyClearing = false; // 设置标志位
                }
            }
        };
        groundCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // 先移除之前的回调
                handler.removeCallbacks(trolleyInputRunnable);
                // 延迟3000毫秒执行，如果用户持续输入，会一直移除回调
                handler.postDelayed(trolleyInputRunnable, 3000);
            }
        });

        // 小车类型选择监听
        agvType_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 如果是第0项（空选项），则不执行任何有效操作
                if (position != 0) {
                    selectedAgvTypes = agvTypes.get(position);
                    enableHeightSpinner();
                    fetchStorage();  // 只有用户真正选择时才去获取库区
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 库区选择监听
        storage_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 如果是第0项（空选项），则不执行任何有效操作
                if (position != 0) {
                    // 只有当用户选择了有效选项（position > 0）时才执行后续逻辑
                    selectedStorage = storage.get(position);
                    fetchLocation(); // 只有用户真正选择时才去获取库位
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 库位选择监听
        location_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 如果是第0项（空选项），则不执行任何有效操作
                if (position != 0) {
                    selectedLocation = location.get(position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 高度选择监听
        height_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 如果是第0项（空选项），则不执行任何有效操作
                if (position != 0) {
                    selectedHeight = height.get(position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTrolley();

            }
        });

        groundScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groundCodeEditText.setText("");
                selectedCellId = "";
                isProgrammaticallyClearing = true;
                scanMessageTextView.setText("Scan the trolley code");
                scanDialog.show();
                scannerManager.requestScan(SCAN_REQUEST_ID);
            }
        });
    }

    private void enableAgvTypeSpinner() {
        // 启用小车类型下拉框并加载数据
        agvType_spinner.setEnabled(true);
        // 加载小车类型
        agvTypes.clear();
        agvTypes.add(""); // 空字符串，或者 agvTypes.add("请选择");
        agvTypes.add("AGF");

        ((ArrayAdapter) agvType_spinner.getAdapter()).notifyDataSetChanged();
        // 关键：设置默认选中第一项（即空选项）
        agvType_spinner.setSelection(0);
        Toast.makeText(SendFlTrolleyActivity.this, "agvType enable", Toast.LENGTH_SHORT).show();
    }

    private void enableHeightSpinner() {
        // 启用小车类型下拉框并加载数据
        height_spinner.setEnabled(true);
        // 加载小车类型
        height.clear();
        height.add(""); // 空字符串，或者 height.add("请选择");
        height.add("0"); //从最底层开始搬
        height.add("1"); //从第二层开始搬
        height.add("2"); //从第三层开始搬

        ((ArrayAdapter) height_spinner.getAdapter()).notifyDataSetChanged();
        // 关键：设置默认选中第一项（即空选项）
        height_spinner.setSelection(0);
        Toast.makeText(SendFlTrolleyActivity.this, "height enable", Toast.LENGTH_SHORT).show();
    }

    private void enableStorageSpinner() {
        // 启用库区下拉框并加载数据
        storage_spinner.setEnabled(true);
        ((ArrayAdapter) storage_spinner.getAdapter()).notifyDataSetChanged();
        storage_spinner.setSelection(0); // 新增这一行，确保默认显示为空
        agvType_spinner.setEnabled(false);
        Toast.makeText(SendFlTrolleyActivity.this, "storage enable", Toast.LENGTH_SHORT).show();
    }

    private void enableLocationSpinner() {
        // 启用库位下拉框并加载数据
        location_spinner.setEnabled(true);
        ((ArrayAdapter) location_spinner.getAdapter()).notifyDataSetChanged();
        location_spinner.setSelection(0); // 新增这一行，确保默认显示为空
        storage_spinner.setEnabled(false);
        Toast.makeText(SendFlTrolleyActivity.this, "location enable", Toast.LENGTH_SHORT).show();
    }

    private void disableAllSpinners() {
        agvType_spinner.setEnabled(false);
        storage_spinner.setEnabled(false);
        location_spinner.setEnabled(false);
        height_spinner.setEnabled(false);
        agvTypes.clear();
        storage.clear();
        location.clear();
        height.clear();
        ((ArrayAdapter) agvType_spinner.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter) storage_spinner.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter) location_spinner.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter) height_spinner.getAdapter()).notifyDataSetChanged();

        Toast.makeText(SendFlTrolleyActivity.this, "agvType、storage、location、height disenable", Toast.LENGTH_SHORT).show();
    }

    private void fetchStorage() {
        // 显示加载中提示
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("加载库区数据中...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();

        // 使用线程执行网络请求
        new Thread(() -> {
            try {
                storageResponseBody response = TrolleyTransporter.sendFetchStorageAGF(selectedCellId,selectedAgvTypes);
                boolean success = false;
                String message = "";

                if (response != null) {
                    success = response.getCode().equals("0");
                    message = response.getMessage();
                } else {
                    Log.e("Dialog", "API 返回 null 对象");
                    message = "API返回空响应";
                }

                final boolean finalSuccess = success;
                final String finalMessage = message;

                runOnUiThread(() ->  {
                    //progressDialog.dismiss();
                    //progressBar.setVisibility(View.GONE);

                    if (finalSuccess) {
                        // 解析响应数据，格式："数量,库区1,库区2,库区3"
                        String[] parts = finalMessage.split(",");
                        int count = Integer.parseInt(parts[0]);
                        storage.clear();
                        // 在添加真实数据前，先添加一个空字符串作为空选项
                        storage.add(""); // 新增这一行
                        for (int i = 1; i <= count; i++) {
                            storage.add(parts[i]);
                        }
                        ((ArrayAdapter) storage_spinner.getAdapter()).notifyDataSetChanged();

                        //storage_spinner.setEnabled(true);

                        Toast.makeText(SendFlTrolleyActivity.this, "fetch storage OK", Toast.LENGTH_SHORT).show();
                        Log.d("Dialog", "获取目标库区成功");
                        enableStorageSpinner();
                    } else {
                        groundCodeEditText.setText("");
                        selectedCellId = "";
                        isProgrammaticallyClearing = true;
                        Toast.makeText(SendFlTrolleyActivity.this, "fetch storage Failed:" + finalMessage, Toast.LENGTH_SHORT).show();
                        Log.d("Dialog", "获取目标库区失败");
                    }
                    sendButton.setEnabled(true); // 重新启用按钮
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    //progressDialog.dismiss();
                    groundCodeEditText.setText("");
                    selectedCellId = "";
                    isProgrammaticallyClearing = true;
                    Toast.makeText(SendFlTrolleyActivity.this, "获取库区数据异常", Toast.LENGTH_SHORT).show();
                    Log.e("SendTrolley", "获取库区数据异常: " + e.getMessage());
                });
            }
        }).start();
    }

    private void fetchLocation() {
        // 显示加载中提示
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("加载库位数据中...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();

        new Thread(() -> {
            try {
                locationResponseBody response = TrolleyTransporter.sendFetchLocationAGF(selectedCellId,selectedAgvTypes,selectedStorage);
                boolean success = false;
                String message = "";

                if (response != null) {
                    success = response.getCode().equals("0");
                    message = response.getMessage();
                } else {
                    Log.e("Dialog", "API 返回 null 对象");
                    message = "API返回空响应";
                }

                final boolean finalSuccess = success;
                final String finalMessage = message;

                runOnUiThread(() ->  {
                    //progressDialog.dismiss();
                    //progressBar.setVisibility(View.GONE);

                    if (finalSuccess) {
                        // 解析响应数据，格式："数量,库位1,库位2,......,库位3"
                        String[] parts = finalMessage.split(",");
                        int count = Integer.parseInt(parts[0]);
                        location.clear();
                        // 在添加真实数据前，先添加一个空字符串作为空选项
                        location.add(""); // 新增这一行
                        for (int i = 1; i <= count; i++) {
                            location.add(parts[i]);
                        }
                        ((ArrayAdapter) location_spinner.getAdapter()).notifyDataSetChanged();
                        //location_spinner.setEnabled(true);

                        Toast.makeText(SendFlTrolleyActivity.this, "fetch location OK", Toast.LENGTH_SHORT).show();
                        Log.d("Dialog", "获取目标库位成功");
                        enableLocationSpinner();
                    } else {
                        Toast.makeText(SendFlTrolleyActivity.this, "fetch location Failed" + finalMessage, Toast.LENGTH_SHORT).show();
                        groundCodeEditText.setText("");
                        selectedCellId = "";
                        isProgrammaticallyClearing = true;
                        Log.d("Dialog", "获取目标库位失败");
                    }
                    sendButton.setEnabled(true); // 重新启用按钮
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    //progressDialog.dismiss();
                    groundCodeEditText.setText("");
                    selectedCellId = "";
                    isProgrammaticallyClearing = true;
                    Toast.makeText(SendFlTrolleyActivity.this, "获取库位数据异常", Toast.LENGTH_SHORT).show();
                    Log.e("SendTrolley", "获取库位数据异常: " + e.getMessage());
                });
            }
        }).start();
    }

    private void sendTrolley() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String cellId = groundCodeEditText.getText().toString();
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (cellId.isEmpty() || selectedAgvTypes.isEmpty()) {
                            Toast.makeText(SendFlTrolleyActivity.this, "地码和小车类型不能为空 Mã mặt đất không được để trống", Toast.LENGTH_SHORT).show();
                            sendButton.setEnabled(true); // 重新启用按钮
                            return;
                        }
//                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
                if (Objects.equals(selectedAgvTypes, "AGF")) {
                    TolleryTransportResponseBody agf_response = TrolleyTransporter.sendSelectedAGF(selectedCellId,selectedAgvTypes,selectedStorage,selectedLocation,selectedHeight);
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
                            sendButton.setEnabled(true); // 重新启用按钮
                            //progressBar.setVisibility(View.GONE);

                            if (finalSuccess) {
                                Toast.makeText(SendFlTrolleyActivity.this, "send AGF OK", Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(SendTrolleyActivity.this, "AGF模具下料成功", Toast.LENGTH_SHORT).show();
                                Log.d("Dialog", "生成AGF下料任务成功");
                            } else {
                                Toast.makeText(SendFlTrolleyActivity.this, "send AGF Failed" + finalMessage, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(SendTrolleyActivity.this, "AGF模具下料失败" + finalMessage, Toast.LENGTH_SHORT).show();
                                Log.d("Dialog", "生成AGF下料任务失败！");
                            }
                            groundCodeEditText.setText("");
                            selectedCellId = "";
                            isProgrammaticallyClearing = true; // 无论发送成功或失败，都要清除上一次的数据
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