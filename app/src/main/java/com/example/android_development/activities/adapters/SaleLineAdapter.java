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

public class SaleLineAdapter extends ArrayAdapter<SaleLine> {
    private LayoutInflater inflater;
    public interface OnDeleteListener { void onDelete(int position); }
    private OnDeleteListener deleteListener;

    public SaleLineAdapter(Context ctx, List<SaleLine> list) {
        super(ctx, 0, list);
        inflater = LayoutInflater.from(ctx);
    }

    public void setOnDeleteListener(OnDeleteListener l) { this.deleteListener = l; }

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
