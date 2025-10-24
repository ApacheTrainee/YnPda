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

import com.fii.targ.gdlpda.model.TolleryTransportResponseBody;
import com.fii.targ.gdlpda.model.locationResponseBody;
import com.fii.targ.gdlpda.model.storageResponseBody;
import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import com.fii.targ.gdlpda.service.TrolleyTransporter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SendTrolleyActivity extends AppCompatActivity {
    private static final String TAG = "SendTrolleyActivity";
    TextView trolleyCodeEditText;
//    TextView sendNotes;
    ImageButton trolleyScanButton;
    Button sendButton;
//    ProgressBar progressBar;

    ScannerManager scannerManager;
    private TextView scanMessageTextView;
    private Dialog scanDialog;
    private Spinner agvType_spinner;
    private Spinner storage_spinner;
    private Spinner location_spinner;
    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_CART;
    // 存储从MCS获取的数据
    private List<String> agvTypes = new ArrayList<>();
    private List<String> storage = new ArrayList<>();
    private List<String> location = new ArrayList<>();

    // 当前选择的值
    private String selectedCartCode = "";
    private String selectedAgvTypes = "";
    private String selectedStorage = "";
    private String selectedLocation = "";
    private boolean isProgrammaticallyClearing = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable trolleyInputRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_trolley);
        trolleyCodeEditText = findViewById(R.id.trolley_code_input);
        trolleyScanButton = findViewById(R.id.trolley_code_scan);
//        progressBar = findViewById(R.id.progress_bar);
//        sendNotes = findViewById(R.id.send_notes);
        sendButton = findViewById(R.id.submit_button);

        // 初始化下拉框
        agvType_spinner = findViewById(R.id.agvType_spinner);
        storage_spinner = findViewById(R.id.storage_spinner);
        location_spinner = findViewById(R.id.location_spinner);
        ArrayAdapter<String> agvTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, agvTypes);
        agvTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        agvType_spinner.setAdapter(agvTypeAdapter);
        ArrayAdapter<String> storageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, storage);
        storageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storage_spinner.setAdapter(storageAdapter);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, location);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        location_spinner.setAdapter(locationAdapter);
        // 禁用下拉框
        disableAllSpinners();

//        sendNotes.setVisibility(View.GONE);

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
                        trolleyCodeEditText.requestFocus();
//                        trolleyCodeEditText.post(() -> {
//                            trolleyCodeEditText.setText(barcodeData);
//                        });
                        trolleyCodeEditText.setText(barcodeData);
                        selectedCartCode = barcodeData;
                        //enableAgvTypeSpinner();
                        Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " Barcode data: " + barcodeData);
                        trolleyCodeEditText.clearFocus();
                    }
                });
            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    Toast.makeText(SendTrolleyActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // 货架码输入监听 - 使用延时检测
        trolleyInputRunnable = new Runnable() {
            @Override
            public void run() {
                selectedCartCode = trolleyCodeEditText.getText().toString();
                if (!selectedCartCode.isEmpty()) {
                    enableAgvTypeSpinner();
                } else if(isProgrammaticallyClearing){
                    disableAllSpinners();
                    isProgrammaticallyClearing = false; // 设置标志位
                }
            }
        };
        trolleyCodeEditText.addTextChangedListener(new TextWatcher() {
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

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTrolley();

            }
        });

        trolleyScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trolleyCodeEditText.setText("");
                selectedCartCode = "";
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
        agvTypes.add("AGV");
        agvTypes.add("AGF1");

        ((ArrayAdapter) agvType_spinner.getAdapter()).notifyDataSetChanged();
        // 关键：设置默认选中第一项（即空选项）
        agvType_spinner.setSelection(0);
        Toast.makeText(SendTrolleyActivity.this, "agvType enable", Toast.LENGTH_SHORT).show();
    }

    private void enableStorageSpinner() {
        // 启用库区下拉框并加载数据
        storage_spinner.setEnabled(true);
        ((ArrayAdapter) storage_spinner.getAdapter()).notifyDataSetChanged();
        storage_spinner.setSelection(0); // 新增这一行，确保默认显示为空
        agvType_spinner.setEnabled(false);
        Toast.makeText(SendTrolleyActivity.this, "storage enable", Toast.LENGTH_SHORT).show();
    }

    private void enableLocationSpinner() {
        // 启用库位下拉框并加载数据
        location_spinner.setEnabled(true);
        ((ArrayAdapter) location_spinner.getAdapter()).notifyDataSetChanged();
        location_spinner.setSelection(0); // 新增这一行，确保默认显示为空
        storage_spinner.setEnabled(false);
        Toast.makeText(SendTrolleyActivity.this, "location enable", Toast.LENGTH_SHORT).show();
    }

    private void disableAllSpinners() {
        agvType_spinner.setEnabled(false);
        storage_spinner.setEnabled(false);
        location_spinner.setEnabled(false);
        agvTypes.clear();
        storage.clear();
        location.clear();
        ((ArrayAdapter) agvType_spinner.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter) storage_spinner.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter) location_spinner.getAdapter()).notifyDataSetChanged();

        Toast.makeText(SendTrolleyActivity.this, "agvType、storage、location disenable", Toast.LENGTH_SHORT).show();
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
                storageResponseBody response = TrolleyTransporter.sendFetchStorage(selectedCartCode,selectedAgvTypes);
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

                        Toast.makeText(SendTrolleyActivity.this, "fetch storage OK", Toast.LENGTH_SHORT).show();
                        Log.d("Dialog", "获取目标库区成功");
                        //sendNotes.setVisibility(View.VISIBLE);
                        enableStorageSpinner();
                    } else {
                        trolleyCodeEditText.setText("");
                        selectedCartCode = "";
                        isProgrammaticallyClearing = true;
                        //sendNotes.setVisibility(View.GONE);
                        Toast.makeText(SendTrolleyActivity.this, "fetch storage Failed:" + finalMessage, Toast.LENGTH_SHORT).show();
                        Log.d("Dialog", "获取目标库区失败");
                    }
                    sendButton.setEnabled(true); // 重新启用按钮
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    //progressDialog.dismiss();
                    trolleyCodeEditText.setText("");
                    selectedCartCode = "";
                    isProgrammaticallyClearing = true;
                    Toast.makeText(SendTrolleyActivity.this, "获取库区数据异常", Toast.LENGTH_SHORT).show();
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
                locationResponseBody response = TrolleyTransporter.sendFetchLocation(selectedCartCode,selectedAgvTypes,selectedStorage);
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

                        Toast.makeText(SendTrolleyActivity.this, "fetch location OK", Toast.LENGTH_SHORT).show();
                        Log.d("Dialog", "获取目标库位成功");
                        //sendNotes.setVisibility(View.VISIBLE);
                        enableLocationSpinner();
                    } else {
                        Toast.makeText(SendTrolleyActivity.this, "fetch location Failed" + finalMessage, Toast.LENGTH_SHORT).show();
                        trolleyCodeEditText.setText("");
                        selectedCartCode = "";
                        isProgrammaticallyClearing = true;
                        Log.d("Dialog", "获取目标库位失败");
                        //sendNotes.setVisibility(View.GONE);
                    }
                    sendButton.setEnabled(true); // 重新启用按钮
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    //progressDialog.dismiss();
                    trolleyCodeEditText.setText("");
                    selectedCartCode = "";
                    isProgrammaticallyClearing = true;
                    Toast.makeText(SendTrolleyActivity.this, "获取库位数据异常", Toast.LENGTH_SHORT).show();
                    Log.e("SendTrolley", "获取库位数据异常: " + e.getMessage());
                });
            }
        }).start();
    }

    private void sendTrolley() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String trolleyId = trolleyCodeEditText.getText().toString();
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (trolleyId.isEmpty() || selectedAgvTypes.isEmpty()) {
                            Toast.makeText(SendTrolleyActivity.this, "货架码和小车类型不能为空 Mã kệ và loại xe không được để trống", Toast.LENGTH_SHORT).show();
                            sendButton.setEnabled(true); // 重新启用按钮
                            return;
                        }
                        //progressBar.setVisibility(View.VISIBLE);
                    }
                });
                if (Objects.equals(selectedAgvTypes, "AGF1")) {
                    TolleryTransportResponseBody agf_response = TrolleyTransporter.sendSelected(trolleyId,selectedAgvTypes,selectedStorage,selectedLocation);
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
                                Toast.makeText(SendTrolleyActivity.this, "send AGF OK", Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(SendTrolleyActivity.this, "AGF模具下料成功", Toast.LENGTH_SHORT).show();
                                Log.d("Dialog", "生成AGF模具下料任务成功");
                                //sendNotes.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(SendTrolleyActivity.this, "send AGF Failed" + finalMessage, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(SendTrolleyActivity.this, "AGF模具下料失败" + finalMessage, Toast.LENGTH_SHORT).show();
                                Log.d("Dialog", "生成AGF模具下料任务失败！");
                                //sendNotes.setVisibility(View.GONE);
                            }
                            trolleyCodeEditText.setText("");
                            selectedCartCode = "";
                            isProgrammaticallyClearing = true; // 无论发送成功或失败，都要清除上一次的数据
                        }
                    });
                } else {
                    TolleryTransportResponseBody agv_response = TrolleyTransporter.sendSelected(trolleyId,selectedAgvTypes,selectedStorage,selectedLocation);
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
                            sendButton.setEnabled(true); // 重新启用按钮
                            //progressBar.setVisibility(View.GONE);

                            if (finalSuccess) {
                                Toast.makeText(SendTrolleyActivity.this, "send AGV OK", Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(SendTrolleyActivity.this, "AGV下料成功", Toast.LENGTH_SHORT).show();
                                Log.d("Dialog", "生成AGV下料任务成功");
                                //sendNotes.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(SendTrolleyActivity.this, "send AGV Failed" + finalMessage, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(SendTrolleyActivity.this, "AGV下料失败" + finalMessage, Toast.LENGTH_SHORT).show();
                                Log.d("Dialog", "生成AGV下料任务失败！");
                                //sendNotes.setVisibility(View.GONE);
                            }
                            trolleyCodeEditText.setText("");
                            selectedCartCode = "";
                            isProgrammaticallyClearing = true; // 无论发送成功或失败，都要清除上一次的数据
                        }
                    });
                }

                //showMoldTaskDialog(trolleyId);
            }
        }).start();
    }

    private void showMoldTaskDialog(String trolleyId) {
        MoldTaskDialogFragment dialog = MoldTaskDialogFragment.newInstance(trolleyId);

        dialog.setMoldTaskDialogListener(new MoldTaskDialogFragment.MoldTaskDialogListener() {
            @Override
            public void onYesSelected(String trolleyId) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 生成AGF的模具搬运任务
                        Log.d("Dialog", "选择yes，准备生成AGF任务");
                        String agvType = "AGF1";
                        TolleryTransportResponseBody agf_response = TrolleyTransporter.sendMold(trolleyId,agvType);
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
                                    Toast.makeText(SendTrolleyActivity.this, "send AGF OK", Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(SendTrolleyActivity.this, "AGF模具下料成功", Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "生成AGF模具下料任务成功");
                                    //sendNotes.setVisibility(View.VISIBLE);
                                } else {
                                    Toast.makeText(SendTrolleyActivity.this, "send AGF Failed" + finalMessage, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(SendTrolleyActivity.this, "AGF模具下料失败" + finalMessage, Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "生成AGF模具下料任务成功");
                                    //sendNotes.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onNoSelected(String trolleyId) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 生成AGV的模具搬运任务
                        Log.d("Dialog", "选择no，准备生成AGV任务");
                        String agvType = "AGV";
                        TolleryTransportResponseBody agv_response = TrolleyTransporter.sendMold(trolleyId,agvType);
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
                                sendButton.setEnabled(true); // 重新启用按钮
                                //progressBar.setVisibility(View.GONE);

                                if (finalSuccess) {
                                    Toast.makeText(SendTrolleyActivity.this, "send AGV OK", Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(SendTrolleyActivity.this, "AGV下料成功", Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "生成AGV下料任务成功");
                                    //sendNotes.setVisibility(View.VISIBLE);
                                } else {
                                    Toast.makeText(SendTrolleyActivity.this, "send AGV Failed" + finalMessage, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(SendTrolleyActivity.this, "AGV下料失败" + finalMessage, Toast.LENGTH_SHORT).show();
                                    Log.d("Dialog", "生成AGV下料任务失败！");
                                    //sendNotes.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onCancelSelected() {
                // 取消操作
                Log.d("Dialog", "选择取消，不生成任务");
                Toast.makeText(SendTrolleyActivity.this, "cancel", Toast.LENGTH_SHORT).show();
//                Toast.makeText(SendTrolleyActivity.this, "操作已取消", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("Dialog", "显示模具选择对话框");
        dialog.show(getSupportFragmentManager(), "MoldTaskDialog");
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