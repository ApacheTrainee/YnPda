package com.fii.targ.gdlpda;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fii.targ.gdlpda.service.Constants;
import com.fii.targ.gdlpda.service.LoginValidation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String LOGIN_PREFS = "LoginPrefs";
    private static final String HST_USER = "LoginHstUser";
    private static final String HST_PASSWORD = "LoginHstPsw";

    private LoginTask loginTask;
    private ProgressBar progressBar;
    private Button loginButton;

    private ImageButton settingButton;
    private TextView apkVersion;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apkVersion = findViewById(R.id.version);
        apkVersion.setText("Version: " + getApkVersion());
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progress_bar);
        settingButton = findViewById(R.id.settings_button);
        readLoginHst(this);
        loadServerSettings();

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String usernameInput = username.getText().toString();
                String passwordInput = password.getText().toString();

                saveLoginHst(usernameInput, passwordInput, LoginActivity.this);

                // Perform login validation with backend
                // new Thread(() -> {
                //     boolean isValid = LoginValidation.validateLogin(usernameInput, passwordInput, LoginActivity.this);
                //     runOnUiThread(() -> {
                //         if (isValid) {
                //             // Navigate to HomeActivity
                //             Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                //             startActivity(intent);
                //             finish();
                //         } else {
                //             // Show error message
                //             Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                //         }
                //     });
                // }).start();
                // Cancel any existing login task
                if (loginTask != null) {
                    loginTask.cancel(true);
                }

                // Start a new login task
                loginTask = new LoginTask(usernameInput, passwordInput, LoginActivity.this);
                loginTask.execute();
            }
        });
    }

    private String getApkVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void saveLoginHst(String user, String psw, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HST_USER, user);
        editor.putString(HST_PASSWORD, psw);
        editor.apply();
    }

    private void readLoginHst(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(HST_USER, "");
        String password = sharedPreferences.getString(HST_PASSWORD, "");

        EditText usernameEditText = findViewById(R.id.username);
        EditText passwordEditText = findViewById(R.id.password);

        usernameEditText.setText(username);
        passwordEditText.setText(password);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the login task if the activity is destroyed
        if (loginTask != null) {
            loginTask.cancel(true);
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {

        private String username;
        private String password;
        private Context context;

        public LoginTask(String username, String password, Context context) {
            this.username = username;
            this.password = password;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show the progress bar and disable the login button
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return LoginValidation.validateLogin(username, password, context);
        }

        @Override
        protected void onPostExecute(Boolean isValid) {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            if (isValid) {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
                ((LoginActivity) context).finish();
            } else {
                Toast.makeText(context, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            Toast.makeText(context, "Login task was cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadServerSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("ServerSettings", MODE_PRIVATE);
        String ipAddress = sharedPreferences.getString("ServerIP", "");
        String port = sharedPreferences.getString("ServerPort", "");

        Constants.setSettingIP(ipAddress);
        Constants.setSettingPort(port);
    }
}

