package com.example.android_development.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_development.R;
import com.example.android_development.model.StockCount;

import java.util.List;

/**
 * 盘点记录列表 RecyclerView 适配器。
 *
 * <p>用于展示 {@link StockCount} 的 ID 与状态，并提供点击回调（例如打开盘点详情页）。</p>
 */
public class StockCountAdapter extends RecyclerView.Adapter<StockCountAdapter.ViewHolder> {
    private final Context context;
    private final List<StockCount> data;
    private OnItemClickListener listener;

    /** 盘点记录点击回调 */
    public interface OnItemClickListener {
        void onItemClick(int position, StockCount sc);
    }

    public StockCountAdapter(Context context, List<StockCount> data) {
        this.context = context;
        this.data = data;
    }

    /** 设置列表项点击回调 */
    public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockCount sc = data.get(position);
        if (sc == null) return;
        holder.t1.setText(sc.getId() == null ? "-" : sc.getId());
        holder.t2.setText(sc.getStatus() == null ? "-" : sc.getStatus());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position, sc);
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
