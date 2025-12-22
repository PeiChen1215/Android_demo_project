package com.example.android_development.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_development.R;

import java.util.List;
import java.util.Map;

public class SimpleMapRecyclerAdapter extends RecyclerView.Adapter<SimpleMapRecyclerAdapter.ViewHolder> {
    private final Context context;
    private final List<Map<String, String>> data;
    private final int layoutRes;
    private final String[] fromKeys;
    private final int[] toViewIds;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    public SimpleMapRecyclerAdapter(Context context, List<Map<String, String>> data, int layoutRes, String[] fromKeys, int[] toViewIds) {
        this.context = context;
        this.data = data;
        this.layoutRes = layoutRes;
        this.fromKeys = fromKeys;
        this.toViewIds = toViewIds;
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.onItemClickListener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(layoutRes, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> map = data.get(position);
        if (map == null) return;
        // bind each mapping
        for (int i = 0; i < fromKeys.length && i < toViewIds.length; i++) {
            String key = fromKeys[i];
            int viewId = toViewIds[i];
            View view = holder.itemView.findViewById(viewId);
            if (view instanceof TextView) {
                String val = map.get(key);
                ((TextView) view).setText(val == null ? "" : val);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) onItemClickListener.onItemClick(position);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemClickListener != null) return onItemClickListener.onItemLongClick(position);
            return false;
        });
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) { super(itemView); }
    }
}
