package com.fii.targ.gdlpda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class QueryModuleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_module);

        // Bind Products button
        ImageButton bindProductButton = findViewById(R.id.bindproduct);
        bindProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(QueryModuleActivity.this, BindProductActivity.class);
                Intent intent = new Intent(QueryModuleActivity.this, BindGroundActivity.class);
                startActivity(intent);
            }
        });

        // Bind Ground button
         ImageButton bindGroundButton = findViewById(R.id.bindground);
         bindGroundButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(QueryModuleActivity.this, BindProductActivity.class);
                 startActivity(intent);
             }
         });

        // Unbind button
        ImageButton unbindButton = findViewById(R.id.unbind);
        unbindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QueryModuleActivity.this, UnbindActivity.class);
                startActivity(intent);
            }
        });

        // Unbind Product button
        ImageButton unbindProductButton = findViewById(R.id.unbindground);
        unbindProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QueryModuleActivity.this, UnbindProductActivity.class);
                startActivity(intent);
            }
        });

        // Query button
        ImageButton queryButton = findViewById(R.id.query);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QueryModuleActivity.this, QueryActivity.class);
                startActivity(intent);
            }
        });
        // Query Ground button
        ImageButton queryGroundButton = findViewById(R.id.query_ground);
        queryGroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QueryModuleActivity.this, QueryGroundActivity.class);
                startActivity(intent);
            }
        });

        // Home button
        ImageButton homeButton = findViewById(R.id.home);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QueryModuleActivity.this, QueryModuleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // Forklift button
        ImageButton forkliftButton = findViewById(R.id.forklift);
        forkliftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QueryModuleActivity.this, ForkliftActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 返回主页
        Intent intent = new Intent(QueryModuleActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 清除栈顶，返回主页
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // No additional cleanup is required as there are no background tasks or services started in this activity.
    }
}