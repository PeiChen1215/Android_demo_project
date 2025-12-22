package com.example.android_development.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.android_development.adapters.StockCountAdapter;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;

public class StockCountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_count);
        initViews();
        loadCounts();
    }

    private RecyclerView listViewCounts;
    private android.widget.TextView textViewNoCounts;
    private android.widget.Button buttonNewCount;
    private com.example.android_development.database.InventoryDAO inventoryDAO;

    private void initViews() {
        listViewCounts = findViewById(R.id.listViewCounts);
        textViewNoCounts = findViewById(R.id.textViewNoCounts);
        buttonNewCount = findViewById(R.id.buttonNewCount);

        if (listViewCounts.getLayoutManager() == null) listViewCounts.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper db = new DatabaseHelper(this);
        inventoryDAO = new com.example.android_development.database.InventoryDAO(db.getWritableDatabase());

        buttonNewCount.setOnClickListener(v -> {
            com.example.android_development.model.StockCount sc = new com.example.android_development.model.StockCount();
            sc.setStatus("OPEN");
            sc.setCreatedBy(null);
            long res = inventoryDAO.createStockCount(sc);
            if (res != -1) loadCounts();
        });
    }

    private void loadCounts() {
        java.util.List<com.example.android_development.model.StockCount> list = inventoryDAO.getAllStockCounts();
        if (list == null || list.isEmpty()) {
            listViewCounts.setVisibility(android.view.View.GONE);
            textViewNoCounts.setVisibility(android.view.View.VISIBLE);
            return;
        }
        listViewCounts.setVisibility(android.view.View.VISIBLE);
        textViewNoCounts.setVisibility(android.view.View.GONE);

        java.util.List<java.util.Map<String,String>> data = new java.util.ArrayList<>();
        for (com.example.android_development.model.StockCount sc : list) {
            java.util.Map<String,String> m = new java.util.HashMap<>();
            m.put("id", sc.getId() == null ? "-" : sc.getId());
            m.put("status", sc.getStatus() == null ? "-" : sc.getStatus());
            data.add(m);
        }

        StockCountAdapter adapter = new StockCountAdapter(this, list);
        adapter.setOnItemClickListener((position, sc) -> {
            // TODO: open count detail
        });
        listViewCounts.setAdapter(adapter);
    }
}
