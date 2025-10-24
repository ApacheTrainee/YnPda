package com.fii.targ.gdlpda.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class UpdateManager {
    private static final String TAG = "UpdateManager";
    private static final String UPDATE_URL = "http://10.81.175.67:8800/update/";

    private Context context;
    private UpdateInfo updateInfo;
    private File apkFile;
    private boolean isDownloading = false;
    private String expectedHash;

    public UpdateManager(Context context) {
        this.context = context;
    }

    public void checkForUpdate(Activity activity, TextView apkDownloadTextView) {
        Log.d(TAG, "Checking for update...");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UPDATE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UpdateService service = retrofit.create(UpdateService.class);
        service.getLatestVersion().enqueue(new retrofit2.Callback<UpdateInfo>() {
            @Override
            public void onResponse(Call<UpdateInfo> call, retrofit2.Response<UpdateInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateInfo = response.body();
                    try {
                        PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                        int currentVersion = packageInfo.versionCode;

                        Log.d(TAG, "Current version: " + currentVersion);
                        Log.d(TAG, "Latest version: " + updateInfo.versionCode);
                        

                        if (updateInfo.versionCode > currentVersion) {
                            new AlertDialog.Builder(activity)
                                    .setTitle("Update Available")
                                    .setMessage("New version " + updateInfo.versionName + " is available. Download?")
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        downloadApk(updateInfo.apkUrl, updateInfo.versionName, updateInfo.hash, apkDownloadTextView);
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        } else {
                            Toast.makeText(activity, "Already latest version", Toast.LENGTH_SHORT).show();
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdateInfo> call, Throwable t) {
                Log.e(TAG, "Update check failed", t);
                Toast.makeText(context, "Update check failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void downloadApk(String apkUrl, String versionName, String hash, TextView apkDownloadTextView) {
        if (isDownloading) return;
        isDownloading = true;
        expectedHash = hash;

        String apkFileName = "app-" + versionName + ".apk";
        apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), apkFileName);

        if (apkFile.exists() && isFileValid(apkFile, hash)) {
            showUpdateDialog();
            return;
        }

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(apkUrl).build();
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    throw new RuntimeException("Failed to download file: " + response);
                }

                long total = response.body().contentLength();
                long downloaded = 0;

                InputStream in = response.body().byteStream();
                FileOutputStream out = new FileOutputStream(apkFile);

                byte[] buffer = new byte[4096];
                int count;
                Handler handler = new Handler(context.getMainLooper());

                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                    downloaded += count;

                    int progress = (int) (100 * downloaded / total);
                    int finalProgress = progress;
                    handler.post(() -> apkDownloadTextView.setText("Downloading: " + finalProgress + "%"));
                }

                out.flush();
                out.close();
                in.close();

                handler.post(() -> {
                    isDownloading = false;
                    if (isFileValid(apkFile, hash)) {
                        apkDownloadTextView.setText("Download complete");
                        showUpdateDialog();
                    } else {
                        apkDownloadTextView.setText("File verification failed");
                        Toast.makeText(context, "APK hash mismatch", Toast.LENGTH_LONG).show();
                        apkFile.delete();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Download failed", e);
                isDownloading = false;
                new Handler(context.getMainLooper()).post(() ->
                        Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private boolean isFileValid(File file, String expectedHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            fis.close();

            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString().equalsIgnoreCase(expectedHash);
        } catch (Exception e) {
            Log.e(TAG, "Hash check failed", e);
            return false;
        }
    }

    private void showUpdateDialog() {
        if (!(context instanceof Activity)) return;

        ((Activity) context).runOnUiThread(() -> new AlertDialog.Builder(context)
                .setTitle("Install Update")
                .setMessage("APK downloaded. Install now?")
                .setPositiveButton("Install", (dialog, which) -> installApk())
                .setNegativeButton("Later", null)
                .show());
    }

    private void installApk() {
        if (apkFile == null || !apkFile.exists()) {
            Toast.makeText(context, "APK not found", Toast.LENGTH_SHORT).show();
            return;
        }
    
        try {
            Uri apkUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider", apkFile);
    
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
    
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to install APK", e);
            Toast.makeText(context, "Failed to install APK", Toast.LENGTH_SHORT).show();
        }
    }
    // Retrofit 接口
    private interface UpdateService {
        @GET("update.json")
        Call<UpdateInfo> getLatestVersion();
    }

    // JSON 数据结构
    private static class UpdateInfo {
        @SerializedName("versionCode")
        int versionCode;

        @SerializedName("versionName")
        String versionName;

        @SerializedName("apkUrl")
        String apkUrl;

        @SerializedName("hash")
        String hash;
    }
}