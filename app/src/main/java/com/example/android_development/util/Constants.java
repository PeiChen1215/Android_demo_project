package com.example.android_development.util;

public class Constants {
    // 数据库常量
    public static final String DATABASE_NAME = "supermarket.db";
    public static final int DATABASE_VERSION = 1;

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
}