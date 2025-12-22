package com.example.android_development.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.model.Product;
import com.example.android_development.util.PrefsManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.SimpleAdapter;

public class ProductQueryActivity extends AppCompatActivity {

    private ListView listViewProducts;
    private TextView textViewEmpty;
    private Button buttonBack;

    private ProductDAO productDAO;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_query);

        // 初始化视图
        initViews();

        // 初始化数据库
        initDatabase();

        // 设置点击事件
        setupClickListeners();

        // 加载商品数据
        loadProducts();
    }

    private void initViews() {
        listViewProducts = findViewById(R.id.listViewProducts);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void initDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        productDAO = new ProductDAO(dbHelper.getWritableDatabase());
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());

        // 列表项点击事件 - 只能查看详情
        listViewProducts.setOnItemClickListener((parent, view, position, id) -> {
            if (productList != null && position < productList.size()) {
                Product product = productList.get(position);

                // 跳转到商品详情页面（只读）
                Toast.makeText(ProductQueryActivity.this,
                        getString(R.string.view_product, product.getName()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        // 从数据库获取所有商品
        productList = productDAO.getAllProducts();

        if (productList == null || productList.isEmpty()) {
            // 显示空状态提示
            listViewProducts.setVisibility(android.view.View.GONE);
            textViewEmpty.setVisibility(android.view.View.VISIBLE);
            textViewEmpty.setText(getString(R.string.no_products));
        } else {
            // 显示商品列表
            listViewProducts.setVisibility(android.view.View.VISIBLE);
            textViewEmpty.setVisibility(android.view.View.GONE);

            // 创建SimpleAdapter需要的数据
            List<Map<String, String>> data = new ArrayList<>();

            for (Product product : productList) {
                Map<String, String> map = new HashMap<>();
                map.put("name", product.getName());
                map.put("price", String.format("￥%.2f", product.getPrice()));
                map.put("stock", String.format("库存: %d", product.getStock()));
                map.put("category", getCategoryName(product.getCategory()));

                data.add(map);
            }

            // 创建适配器
            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    data,
                    R.layout.item_product,
                    new String[]{"name", "price", "stock", "category"},
                    new int[]{R.id.textViewProductName, R.id.textViewProductPrice,
                            R.id.textViewProductStock, R.id.textViewProductCategory}
            );

            listViewProducts.setAdapter(adapter);
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

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新列表
        loadProducts();
    }
}