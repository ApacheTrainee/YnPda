package com.fii.targ.gdlpda;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

// 第二个对话框
public class SecondDialogFragment extends DialogFragment {
    public interface SecondDialogListener {
        void onOptionASelected(String cartCode, String groundCode, String productCode);
        void onOptionBSelected(String cartCode, String groundCode, String productCode);
        void onCancelSelected();
    }

    // 添加静态方法创建带参数的实例
    public static SecondDialogFragment newInstance(String cartCode, String groundCode, String productCode) {
        SecondDialogFragment fragment = new SecondDialogFragment();
        Bundle args = new Bundle();
        args.putString("cartCode", cartCode);
        args.putString("groundCode", groundCode);
        args.putString("productCode", productCode);
        fragment.setArguments(args);
        return fragment;
    }

    private SecondDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 从参数中获取值
        Bundle args = getArguments();
        String cartCode = args.getString("cartCode", "");
        String groundCode = args.getString("groundCode", "");
        String productCode = args.getString("productCode", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("确认小车类型 Xác nhận loại xe")
                .setMessage("AGV / AGF")
                .setNegativeButton("AGV", (dialog, which) -> {
                    if (listener != null) listener.onOptionASelected(cartCode, groundCode, productCode);
                })
                .setPositiveButton("AGF", (dialog, which) -> {
                    if (listener != null) listener.onOptionBSelected(cartCode, groundCode, productCode);
                })
                .setNeutralButton("cancel", (dialog, which) -> {
                    if (listener != null) listener.onCancelSelected();
                });
        return builder.create();
    }

    public void setSecondDialogListener(SecondDialogListener listener) {
        this.listener = listener;
    }
}
