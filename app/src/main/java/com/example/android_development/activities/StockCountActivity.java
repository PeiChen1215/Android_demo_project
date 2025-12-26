package com.example.android_development.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.core.content.FileProvider;
import com.example.android_development.adapters.StockCountAdapter;
import com.example.android_development.adapters.ProductAdapter;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.services.InventoryService;
import com.example.android_development.model.Product;
import java.util.List;

/**
 * 库存盘点与低库存预警页面。
 *
 * <p>上半部分展示历史盘点记录；下半部分展示低库存预警列表，并支持导出 CSV 进行分享。
 * 低库存数据由 {@link InventoryService} 统一计算。</p>
 */
public class StockCountActivity extends AppCompatActivity {

    private RecyclerView listViewCounts;
    private RecyclerView listViewLowStockAlerts;
    private android.widget.TextView textViewNoCounts;
    private android.widget.TextView textViewLowStockCount;
    private android.widget.TextView textViewNoLowStock;
    private android.widget.Button buttonExportLowStock;
    private com.example.android_development.database.InventoryDAO inventoryDAO;
    private InventoryService inventoryService;

    /**
     * Activity 创建：初始化视图与服务对象，并加载盘点记录与低库存预警。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_count);
        initViews();
        loadCounts();
        loadLowStockAlerts();
    }

    /**
     * 页面回到前台：刷新低库存预警（库存可能在其他页面被修改）。
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到页面都刷新低库存预警
        loadLowStockAlerts();
    }

    /**
     * 初始化控件引用、RecyclerView 布局管理器，以及 DAO/Service。
     */
    private void initViews() {
        listViewCounts = findViewById(R.id.listViewCounts);
        listViewLowStockAlerts = findViewById(R.id.listViewLowStockAlerts);
        textViewNoCounts = findViewById(R.id.textViewNoCounts);
        textViewLowStockCount = findViewById(R.id.textViewLowStockCount);
        textViewNoLowStock = findViewById(R.id.textViewNoLowStock);
        buttonExportLowStock = findViewById(R.id.buttonExportLowStock);

        if (listViewCounts.getLayoutManager() == null) listViewCounts.setLayoutManager(new LinearLayoutManager(this));
        if (listViewLowStockAlerts.getLayoutManager() == null) listViewLowStockAlerts.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper db = new DatabaseHelper(this);
        inventoryDAO = new com.example.android_development.database.InventoryDAO(db.getWritableDatabase());
        inventoryService = new InventoryService(this);

        buttonExportLowStock.setOnClickListener(v -> exportLowStockCsv());
    }

    /**
     * 加载并展示盘点记录列表。
     */
    private void loadCounts() {
        java.util.List<com.example.android_development.model.StockCount> list = inventoryDAO.getAllStockCounts();
        if (list == null || list.isEmpty()) {
            listViewCounts.setVisibility(android.view.View.GONE);
            textViewNoCounts.setVisibility(android.view.View.VISIBLE);
            return;
        }
        listViewCounts.setVisibility(android.view.View.VISIBLE);
        textViewNoCounts.setVisibility(android.view.View.GONE);

        StockCountAdapter adapter = new StockCountAdapter(this, list);
        adapter.setOnItemClickListener((position, sc) -> {
            // 预留扩展点：后续可在此打开“盘点记录详情页”
        });
        listViewCounts.setAdapter(adapter);
    }

    /**
     * 加载并展示低库存预警列表，同时更新预警数量。
     */
    private void loadLowStockAlerts() {
        List<Product> lowStockProducts = inventoryService.getLowStockAlerts();
        int alertCount = inventoryService.getLowStockAlertCount();

        // 更新预警数量显示
        textViewLowStockCount.setText("共有 " + alertCount + " 件商品库存不足");

        if (lowStockProducts == null || lowStockProducts.isEmpty()) {
            listViewLowStockAlerts.setVisibility(android.view.View.GONE);
            textViewNoLowStock.setVisibility(android.view.View.VISIBLE);
            return;
        }

        listViewLowStockAlerts.setVisibility(android.view.View.VISIBLE);
        textViewNoLowStock.setVisibility(android.view.View.GONE);

        // 创建简化的商品适配器，只显示基本信息，不显示操作按钮
        ProductAdapter adapter = new ProductAdapter(this, lowStockProducts, false);
        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, Product product) {
                // 点击跳转到商品详情页
                android.content.Intent intent = new android.content.Intent(StockCountActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(int position, Product product) {
                return false;
            }

            @Override
            public void onActionEdit(int position, Product product) {
                // 不显示编辑按钮
            }

            @Override
            public void onActionDelete(int position, Product product) {
                // 不显示删除按钮
            }

            @Override
            public void onActionAdjustStock(int position, Product product) {
                // 不显示调整按钮
            }
        });
        listViewLowStockAlerts.setAdapter(adapter);
    }

    /**
     * 导出低库存预警为 CSV，并通过系统分享。
     */
    private void exportLowStockCsv() {
        List<Product> lowStockProducts = inventoryService.getLowStockAlerts();
        if (lowStockProducts == null || lowStockProducts.isEmpty()) {
            android.widget.Toast.makeText(this, "没有低库存商品需要导出", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        // CSV 表头
        sb.append("商品名称,条码,当前货架库存,最低库存预警,仓库库存,分类,品牌,单位,价格,成本\n");

        // CSV 数据行
        for (Product product : lowStockProducts) {
            sb.append(escapeCsvValue(product.getName())).append(',');
            sb.append(escapeCsvValue(product.getBarcode() != null ? product.getBarcode() : "")).append(',');
            sb.append(product.getStock()).append(',');
            sb.append(product.getMinStock()).append(',');
            sb.append(product.getWarehouseStock()).append(',');
            sb.append(escapeCsvValue(product.getCategory() != null ? product.getCategory() : "")).append(',');
            sb.append(escapeCsvValue(product.getBrand() != null ? product.getBrand() : "")).append(',');
            sb.append(escapeCsvValue(product.getUnit() != null ? product.getUnit() : "")).append(',');
            sb.append(String.format("%.2f", product.getPrice())).append(',');
            sb.append(String.format("%.2f", product.getCost())).append('\n');
        }

        try {
            java.io.File dir = getExternalCacheDir();
            if (dir == null) dir = getCacheDir();
            java.io.File f = new java.io.File(dir, "low_stock_alerts.csv");
            java.io.FileWriter fw = new java.io.FileWriter(f);
            fw.write(sb.toString());
            fw.close();

            android.net.Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", f);
            android.content.Intent share = new android.content.Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/csv");
            share.putExtra(android.content.Intent.EXTRA_STREAM, uri);
            share.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(android.content.Intent.createChooser(share, "分享低库存预警"));
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "导出失败: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * CSV 字段转义：当包含逗号/引号/换行时进行包裹与转义。
     */
    private String escapeCsvValue(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
