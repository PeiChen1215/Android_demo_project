package com.example.android_development.util;

public class Constants {
    // 数据库常量
    public static final String DATABASE_NAME = "supermarket.db";
    public static final int DATABASE_VERSION = 3;

    // 用户表
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_CREATED_AT = "created_at";

    // SharedPreferences 常量
    public static final String PREFS_NAME = "supermarket_prefs";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_ROLE = "user_role";

    // 用户角色
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CASHIER = "cashier";
    public static final String ROLE_STOCK = "stock";
    public static final String ROLE_BUYER = "buyer";          // 采购员（采购子系统）
    public static final String ROLE_INVENTORY = "inventory";  // 盘点员（盘点子系统）

    // 商品表
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_PRODUCT_NAME = "product_name";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_BRAND = "brand";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_COST = "cost";  // 成本价
    public static final String COLUMN_STOCK = "stock";
    public static final String COLUMN_MIN_STOCK = "min_stock";  // 最低库存预警
    public static final String COLUMN_UNIT = "unit";  // 单位（如：瓶、袋、个）
    public static final String COLUMN_BARCODE = "barcode";  // 条码
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_SUPPLIER_ID = "supplier_id";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // 商品分类
    public static final String CATEGORY_DAILY = "daily";      // 日用品
    public static final String CATEGORY_FOOD = "food";        // 食品
    public static final String CATEGORY_DRINK = "drink";      // 饮料
    public static final String CATEGORY_SNACK = "snack";      // 零食
    public static final String CATEGORY_CLEANING = "cleaning"; // 清洁用品
    public static final String CATEGORY_OTHER = "other";      // 其他

    // 库存事务表
    public static final String TABLE_STOCK_TRANSACTIONS = "stock_transactions";
    public static final String COLUMN_STOCK_TX_ID = "tx_id";
    public static final String COLUMN_STOCK_TX_PRODUCT_ID = "product_id";
    public static final String COLUMN_STOCK_TX_USER_ID = "user_id";
    public static final String COLUMN_STOCK_TX_USER_ROLE = "user_role";
    public static final String COLUMN_STOCK_TX_TYPE = "tx_type"; // IN/OUT
    public static final String COLUMN_STOCK_TX_QUANTITY = "quantity";
    public static final String COLUMN_STOCK_TX_BEFORE = "stock_before";
    public static final String COLUMN_STOCK_TX_AFTER = "stock_after";
    public static final String COLUMN_STOCK_TX_REASON = "reason";
    public static final String COLUMN_STOCK_TX_TIMESTAMP = "timestamp";
}