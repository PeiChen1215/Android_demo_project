package com.example.android_development.activities.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.android_development.R;
import com.example.android_development.model.PurchaseLine;
import com.example.android_development.model.Product;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.database.DatabaseHelper;
import java.util.List;

public class PoLineAdapter extends BaseAdapter {
    private Context ctx;
    private List<PurchaseLine> lines;
    private List<Product> products; // parallel list to show names
    private android.database.sqlite.SQLiteDatabase db;

    public PoLineAdapter(Context ctx, List<PurchaseLine> lines, List<Product> products) {
        this.ctx = ctx; this.lines = lines; this.products = products;
        try {
            DatabaseHelper dh = new DatabaseHelper(ctx);
            db = dh.getWritableDatabase();
        } catch (Exception ignored) { db = null; }
    }

    @Override public int getCount() { return lines.size(); }
    @Override public Object getItem(int position) { return lines.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = LayoutInflater.from(ctx).inflate(R.layout.item_po_line, parent, false);
        TextView tvName = convertView.findViewById(R.id.tv_product_name);
        final EditText etQty = convertView.findViewById(R.id.et_qty);
        Button btnRemove = convertView.findViewById(R.id.btn_remove);

        PurchaseLine line = lines.get(position);
        Product p = null;
        if (products != null && position < products.size()) p = products.get(position);
        tvName.setText((p != null && p.getName() != null) ? p.getName() : (line.getProductId() == null ? "(未选择)" : line.getProductId()));
        etQty.setText(String.valueOf(line.getQty()));

        etQty.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String s = etQty.getText().toString();
                if (!TextUtils.isEmpty(s)) {
                    try {
                        int val = Integer.parseInt(s);
                        if (val < 0) val = 0;
                        lines.get(position).setQty(val);
                        etQty.setText(String.valueOf(val));
                    } catch (Exception ignored) { etQty.setText("0"); lines.get(position).setQty(0); }
                } else {
                    etQty.setText("0"); lines.get(position).setQty(0);
                }
            }
        });

        // 点击商品名选择商品
        tvName.setOnClickListener(v -> {
            if (db == null) return;
            ProductDAO productDAO = new ProductDAO(db);
            List<Product> all = productDAO.getAllProducts();
            if (all == null || all.isEmpty()) {
                android.widget.Toast.makeText(ctx, "没有可选商品", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            final List<String> names = new java.util.ArrayList<>();
            for (Product prod : all) names.add(prod.getName() == null ? prod.getId() : prod.getName());
            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(ctx);
            b.setTitle("选择商品");
            b.setItems(names.toArray(new String[0]), (dialog, which) -> {
                Product sel = all.get(which);
                lines.get(position).setProductId(sel.getId());
                if (products != null) {
                    if (position < products.size()) products.set(position, sel);
                    else products.add(sel);
                }
                tvName.setText(sel.getName());
            });
            b.setNegativeButton("取消", null);
            b.show();
        });

        btnRemove.setOnClickListener(v -> {
            lines.remove(position);
            if (products != null && position < products.size()) products.remove(position);
            notifyDataSetChanged();
        });

        return convertView;
    }
}
