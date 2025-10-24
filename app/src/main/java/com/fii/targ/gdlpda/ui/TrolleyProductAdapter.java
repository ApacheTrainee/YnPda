package com.fii.targ.gdlpda.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Handler;
import android.widget.Toast;

import com.fii.targ.gdlpda.BindProductActivity;
import com.fii.targ.gdlpda.R;
import com.fii.targ.gdlpda.scanner.Constants;
import com.fii.targ.gdlpda.scanner.ScannerManager;

public class TrolleyProductAdapter extends RecyclerView.Adapter<TrolleyProductAdapter.TrolleyProductViewHolder> {
    private static String TAG = "TrolleyProductAdapter";

    private List<TrolleyProduct> items;
    boolean readonly;
    private Context context;
    private Dialog scanDialog;
    private TextView scanMessageTextView;
    private ScannerManager scannerManager;
    private Map<Integer, TrolleyProductViewHolder> viewHolderMap = new HashMap<>();
    private static int SCAN_REQUEST_ID = Constants.SCAN_TYPE_L10_PRODUCT;
    private int curScanRequestId = SCAN_REQUEST_ID;
    private boolean autoScanNext = true;

    public TrolleyProductAdapter(Context context, List<TrolleyProduct> items, boolean readonly) {
        this.context = context;
        this.items = items;
        this.readonly = readonly;
        scannerManager = new ScannerManager(context, scanListener);

        scanDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_scan, null);
        scanDialog.setContentView(dialogView);
        scanDialog.setCancelable(false); // Prevent closing the dialog by tapping outside

        // Get the TextView reference
        scanMessageTextView = dialogView.findViewById(R.id.scan_message);

        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> cancelCurrentScan());
    }

    public void showScanDialog(String message) {
        // Set the scan message
        scanMessageTextView.setText(message);

        // Show the scan dialog
        scanDialog.show();
    }

    public void hideScanDialog() {
        // Dismiss the scan dialog
        if (scanDialog.isShowing()) {
            scanDialog.dismiss();
        }
    }

    public void cancelCurrentScan() {
        scannerManager.cancelScan();
        hideScanDialog();
        autoScanNext = false;
    }

    public static class TrolleyProductViewHolder extends RecyclerView.ViewHolder {
        public TextView layerTextView;
        public ImageButton qrCodeScan;
        public TextView readOnlyTextView;
        public ImageButton deleteImageView;

        public TrolleyProductViewHolder(View itemView) {
            super(itemView);
            layerTextView = itemView.findViewById(R.id.layerTextView);
            qrCodeScan = itemView.findViewById(R.id.qr_code_scan);
            readOnlyTextView = itemView.findViewById(R.id.readOnlyTextView);
            deleteImageView = itemView.findViewById(R.id.deleteImageView);
        }

        public void bind(boolean readonly) {
            // Set visibility based on readonly flag
            if (readonly) {
                qrCodeScan.setVisibility(View.GONE);
                deleteImageView.setVisibility(View.GONE);
            } else {
                qrCodeScan.setVisibility(View.VISIBLE);
                deleteImageView.setVisibility(View.VISIBLE);
            }
        }

        public String getProductSnValue() {
            return readOnlyTextView.getText().toString();
        }
    }

    private ScannerManager.ScanListener scanListener = new ScannerManager.ScanListener() {
        @Override
        public void onScanResult(int requestId, String barcodeData) {
                Log.d(TAG, "requestId: " + requestId);
                Log.d(TAG, "barcodeData: " + barcodeData);
                handleScanResult(requestId, barcodeData);
        }

        @Override
        public void onScanError(int requestId, String error) {
            handleScanError(error);

        }
    };

    private void handleScanResult(int requestId, String data) {
        int layer = requestId - SCAN_REQUEST_ID;
        if (layer < 1 || layer > 8) {
            Log.d(TAG, "Exception! Error layer" + layer);
        }
        int position = layer - 1;
        new Handler(Looper.getMainLooper()).post(() -> {
            TrolleyProductViewHolder holder = viewHolderMap.get(position);
            if (holder != null) {
                holder.readOnlyTextView.setText(data);
                hideScanDialog();
                try {
                    Thread.sleep(1000);
                    requestScanNext(layer);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private void handleScanError(String error) {               
        new Handler(Looper.getMainLooper()).post(() -> {
            hideScanDialog();
            Log.d(TAG, "SCAN_REQUEST_ID " + SCAN_REQUEST_ID + " " + error);
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    public TrolleyProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trolley_product, parent, false);

        return new TrolleyProductViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull TrolleyProductViewHolder holder, int position) {
        TrolleyProduct item = items.get(position);
        holder.layerTextView.setText(item.getPosition());
        holder.readOnlyTextView.setText(item.getProductSN());
        holder.bind(readonly);

        viewHolderMap.put(position, holder);
        int layer = position + 1;

        // Set click listeners for buttons if needed
        holder.qrCodeScan.setOnClickListener(v -> {
            autoScanNext = true;
            showScanDialog("Please Scan product at " + layer + " layer ... ");
            // TODO DEBUG
            // Log.d(TAG, "holder.qrCodeScan.setOnClickListener. position: " + position);
            scannerManager.requestScan(layer + SCAN_REQUEST_ID);
        });

        holder.deleteImageView.setOnClickListener(v -> {
            holder.readOnlyTextView.setText("");
        });
    }

    public void requestScanNext(int layer) {
        if (layer == 8 || !autoScanNext)
            return;
        layer++;

        curScanRequestId = SCAN_REQUEST_ID + layer;
        showScanDialog("Please Scan product at " + layer + " layer ... ");
        scannerManager.requestScan(curScanRequestId);

    }

    private void clearEditText() {
        for (int i = 0; i < items.size(); i++) {
            TrolleyProductViewHolder holder = viewHolderMap.get(i);
            if (holder != null) {
                holder.readOnlyTextView.setText("");
            }
        }
    }

    public void requestStartScan() {
        curScanRequestId = SCAN_REQUEST_ID;
        clearEditText();
        scannerManager.clearScanRequestQueue();
        scannerManager.requestScan(curScanRequestId);

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public void setItem(int i, String productSN) {
        TrolleyProduct trolleyProduct = this.items.get(i);
        trolleyProduct.setProductSN(productSN);
    }

    public void setItems(List<TrolleyProduct> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public List<TrolleyProduct> getItems() {
        return this.items;
    }

}