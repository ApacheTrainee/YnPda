package com.fii.targ.gdlpda;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fii.targ.gdlpda.service.Constants;
import com.fii.targ.gdlpda.update.UpdateManager;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    ImageView fiiImage;
    TextView descriptionText;
    TextView userInfo;
    TextView employeeNumber;
    TextView apkVersion;
    Button versionCheckButton;

    private static final String KEY_PREFS_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_DEPARTMENT_ID = "department_id";
    private static final String KEY_EMPLOYEE_ID = "employee_id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Initialize UI elements
        fiiImage = findViewById(R.id.fii_image);
        descriptionText = findViewById(R.id.description_text);
        userInfo = findViewById(R.id.user_info);
        employeeNumber = findViewById(R.id.employee_number);
        apkVersion = findViewById(R.id.apk_version);
        versionCheckButton = findViewById(R.id.check_version_button);
        TextView apkDownloadTextView = findViewById(R.id.apk_download_text_view);

        versionCheckButton.setOnClickListener(v -> {
            // Check for new version

            // Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            // intent.setData(Uri.parse("package:com.android.providers.downloads"));
            // this.startActivity(intent);
    
            // UpdateManager updateManager = ((UpdateApplication) getApplication()).getUpdateManager();
            UpdateManager updateManager = new UpdateManager(this);
            updateManager.checkForUpdate(this, apkDownloadTextView);
        });

        // Set data to UI elements
        descriptionText.setText("GDL AGV Infomation System");
        displayLoginInformation();
        displayApkVersion();
    
    }
    public TextView getApkDownloadTextView() {
        return findViewById(R.id.apk_download_text_view);
    }
    private void displayApkVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;

            apkVersion.setText("Version: " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            apkVersion.setText("Version information not available");
        }
    }

    private void displayLoginInformation() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_PREFS, Context.MODE_PRIVATE);

        String username = sharedPreferences.getString(KEY_PREFS_NAME, "N/A");
        String department = sharedPreferences.getString(KEY_DEPARTMENT_ID, "N/A");
        String employeeId = sharedPreferences.getString(KEY_EMPLOYEE_ID, "N/A");

        userInfo.setText("Username: " + username);
        employeeNumber.setText("Employee ID: " + employeeId);
    }
}