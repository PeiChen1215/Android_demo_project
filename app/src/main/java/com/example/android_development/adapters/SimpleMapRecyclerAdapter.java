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

/**
 * 通用的 Map->布局绑定 RecyclerView 适配器。
 *
 * <p>通过 {@code fromKeys}/{@code toViewIds} 建立映射关系，将 Map 中的字符串值绑定到指定的 TextView 上。
 * 适用于轻量“标题/详情/备注”等列表展示场景。</p>
 */
public class SimpleMapRecyclerAdapter extends RecyclerView.Adapter<SimpleMapRecyclerAdapter.ViewHolder> {
    private final Context context;
    private final List<Map<String, String>> data;
    private final int layoutRes;
    private final String[] fromKeys;
    private final int[] toViewIds;
    private OnItemClickListener onItemClickListener;

    /** 列表项点击/长按回调（仅回传位置）。 */
    public interface OnItemClickListener {
        /** 点击列表项 */
        void onItemClick(int position);

        /** 长按列表项（返回 true 表示消费事件） */
        boolean onItemLongClick(int position);
    }

    public SimpleMapRecyclerAdapter(Context context, List<Map<String, String>> data, int layoutRes, String[] fromKeys, int[] toViewIds) {
        this.context = context;
        this.data = data;
        this.layoutRes = layoutRes;
        this.fromKeys = fromKeys;
        this.toViewIds = toViewIds;
    }

    /** 设置点击/长按回调 */
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
        // 按 fromKeys -> toViewIds 的映射关系逐个绑定文本
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
