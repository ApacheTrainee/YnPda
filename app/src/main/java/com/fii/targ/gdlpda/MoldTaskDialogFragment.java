package com.fii.targ.gdlpda;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

// 模具任务对话框
public class MoldTaskDialogFragment extends DialogFragment {

    public interface MoldTaskDialogListener {
        void onYesSelected(String trolleyId);
        void onNoSelected(String trolleyId);
        void onCancelSelected();
    }

    // 创建带参数的对话框实例
    public static MoldTaskDialogFragment newInstance(String trolleyId) {
        MoldTaskDialogFragment fragment = new MoldTaskDialogFragment();
        Bundle args = new Bundle();
        args.putString("trolleyId", trolleyId);
        fragment.setArguments(args);
        return fragment;
    }

    private MoldTaskDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 从参数中获取值
        Bundle args = getArguments();
        String trolleyId = args.getString("trolleyId", "");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("是否为模具搬运任务 Có phải là nhiệm vụ vận chuyển khuôn không?")
                .setMessage("NO / YES")
                .setNegativeButton("NO", (dialog, which) -> {
                    if (listener != null) {
                        listener.onNoSelected(trolleyId);
                    }
                })
                .setPositiveButton("YES", (dialog, which) -> {
                    if (listener != null) {
                        listener.onYesSelected(trolleyId);
                    }
                })
                .setNeutralButton("取消", (dialog, which) -> {
                    if (listener != null) {
                        listener.onCancelSelected();
                    }
                });

        return builder.create();
    }

    public void setMoldTaskDialogListener(MoldTaskDialogListener listener) {
        this.listener = listener;
    }
}