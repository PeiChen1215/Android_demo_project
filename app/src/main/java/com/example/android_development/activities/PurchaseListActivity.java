package com.example.android_development.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.content.Intent;
import com.example.android_development.adapters.PurchaseAdapter;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;

public class PurchaseListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_list);
        initViews();
        loadPurchaseOrders();
    }

    private RecyclerView listViewPurchases;
    private android.widget.TextView textViewNoPO;
    private android.widget.ImageButton buttonNewPurchase;
    private com.example.android_development.database.PurchaseDAO purchaseDAO;

    private void initViews() {
        listViewPurchases = findViewById(R.id.listViewPurchases);
        textViewNoPO = findViewById(R.id.textViewNoPO);
        buttonNewPurchase = findViewById(R.id.buttonNewPurchase);

        if (listViewPurchases.getLayoutManager() == null) listViewPurchases.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper db = new DatabaseHelper(this);
        purchaseDAO = new com.example.android_development.database.PurchaseDAO(db.getWritableDatabase());

        buttonNewPurchase.setOnClickListener(v -> {
            // create a minimal PO and refresh
            com.example.android_development.model.PurchaseOrder po = new com.example.android_development.model.PurchaseOrder();
            po.setStatus("OPEN");
            long res = purchaseDAO.addPurchaseOrder(po);
            if (res != -1) loadPurchaseOrders();
        });
    }

    private void loadPurchaseOrders() {
        java.util.List<com.example.android_development.model.PurchaseOrder> list = purchaseDAO.getAllPurchaseOrders();
        if (list == null || list.isEmpty()) {
            listViewPurchases.setVisibility(android.view.View.GONE);
            textViewNoPO.setVisibility(android.view.View.VISIBLE);
            return;
        }
        listViewPurchases.setVisibility(android.view.View.VISIBLE);
        textViewNoPO.setVisibility(android.view.View.GONE);

        java.util.List<java.util.Map<String,String>> data = new java.util.ArrayList<>();
        for (com.example.android_development.model.PurchaseOrder po : list) {
            java.util.Map<String,String> m = new java.util.HashMap<>();
            m.put("id", po.getId() == null ? "-" : po.getId());
            m.put("supplier", po.getSupplierId() == null ? "-" : po.getSupplierId());
            m.put("status", po.getStatus() == null ? "-" : po.getStatus());
            data.add(m);
        }

        PurchaseAdapter adapter = new PurchaseAdapter(this, list);
        adapter.setOnItemClickListener((position, po) -> {
            if (po != null && po.getId() != null) {
                Intent it = new Intent(PurchaseListActivity.this, PurchaseDetailActivity.class);
                it.putExtra("po_id", po.getId());
                startActivity(it);
            }
        });
        listViewPurchases.setAdapter(adapter);
    }
}
