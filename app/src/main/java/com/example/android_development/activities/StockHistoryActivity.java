package com.example.android_development.activities;

import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.android_development.adapters.StockTransactionAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.model.StockTransaction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 库存变动历史页面。
 *
 * <p>支持两种模式：
 * 1）单商品历史：通过 Intent 传入 product_id，仅显示该商品的库存事务；
 * 2）全局历史：不传 product_id，展示全部库存事务，并提供按商品名称搜索。</p>
 *
 * <p>注意：为了兼容旧的历史记录，进入全局模式时会尝试回填历史表中的 product_name 字段，
 * 以便即使商品已删除也能显示名称。</p>
 */
public class StockHistoryActivity extends AppCompatActivity {

    private RecyclerView listViewStockHistory;
    private ProductDAO productDAO;
    private android.widget.ImageButton buttonHistoryBack;
    private android.widget.LinearLayout layoutHistorySearch;
    private android.widget.EditText etHistorySearch;
    private android.widget.Button buttonSearchHistory;
    private android.widget.Button buttonClearHistorySearch;

    /**
     * Activity 创建：初始化列表与 DAO，并根据是否传入 product_id 决定进入“单商品历史”或“全局历史”。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_history);

        listViewStockHistory = findViewById(R.id.listViewStockHistory);
        if (listViewStockHistory.getLayoutManager() == null) listViewStockHistory.setLayoutManager(new LinearLayoutManager(this));
        buttonHistoryBack = findViewById(R.id.buttonHistoryBack);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        productDAO = new ProductDAO(dbHelper.getReadableDatabase());

        String productId = getIntent().getStringExtra("product_id");
        layoutHistorySearch = findViewById(R.id.layoutHistorySearch);
        etHistorySearch = findViewById(R.id.etHistorySearch);
        buttonSearchHistory = findViewById(R.id.buttonSearchHistory);
        buttonClearHistorySearch = findViewById(R.id.buttonClearHistorySearch);

        if (productId == null) {
            // 全局历史视图：显示搜索控件并加载全部历史
            layoutHistorySearch.setVisibility(android.view.View.VISIBLE);
            // 尝试回填历史表中的 product_name（从 products 表拷贝），以便旧记录显示名称
            DatabaseHelper dbh = new DatabaseHelper(this);
            ProductDAO dao = new ProductDAO(dbh.getWritableDatabase());
            dao.backfillStockTransactionProductNames();
            loadAllHistory();

            buttonSearchHistory.setOnClickListener(v -> {
                String kw = etHistorySearch.getText().toString().trim();
                if (kw.isEmpty()) loadAllHistory();
                else loadHistoryByProductName(kw);
            });

            buttonClearHistorySearch.setOnClickListener(v -> {
                etHistorySearch.setText("");
                loadAllHistory();
            });
        } else {
            loadHistory(productId);
        }

        buttonHistoryBack.setOnClickListener(v -> finish());
    }

    /**
     * 将库存事务类型与数量格式化为列表标题。
     */
    private String formatTitle(StockTransaction tx, String qtyStr) {
        String type = tx.getType() != null ? tx.getType().toUpperCase() : "";
        switch (type) {
            case "IN":
                return getString(R.string.stock_tx_type_in, qtyStr);
            case "OUT":
                return getString(R.string.stock_tx_type_out, qtyStr);
            case "ADD":
                return getString(R.string.stock_tx_type_add, qtyStr);
            case "DELETE":
                return getString(R.string.stock_tx_type_delete, qtyStr);
            case "IN_FROM_WAREHOUSE":
                return getString(R.string.stock_tx_type_in_from_warehouse, qtyStr);
            case "WAREHOUSE_OUT":
                return getString(R.string.stock_tx_type_warehouse_out, qtyStr);
            case "WAREHOUSE_IN":
                return getString(R.string.stock_tx_type_warehouse_in, qtyStr);
            case "WAREHOUSE_IN_FROM_SHELF":
                return getString(R.string.stock_tx_type_warehouse_in_from_shelf, qtyStr);
            default:
                return type.isEmpty() ? qtyStr : type + " " + qtyStr;
        }
    }

    /**
     * 加载指定商品的库存事务历史并展示。
     */
    private void loadHistory(String productId) {
        List<StockTransaction> list = productDAO.getStockHistory(productId);

        List<Map<String, String>> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        // 用 DatabaseHelper 将 userId 映射为用户显示名
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        for (StockTransaction tx : list) {
            Map<String, String> map = new HashMap<>();
            String qtyStr = tx.getQuantity() > 0 ? "+" + tx.getQuantity() : String.valueOf(tx.getQuantity());
            String title = formatTitle(tx, qtyStr);
            // 若在全局模式，显示产品名称以便识别已删除的商品
            if (tx.getProductName() != null && !tx.getProductName().isEmpty()) {
                title = title + " — " + tx.getProductName();
            }
            map.put("title", title);

            String user = "-";
            if (tx.getUserId() != null && !tx.getUserId().isEmpty()) {
                com.example.android_development.model.User u = dbHelper.getUserByIdObject(tx.getUserId());
                if (u != null) {
                    if (u.getFullName() != null && !u.getFullName().isEmpty()) user = u.getFullName();
                    else if (u.getUsername() != null && !u.getUsername().isEmpty()) user = u.getUsername();
                }
            }

            String detail;
            if (tx.getUserRole() != null && !tx.getUserRole().isEmpty()) {
                detail = getString(R.string.stock_tx_detail_with_role, tx.getStockBefore(), tx.getStockAfter(), user, tx.getUserRole());
            } else {
                detail = getString(R.string.stock_tx_detail, tx.getStockBefore(), tx.getStockAfter(), user);
            }
            map.put("detail", detail);
            map.put("reason", (tx.getReason() == null || tx.getReason().isEmpty()) ? sdf.format(tx.getTimestamp()) : sdf.format(tx.getTimestamp()) + " — " + tx.getReason());
            data.add(map);
        }

        // 使用类型化 StockTransactionAdapter
        java.util.List<StockTransaction> txList = new java.util.ArrayList<>();
        for (Map<String, String> m : data) {
            // title/detail 已在 map 中拼好；这里创建一个“展示用”的 StockTransaction 承载字符串
            StockTransaction t = new StockTransaction();
            t.setType(m.get("title"));
            t.setReason(m.get("reason"));
            // 复用 userId 字段存放详情文本（仅用于列表展示）
            t.setUserId(m.get("detail"));
            txList.add(t);
        }
        StockTransactionAdapter adapter = new StockTransactionAdapter(this, txList);
        listViewStockHistory.setAdapter(adapter);
    }

    /**
     * 加载全部库存事务历史并展示（全局模式）。
     */
    private void loadAllHistory() {
        List<StockTransaction> list = productDAO.getAllStockHistory();

        populateListFromTransactions(list);
    }

    /**
     * 按商品名称模糊搜索库存事务历史并展示（全局模式）。
     */
    private void loadHistoryByProductName(String productName) {
        List<StockTransaction> list = productDAO.searchStockHistoryByProductName(productName);
        populateListFromTransactions(list);
    }

    /**
     * 将库存事务列表转换为适配器数据并绑定到 RecyclerView。
     *
     * <p>当前实现中，会把展示用字符串“塞入” StockTransaction 的部分字段中以便复用适配器：
     * type 字段承载 title，reason 字段承载时间/原因，userId 字段承载 detail（仅用于展示）。</p>
     */
    private void populateListFromTransactions(List<StockTransaction> list) {
        List<Map<String, String>> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        for (StockTransaction tx : list) {
            Map<String, String> map = new HashMap<>();
            String qtyStr = tx.getQuantity() > 0 ? "+" + tx.getQuantity() : String.valueOf(tx.getQuantity());
            String title = formatTitle(tx, qtyStr);
            if (tx.getProductName() != null && !tx.getProductName().isEmpty()) {
                title = title + " — " + tx.getProductName();
            }
            map.put("title", title);

            String user = "-";
            if (tx.getUserId() != null && !tx.getUserId().isEmpty()) {
                com.example.android_development.model.User u = dbHelper.getUserByIdObject(tx.getUserId());
                if (u != null) {
                    if (u.getFullName() != null && !u.getFullName().isEmpty()) user = u.getFullName();
                    else if (u.getUsername() != null && !u.getUsername().isEmpty()) user = u.getUsername();
                }
            }

            String detail;
            if (tx.getUserRole() != null && !tx.getUserRole().isEmpty()) {
                detail = getString(R.string.stock_tx_detail_with_role, tx.getStockBefore(), tx.getStockAfter(), user, tx.getUserRole());
            } else {
                detail = getString(R.string.stock_tx_detail, tx.getStockBefore(), tx.getStockAfter(), user);
            }
            map.put("detail", detail);
            map.put("reason", (tx.getReason() == null || tx.getReason().isEmpty()) ? sdf.format(tx.getTimestamp()) : sdf.format(tx.getTimestamp()) + " — " + tx.getReason());
            data.add(map);
        }

        java.util.List<StockTransaction> txList = new java.util.ArrayList<>();
        for (Map<String, String> m : data) {
            StockTransaction t = new StockTransaction();
            t.setType(m.get("title"));
            t.setReason(m.get("reason"));
            t.setUserId(m.get("detail"));
            txList.add(t);
        }
        StockTransactionAdapter adapter = new StockTransactionAdapter(this, txList);
        listViewStockHistory.setAdapter(adapter);
    }
}
