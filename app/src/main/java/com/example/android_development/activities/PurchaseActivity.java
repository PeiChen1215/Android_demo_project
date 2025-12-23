package com.example.android_development.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_development.R;
import com.example.android_development.adapters.ProductAdapter;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.InventoryDAO;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.model.Product;
import java.util.ArrayList;
import java.util.List;

public class PurchaseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private ProductDAO productDAO;
    private InventoryDAO inventoryDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        recyclerView = findViewById(R.id.recyclerPurchase);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper db = new DatabaseHelper(this);
        productDAO = new ProductDAO(db.getReadableDatabase());
        inventoryDAO = new InventoryDAO(db.getWritableDatabase());

        loadLowWarehouseProducts();
    }

    private void loadLowWarehouseProducts() {
        List<Product> list = productDAO.getLowWarehouseStockProducts();
        if (list == null) list = new ArrayList<>();
        adapter = new ProductAdapter(this, list, false);
        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, Product product) {
                showPurchaseDialog(product);
            }

            @Override
            public boolean onItemLongClick(int position, Product product) {
                return false;
            }

            @Override
            public void onActionEdit(int position, Product product) {}

            @Override
            public void onActionDelete(int position, Product product) {}

            @Override
            public void onActionAdjustStock(int position, Product product) {
                showPurchaseDialog(product);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void showPurchaseDialog(Product p) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_restock, null, false);
        EditText etQty = v.findViewById(R.id.et_restock_qty);
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("采购入库: " + (p.getName() == null ? "" : p.getName()))
                .setView(v)
                .setPositiveButton("入库", (dialog, which) -> {
                    String s = etQty.getText().toString().trim();
                    int qty = 0;
                    try { qty = Integer.parseInt(s); } catch (Exception ex) { qty = 0; }
                    if (qty <= 0) { Toast.makeText(PurchaseActivity.this, "请输入有效数量", Toast.LENGTH_SHORT).show(); return; }
                    boolean ok = inventoryDAO.receivePurchase(p.getId(), qty);
                    if (ok) {
                        Toast.makeText(PurchaseActivity.this, "入库成功", Toast.LENGTH_SHORT).show();
                        loadLowWarehouseProducts();
                    } else {
                        Toast.makeText(PurchaseActivity.this, "入库失败（异常）", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dlg.show();
    }
}
