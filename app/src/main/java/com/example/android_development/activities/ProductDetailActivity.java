package com.example.android_development.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.model.Product;
import com.example.android_development.util.Constants;
import com.example.android_development.util.PrefsManager;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView textViewProductName;
    private TextView textViewProductCategory;
    private TextView textViewProductPrice;
    private TextView textViewProductStock;
    private TextView textViewProductBrand;
    private TextView textViewProductUnit;
    private TextView textViewProductBarcode;
    private TextView textViewProductDescription;

    private Button buttonBack;
    private Button buttonEdit;

    private ProductDAO productDAO;
    private Product currentProduct;
    private PrefsManager prefsManager;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // 获取当前用户角色
        prefsManager = new PrefsManager(this);
        currentUserRole = prefsManager.getUserRole();

        // 初始化视图
        initViews();

        // 根据角色设置UI
        setupUIByRole();

        // 初始化数据库
        initDatabase();

        // 设置点击事件
        setupClickListeners();

        // 加载商品数据
        loadProductData();
    }

    private void initViews() {
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductCategory = findViewById(R.id.textViewProductCategory);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        textViewProductStock = findViewById(R.id.textViewProductStock);
        textViewProductBrand = findViewById(R.id.textViewProductBrand);
        textViewProductUnit = findViewById(R.id.textViewProductUnit);
        textViewProductBarcode = findViewById(R.id.textViewProductBarcode);
        textViewProductDescription = findViewById(R.id.textViewProductDescription);

        buttonBack = findViewById(R.id.buttonBack);
        buttonEdit = findViewById(R.id.buttonEdit);
    }

    private void setupUIByRole() {
        // 根据角色设置编辑按钮
        if (Constants.ROLE_ADMIN.equals(currentUserRole) ||
                Constants.ROLE_STOCK.equals(currentUserRole)) {
            buttonEdit.setVisibility(android.view.View.VISIBLE);
            if (Constants.ROLE_ADMIN.equals(currentUserRole)) {
                buttonEdit.setText("编辑商品");
            } else if (Constants.ROLE_STOCK.equals(currentUserRole)) {
                buttonEdit.setText("管理库存");
            }
        } else {
            buttonEdit.setVisibility(android.view.View.GONE);
        }
    }

    private void initDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        productDAO = new ProductDAO(dbHelper.getWritableDatabase());
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());

        buttonEdit.setOnClickListener(v -> {
            if (Constants.ROLE_ADMIN.equals(currentUserRole)) {
                Toast.makeText(this, "管理员：进入商品编辑功能", Toast.LENGTH_SHORT).show();
                // TODO: 跳转到商品编辑页面
            } else if (Constants.ROLE_STOCK.equals(currentUserRole)) {
                Toast.makeText(this, "库存管理员：进入库存管理功能", Toast.LENGTH_SHORT).show();
                // TODO: 跳转到库存管理页面
            }
        });
    }

    private void loadProductData() {
        // 获取传递过来的商品ID
        String productId = getIntent().getStringExtra("product_id");

        if (productId != null) {
            currentProduct = productDAO.getProductById(productId);

            if (currentProduct != null) {
                displayProductInfo();
            } else {
                Toast.makeText(this, "商品不存在", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "商品ID无效", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayProductInfo() {
        textViewProductName.setText(currentProduct.getName());
        textViewProductCategory.setText("分类: " + getCategoryName(currentProduct.getCategory()));
        textViewProductPrice.setText(String.format("价格: ￥%.2f", currentProduct.getPrice()));
        textViewProductStock.setText("库存: " + currentProduct.getStock());
        textViewProductBrand.setText("品牌: " + (currentProduct.getBrand() != null ? currentProduct.getBrand() : "未设置"));
        textViewProductUnit.setText("单位: " + (currentProduct.getUnit() != null ? currentProduct.getUnit() : "未设置"));
        textViewProductBarcode.setText("条码: " + (currentProduct.getBarcode() != null ? currentProduct.getBarcode() : "未设置"));
        textViewProductDescription.setText("描述: " + (currentProduct.getDescription() != null ? currentProduct.getDescription() : "无"));

        // 如果库存低于预警值，显示提醒
        if (currentProduct.isLowStock()) {
            textViewProductStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            textViewProductStock.append(" (库存不足!)");
        }
    }

    private String getCategoryName(String category) {
        if (category == null) return "未分类";

        switch (category) {
            case "daily":
                return "日用品";
            case "food":
                return "食品";
            case "drink":
                return "饮料";
            case "snack":
                return "零食";
            case "cleaning":
                return "清洁用品";
            case "other":
                return "其他";
            default:
                return category;
        }
    }
}