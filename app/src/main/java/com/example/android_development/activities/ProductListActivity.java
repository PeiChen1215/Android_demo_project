package com.example.android_development.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_development.adapters.ProductAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
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

/**
 * 商品列表页面。
 *
 * <p>提供商品分页浏览、关键词搜索（支持分页继续加载）、查看详情、库存历史查看等能力；
 * 并根据当前用户角色控制新增/删除等管理操作入口。</p>
 */
public class ProductListActivity extends AppCompatActivity {

    private RecyclerView listViewProducts;
    private TextView textViewEmpty;
    private TextView textViewTitle;
    private ImageButton buttonAddProduct;
    private ImageButton buttonStockHistory;
    private android.widget.EditText etSearch;
    private Button buttonSearch;
    private Button buttonClearSearch;
    private ImageButton buttonBack;
    private ProductDAO productDAO;
    private List<Product> productList;
    private ProductAdapter simpleAdapter;
    private List<Map<String, String>> adapterData;
    private Button btnLoadMore;
    private static final int PAGE_SIZE = 20;
    private int currentOffset = 0;
    private boolean loadingMore = false;
    private PrefsManager prefsManager;
    private String currentUserRole;

    /**
     * Activity 创建：读取登录用户角色，初始化视图/数据库/事件，并加载商品列表。
     */
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

    /**
     * 初始化页面控件引用。
     */
    private void initViews() {
        listViewProducts = findViewById(R.id.listViewProducts);
        btnLoadMore = findViewById(R.id.btnLoadMore);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        textViewTitle = findViewById(R.id.textViewTitle);
        buttonAddProduct = findViewById(R.id.buttonAddProduct);
        buttonStockHistory = findViewById(R.id.buttonStockHistory);
        etSearch = findViewById(R.id.et_search);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonClearSearch = findViewById(R.id.buttonClearSearch);
        buttonBack = findViewById(R.id.buttonBack);
    }

    /**
     * 按当前用户角色配置页面 UI（标题、按钮可见性等）。
     */
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

    /**
     * 初始化数据库访问对象（DAO）。
     */
    private void initDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        productDAO = new ProductDAO(dbHelper.getWritableDatabase());
    }

    /**
     * 绑定页面交互事件：返回、搜索/清空、新增商品、查看库存历史等。
     */
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

        // 列表项点击 / 长按通过适配器回调处理（在 loadProducts 中绑定）
    }

    /**
     * 弹出删除确认对话框。
     *
     * <p>实际删除由 {@link #deleteProduct(Product)} 执行，并提供撤销入口。</p>
     */
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

    /**
     * 删除商品。
     *
     * <p>通过 {@link DatabaseHelper#deleteProductAsUser(String, String)} 做基于 userId 的权限校验；
     * 删除成功后使用 Snackbar 提供“撤销”操作。</p>
     */
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

    /**
     * 加载商品列表（分页第一页）。
     *
     * <p>会重置分页偏移量，并根据结果控制“加载更多”按钮显示。</p>
     */
    private void loadProducts() {
        // 分页加载：重置偏移并加载第一页
        currentOffset = 0;
        adapterData = new ArrayList<>();

        // 控制加载更多按钮
        btnLoadMore.setVisibility(View.GONE);

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

            // 使用类型化适配器（根据角色控制操作按钮显示）
            boolean canModify = Constants.ROLE_ADMIN.equals(currentUserRole) || Constants.ROLE_STOCK.equals(currentUserRole);
            simpleAdapter = new ProductAdapter(this, productList, canModify);
            simpleAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, Product product) {
                    Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                    intent.putExtra("product_id", product.getId());
                    startActivity(intent);
                }

                @Override
                public boolean onItemLongClick(int position, Product product) {
                    if (Constants.ROLE_ADMIN.equals(currentUserRole)) {
                        showDeleteConfirmation(product);
                        return true;
                    } else {
                        Toast.makeText(ProductListActivity.this, getString(R.string.no_permission_delete), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                
                @Override
                public void onActionEdit(int position, Product product) {
                    Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                    intent.putExtra("product_id", product.getId());
                    startActivity(intent);
                }

                @Override
                public void onActionDelete(int position, Product product) {
                    if (Constants.ROLE_ADMIN.equals(currentUserRole)) {
                        showDeleteConfirmation(product);
                    } else {
                        Toast.makeText(ProductListActivity.this, getString(R.string.no_permission_delete), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onActionAdjustStock(int position, Product product) {
                    Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                    intent.putExtra("product_id", product.getId());
                    intent.putExtra("mode", "adjust_stock");
                    startActivity(intent);
                }
            });

            listViewProducts.setAdapter(simpleAdapter);

            // 如果可能还有下一页，显示加载更多按钮
            if (productList.size() == PAGE_SIZE) {
                btnLoadMore.setVisibility(View.VISIBLE);
                btnLoadMore.setOnClickListener(v -> {
                    if (!loadingMore) loadNextPage();
                });
            } else {
                btnLoadMore.setVisibility(View.GONE);
            }

            // 准备下一页
            currentOffset += productList.size();
        }
    }

    /**
     * 加载下一页商品并追加到当前列表。
     */
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
            // 如果没有更多，隐藏按钮
            if (next.size() < PAGE_SIZE) btnLoadMore.setVisibility(View.GONE);
        } else {
            // 没有更多，隐藏按钮
            btnLoadMore.setVisibility(View.GONE);
        }
        btnLoadMore.setEnabled(true);
        loadingMore = false;
    }

    /**
     * 按关键词搜索商品（分页第一页）。
     *
     * <p>搜索模式下同样支持“加载更多”继续分页追加。</p>
     */
    private void performSearch(String keyword) {
        if (keyword == null) keyword = "";
        // 分页搜索：重置偏移并加载第一页
        currentOffset = 0;
        adapterData = new ArrayList<>();
        // 搜索时隐藏加载更多按钮，后面根据结果显示
        btnLoadMore.setVisibility(View.GONE);

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

            // 刷新 adapter 数据以适应分页追加
                if (simpleAdapter == null) {
                boolean canModify = Constants.ROLE_ADMIN.equals(currentUserRole) || Constants.ROLE_STOCK.equals(currentUserRole);
                simpleAdapter = new ProductAdapter(this, new java.util.ArrayList<>(), canModify);
                simpleAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, Product product) {
                        Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                        intent.putExtra("product_id", product.getId());
                        startActivity(intent);
                    }

                    @Override
                    public boolean onItemLongClick(int position, Product product) {
                        if (Constants.ROLE_ADMIN.equals(currentUserRole)) {
                            showDeleteConfirmation(product);
                            return true;
                        } else {
                            Toast.makeText(ProductListActivity.this, getString(R.string.no_permission_delete), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }

                    @Override
                    public void onActionEdit(int position, Product product) {
                        // 编辑
                        Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                        intent.putExtra("product_id", product.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onActionDelete(int position, Product product) {
                        // 删除（仅管理员）
                        if (Constants.ROLE_ADMIN.equals(currentUserRole)) showDeleteConfirmation(product);
                        else Toast.makeText(ProductListActivity.this, getString(R.string.no_permission_delete), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onActionAdjustStock(int position, Product product) {
                        // 跳转到库存调整页面（复用 ProductDetailActivity，附加模式）
                        Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                        intent.putExtra("product_id", product.getId());
                        intent.putExtra("mode", "adjust_stock");
                        startActivity(intent);
                    }
                });
                listViewProducts.setAdapter(simpleAdapter);
            }

            // 提交（替换）列表以触发 Diff 更新
            simpleAdapter.submitList(productList);

            currentOffset += productList.size();
            if (productList.size() == PAGE_SIZE) {
                btnLoadMore.setVisibility(View.VISIBLE);
                btnLoadMore.setOnClickListener(v -> {
                    if (!loadingMore) loadNextPage();
                });
            } else {
                btnLoadMore.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 将分类编码转换为中文名称。
     */
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

    /**
     * 页面回到前台：刷新列表数据。
     *
     * <p>这里额外做 LayoutManager 兜底，避免 RecyclerView 未正确初始化导致异常。</p>
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 兜底：确保 RecyclerView 已设置 LayoutManager
        if (listViewProducts.getLayoutManager() == null) {
            listViewProducts.setLayoutManager(new LinearLayoutManager(this));
        }
        // 当从详情页面返回时，刷新列表
        loadProducts();
    }
}