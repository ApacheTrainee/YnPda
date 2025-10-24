package com.fii.targ.gdlpda;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

// 第一个对话框
public class FirstDialogFragment extends DialogFragment {
    public interface FirstDialogListener {
        void onOptionBSelected();
    }

    private FirstDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("是否立即生成任务 Có muốn tạo nhiệm vụ ngay lập tức không?")
                .setMessage("NO / YES")
                .setPositiveButton("YES", (dialog, which) -> {
                    if (listener != null) listener.onOptionBSelected();
                })
                .setNegativeButton("NO", (dialog, which) -> dismiss());
        return builder.create();
    }

    public void setFirstDialogListener(FirstDialogListener listener) {
        this.listener = listener;
    }
}
