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
    private final java.util.List<String> supplierNames;

    public interface OnItemClickListener {
        void onItemClick(int position, PurchaseOrder po);
    }

    public PurchaseAdapter(Context context, List<PurchaseOrder> data) {
        this(context, data, null);
    }

    public PurchaseAdapter(Context context, List<PurchaseOrder> data, java.util.List<String> supplierNames) {
        this.context = context;
        this.data = data;
        this.supplierNames = supplierNames;
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
        String title = (po.getName() != null && !po.getName().trim().isEmpty()) ? po.getName() : (po.getId() == null ? "-" : po.getId());
        holder.t1.setText(title);
        String status = po.getStatus() == null ? "-" : po.getStatus();
        holder.t2.setText(status);
        // color status portion if possible (best-effort)
        try {
            int color = android.graphics.Color.DKGRAY;
            if ("approved".equalsIgnoreCase(po.getStatus())) color = android.graphics.Color.parseColor("#388E3C");
            else if ("received".equalsIgnoreCase(po.getStatus())) color = android.graphics.Color.parseColor("#1976D2");
            else if ("rejected".equalsIgnoreCase(po.getStatus())) color = android.graphics.Color.parseColor("#D32F2F");
            else if ("submitted".equalsIgnoreCase(po.getStatus())) color = android.graphics.Color.parseColor("#F57C00");
            holder.t2.setTextColor(color);
        } catch (Exception ignored) {}
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position, po);
        });
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    public void updateData(java.util.List<PurchaseOrder> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    public void appendData(java.util.List<PurchaseOrder> more) {
        if (more == null || more.isEmpty()) return;
        int start = data.size();
        data.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView t1, t2;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            t1 = itemView.findViewById(android.R.id.text1);
            t2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
