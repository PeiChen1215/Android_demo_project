package com.example.android_development.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.android_development.adapters.ProductAdapter;
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

public class ProductQueryActivity extends AppCompatActivity {

    private RecyclerView listViewProducts;
    private TextView textViewEmpty;
    private android.widget.ImageButton buttonBack;

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
        if (listViewProducts.getLayoutManager() == null) listViewProducts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        productDAO = new ProductDAO(dbHelper.getWritableDatabase());
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());

        // 点击通过适配器回调处理（在 loadProducts 中绑定）
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

            // 使用类型化 ProductAdapter（查询界面默认不显示操作按钮）
            ProductAdapter adapter = new ProductAdapter(this, productList, false);
            adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, Product product) {
                    Toast.makeText(ProductQueryActivity.this,
                            getString(R.string.view_product, product.getName()), Toast.LENGTH_SHORT).show();
                }

                @Override
                public boolean onItemLongClick(int position, Product product) { return false; }

                @Override
                public void onActionEdit(int position, Product product) {
                    Toast.makeText(ProductQueryActivity.this, "无权限编辑", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onActionDelete(int position, Product product) {
                    Toast.makeText(ProductQueryActivity.this, "无权限删除", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onActionAdjustStock(int position, Product product) {
                    Toast.makeText(ProductQueryActivity.this, "无权限调整库存", Toast.LENGTH_SHORT).show();
                }
            });

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