
package com.fii.targ.gdlpda;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fii.targ.gdlpda.service.Constants;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    private EditText ipAddressInput;
    private EditText portInput;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ipAddressInput = findViewById(R.id.ip_address_input);
        portInput = findViewById(R.id.port_input);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveServerSettings();
            }
        });

        loadServerSettings();
    }

    private void saveServerSettings() {
        String ipAddress = ipAddressInput.getText().toString();
        String port = portInput.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences("ServerSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ServerIP", ipAddress);
        editor.putString("ServerPort", port);
        editor.apply();

        Constants.setSettingIP(ipAddress);
        Constants.setSettingPort(port);


        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
    }

    private void loadServerSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("ServerSettings", MODE_PRIVATE);
        String ipAddress = sharedPreferences.getString("ServerIP", "");
        String port = sharedPreferences.getString("ServerPort", "");

        ipAddressInput.setText(ipAddress);
        portInput.setText(port);
    }
}