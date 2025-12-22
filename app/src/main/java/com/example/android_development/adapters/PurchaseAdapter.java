package com.example.android_development.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_development.R;
import com.example.android_development.model.PurchaseOrder;

import java.util.List;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {
    private final Context context;
    private final List<PurchaseOrder> data;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position, PurchaseOrder po);
    }

    public PurchaseAdapter(Context context, List<PurchaseOrder> data) {
        this.context = context;
        this.data = data;
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchaseOrder po = data.get(position);
        if (po == null) return;
        holder.t1.setText(po.getId() == null ? "-" : po.getId());
        holder.t2.setText(po.getStatus() == null ? "-" : po.getStatus());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position, po);
        });
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView t1, t2;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            t1 = itemView.findViewById(android.R.id.text1);
            t2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
