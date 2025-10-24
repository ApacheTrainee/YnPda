package com.fii.targ.gdlpda;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fii.targ.gdlpda.model.Task;
import com.fii.targ.gdlpda.service.TaskService;
import com.fii.targ.gdlpda.ui.TaskAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskActivity extends AppCompatActivity {

    private static final String TAG = "TaskActivity";

    private List<Task> taskList;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;

    private ProgressBar progressBar;
    private TextView taskCountTextView;
    private ImageButton refreshButton;

    private ExecutorService executorService;
    private Future<?> futureTask;

    private Handler handler; // 用于定时刷新
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_task);

        // 初始化控件
        progressBar = findViewById(R.id.progress_bar);
        taskCountTextView = findViewById(R.id.task_count);
        refreshButton = findViewById(R.id.refresh_button);

        // 初始化任务列表
        taskList = new ArrayList<>();
        recyclerView = findViewById(R.id.task_list);
        // 初始化线程池
        executorService = Executors.newSingleThreadExecutor();
        adapter = new TaskAdapter(this, taskList, task -> {
            executorService.submit(() -> {
                showCancelTaskDialog(task);
//                boolean success = TaskService.cancelTask(task.getTaskNumber());
//                runOnUiThread(() -> {
//                    if (success) {
//                        Toast.makeText(TaskActivity.this, "Task " + task.getTaskNumber() + " canceled", Toast.LENGTH_SHORT).show();
//                        taskList.remove(task);
//                        adapter.notifyDataSetChanged();
//                        updateTaskCount();
//                    } else {
//                        Toast.makeText(TaskActivity.this, "Failed", Toast.LENGTH_SHORT).show();
////                        Toast.makeText(TaskActivity.this, "取消任务失败", Toast.LENGTH_SHORT).show();
//                    }
//                });
            });
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 刷新按钮点击事件
        refreshButton.setOnClickListener(v -> refreshTasks());

        // 底部导航按钮
        ImageButton homeButton = findViewById(R.id.task_nav);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(TaskActivity.this, ExceptionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        ImageButton exceptionButton = findViewById(R.id.task);
        exceptionButton.setOnClickListener(v -> {
            Intent intent = new Intent(TaskActivity.this, TaskActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

         // 初始化定时刷新
         handler = new Handler(Looper.getMainLooper());
         refreshRunnable = new Runnable() {
             @Override
             public void run() {
                 refreshTasks(); // 调用刷新任务方法
                 handler.postDelayed(this, 1000); // 1秒后再次执行
             }
         };
 
         // 开始定时刷新
         handler.post(refreshRunnable);

        // 加载任务数据
        refreshTasks();
    }

    private void showCancelTaskDialog(Task task) {
        CancelTaskDialogFragment dialog = CancelTaskDialogFragment.newInstance(task);

        dialog.setCancelTaskDialogListener(new CancelTaskDialogFragment.CancelTaskDialogListener() {
            @Override
            public void onYes(Task task) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Dialog", "选择yes，准备取消任务");
                        boolean success = TaskService.cancelTask(task.getTaskNumber());
                        runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(TaskActivity.this, "Task " + task.getTaskNumber() + " canceled", Toast.LENGTH_SHORT).show();
                                taskList.remove(task);
                                adapter.notifyDataSetChanged();
                                updateTaskCount();
                            } else {
                                Toast.makeText(TaskActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                        Toast.makeText(TaskActivity.this, "取消任务失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onCancel() {
                // 取消操作
                Log.d("Dialog", "选择取消，不生成任务");
                Toast.makeText(TaskActivity.this, "cancel", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("Dialog", "显示取消任务对话框");
        dialog.show(getSupportFragmentManager(), "TaskCancelDialog");
    }

    @Override
    public void onBackPressed() {
        // 返回主页
        Intent intent = new Intent(TaskActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 清除栈顶，返回主页
        startActivity(intent);
        finish();
    }

    private void refreshTasks() {
        futureTask = executorService.submit(() -> {
            //runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

            // 从服务器获取任务数据
            List<Task> tasks = TaskService.getTasksFromServer(10, false);
            if (tasks == null) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    //Toast.makeText(TaskActivity.this, "Failed to fetch tasks", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                taskList.clear();
                taskList.addAll(tasks);
                adapter.notifyDataSetChanged();
                updateTaskCount();
            });
        });
    }

    private void updateTaskCount() {
        int taskCount = adapter.getItemCount();
        if (taskCount > 0) {
            taskCountTextView.setVisibility(View.VISIBLE);
            taskCountTextView.setText(String.valueOf(taskCount));
        } else {
            taskCountTextView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (futureTask != null && !futureTask.isDone()) {
            futureTask.cancel(true);
        }
        executorService.shutdown();

        handler.removeCallbacks(refreshRunnable);
    }
}