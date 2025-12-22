package com.example.android_development.activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

public class StockHistoryActivity extends AppCompatActivity {

    private ListView listViewStockHistory;
    private ProductDAO productDAO;
    private android.widget.Button buttonHistoryBack;
    private android.widget.LinearLayout layoutHistorySearch;
    private android.widget.EditText etHistorySearch;
    private android.widget.Button buttonSearchHistory;
    private android.widget.Button buttonClearHistorySearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_history);

        listViewStockHistory = findViewById(R.id.listViewStockHistory);
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

    private void loadHistory(String productId) {
        List<StockTransaction> list = productDAO.getStockHistory(productId);

        List<Map<String, String>> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        // 用 DatabaseHelper 将 userId 映射为用户显示名
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        for (StockTransaction tx : list) {
            Map<String, String> map = new HashMap<>();
            String qtyStr = tx.getQuantity() > 0 ? "+" + tx.getQuantity() : String.valueOf(tx.getQuantity());
            String title = "IN".equalsIgnoreCase(tx.getType()) ? getString(R.string.stock_tx_type_in, qtyStr) : getString(R.string.stock_tx_type_out, qtyStr);
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

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                R.layout.item_stock_tx,
                new String[]{"title", "detail", "reason"},
                new int[]{R.id.textViewTxType, R.id.textViewTxDetail, R.id.textViewTxReason}
        );

        listViewStockHistory.setAdapter(adapter);
    }

    private void loadAllHistory() {
        List<StockTransaction> list = productDAO.getAllStockHistory();

        populateListFromTransactions(list);
    }

    private void loadHistoryByProductName(String productName) {
        List<StockTransaction> list = productDAO.searchStockHistoryByProductName(productName);
        populateListFromTransactions(list);
    }

    private void populateListFromTransactions(List<StockTransaction> list) {
        List<Map<String, String>> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        for (StockTransaction tx : list) {
            Map<String, String> map = new HashMap<>();
            String qtyStr = tx.getQuantity() > 0 ? "+" + tx.getQuantity() : String.valueOf(tx.getQuantity());
            String type = tx.getType() != null ? tx.getType().toUpperCase() : "";
            String title;
            switch (type) {
                case "IN":
                    title = getString(R.string.stock_tx_type_in, qtyStr);
                    break;
                case "OUT":
                    title = getString(R.string.stock_tx_type_out, qtyStr);
                    break;
                case "ADD":
                    title = getString(R.string.stock_tx_type_add, qtyStr);
                    break;
                case "DELETE":
                    title = getString(R.string.stock_tx_type_delete, qtyStr);
                    break;
                default:
                    title = type.isEmpty() ? qtyStr : type + " " + qtyStr;
            }
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

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                R.layout.item_stock_tx,
                new String[]{"title", "detail", "reason"},
                new int[]{R.id.textViewTxType, R.id.textViewTxDetail, R.id.textViewTxReason}
        );

        listViewStockHistory.setAdapter(adapter);
    }
}
