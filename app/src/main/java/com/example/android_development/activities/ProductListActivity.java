package com.example.android_development.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;
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
    private Button buttonStockHistory;
    private android.widget.EditText etSearch;
    private Button buttonSearch;
    private Button buttonClearSearch;
    private Button buttonBack;
    private ProductDAO productDAO;
    private List<Product> productList;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, String>> adapterData;
    private android.view.View footerView;
    private Button btnLoadMore;
    private static final int PAGE_SIZE = 20;
    private int currentOffset = 0;
    private boolean loadingMore = false;
    private PrefsManager prefsManager;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // 获取当前用户角色
        prefsManager = new PrefsManager(this);
        currentUserRole = prefsManager.getUserRole();

        // 如果 SharedPreferences 中没有角色信息，尝试通过 userId 从数据库读取角色（适配调试场景）
        if (currentUserRole == null || currentUserRole.isEmpty()) {
            String uid = prefsManager.getUserId();
            if (uid != null && !uid.isEmpty()) {
                DatabaseHelper dh = new DatabaseHelper(this);
                com.example.android_development.model.User u = dh.getUserByIdObject(uid);
                if (u != null) currentUserRole = u.getRole();
            }
        }

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
        buttonStockHistory = findViewById(R.id.buttonStockHistory);
        etSearch = findViewById(R.id.et_search);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonClearSearch = findViewById(R.id.buttonClearSearch);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void setupUIByRole() {
        // 根据角色设置标题
        if (Constants.ROLE_ADMIN.equals(currentUserRole) ||
                Constants.ROLE_STOCK.equals(currentUserRole)) {
            textViewTitle.setText(getString(R.string.title_product_manage));
            buttonAddProduct.setVisibility(View.VISIBLE);
        } else {
            textViewTitle.setText(getString(R.string.title_product_query));
            buttonAddProduct.setVisibility(View.GONE);
        }
    }

    private void initDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        productDAO = new ProductDAO(dbHelper.getWritableDatabase());
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());

        buttonSearch.setOnClickListener(v -> {
            String kw = etSearch.getText().toString().trim();
            if (kw.isEmpty()) {
                loadProducts();
            } else {
                performSearch(kw);
            }
        });

        buttonClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            loadProducts();
        });

        buttonAddProduct.setOnClickListener(v -> {
            // 根据角色判断是否有添加权限
            if (Constants.ROLE_ADMIN.equals(currentUserRole) ||
                    Constants.ROLE_STOCK.equals(currentUserRole)) {
                // 跳转到添加商品页面
                Intent intent = new Intent(ProductListActivity.this, ProductAddActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(ProductListActivity.this, getString(R.string.no_permission_add), Toast.LENGTH_SHORT).show();
            }
        });

        buttonStockHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, StockHistoryActivity.class);
            // 不传 product_id 表示全局历史
            startActivity(intent);
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
                    Toast.makeText(this, getString(R.string.no_permission_delete), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            return false;
        });
    }

    private void showDeleteConfirmation(Product product) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.confirm_delete_msg, product.getName()))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    deleteProduct(product);
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void deleteProduct(Product product) {
        // 使用 DatabaseHelper 的基于 userId 的权限校验删除, 并提供撤销
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String userId = prefsManager.getUserId();

        // 先拷贝被删除的商品以便撤销使用
        final Product deletedProduct = product;

        int result = dbHelper.deleteProductAsUser(userId, product.getId());

        if (result > 0) {
            Toast.makeText(this, getString(R.string.deleted_success), Toast.LENGTH_SHORT).show();
            loadProducts(); // 刷新列表

            // 显示 Snackbar 提供撤销操作
            View root = findViewById(android.R.id.content);
            Snackbar.make(root, getString(R.string.deleted_snackbar), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo), v -> {
                        long addRes = dbHelper.addProductAsUser(userId, deletedProduct);
                        if (addRes == -1) {
                            Toast.makeText(this, getString(R.string.undo_failed), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, getString(R.string.undo_success), Toast.LENGTH_SHORT).show();
                            loadProducts();
                        }
                    })
                    .show();
        } else {
            Toast.makeText(this, getString(R.string.deleted_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProducts() {
        // 分页加载：重置偏移并加载第一页
        currentOffset = 0;
        adapterData = new ArrayList<>();
        if (listViewProducts.getFooterViewsCount() == 0) {
            footerView = getLayoutInflater().inflate(R.layout.list_footer_load_more, null);
            btnLoadMore = footerView.findViewById(R.id.btnLoadMore);
            btnLoadMore.setOnClickListener(v -> {
                if (!loadingMore) loadNextPage();
            });
            listViewProducts.addFooterView(footerView);
        }

        productList = productDAO.getProductsPage(PAGE_SIZE, currentOffset);

        if (productList == null || productList.isEmpty()) {
            // 显示空状态提示
            listViewProducts.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.VISIBLE);
            String hint = "";
            if (Constants.ROLE_ADMIN.equals(currentUserRole) || Constants.ROLE_STOCK.equals(currentUserRole)) {
                hint = getString(R.string.no_products_hint_admin);
            }
            textViewEmpty.setText(getString(R.string.no_products_full, hint));
        } else {
            // 显示商品列表
            listViewProducts.setVisibility(View.VISIBLE);
            textViewEmpty.setVisibility(View.GONE);
            // 填充数据并设置适配器
            adapterData.clear();
            for (Product product : productList) {
                Map<String, String> map = new HashMap<>();
                map.put("name", product.getName());
                map.put("price", String.format("￥%.2f", product.getPrice()));
                map.put("stock", String.format("库存: %d", product.getStock()));
                map.put("category", getCategoryName(product.getCategory()));
                adapterData.add(map);
            }

            simpleAdapter = new SimpleAdapter(
                    this,
                    adapterData,
                    R.layout.item_product,
                    new String[]{"name", "price", "stock", "category"},
                    new int[]{R.id.textViewProductName, R.id.textViewProductPrice,
                            R.id.textViewProductStock, R.id.textViewProductCategory}
            );

            listViewProducts.setAdapter(simpleAdapter);

            // 准备下一页
            currentOffset += productList.size();
        }
    }

    private void loadNextPage() {
        loadingMore = true;
        btnLoadMore.setEnabled(false);
        List<Product> next = productDAO.getProductsPage(PAGE_SIZE, currentOffset);
        if (next != null && !next.isEmpty()) {
            for (Product product : next) {
                Map<String, String> map = new HashMap<>();
                map.put("name", product.getName());
                map.put("price", String.format("￥%.2f", product.getPrice()));
                map.put("stock", String.format("库存: %d", product.getStock()));
                map.put("category", getCategoryName(product.getCategory()));
                adapterData.add(map);
            }
            simpleAdapter.notifyDataSetChanged();
            currentOffset += next.size();
        } else {
            // 没有更多，隐藏按钮
            if (footerView != null) footerView.setVisibility(View.GONE);
        }
        btnLoadMore.setEnabled(true);
        loadingMore = false;
    }

    private void performSearch(String keyword) {
        if (keyword == null) keyword = "";
        // 分页搜索：重置偏移并加载第一页
        currentOffset = 0;
        adapterData = new ArrayList<>();
        if (listViewProducts.getFooterViewsCount() == 0) {
            final String searchKw = keyword; // lambda 要求捕获的局部变量为 effectively final
            footerView = getLayoutInflater().inflate(R.layout.list_footer_load_more, null);
            btnLoadMore = footerView.findViewById(R.id.btnLoadMore);
            btnLoadMore.setOnClickListener(v -> {
                if (!loadingMore) {
                    List<Product> next = productDAO.searchProductsPage(searchKw, PAGE_SIZE, currentOffset);
                    if (next != null && !next.isEmpty()) {
                        for (Product p : next) {
                            Map<String, String> map = new HashMap<>();
                            map.put("name", p.getName());
                            map.put("price", String.format("￥%.2f", p.getPrice()));
                            map.put("stock", String.format("库存: %d", p.getStock()));
                            map.put("category", getCategoryName(p.getCategory()));
                            adapterData.add(map);
                        }
                        simpleAdapter.notifyDataSetChanged();
                        currentOffset += next.size();
                    } else {
                        if (footerView != null) footerView.setVisibility(View.GONE);
                    }
                }
            });
            listViewProducts.addFooterView(footerView);
        }

        List<Product> results = productDAO.searchProductsPage(keyword, PAGE_SIZE, currentOffset);
        productList = results;

        if (productList == null || productList.isEmpty()) {
            listViewProducts.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.VISIBLE);
            textViewEmpty.setText(getString(R.string.no_search_results));
        } else {
            listViewProducts.setVisibility(View.VISIBLE);
            textViewEmpty.setVisibility(View.GONE);

            adapterData.clear();
            for (Product product : productList) {
                java.util.Map<String, String> map = new java.util.HashMap<>();
                map.put("name", product.getName());
                map.put("price", String.format("￥%.2f", product.getPrice()));
                map.put("stock", String.format("库存: %d", product.getStock()));
                map.put("category", getCategoryName(product.getCategory()));
                adapterData.add(map);
            }

            simpleAdapter = new SimpleAdapter(
                    this,
                    adapterData,
                    R.layout.item_product,
                    new String[]{"name", "price", "stock", "category"},
                    new int[]{R.id.textViewProductName, R.id.textViewProductPrice,
                            R.id.textViewProductStock, R.id.textViewProductCategory}
            );

            listViewProducts.setAdapter(simpleAdapter);

            currentOffset += productList.size();
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