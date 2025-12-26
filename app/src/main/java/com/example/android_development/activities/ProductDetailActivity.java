package com.example.android_development.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.model.Product;
import com.example.android_development.util.Constants;
import com.example.android_development.util.PrefsManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 商品详情页面。
 *
 * <p>展示商品基本信息（分类/价格/货架库存/仓库库存/条码/品牌/单位/描述/过期日期等），
 * 并根据当前用户角色显示“编辑商品/库存管理/查看库存历史”等入口。</p>
 *
 * <p>页面支持输入缩略图 URL 并进行防抖预览（使用 Glide 加载）。</p>
 */
public class ProductDetailActivity extends AppCompatActivity {

    private TextView textViewProductName;
    private TextView textViewProductCategory;
    private TextView textViewProductPrice;
    private TextView textViewProductStock;
    private TextView textViewWarehouseStock; // 新增：仓库库存
    private TextView textViewProductBrand;
    private TextView textViewProductUnit;
    private TextView textViewProductBarcode;
    private TextView textViewProductDescription;
    private TextView textViewProductExpiry; // 新增：到期日

    private EditText editTextThumbUrl;
    private ImageView imageViewThumbPreview;

    private Button buttonBack;
    private Button buttonEdit;
    private Button buttonHistory;

    private ProductDAO productDAO;
    private Product currentProduct;
    private PrefsManager prefsManager;
    private String currentUserRole;

    private Handler previewHandler;
    private Runnable previewRunnable;
    private final int PREVIEW_DELAY_MS = 600;
    private ExecutorService networkExecutor = Executors.newSingleThreadExecutor();

    /**
     * Activity 创建：初始化视图、权限相关 UI、数据库对象与点击事件，并加载商品数据。
     */
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

    /**
     * 初始化控件引用。
     */
    private void initViews() {
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductCategory = findViewById(R.id.textViewProductCategory);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        textViewProductStock = findViewById(R.id.textViewProductStock);
        textViewWarehouseStock = findViewById(R.id.textViewWarehouseStock); // 新增：仓库库存
        textViewProductBrand = findViewById(R.id.textViewProductBrand);
        textViewProductUnit = findViewById(R.id.textViewProductUnit);
        textViewProductBarcode = findViewById(R.id.textViewProductBarcode);
        textViewProductDescription = findViewById(R.id.textViewProductDescription);
        textViewProductExpiry = findViewById(R.id.textViewProductExpiry); // 新增：到期日

        buttonBack = findViewById(R.id.buttonBack);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonEdit = findViewById(R.id.buttonEdit);
        // 缩略图 URL 输入 + 预览
        editTextThumbUrl = findViewById(R.id.editTextThumbUrl);
        imageViewThumbPreview = findViewById(R.id.imageViewThumbPreview);
    }

    /**
     * 根据当前用户角色设置页面可操作入口（编辑/库存管理/历史）。
     */
    private void setupUIByRole() {
        // 根据角色设置编辑按钮
        if (Constants.ROLE_ADMIN.equals(currentUserRole) ||
                Constants.ROLE_STOCK.equals(currentUserRole)) {
            buttonEdit.setVisibility(android.view.View.VISIBLE);
            buttonHistory.setVisibility(android.view.View.VISIBLE);
            if (Constants.ROLE_ADMIN.equals(currentUserRole)) {
                buttonEdit.setText(getString(R.string.edit_product));
            } else if (Constants.ROLE_STOCK.equals(currentUserRole)) {
                buttonEdit.setText(getString(R.string.manage_stock));
            }
        } else {
            buttonEdit.setVisibility(android.view.View.GONE);
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
     * 绑定页面按钮事件：返回、编辑/库存管理、查看库存历史。
     */
    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());

        buttonEdit.setOnClickListener(v -> {
            if (Constants.ROLE_ADMIN.equals(currentUserRole)) {
                Toast.makeText(this, "管理员：进入商品编辑功能", Toast.LENGTH_SHORT).show();
                // 跳转到商品编辑页面（复用添加页面的编辑模式）
                Intent intent = new Intent(ProductDetailActivity.this, ProductAddActivity.class);
                intent.putExtra("product_id", currentProduct.getId());
                startActivity(intent);
            } else if (Constants.ROLE_STOCK.equals(currentUserRole)) {
                Toast.makeText(this, "库存管理员：进入库存管理功能", Toast.LENGTH_SHORT).show();
                // 跳转到库存调整页面
                Intent intent = new Intent(ProductDetailActivity.this, StockAdjustActivity.class);
                intent.putExtra("product_id", currentProduct.getId());
                startActivity(intent);
            }
        });

        buttonHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, StockHistoryActivity.class);
            intent.putExtra("product_id", currentProduct.getId());
            startActivity(intent);
        });
    }

    /**
     * 加载商品数据：从 Intent 获取 product_id 并查询商品，成功后渲染页面。
     */
    private void loadProductData() {
        // 获取传递过来的商品ID
        String productId = getIntent().getStringExtra("product_id");

        if (productId != null) {
            currentProduct = productDAO.getProductById(productId);

            if (currentProduct != null) {
                displayProductInfo();
            } else {
                Toast.makeText(this, getString(R.string.product_not_found), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, getString(R.string.invalid_product_id), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Activity 可见时：初始化缩略图 URL 预览的防抖逻辑（输入停止后延迟加载预览）。
     */
    @Override
    protected void onStart() {
        super.onStart();
        // 初始化预览 handler 和 runnable
        previewHandler = new Handler(Looper.getMainLooper());
        previewRunnable = new Runnable() {
            @Override
            public void run() {
                String url = editTextThumbUrl.getText() != null ? editTextThumbUrl.getText().toString().trim() : "";
                if (isValidImageUrl(url)) {
                    Glide.with(ProductDetailActivity.this)
                            .load(url)
                            .centerCrop()
                            .into(imageViewThumbPreview);
                } else {
                    imageViewThumbPreview.setImageDrawable(null);
                }
            }
        };

        // TextWatcher: 防抖处理，输入停止后进行预览
        if (editTextThumbUrl != null) {
            editTextThumbUrl.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (previewHandler != null && previewRunnable != null) {
                        previewHandler.removeCallbacks(previewRunnable);
                        previewHandler.postDelayed(previewRunnable, PREVIEW_DELAY_MS);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }
    }

    /**
     * 将商品信息渲染到界面。
     *
     * <p>当库存低于预警值时，会高亮库存文本并追加提示。</p>
     */
    private void displayProductInfo() {
        textViewProductName.setText(currentProduct.getName());
        textViewProductCategory.setText(getString(R.string.label_category, getCategoryName(currentProduct.getCategory())));
        textViewProductPrice.setText(String.format(getString(R.string.label_price), currentProduct.getPrice()));
        textViewProductStock.setText(getString(R.string.label_stock, currentProduct.getStock()));
        if (textViewWarehouseStock != null) {
            textViewWarehouseStock.setText("仓库库存: " + currentProduct.getWarehouseStock());
        }
        textViewProductBrand.setText(getString(R.string.label_brand, (currentProduct.getBrand() != null ? currentProduct.getBrand() : "未设置")));
        textViewProductUnit.setText(getString(R.string.label_unit, (currentProduct.getUnit() != null ? currentProduct.getUnit() : "未设置")));
        textViewProductBarcode.setText(getString(R.string.label_barcode, (currentProduct.getBarcode() != null ? currentProduct.getBarcode() : "未设置")));
        textViewProductDescription.setText(getString(R.string.label_description, (currentProduct.getDescription() != null ? currentProduct.getDescription() : "无")));
        
        if (textViewProductExpiry != null) {
            String expiry = currentProduct.getExpirationDate() > 0 ? 
                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(currentProduct.getExpirationDate())) : "未设置";
            textViewProductExpiry.setText("过期日期: " + expiry);
        }

        // 如果库存低于预警值，显示提醒
        if (currentProduct.isLowStock()) {
            textViewProductStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            textViewProductStock.append(" " + getString(R.string.low_stock_suffix));
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
     * 校验是否为可用于图片预览的 URL。
     *
     * <p>规则：必须是 http/https 且以常见图片后缀结尾。</p>
     */
    private boolean isValidImageUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        if (!Patterns.WEB_URL.matcher(url).matches()) return false;
        String lower = url.toLowerCase();
        if (!(lower.startsWith("http://") || lower.startsWith("https://"))) return false;
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp") || lower.endsWith(".svg");
    }
}