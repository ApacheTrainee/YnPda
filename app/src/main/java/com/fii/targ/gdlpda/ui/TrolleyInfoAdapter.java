package com.fii.targ.gdlpda.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fii.targ.gdlpda.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TrolleyInfoAdapter extends RecyclerView.Adapter<TrolleyInfoAdapter.ViewHolder> {
    private List<TrolleyInfo> trolleyInfoList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TrolleyInfo trolleyInfo);
        void onCallButtonClick(TrolleyInfo trolleyInfo, Button callButton);
    }

    public TrolleyInfoAdapter(List<TrolleyInfo> trolleyInfoList, OnItemClickListener listener) {
        this.trolleyInfoList = trolleyInfoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trolley_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrolleyInfo trolleyInfo = trolleyInfoList.get(position);
        holder.bind(trolleyInfo, listener);
    }

    @Override
    public int getItemCount() {
        return trolleyInfoList.size();
    }

    public void setItems(List<TrolleyInfo> trolleyInfos) {
        this.trolleyInfoList = trolleyInfos;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView trolleySnTextView;
        private TextView trolleyLocationTextView;
        private Button callButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            trolleySnTextView = itemView.findViewById(R.id.trolley_sn);
            trolleyLocationTextView = itemView.findViewById(R.id.location_info);
            callButton = itemView.findViewById(R.id.call_button);
        }

        public void bind(final TrolleyInfo trolleyInfo, final OnItemClickListener listener) {
            trolleySnTextView.setText(trolleyInfo.getSn());
            trolleyLocationTextView.setText(trolleyInfo.getCellId());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(trolleyInfo);
                }
            });
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCallButtonClick(trolleyInfo, callButton);
                }
            });
        }
    }
}