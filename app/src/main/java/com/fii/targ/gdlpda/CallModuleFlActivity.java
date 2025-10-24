package com.fii.targ.gdlpda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallModuleFlActivity extends AppCompatActivity {

    private ExecutorService executorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_module_l11);

        // 初始化线程池
        executorService = Executors.newSingleThreadExecutor();

        // Call Trolley 按钮
        ImageButton callTrolleyButton = findViewById(R.id.calltrolley);
        callTrolleyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CallModuleFlActivity.this, CallFlTrolleyActivity.class);
                startActivity(intent);
            }
        });

        // Send Trolley 按钮
        ImageButton sendTrolleyButton = findViewById(R.id.sendtrolley);
        sendTrolleyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CallModuleFlActivity.this, SendFlTrolleyActivity.class);
                startActivity(intent);
            }
        });

        // Home 按钮
        ImageButton homeButton = findViewById(R.id.home);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CallModuleFlActivity.this, CallModuleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 清除栈顶，返回主页
                startActivity(intent);
            }
        });

         // Forklift 按钮
         ImageButton forkliftButton = findViewById(R.id.forklift);
         forkliftButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(CallModuleFlActivity.this, CallModuleFlActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 清除栈顶，返回主页
                 startActivity(intent);
             }
         });


    }

    @Override
    public void onBackPressed() {
        // 返回主页
        Intent intent = new Intent(CallModuleFlActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 清除栈顶，返回主页
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}