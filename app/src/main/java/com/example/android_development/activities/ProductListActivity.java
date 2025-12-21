package com.example.android_development.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.model.Product;
import com.example.android_development.util.Constants;
import com.example.android_development.util.PrefsManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductListActivity extends AppCompatActivity {

    private ListView listViewProducts;
    private TextView textViewEmpty;
    private TextView textViewTitle;
    private Button buttonAddProduct;
    private Button buttonBack;

    private ProductDAO productDAO;
    private List<Product> productList;
    private PrefsManager prefsManager;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

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
        loadProducts();
    }

    private void initViews() {
        listViewProducts = findViewById(R.id.listViewProducts);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        textViewTitle = findViewById(R.id.textViewTitle);
        buttonAddProduct = findViewById(R.id.buttonAddProduct);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void setupUIByRole() {
        // 根据角色设置标题
        if (Constants.ROLE_ADMIN.equals(currentUserRole) ||
                Constants.ROLE_STOCK.equals(currentUserRole)) {
            textViewTitle.setText("商品管理");
            buttonAddProduct.setVisibility(View.VISIBLE);
        } else {
            textViewTitle.setText("商品查询");
            buttonAddProduct.setVisibility(View.GONE);
        }
    }

    private void initDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        productDAO = new ProductDAO(dbHelper.getWritableDatabase());
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());

        buttonAddProduct.setOnClickListener(v -> {
            // 根据角色判断是否有添加权限
            if (Constants.ROLE_ADMIN.equals(currentUserRole) ||
                    Constants.ROLE_STOCK.equals(currentUserRole)) {
                // TODO: 跳转到添加商品页面
                Toast.makeText(ProductListActivity.this, "添加商品功能开发中", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProductListActivity.this, "权限不足：只有管理员和库存管理员可以添加商品", Toast.LENGTH_SHORT).show();
            }
        });

        // 列表项点击事件
        listViewProducts.setOnItemClickListener((parent, view, position, id) -> {
            if (productList != null && position < productList.size()) {
                Product product = productList.get(position);

                // 根据角色决定跳转到哪个页面
                Intent intent;

                if (Constants.ROLE_ADMIN.equals(currentUserRole)) {
                    // 管理员：跳转到完整编辑页面（待实现）
                    Toast.makeText(this, "管理员：进入商品编辑", Toast.LENGTH_SHORT).show();
                    intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                } else if (Constants.ROLE_STOCK.equals(currentUserRole)) {
                    // 库存管理员：跳转到库存编辑页面（待实现）
                    Toast.makeText(this, "库存管理员：进入库存管理", Toast.LENGTH_SHORT).show();
                    intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                } else {
                    // 其他角色：只能查看
                    intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                }

                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }
        });

        // 列表项长按事件（删除功能）
        listViewProducts.setOnItemLongClickListener((parent, view, position, id) -> {
            if (productList != null && position < productList.size()) {
                Product product = productList.get(position);

                // 只有管理员可以删除
                if (Constants.ROLE_ADMIN.equals(currentUserRole)) {
                    showDeleteConfirmation(product);
                    return true;
                } else {
                    Toast.makeText(this, "权限不足：只有管理员可以删除商品", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            return false;
        });
    }

    private void showDeleteConfirmation(Product product) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除商品：" + product.getName() + "吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    deleteProduct(product);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteProduct(Product product) {
        int result = productDAO.deleteProduct(product.getId());

        if (result > 0) {
            Toast.makeText(this, "商品删除成功", Toast.LENGTH_SHORT).show();
            loadProducts(); // 刷新列表
        } else {
            Toast.makeText(this, "商品删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProducts() {
        // 从数据库获取所有商品
        productList = productDAO.getAllProducts();

        if (productList == null || productList.isEmpty()) {
            // 显示空状态提示
            listViewProducts.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.VISIBLE);
            textViewEmpty.setText("暂无商品数据\n" +
                    (Constants.ROLE_ADMIN.equals(currentUserRole) ||
                            Constants.ROLE_STOCK.equals(currentUserRole)
                            ? "点击右上角添加按钮添加商品" : ""));
        } else {
            // 显示商品列表
            listViewProducts.setVisibility(View.VISIBLE);
            textViewEmpty.setVisibility(View.GONE);

            // 创建SimpleAdapter需要的数据
            List<Map<String, String>> data = new ArrayList<>();

            for (Product product : productList) {
                Map<String, String> map = new HashMap<>();
                map.put("name", product.getName());
                map.put("price", String.format("￥%.2f", product.getPrice()));
                map.put("stock", "库存: " + product.getStock());
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
        // 当从详情页面返回时，刷新列表
        loadProducts();
    }
}