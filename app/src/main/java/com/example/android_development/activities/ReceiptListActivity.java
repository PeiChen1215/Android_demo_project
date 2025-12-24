package com.example.android_development.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.SaleDAO;
import com.example.android_development.model.Sale;
import java.util.ArrayList;
import java.util.List;

public class ReceiptListActivity extends AppCompatActivity {

    private ListView listReceipts;
    private DatabaseHelper dbHelper;
    private SaleDAO saleDAO;
    private List<Sale> currentSales = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_list);

        listReceipts = findViewById(R.id.list_receipts);

        dbHelper = new DatabaseHelper(this);
        saleDAO = new SaleDAO(dbHelper.getReadableDatabase());

        loadReceipts();

        listReceipts.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < currentSales.size()) {
                Sale s = currentSales.get(position);
                Intent i = new Intent(ReceiptListActivity.this, ReceiptDetailActivity.class);
                i.putExtra("sale_id", s.getId());
                startActivity(i);
            }
        });
    }

    private void loadReceipts() {
        // load recent 200 sales
        currentSales = saleDAO.getRecentSales(200);
        List<String> display = new ArrayList<>();
        for (Sale s : currentSales) {
            String label = java.text.SimpleDateFormat.getDateTimeInstance().format(new java.util.Date(s.getTimestamp()));
            String note = s.isRefunded() ? "（已退单）" : "";
            String operator = "";
            try {
                if (s.getUserId() != null) {
                    com.example.android_development.model.User u = dbHelper.getUserByIdObject(s.getUserId());
                    if (u != null) operator = " 操作:" + (u.getFullName() != null ? u.getFullName() : u.getUsername());
                    else operator = " 操作:" + s.getUserId();
                }
            } catch (Exception ignored) {}
            display.add(label + "  —  " + String.format("%.2f", s.getTotal()) + " " + note + operator);
        }
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, display);
        listReceipts.setAdapter(ad);
    }
}
