package com.fii.targ.gdlpda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fii.targ.gdlpda.model.ExceptionItem;
import com.fii.targ.gdlpda.model.FetchErrorMsgResponse;
import com.fii.targ.gdlpda.model.SysErrorMsg;
import com.fii.targ.gdlpda.service.ExceptionMessageService;
import com.fii.targ.gdlpda.service.ExceptionOperation;
import com.fii.targ.gdlpda.service.HeartbeatService;
import com.fii.targ.gdlpda.ui.ExceptionAdapter;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ExceptionActivity extends AppCompatActivity {

    private static final String TAG = "ExceptionActivity";
    List<ExceptionItem> exceptionList;
    RecyclerView recyclerView;
    private ExecutorService executorService;
    private Future<?> futureTask;
    private Future<?> futureTaskClear;

    ExceptionAdapter adapter;

    private TextView exceptionCountTextView;
    private ImageButton refreshButton;

    ProgressBar progressBar;
    private Handler handler;
    private Runnable heartbeatRunnable;
    private static final long HEARTBEAT_INTERVAL = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_exception);

        progressBar = findViewById(R.id.progress_bar);
        initService();

        exceptionCountTextView = findViewById(R.id.exception_count);
        refreshButton = findViewById(R.id.refresh_button);

        
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshExceptions();
            }
        });
        



        // Home button
        ImageButton homeButton = findViewById(R.id.exception_nav);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(ExceptionActivity.this instanceof ExceptionActivity)) {
                    Intent intent = new Intent(ExceptionActivity.this, ExceptionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });

        ImageButton forkliftButton = findViewById(R.id.task);
        forkliftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExceptionActivity.this, TaskActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
       
    }

    @Override
    public void onBackPressed() {
        // 返回主页
        Intent intent = new Intent(ExceptionActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 清除栈顶，返回主页
        startActivity(intent);
        finish();
    }

    private void clearExceptionDetails() {
        // Implement the logic to clear exception details
    }

    private void initService() {
        exceptionList = new ArrayList<>();

        recyclerView = findViewById(R.id.exception_list);
        adapter = new ExceptionAdapter(ExceptionActivity.this, exceptionList, new ExceptionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ExceptionItem exceptionItem) {

            }

            @Override
            public void onClearButtonClick(ExceptionItem exceptionItem, ImageButton clearButton) {
                clearButton.setEnabled(false);
                dumpException(exceptionItem);
                showClearExceptionDialog(exceptionItem, clearButton);
            }

        });

        executorService = Executors.newSingleThreadExecutor();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        IntentFilter filter = new IntentFilter("com.fii.targ.gdlpda.UPDATE_EXCEPTION");
        IntentFilter filterClear = new IntentFilter("com.fii.targ.gdlpda.CLEAR_EXCEPTION");
        registerReceiver(exceptionReceiver, filter);
        registerReceiver(exceptionReceiver, filterClear);

        // Start the ExceptionMessageService
        Intent serviceIntent = new Intent(this, ExceptionMessageService.class);
        startService(serviceIntent);

        Intent heartbeatServiceIntent = new Intent(this, HeartbeatService.class);
        startService(heartbeatServiceIntent);

        refreshExceptions();
    }

    private void refreshExceptions() {
        futureTask = executorService.submit(new Runnable() {
            List<ExceptionItem> exceptions = new ArrayList<>();
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

                FetchErrorMsgResponse response = ExceptionOperation.getExceptionsFromMCS();

                if (response.getCode().equals("0")) {
                    List<SysErrorMsg> sysErrorMsgs = response.getData();
                    if (sysErrorMsgs != null && !sysErrorMsgs.isEmpty()) {
                        exceptions = ExceptionOperation.convertToExceptionItemList(sysErrorMsgs);
                    }


                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExceptionActivity.this, "Failed to fetch exceptions: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }    
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        exceptionList.clear();
                        dumpExceptions(exceptions); //DEBUG TODO
                        exceptionList.addAll(exceptions);
                        adapter.notifyDataSetChanged();
                        updateExceptionCount();
                    }
                });
            }
        });
    }
    
    private void updateExceptionCount() {
        // Logic to get the current number of exception messages
        int exceptionCount = adapter.getItemCount();
        if (exceptionCount > 0) {
            exceptionCountTextView.setVisibility(View.VISIBLE);
            exceptionCountTextView.setText(String.valueOf(exceptionCount));
        } else {
            exceptionCountTextView.setVisibility(View.GONE);
        }
        
    }

    private void dumpExceptions(List<ExceptionItem> exceptions) {
        for (ExceptionItem exception : exceptions) {
            Log.d("Exception", exception.toString());
        }
    }

    private void dumpException(ExceptionItem exception) {

        Log.d("Exception", exception.toString());

    }
    
    private void dumpSysErrorMsgs(List<SysErrorMsg> sysErrorMsgs) {
        if (sysErrorMsgs == null) return;
        for (SysErrorMsg sysErrorMsg : sysErrorMsgs) {
            Log.d("SysErrorMsg", sysErrorMsg.toString());
        }
    }

    private BroadcastReceiver exceptionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.fii.targ.gdlpda.UPDATE_EXCEPTION")) {

                String errorUid = intent.getStringExtra("errorUid");
                String errorCode = intent.getStringExtra("errorCode");
                String errorSource = intent.getStringExtra("errorSource");
                String errorMessage = intent.getStringExtra("errorMessage");
                String errorDeviceId = intent.getStringExtra("errorDeviceID");
                String errorTime = intent.getStringExtra("errorTime");
                String notes = intent.getStringExtra("notes");
                String errorLevel = intent.getStringExtra("errorLevel");

                if (errorMessage != null) {
                    exceptionList.add(new ExceptionItem(errorUid, errorCode, errorSource, errorMessage, errorDeviceId, errorTime, notes, errorLevel));
                    adapter.notifyDataSetChanged();
                    updateExceptionCount();
                }
            } else if (intent.getAction().equals("com.fii.targ.gdlpda.CLEAR_EXCEPTION")) {
                // 从exceptionList中删除对应的异常
                String errorUid = intent.getStringExtra("errorUid");
                //判断exceptionItem是否为空
                Log.d(TAG, "CLEAR_EXCEPTION. errorUid: " + errorUid);
                if (exceptionList != null || exceptionList.size() > 0) {
                    for (ExceptionItem exceptionItem : exceptionList) {
                        if (exceptionItem.getErrorUid().equals(errorUid)) {
                            exceptionList.remove(exceptionItem);
                            adapter.notifyDataSetChanged();
                            updateExceptionCount();
                            break;
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(exceptionReceiver);
        // Stop the ExceptionMessageService
        Intent serviceIntent = new Intent(this, ExceptionMessageService.class);
        stopService(serviceIntent);

        if (futureTask != null && !futureTask.isDone()) {
            futureTask.cancel(true);
        }
        if (futureTaskClear != null && !futureTaskClear.isDone()) {
            futureTaskClear.cancel(true);
        }
        executorService.shutdown();
    }

    private void clearException(ExceptionItem exceptionItem, ImageButton clearButton, boolean solved) {
        futureTaskClear = executorService.submit(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

                boolean success = ExceptionOperation.clear(exceptionItem.getErrorUid(), solved);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearButton.setEnabled(true); // 重新启用按钮
                        progressBar.setVisibility(View.GONE);

                        if (success) {
                            Toast.makeText(ExceptionActivity.this, "Exception cleared successfully", Toast.LENGTH_SHORT).show();
                            exceptionList.remove(exceptionItem);
                            adapter.notifyDataSetChanged();
                            updateExceptionCount();
                        } else {
                            Toast.makeText(ExceptionActivity.this, "Failed to clear exception", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
    private void showClearExceptionDialog(ExceptionItem exceptionItem, ImageButton clearButton) {
        new AlertDialog.Builder(this)
                .setTitle("Clear Exception")
                .setMessage("Has the exception been resolved?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearException(exceptionItem, clearButton, true);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearException(exceptionItem, clearButton, false);
                    }
                })
                .setCancelable(false)
                .show();
    }

}