package com.example.android_development.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_development.R;
import com.example.android_development.model.StockTransaction;

import java.util.List;

public class StockTransactionAdapter extends RecyclerView.Adapter<StockTransactionAdapter.ViewHolder> {
    private final Context context;
    private final List<StockTransaction> data;

    public StockTransactionAdapter(Context context, List<StockTransaction> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_stock_tx, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockTransaction tx = data.get(position);
        if (tx == null) return;
        // title stored in type (may include product name already)
        holder.title.setText(tx.getType() == null ? "" : tx.getType());
        // detail stored in userId field for lightweight display; show as-is
        holder.detail.setText(tx.getUserId() == null ? "-" : tx.getUserId());
        holder.reason.setText(tx.getReason() == null ? "" : tx.getReason());

        // click shows detail dialog with full info
        holder.itemView.setOnClickListener(v -> {
            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(v.getContext());
            b.setTitle(holder.title.getText());
            b.setMessage("详情:\n" + holder.detail.getText() + "\n" + holder.reason.getText());
            b.setPositiveButton("关闭", null);
            b.show();
        });
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, detail, reason;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewTxType);
            detail = itemView.findViewById(R.id.textViewTxDetail);
            reason = itemView.findViewById(R.id.textViewTxReason);
        }
    }
}
