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

/**
 * 库存事务（出入库/调整等）列表 RecyclerView 适配器。
 *
 * <p>用于 StockHistory 列表展示。当前实现复用了 {@link StockTransaction} 的字段承载“展示用字符串”：
 * type 作为标题、userId 作为详情文本、reason 作为时间/原因文本。</p>
 */
public class StockTransactionAdapter extends RecyclerView.Adapter<StockTransactionAdapter.ViewHolder> {
    private final Context context;
    private final List<StockTransaction> data;

    /** 构造函数：传入要展示的事务列表 */
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
        // 这里将 type 字段作为列表标题展示（可能已拼接了商品名）
        holder.title.setText(tx.getType() == null ? "" : tx.getType());
        // 这里将 userId 字段复用为“详情文本”展示（仅用于轻量展示，直接原样显示）
        holder.detail.setText(tx.getUserId() == null ? "-" : tx.getUserId());
        holder.reason.setText(tx.getReason() == null ? "" : tx.getReason());

        // 点击弹窗显示完整详情
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
