package com.fii.targ.gdlpda.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fii.targ.gdlpda.R;
import com.fii.targ.gdlpda.model.ExceptionItem;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExceptionAdapter extends RecyclerView.Adapter<ExceptionAdapter.ViewHolder> {

    private List<ExceptionItem> exceptionList;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(ExceptionItem exception);
        void onClearButtonClick(ExceptionItem exception, ImageButton clearButton);
    }

    public ExceptionAdapter(Context context, List<ExceptionItem> exceptionList, OnItemClickListener listener) {
        this.context = context;
        this.exceptionList = exceptionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exception, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExceptionItem exceptionItem = exceptionList.get(position);
        // holder.errorCodeTextView.setText("Error Code: " + exceptionItem.getErrorCode());
        // holder.errorMessageTextView.setText("Message: " + exceptionItem.getErrorMessage());
        // holder.errorTimeTextView.setText("Time: " + exceptionItem.getErrorTime());
        // holder.errorDeviceIdTextView.setText("Device ID: " + exceptionItem.getErrorDeviceID());
        // holder.errorRecoveryStepsTextView.setText("Recovery Steps: " + exceptionItem.getNotes());
        switch (exceptionItem.getErrorLevel()) {
            case "Critical":
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.critical_error_background));
                holder.errorCodeTextView.setTextColor(ContextCompat.getColor(context, R.color.critical_error_text));
                break;
            case "Warning":
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.warning_error_background));
                holder.errorCodeTextView.setTextColor(ContextCompat.getColor(context, R.color.warning_error_text));
                break;
            case "Info":
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.info_error_background));
                holder.errorCodeTextView.setTextColor(ContextCompat.getColor(context, R.color.info_error_text));
                break;
            default:
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.default_background));
                holder.errorCodeTextView.setTextColor(ContextCompat.getColor(context, R.color.default_text));
                break;
        }
        holder.bind(exceptionItem, listener);
    }

    @Override
    public int getItemCount() {
        return exceptionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView errorCodeTextView;
        public TextView errorMessageTextView;
        public TextView errorRecoveryStepsTextView;
        public TextView errorTimeTextView;
        public TextView errorDeviceIdTextView;
        public TextView errorSolvedTextView;
        public ImageButton clearButton;

        public ViewHolder(View itemView) {
            super(itemView);
            errorCodeTextView = itemView.findViewById(R.id.exception_topic);
            errorMessageTextView = itemView.findViewById(R.id.exception_details);
            errorDeviceIdTextView = itemView.findViewById(R.id.device_id);
            errorRecoveryStepsTextView = itemView.findViewById(R.id.recovery_steps);
            errorTimeTextView = itemView.findViewById(R.id.error_time);

            clearButton = itemView.findViewById(R.id.clear);
        }

        public void bind(final ExceptionItem exceptionItem, final OnItemClickListener listener) {
            String errorCode = exceptionItem.getErrorUid();
            if (errorCode.length() > 10) {
                errorCode = errorCode.substring(errorCode.length() - 10);
            }
            errorCodeTextView.setText(errorCode);
            errorMessageTextView.setText(exceptionItem.getErrorMessage());
            errorTimeTextView.setText(exceptionItem.getErrorTime());
            errorDeviceIdTextView.setText("("+exceptionItem.getErrorSource()+") "+ exceptionItem.getErrorDeviceID());
            errorRecoveryStepsTextView.setText(exceptionItem.getNotes());
            

            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(exceptionItem);
                }
            });
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClearButtonClick(exceptionItem, clearButton);
                }
            });
        }
    }
}