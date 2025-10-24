package com.fii.targ.gdlpda;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.fii.targ.gdlpda.model.Task;

// 取消任务对话框
public class CancelTaskDialogFragment extends DialogFragment {

    public interface CancelTaskDialogListener {
        void onYes(Task task);
        void onCancel();
    }

    // 创建带参数的对话框实例
    public static CancelTaskDialogFragment newInstance(Task task) {
        CancelTaskDialogFragment fragment = new CancelTaskDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("task", task);
        fragment.setArguments(args);
        return fragment;
    }

    private CancelTaskDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 从参数中获取值
        Bundle args = getArguments();
        Task task;
        if (args != null) {
            task = args.getParcelable("task");
        } else  {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("错误")
                    .setMessage("任务信息丢失")
                    .setPositiveButton("确定", (dialog, which) -> dismiss())
                    .create();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("是否取消任务 ?")
                .setMessage("NO / YES")
                .setPositiveButton("YES", (dialog, which) -> {
                    if (listener != null) {
                        listener.onYes(task);
                    }
                })
                .setNeutralButton("NO", (dialog, which) -> {
                    if (listener != null) {
                        listener.onCancel();
                    }
                });

        return builder.create();
    }

    public void setCancelTaskDialogListener(CancelTaskDialogListener listener) {
        this.listener = listener;
    }
}