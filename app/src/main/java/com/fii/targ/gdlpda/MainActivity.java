package com.fii.targ.gdlpda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化按钮
        ImageButton bindUnbindButton = findViewById(R.id.query_button);
        ImageButton callSendTrolleyButton = findViewById(R.id.call_agv_button);
        ImageButton requestButton = findViewById(R.id.request_button);
        ImageButton exceptionMessageButton = findViewById(R.id.running_status_exception_button);
        ImageButton taskButton = findViewById(R.id.running_status_task_button);        

        // 跳转到 Bind and Unbind 页面
        bindUnbindButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QueryModuleActivity.class);
            startActivity(intent);
        });

        // 跳转到 Call and Send Trolley 页面
        callSendTrolleyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CallModuleActivity.class);
            startActivity(intent);
        });

        // 跳转到 request 页面
        requestButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HalfAutomaticActivity.class);
            startActivity(intent);
        });

        // 跳转到 Exception Message 页面
        exceptionMessageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExceptionActivity.class);
            startActivity(intent);
        });

        taskButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TaskActivity.class);
            startActivity(intent);
        });

            // Home button
            ImageButton homeButton = findViewById(R.id.home);
            homeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!(MainActivity.this instanceof MainActivity)) {
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            });

            // Info button
            ImageButton infoButton = findViewById(R.id.info);
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);
                }
            });
    }
}