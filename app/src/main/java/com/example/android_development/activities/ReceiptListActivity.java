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

/**
 * 收据列表页面。
 *
 * <p>加载最近的销售记录并以列表方式展示，点击某条记录进入 {@link ReceiptDetailActivity} 查看收据详情。
 * 列表展示会附带是否已退单、以及尽力显示操作人信息（从 userId 映射到用户名/姓名）。</p>
 */
public class ReceiptListActivity extends AppCompatActivity {

    private ListView listReceipts;
    private DatabaseHelper dbHelper;
    private SaleDAO saleDAO;
    private List<Sale> currentSales = new ArrayList<>();

    /**
     * Activity 创建：初始化列表与 DAO，加载收据列表并绑定点击跳转。
     */
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

    /**
     * 加载并展示最近的收据（销售单）列表。
     */
    private void loadReceipts() {
        // 加载最近 200 条销售记录
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
