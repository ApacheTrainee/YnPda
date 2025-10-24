package com.fii.targ.gdlpda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ForkliftActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forklift);

        // Bind Products button
        ImageButton bindProductButton = findViewById(R.id.bindproduct);
        bindProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForkliftActivity.this, BindFlProductActivity.class);
                startActivity(intent);
            }
        });

        // Unbind button
        ImageButton unbindButton = findViewById(R.id.unbind);
        unbindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForkliftActivity.this, UnbindFlActivity.class);
                startActivity(intent);
            }
        });

        // Home button
        ImageButton homeButton = findViewById(R.id.home);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForkliftActivity.this, QueryModuleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
                startActivity(intent);
            }
        });

        // Forklift button
        ImageButton forkliftButton = findViewById(R.id.forklift);
        forkliftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForkliftActivity.this, ForkliftActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 返回主页
        Intent intent = new Intent(ForkliftActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 清除栈顶，返回主页
        startActivity(intent);
        finish();
    }
}