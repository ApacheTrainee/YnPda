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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.fii.targ.gdlpda.model.TolleryTransportResponseBody;
import com.fii.targ.gdlpda.model.locationResponseBody;
import com.fii.targ.gdlpda.model.storageResponseBody;
import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.scanner.ScannerManager;
import com.fii.targ.gdlpda.service.TrolleyFlTransporter;
import com.fii.targ.gdlpda.service.TrolleyTransporter;
import com.fii.targ.gdlpda.ui.TrolleyInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CallFlTrolleyActivity extends AppCompatActivity {

    private static final String TAG = "CallFlTrolleyActivity";

    private EditText groundCodeEditText;
    private ImageButton groundScanButton;
    private Button callAGVButton;
//    private TextView callNotes;
//    private TrolleyInfoAdapter adapter;
//    private ProgressBar progressBar;
//    private List<TrolleyInfo> trolleyInfoData;
//    private boolean isFetching = false;
//    private ExecutorService executorService;
//    private Future<?> futureTask;

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
    private String selectedGroundCode = "";
    private String selectedAgvTypes = "";
    private String selectedStorage = "";
    private String selectedLocation = "";
    private String selectedHeight = "";
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable groundInputRunnable;
    ScannerManager scannerManager;

    private TextView scanMessageTextView;
    private Dialog scanDialog;
    private boolean isProgrammaticallyClearing = false;
    private static final int SCAN_REQUEST_ID = Constants.SCAN_TYPE_VN_AGV_GROUND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_fl_trolley);

        groundCodeEditText = findViewById(R.id.ground_code_input);
        groundScanButton = findViewById(R.id.ground_code_scan);
        RecyclerView trolleyInfoList = findViewById(R.id.trolley_info_list);
//        progressBar = findViewById(R.id.progress_bar);
        callAGVButton = findViewById(R.id.call_agv_button);
//        executorService = Executors.newSingleThreadExecutor();

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
                        selectedGroundCode = barcodeData;
                        //enableAgvTypeSpinner();
                        Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " Barcode data: " + groundCodeEditText.getText().toString());
                        groundCodeEditText.clearFocus();
                        //fetchAvailableTrolleys();
                    }
                });
            }

            @Override
            public void onScanError(int requestId, String error) {
                runOnUiThread(() -> {
                    onScanComplete();
                    String errorMessage = error;
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "由于未知错误，扫描失败";
                    }
 
                    Toast.makeText(CallFlTrolleyActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }

        });

        // 地码输入监听 - 使用延时检测
        groundInputRunnable = new Runnable() {
            @Override
            public void run() {
                selectedGroundCode = groundCodeEditText.getText().toString();
                if (!selectedGroundCode.isEmpty()) {
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
                handler.removeCallbacks(groundInputRunnable);
                // 延迟3000毫秒执行，如果用户持续输入，会一直移除回调
                handler.postDelayed(groundInputRunnable, 3000);
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
                    findStorage();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 库区选择监听
        storage_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    selectedStorage = storage.get(position);
                    findLocation();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 库位选择监听
        location_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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

//        trolleyInfoData = new ArrayList<>();
//        adapter = new TrolleyInfoAdapter(trolleyInfoData, new TrolleyInfoAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(TrolleyInfo trolleyInfo) {
//
//            }
//
//            @Override
//            public void onCallButtonClick(TrolleyInfo trolleyInfo, Button callButton) {
//                callButton.setEnabled(false);
//                callTrolley(trolleyInfo, callButton);
//            }
//
//        });
//
//        trolleyInfoList.setLayoutManager(new LinearLayoutManager(this));
//        trolleyInfoList.setAdapter(adapter);

        groundScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groundCodeEditText.setText("");
                selectedGroundCode = "";
                isProgrammaticallyClearing = true;
                scanMessageTextView.setText("Scan the ground code");

                // Show the scan dialog
                scanDialog.show();
        
                // Start the scan
                scannerManager.requestScan(SCAN_REQUEST_ID);
            }
        });
        callAGVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cellId = groundCodeEditText.getText().toString();
                callAGV(cellId);
            }
        });
    }

    private void callTrolley(TrolleyInfo trolleyInfo, Button callButton) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("DEBUG", "callTrolley Thread"  );
                String cellId = groundCodeEditText.getText().toString();
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (cellId.isEmpty()) {
                            Toast.makeText(CallFlTrolleyActivity.this, "地码不能为空 Mã mặt đất không được để trống", Toast.LENGTH_SHORT).show();
                            callButton.setEnabled(true); // 重新启用按钮
                            return;
                        }
                    }
                });

                boolean success = TrolleyFlTransporter.call(cellId, trolleyInfo.getSn());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callButton.setEnabled(true); // 重新启用按钮

                        if (success) {
                            Toast.makeText(CallFlTrolleyActivity.this, "call AGF OK", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(CallFlTrolleyActivity.this, "呼叫成功", Toast.LENGTH_SHORT).show();
                            //fetchAvailableTrolleys(); // 刷新可选列表
                        } else {
                            Toast.makeText(CallFlTrolleyActivity.this, "call AGF Failed", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(CallFlTrolleyActivity.this, "呼叫失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
     
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
        Toast.makeText(CallFlTrolleyActivity.this, "agvType enable", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(CallFlTrolleyActivity.this, "height enable", Toast.LENGTH_SHORT).show();
    }

    private void enableStorageSpinner() {
        // 启用库区下拉框并加载数据
        storage_spinner.setEnabled(true);
        ((ArrayAdapter) storage_spinner.getAdapter()).notifyDataSetChanged();
        storage_spinner.setSelection(0); // 新增这一行，确保默认显示为空
        agvType_spinner.setEnabled(false);
        Toast.makeText(CallFlTrolleyActivity.this, "storage enable", Toast.LENGTH_SHORT).show();
    }

    private void enableLocationSpinner() {
        // 启用库位下拉框并加载数据
        location_spinner.setEnabled(true);
        ((ArrayAdapter) location_spinner.getAdapter()).notifyDataSetChanged();
        location_spinner.setSelection(0); // 新增这一行，确保默认显示为空
        storage_spinner.setEnabled(false);
        Toast.makeText(CallFlTrolleyActivity.this, "location enable", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(CallFlTrolleyActivity.this, "agvType、storage、location、height disenable", Toast.LENGTH_SHORT).show();
    }

    private void findStorage() {
        // 显示加载中提示
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("加载库区数据中...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();

        // 使用线程执行网络请求
        new Thread(() -> {
            try {
                storageResponseBody response = TrolleyTransporter.callFindStorageAGF(selectedGroundCode,selectedAgvTypes);
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

                        Toast.makeText(CallFlTrolleyActivity.this, "find storage OK", Toast.LENGTH_SHORT).show();
                        Log.d("Dialog", "获取起点库区成功");
                        enableStorageSpinner();
                    } else {
                        groundCodeEditText.setText("");
                        selectedGroundCode = "";
                        isProgrammaticallyClearing = true;
                        Toast.makeText(CallFlTrolleyActivity.this, "find storage Failed:" + finalMessage, Toast.LENGTH_SHORT).show();
                        Log.d("Dialog", "获取起点库区失败");
                    }
                    callAGVButton.setEnabled(true); // 重新启用按钮
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    //progressDialog.dismiss();
                    groundCodeEditText.setText("");
                    selectedGroundCode = "";
                    isProgrammaticallyClearing = true;
                    Toast.makeText(CallFlTrolleyActivity.this, "获取库区数据异常", Toast.LENGTH_SHORT).show();
                    Log.e("CallTrolley", "获取库区数据异常: " + e.getMessage());
                });
            }
        }).start();
    }

    private void findLocation() {
        // 显示加载中提示
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("加载库位数据中...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();

        new Thread(() -> {
            try {
                locationResponseBody response = TrolleyTransporter.callFindLocationAGF(selectedGroundCode,selectedAgvTypes,selectedStorage);
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

                        Toast.makeText(CallFlTrolleyActivity.this, "find location OK", Toast.LENGTH_SHORT).show();
                        Log.d("Dialog", "获取起点库位成功");
                        enableLocationSpinner();
                    } else {
                        Toast.makeText(CallFlTrolleyActivity.this, "find location Failed" + finalMessage, Toast.LENGTH_SHORT).show();
                        groundCodeEditText.setText("");
                        selectedGroundCode = "";
                        isProgrammaticallyClearing = true;
                        Log.d("Dialog", "获取起点库位失败");
                    }
                    callAGVButton.setEnabled(true); // 重新启用按钮
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    //progressDialog.dismiss();
                    groundCodeEditText.setText("");
                    selectedGroundCode = "";
                    isProgrammaticallyClearing = true;
                    Toast.makeText(CallFlTrolleyActivity.this, "获取库位数据异常", Toast.LENGTH_SHORT).show();
                    Log.e("CallTrolley", "获取库位数据异常: " + e.getMessage());
                });
            }
        }).start();
    }

    private void callAGV(String cellId ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("DEBUG", "Call AGV Thread"  );
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (cellId.isEmpty() || selectedAgvTypes.isEmpty()) {
                            Toast.makeText(CallFlTrolleyActivity.this, "地码和小车类型不能为空 Mã mặt đất và loại xe không được để trống", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
                if (Objects.equals(selectedAgvTypes, "AGF")) {
                    TolleryTransportResponseBody agf_response = TrolleyTransporter.callSelectedAGF(cellId,selectedAgvTypes,selectedStorage,selectedLocation,selectedHeight);
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
                            callAGVButton.setEnabled(true); // 重新启用按钮
                            //progressBar.setVisibility(View.GONE);

                            if (finalSuccess) {
                                Toast.makeText(CallFlTrolleyActivity.this, "call AGF OK", Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(CallTrolleyActivity.this, "AGF模具上料成功", Toast.LENGTH_SHORT).show();
                                Log.d("Dialog", "生成AGF上料任务成功");
                            } else {
                                Toast.makeText(CallFlTrolleyActivity.this, "call AGF Failed" + finalMessage, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(CallTrolleyActivity.this, "AGF模具上料失败" + finalMessage, Toast.LENGTH_SHORT).show();
                                Log.d("Dialog", "生成AGF上料任务失败！");
                            }
                            groundCodeEditText.setText("");
                            selectedGroundCode = ""; // 无论发送成功或失败，都要清除上一次的数据
                            isProgrammaticallyClearing = false;
                        }
                    });
                }
            }
        }).start();

    }

//    private void fetchAvailableTrolleys() {
//        futureTask = executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                String cellId = groundCodeEditText.getText().toString();
//                Log.d(TAG, "cellId："+cellId);
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        if (isFetching) {
//                            Toast.makeText(CallFlTrolleyActivity.this, "已找到货架，请等待 Đã tìm thấy kệ, vui lòng chờ", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        if (cellId.isEmpty()) {
//                            Toast.makeText(CallFlTrolleyActivity.this, "地码不能为空 Mã mặt đất không được để trống", Toast.LENGTH_SHORT).show();
//                            //progressBar.setVisibility(View.GONE);
//                            isFetching = false;
//                            return;
//                        }
//                        isFetching = true;
//                        //callNotes.setVisibility(View.GONE);
//                        //progressBar.setVisibility(View.VISIBLE);
//                    }
//
//
//                });
//                FetchAvailableTrolleyResponse response = TrolleyFlTransporter.fetchAvailableTrolleys(cellId);
//                trolleyInfoData.clear();
//                if ("0".equals(response.getCode())) {
//                    Log.d(TAG, "response.getCode() 0");
//                    List<CartInfo> cartInfos = response.getData();
//                    if (cartInfos != null && !cartInfos.isEmpty()) {
//                        trolleyInfoData.addAll(TrolleyFlTransporter.convertToTrolleyInfoList(cartInfos));
//                        adapter.setItems(trolleyInfoData);
//                        Log.d(TAG, "trolleyInfoData: " + trolleyInfoData);
//                    }
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            String message = response.getMessage();
//                            if (message == null || message.isEmpty()) {
//                                message = "由于未知错误，找货架失败";
//                            }
//
//                            Toast.makeText(CallFlTrolleyActivity.this, message, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (adapter.getItemCount() > 0) {
//                            //callNotes.setVisibility(View.VISIBLE);
//                        }
//                        adapter.notifyDataSetChanged();
//                        //progressBar.setVisibility(View.GONE); // Hide the progress bar
//                        isFetching = false; // Reset the flag
//                    }
//                });
//            }
//        });
//    }

    List<TrolleyInfo> getTrolleyInfoData() {
        List<TrolleyInfo> debugTrolleyInfos = new ArrayList<>();
        debugTrolleyInfos.add(new TrolleyInfo("TROLLEY1", "CELL1"));
        debugTrolleyInfos.add(new TrolleyInfo("TROLLEY2", "CELL2"));
        debugTrolleyInfos.add(new TrolleyInfo("TROLLEY3", "CELL3"));
        debugTrolleyInfos.add(new TrolleyInfo("TROLLEY4", "CELL4"));
        debugTrolleyInfos.add(new TrolleyInfo("TROLLEY5", "CELL5"));
        debugTrolleyInfos.add(new TrolleyInfo("TROLLEY6", "CELL6"));

        return debugTrolleyInfos;


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
    protected void onDestroy() {
        super.onDestroy();
        if (scanDialog.isShowing()) {
            Log.d("onDestroy", "dismiss");
            scanDialog.dismiss();
        }
//        if (futureTask != null && !futureTask.isDone()) {
//            futureTask.cancel(true);
//        }
//        executorService.shutdown();
    }
}