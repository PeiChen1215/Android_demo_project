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

/**
 * 货架补货（从仓库调拨）页面。
 *
 * <p>展示“货架库存低于预警值”的商品列表，点击商品后输入补货数量，
 * 调用 {@link InventoryDAO#restockShelf(String, int)} 将仓库库存转移到货架库存，并刷新列表。</p>
 */
public class RestockActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private ProductDAO productDAO;
    private InventoryDAO inventoryDAO;

    /**
     * Activity 创建：初始化列表与 DAO，并加载低库存商品。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restock);

        recyclerView = findViewById(R.id.recyclerRestock);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper db = new DatabaseHelper(this);
        productDAO = new ProductDAO(db.getReadableDatabase());
        inventoryDAO = new InventoryDAO(db.getWritableDatabase(), this);

        loadLowStockProducts();
    }

    /**
     * 加载低货架库存商品并绑定到列表。
     */
    private void loadLowStockProducts() {
        List<Product> list = productDAO.getLowStockProducts();
        if (list == null) list = new ArrayList<>();
        adapter = new ProductAdapter(this, list, false);
        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, Product product) {
                showRestockDialog(product);
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
                showRestockDialog(product);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    /**
     * 弹出补货对话框：输入补货数量后执行补货并刷新列表。
     */
    private void showRestockDialog(Product p) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_restock, null, false);
        EditText etQty = v.findViewById(R.id.et_restock_qty);
        androidx.appcompat.app.AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("从仓库补货: " + (p.getName() == null ? "" : p.getName()))
                .setView(v)
                .setPositiveButton("补货", (dialog, which) -> {
                    String s = etQty.getText().toString().trim();
                    int qty = 0;
                    try { qty = Integer.parseInt(s); } catch (Exception ex) { qty = 0; }
                    if (qty <= 0) { Toast.makeText(RestockActivity.this, getString(R.string.enter_valid_quantity), Toast.LENGTH_SHORT).show(); return; }
                    boolean ok = inventoryDAO.restockShelf(p.getId(), qty);
                    if (ok) {
                        Toast.makeText(RestockActivity.this, getString(R.string.restock_success), Toast.LENGTH_SHORT).show();
                        loadLowStockProducts();
                    } else {
                        Toast.makeText(RestockActivity.this, getString(R.string.restock_failed), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .create();
        dlg.show();
    }
}
