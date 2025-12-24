package com.example.android_development.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.text.TextWatcher;
import android.text.Editable;
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

    private android.widget.AutoCompleteTextView etProductKey;
    private EditText etQty;
    private Button btnAddLine, btnCheckout;
    private TextView tvTotal;
    private ListView listViewLines;
    private android.widget.Spinner spPaymentMethod;
    private EditText etPaid;
    private TextView tvChange;

    private DatabaseHelper dbHelper;
    private SaleDAO saleDAO;
    private ProductDAO productDAO;

    private Sale currentSale;
    private SaleLineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        etProductKey = findViewById(R.id.et_product_key);
        etQty = findViewById(R.id.et_qty);
        btnAddLine = findViewById(R.id.btn_add_line);
        btnCheckout = findViewById(R.id.btn_checkout);
        spPaymentMethod = findViewById(R.id.sp_payment_method);
        etPaid = findViewById(R.id.et_paid);
        tvChange = findViewById(R.id.tv_change);
        tvTotal = findViewById(R.id.tv_total);
        listViewLines = findViewById(R.id.list_lines);

        // setup payment method spinner
        ArrayAdapter<String> pmAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"现金", "银行卡", "微信", "支付宝"});
        pmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPaymentMethod.setAdapter(pmAdapter);

        dbHelper = new DatabaseHelper(this);
        saleDAO = new SaleDAO(dbHelper.getWritableDatabase(), this);
        productDAO = new ProductDAO(dbHelper.getReadableDatabase());

        // setup AutoComplete suggestions for product names
        android.widget.AutoCompleteTextView atv = etProductKey;
        android.widget.ArrayAdapter<String> suggestionAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        atv.setAdapter(suggestionAdapter);
        atv.setThreshold(1);
        atv.setOnItemClickListener((parent, view, position, id) -> {
            // when user selects suggestion, keep text (name) — addLine will resolve it
        });
        atv.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim();
                if (q.length() >= 1) {
                    // query product names like q
                    java.util.List<com.example.android_development.model.Product> results = productDAO.getProductsByNameLike(q);
                    suggestionAdapter.clear();
                    for (com.example.android_development.model.Product p : results) suggestionAdapter.add(p.getName());
                    suggestionAdapter.notifyDataSetChanged();
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // support Enter (actionDone) to add line
        atv.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                addLine();
                etProductKey.setText("");
                etQty.requestFocus();
                handled = true;
            }
            return handled;
        });

        etPaid.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { recalcTotal(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        currentSale = new Sale();

        btnAddLine.setOnClickListener(v -> addLine());
        btnCheckout.setOnClickListener(v -> checkout());
        // adapter for list
        adapter = new SaleLineAdapter(this, currentSale.getLines());
        // set delete callback
        adapter.setOnDeleteListener(position -> {
            if (position >= 0 && position < currentSale.getLines().size()) {
                currentSale.getLines().remove(position);
                adapter.clear();
                adapter.addAll(currentSale.getLines());
                adapter.notifyDataSetChanged();
                recalcTotal();
            }
        });
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
        String key = etProductKey.getText().toString().trim();
        String qtyS = etQty.getText().toString().trim();
        if (key.isEmpty() || qtyS.isEmpty()) { Toast.makeText(this, "请输入商品名称或条码和数量", Toast.LENGTH_SHORT).show(); return; }
        int qty = 1;
        try { qty = Integer.parseInt(qtyS); } catch (NumberFormatException e) { Toast.makeText(this, "数量格式不正确", Toast.LENGTH_SHORT).show(); return; }

        // Try barcode first (using DatabaseHelper helper), then name
        Product p = null;
        try { p = dbHelper.getProductByBarcodeObject(key); } catch (Exception ignored) {}
        if (p == null) {
            p = productDAO.getProductByName(key);
        }
        if (p == null) { Toast.makeText(this, "未找到商品", Toast.LENGTH_SHORT).show(); return; }

        // 校验库存：考虑当前销售已包含同商品的数量
        int existingQty = 0;
        for (SaleLine sl : currentSale.getLines()) {
            if (sl.getProductId() != null && sl.getProductId().equals(p.getId())) existingQty += sl.getQty();
        }
        int totalRequested = existingQty + qty;
        if (p.getStock() < totalRequested) {
            Toast.makeText(this, "库存不足，货架库存: " + p.getStock() + "，已请求: " + totalRequested, Toast.LENGTH_SHORT).show();
            return;
        }

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
        // update change display if paid value present
        try {
            String paidS = etPaid.getText().toString().trim();
            if (!paidS.isEmpty()) {
                double paid = Double.parseDouble(paidS);
                double change = paid - t;
                tvChange.setText(String.format("找零: %.2f", change < 0 ? 0.0 : change));
            } else {
                tvChange.setText("找零: 0.00");
            }
        } catch (Exception ignored) {}
    }

    private void checkout() {
        // validate lines
        if (currentSale.getLines() == null || currentSale.getLines().isEmpty()) {
            Toast.makeText(this, "请先添加销售行", Toast.LENGTH_SHORT).show();
            return;
        }
        // 最终库存校验：聚合同一商品的请求数量并与当前货架库存比较
        java.util.Map<String, Integer> reqMap = new java.util.HashMap<>();
        for (SaleLine sl : currentSale.getLines()) {
            if (sl.getProductId() == null) continue;
            int v = reqMap.containsKey(sl.getProductId()) ? reqMap.get(sl.getProductId()) : 0;
            reqMap.put(sl.getProductId(), v + sl.getQty());
        }
        for (java.util.Map.Entry<String, Integer> e : reqMap.entrySet()) {
            String pid = e.getKey();
            int need = e.getValue();
            com.example.android_development.model.Product prod = productDAO.getProductById(pid);
            int available = prod != null ? prod.getStock() : 0;
            if (available < need) {
                String name = prod != null ? prod.getName() : pid;
                Toast.makeText(this, "库存不足：" + name + "，货架库存:" + available + "，需:" + need, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        double total = currentSale.getTotal();
        String paidS = etPaid.getText().toString().trim();
        double paid = 0;
        try { paid = Double.parseDouble(paidS); } catch (Exception e) { Toast.makeText(this, "请输入有效的已付金额", Toast.LENGTH_SHORT).show(); return; }
        if (paid < total) { Toast.makeText(this, "付款金额不足", Toast.LENGTH_SHORT).show(); return; }

        String paymentMethod = spPaymentMethod.getSelectedItem() != null ? spPaymentMethod.getSelectedItem().toString() : "cash";
        currentSale.setPaid(paid);
        currentSale.setPaymentMethod(paymentMethod);

        long res = saleDAO.addSale(currentSale);
        if (res == -1) {
            Toast.makeText(this, "结账失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.receipt_saved), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
