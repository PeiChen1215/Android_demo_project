package com.example.android_development.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.model.Product;

/**
 * 商品新增/编辑页面。
 *
 * <p>当 Intent 传入 product_id 时进入编辑模式；否则为新增模式。
 * 保存时会进行字段格式校验（价格/库存/日期/条码等），并通过 DatabaseHelper 的“按用户权限”方法
 * 执行新增/更新（例如 {@code addProductAsUser/updateProductAsUser}）。</p>
 */
public class ProductAddActivity extends AppCompatActivity {

    private EditText etName, etPrice, etStock;
    private EditText etWarehouseStock, etMinWarehouseStock, etExpiryDate; // 新增字段：仓库库存/最低仓库库存/到期日
    private Button btnAdd;
    private EditText etBrand, etCategory, etMinStock, etUnit, etBarcode, etDescription, etSupplier, etThumbUrl;
    private boolean editMode = false;
    private String editingProductId;
    private DatabaseHelper dbHelper;
    private Button btnCancel;

    /**
     * Activity 创建：初始化表单控件并绑定保存/取消事件；如为编辑模式则回填商品信息。
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_add);

        etName = findViewById(R.id.et_name);
        etBrand = findViewById(R.id.et_brand);
        etCategory = findViewById(R.id.et_category);
        etPrice = findViewById(R.id.et_price);
        etStock = findViewById(R.id.et_stock);
        etWarehouseStock = findViewById(R.id.et_warehouse_stock); // 布局中对应输入框：仓库库存
        etMinWarehouseStock = findViewById(R.id.et_min_warehouse_stock); // 布局中对应输入框：最低仓库库存
        etExpiryDate = findViewById(R.id.et_expiry_date); // 布局中对应输入框：到期日
        etMinStock = findViewById(R.id.et_min_stock);
        etUnit = findViewById(R.id.et_unit);
        etBarcode = findViewById(R.id.et_barcode);
        etSupplier = findViewById(R.id.et_supplier);
        etDescription = findViewById(R.id.et_description);
        etThumbUrl = findViewById(R.id.et_thumb_url);
        btnAdd = findViewById(R.id.btn_add);
        btnCancel = findViewById(R.id.btn_cancel);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });

        // 检查是否为编辑模式
        editingProductId = getIntent().getStringExtra("product_id");
        dbHelper = new DatabaseHelper(this);
        if (editingProductId != null) {
            editMode = true;
            btnAdd.setText(getString(R.string.btn_save_edit));
            loadProductForEdit(editingProductId);
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接返回，不保存
                finish();
            }
        });
    }

    /**
     * 保存商品：根据新增/编辑模式执行不同写入逻辑。
     *
     * <p>主要包含：必填校验、数字/日期解析、最小库存/价格/库存非负校验、条码格式与唯一性校验，
     * 最终构建 {@link Product} 并调用 DatabaseHelper 进行持久化。</p>
     */
    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String priceS = etPrice.getText().toString().trim();
        String stockS = etStock.getText().toString().trim();
        String warehouseStockS = etWarehouseStock != null ? etWarehouseStock.getText().toString().trim() : "0";
        String minWarehouseStockS = etMinWarehouseStock != null ? etMinWarehouseStock.getText().toString().trim() : "0";
        String expiryDateS = etExpiryDate != null ? etExpiryDate.getText().toString().trim() : "";
        String minStockS = etMinStock.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String barcode = etBarcode.getText().toString().trim();
        String supplier = etSupplier.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        boolean hasError = false;
        if (name.isEmpty()) {
            etName.setError(getString(R.string.please_enter_name));
            hasError = true;
        }
        if (priceS.isEmpty()) {
            etPrice.setError(getString(R.string.please_enter_price));
            hasError = true;
        }
        if (stockS.isEmpty()) {
            etStock.setError(getString(R.string.please_enter_stock));
            hasError = true;
        }
        if (hasError) return;

        double price = 0;
        int stock = 0;
        int warehouseStock = 0;
        int minWarehouseStock = 0;
        long expiryDate = 0;

        try {
            price = Double.parseDouble(priceS);
            stock = Integer.parseInt(stockS);
            if (!warehouseStockS.isEmpty()) warehouseStock = Integer.parseInt(warehouseStockS);
            if (!minWarehouseStockS.isEmpty()) minWarehouseStock = Integer.parseInt(minWarehouseStockS);
            if (!expiryDateS.isEmpty()) {
                // 简单解析日期（yyyy-MM-dd）
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date date = sdf.parse(expiryDateS);
                if (date != null) expiryDate = date.getTime();
            }
        } catch (Exception e) {
            Toast.makeText(this, "输入格式错误 (日期格式: yyyy-MM-dd)", Toast.LENGTH_SHORT).show();
            return;
        }

        // 额外字段校验
        int minStock = 0;
        if (!minStockS.isEmpty()) {
            try {
                minStock = Integer.parseInt(minStockS);
                if (minStock < 0) {
                    Toast.makeText(this, getString(R.string.min_stock_negative), Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.min_stock_format_invalid), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (price < 0) {
            etPrice.setError(getString(R.string.price_negative));
            return;
        }
        if (stock < 0) {
            etStock.setError(getString(R.string.stock_negative));
            return;
        }

        // 条码格式简单校验（仅数字或字母，长度在 6-32）
        if (!barcode.isEmpty()) {
            if (!barcode.matches("[A-Za-z0-9]{6,32}")) {
                etBarcode.setError(getString(R.string.barcode_format_invalid));
                return;
            }

            // 唯一性校验：若存在不同商品使用同一条码，则报错
            Product exist = dbHelper.getProductByBarcodeObject(barcode);
            if (exist != null) {
                if (!editMode || (editMode && !editingProductId.equals(exist.getId()))) {
                    etBarcode.setError(getString(R.string.barcode_already_used));
                    return;
                }
            }
        }

        Product p = new Product();
        if (editMode) p.setId(editingProductId);
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        p.setWarehouseStock(warehouseStock);
        p.setMinWarehouseStock(minWarehouseStock);
        p.setExpirationDate(expiryDate);
        p.setBrand(brand.isEmpty() ? null : brand);
        p.setCategory(category.isEmpty() ? com.example.android_development.util.Constants.CATEGORY_OTHER : category);
        p.setMinStock(minStock);
        p.setUnit(unit.isEmpty() ? null : unit);
        p.setBarcode(barcode.isEmpty() ? null : barcode);
        p.setSupplierId(supplier.isEmpty() ? null : supplier);
        p.setDescription(description.isEmpty() ? null : description);
        p.setThumbUrl(etThumbUrl.getText().toString().trim().isEmpty() ? null : etThumbUrl.getText().toString().trim());

        // 从 SharedPreferences 或登录状态中获取当前 userId
        String userId = getSharedPreferences(com.example.android_development.util.Constants.PREFS_NAME, MODE_PRIVATE)
                .getString(com.example.android_development.util.Constants.KEY_USER_ID, null);

        if (editMode) {
            int updated = dbHelper.updateProductAsUser(userId, p);
            if (updated > 0) {
                Toast.makeText(this, getString(R.string.product_update_success), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, getString(R.string.product_update_failed), Toast.LENGTH_SHORT).show();
            }
        } else {
            DatabaseHelper db = new DatabaseHelper(this);
            long res = db.addProductAsUser(userId, p);
            if (res == -1) {
                Toast.makeText(this, getString(R.string.no_permission_add_user), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_add_success), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * 编辑模式下：读取商品并回填到表单。
     */
    private void loadProductForEdit(String productId) {
        Product p = dbHelper.getProductByIdObject(productId);
        if (p == null) return;
        etName.setText(p.getName());
        etBrand.setText(p.getBrand() != null ? p.getBrand() : "");
        etCategory.setText(p.getCategory() != null ? p.getCategory() : "");
        etPrice.setText(String.valueOf(p.getPrice()));
        etStock.setText(String.valueOf(p.getStock()));
        if (etWarehouseStock != null) etWarehouseStock.setText(String.valueOf(p.getWarehouseStock()));
        if (etMinWarehouseStock != null) etMinWarehouseStock.setText(String.valueOf(p.getMinWarehouseStock()));
        if (etExpiryDate != null && p.getExpirationDate() > 0) {
            etExpiryDate.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(p.getExpirationDate())));
        }
        etMinStock.setText(String.valueOf(p.getMinStock()));
        etUnit.setText(p.getUnit() != null ? p.getUnit() : "");
        etBarcode.setText(p.getBarcode() != null ? p.getBarcode() : "");
        etSupplier.setText(p.getSupplierId() != null ? p.getSupplierId() : "");
        etDescription.setText(p.getDescription() != null ? p.getDescription() : "");
        etThumbUrl.setText(p.getThumbUrl() != null ? p.getThumbUrl() : "");
    }
}
