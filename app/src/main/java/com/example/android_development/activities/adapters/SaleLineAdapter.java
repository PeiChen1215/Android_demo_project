package com.example.android_development.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageButton;
import com.example.android_development.R;
import com.example.android_development.model.SaleLine;
import java.util.List;

/**
 * 收银明细行 ListView 适配器。
 *
 * <p>用于在收银页面展示 {@link SaleLine}：商品名、数量与单价、以及小计。
 * 支持点击删除按钮删除某一行（通过回调把删除动作交给页面处理）。</p>
 */
public class SaleLineAdapter extends ArrayAdapter<SaleLine> {
    private LayoutInflater inflater;

    /** 删除按钮回调（回传 position）。 */
    public interface OnDeleteListener { void onDelete(int position); }
    private OnDeleteListener deleteListener;

    public SaleLineAdapter(Context ctx, List<SaleLine> list) {
        super(ctx, 0, list);
        inflater = LayoutInflater.from(ctx);
    }

    /** 设置删除回调 */
    public void setOnDeleteListener(OnDeleteListener l) { this.deleteListener = l; }

    /**
     * 渲染单行视图。
     *
     * <p>注意：position 是 ListView 的位置索引，删除时由外部根据 position 进行数据移除与刷新。</p>
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_sale_line, parent, false);
        }
        SaleLine line = getItem(position);
        TextView tvName = convertView.findViewById(R.id.tv_line_name);
        TextView tvQtyPrice = convertView.findViewById(R.id.tv_line_qty_price);
        TextView tvSubtotal = convertView.findViewById(R.id.tv_line_subtotal);
        ImageButton btnDelete = convertView.findViewById(R.id.btn_line_delete);

        if (line != null) {
            tvName.setText(line.getProductName() != null ? line.getProductName() : line.getProductId());
            tvQtyPrice.setText(line.getQty() + " x " + String.format("%.2f", line.getPrice()));
            double subtotal = line.getQty() * line.getPrice();
            tvSubtotal.setText(String.format("%.2f", subtotal));
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(position);
            });
        }

        return convertView;
    }
}
