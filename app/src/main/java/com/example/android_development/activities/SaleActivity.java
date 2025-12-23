package com.example.android_development.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.SaleDAO;
import com.example.android_development.model.Sale;
import com.example.android_development.model.SaleLine;
import com.example.android_development.model.Product;
import com.example.android_development.database.ProductDAO;
import java.util.ArrayList;
import com.example.android_development.activities.adapters.SaleLineAdapter;
import android.widget.AdapterView;

public class SaleActivity extends AppCompatActivity {

    private EditText etProductId, etQty;
    private Button btnAddLine, btnCheckout;
    private TextView tvTotal;
    private ListView listViewLines;

    private DatabaseHelper dbHelper;
    private SaleDAO saleDAO;
    private ProductDAO productDAO;

    private Sale currentSale;
    private SaleLineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        etProductId = findViewById(R.id.et_product_id);
        etQty = findViewById(R.id.et_qty);
        btnAddLine = findViewById(R.id.btn_add_line);
        btnCheckout = findViewById(R.id.btn_checkout);
        tvTotal = findViewById(R.id.tv_total);
        listViewLines = findViewById(R.id.list_lines);

        dbHelper = new DatabaseHelper(this);
        saleDAO = new SaleDAO(dbHelper.getWritableDatabase(), this);
        productDAO = new ProductDAO(dbHelper.getReadableDatabase());

        currentSale = new Sale();

        btnAddLine.setOnClickListener(v -> addLine());
        btnCheckout.setOnClickListener(v -> checkout());
        // adapter for list
        adapter = new SaleLineAdapter(this, currentSale.getLines());
        listViewLines.setAdapter(adapter);

        listViewLines.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // remove line
                currentSale.getLines().remove(position);
                adapter.clear();
                adapter.addAll(currentSale.getLines());
                adapter.notifyDataSetChanged();
                recalcTotal();
                return true;
            }
        });
    }

    private void addLine() {
        String pid = etProductId.getText().toString().trim();
        String qtyS = etQty.getText().toString().trim();
        if (pid.isEmpty() || qtyS.isEmpty()) { Toast.makeText(this, "请输入商品ID和数量", Toast.LENGTH_SHORT).show(); return; }
        int qty = 1;
        try { qty = Integer.parseInt(qtyS); } catch (NumberFormatException e) { Toast.makeText(this, "数量格式不正确", Toast.LENGTH_SHORT).show(); return; }

        Product p = productDAO.getProductById(pid);
        if (p == null) { Toast.makeText(this, "未找到商品", Toast.LENGTH_SHORT).show(); return; }

        SaleLine l = new SaleLine();
        l.setProductId(p.getId());
        l.setProductName(p.getName());
        l.setQty(qty);
        l.setPrice(p.getPrice());

        ArrayList<SaleLine> lines = new ArrayList<>(currentSale.getLines());
        lines.add(l);
        currentSale.setLines(lines);
        adapter.clear();
        adapter.addAll(currentSale.getLines());
        adapter.notifyDataSetChanged();

        recalcTotal();
    }

    private void recalcTotal() {
        double t = 0;
        for (SaleLine l : currentSale.getLines()) t += l.getQty() * l.getPrice();
        currentSale.setTotal(t);
        tvTotal.setText(String.format("合计: %.2f", t));
    }

    private void checkout() {
        long res = saleDAO.addSale(currentSale);
        if (res == -1) {
            Toast.makeText(this, "结账失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.receipt_saved), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
