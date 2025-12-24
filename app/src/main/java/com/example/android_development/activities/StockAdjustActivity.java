package com.example.android_development.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.model.Product;
import com.example.android_development.util.Constants;
import com.example.android_development.util.PrefsManager;

public class StockAdjustActivity extends AppCompatActivity {

    private TextView textViewProductName;
    private TextView textViewCurrentStock;
    private TextView textViewWarehouseStock;
    private Spinner spinnerTxType;
    private EditText editTextQuantity;
    private EditText editTextReason;
    private Button buttonSaveStock;
    private Button buttonCancelStock;

    private ProductDAO productDAO;
    private Product currentProduct;
    private PrefsManager prefsManager;
    private String currentUserId;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_adjust);

        prefsManager = new PrefsManager(this);
        currentUserId = prefsManager.getUserId();
        currentUserRole = prefsManager.getUserRole();

        initViews();
        initDatabase();
        setupSpinner();
        setupListeners();
        loadProduct();
    }

    private void initViews() {
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewCurrentStock = findViewById(R.id.textViewCurrentStock);
        textViewWarehouseStock = findViewById(R.id.textViewWarehouseStock);
        spinnerTxType = findViewById(R.id.spinnerTxType);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        editTextReason = findViewById(R.id.editTextReason);
        buttonSaveStock = findViewById(R.id.buttonSaveStock);
        buttonCancelStock = findViewById(R.id.buttonCancelStock);
    }

    private void initDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        productDAO = new ProductDAO(dbHelper.getWritableDatabase());
    }

    private void setupSpinner() {
        // 仅保留两个操作：货架出库 与 仓库出库（仓库出库会把货放到货架）
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{getString(R.string.stock_out), getString(R.string.warehouse_out)});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTxType.setAdapter(adapter);
    }

    private void setupListeners() {
        buttonCancelStock.setOnClickListener(v -> finish());

        buttonSaveStock.setOnClickListener(v -> {
            if (!(Constants.ROLE_ADMIN.equals(currentUserRole) || Constants.ROLE_STOCK.equals(currentUserRole))) {
                Toast.makeText(this, getString(R.string.no_permission), Toast.LENGTH_SHORT).show();
                return;
            }

            String qtyStr = editTextQuantity.getText().toString().trim();
            if (qtyStr.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_quantity), Toast.LENGTH_SHORT).show();
                return;
            }

            int qty;
            try { qty = Integer.parseInt(qtyStr); } catch (NumberFormatException e) { qty = 0; }
            if (qty <= 0) {
                Toast.makeText(this, getString(R.string.quantity_positive), Toast.LENGTH_SHORT).show();
                return;
            }

            String sel = spinnerTxType.getSelectedItem().toString();
            // 这里我们简化为两种出库操作：货架出库（SHELF OUT）和仓库出库（WAREHOUSE OUT）
            boolean isWarehouse = sel.equals(getString(R.string.warehouse_out));
            String type = "OUT";
            String reason = editTextReason.getText().toString().trim();

            // 提前校验库存，给出清晰提示
            if (isWarehouse) {
                if ("OUT".equalsIgnoreCase(type) && currentProduct.getWarehouseStock() < qty) {
                    Toast.makeText(this, getString(R.string.insufficient_warehouse_stock), Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if ("OUT".equalsIgnoreCase(type) && currentProduct.getStock() < qty) {
                    Toast.makeText(this, getString(R.string.insufficient_shelf_stock), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            boolean ok;
            if (isWarehouse) {
                ok = productDAO.adjustWarehouseWithTransaction(currentProduct.getId(), qty, type, currentUserId, currentUserRole, reason);
            } else {
                ok = productDAO.adjustStockWithTransaction(currentProduct.getId(), qty, type, currentUserId, currentUserRole, reason);
            }
            if (ok) {
                Toast.makeText(this, getString(R.string.stock_updated), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, getString(R.string.stock_update_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProduct() {
        String productId = getIntent().getStringExtra("product_id");
        if (productId == null) {
            finish();
            return;
        }

        currentProduct = productDAO.getProductById(productId);
        if (currentProduct == null) {
            finish();
            return;
        }

        textViewProductName.setText(currentProduct.getName());
        textViewCurrentStock.setText(getString(R.string.stock_current, currentProduct.getStock()));
        textViewWarehouseStock.setText(getString(R.string.label_warehouse_stock, currentProduct.getWarehouseStock()));
    }
}
