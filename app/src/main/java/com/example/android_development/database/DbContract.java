package com.example.android_development.database;

import com.example.android_development.util.Constants;

public class DbContract {

    // 用户表创建SQL
    public static final String SQL_CREATE_TABLE_USERS =
            "CREATE TABLE " + Constants.TABLE_USERS + " (" +
                    Constants.COLUMN_USER_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_USERNAME + " TEXT UNIQUE NOT NULL," +
                    Constants.COLUMN_PASSWORD + " TEXT NOT NULL," +
                    Constants.COLUMN_ROLE + " TEXT NOT NULL," +
                    Constants.COLUMN_FULL_NAME + " TEXT," +
                    Constants.COLUMN_PHONE + " TEXT," +
                    Constants.COLUMN_EMAIL + " TEXT," +
                    Constants.COLUMN_CREATED_AT + " INTEGER" +
                    ")";

    // 用户表删除SQL
    public static final String SQL_DROP_TABLE_USERS =
            "DROP TABLE IF EXISTS " + Constants.TABLE_USERS;

    // 商品表创建SQL
    public static final String SQL_CREATE_TABLE_PRODUCTS =
            "CREATE TABLE " + Constants.TABLE_PRODUCTS + " (" +
                    Constants.COLUMN_PRODUCT_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                    Constants.COLUMN_CATEGORY + " TEXT," +
                    Constants.COLUMN_BRAND + " TEXT," +
                    Constants.COLUMN_PRICE + " REAL NOT NULL," +
                    Constants.COLUMN_COST + " REAL," +
                    Constants.COLUMN_STOCK + " INTEGER DEFAULT 0," +
                    Constants.COLUMN_MIN_STOCK + " INTEGER DEFAULT 0," +
                    Constants.COLUMN_UNIT + " TEXT," +
                    Constants.COLUMN_BARCODE + " TEXT UNIQUE," +
                    Constants.COLUMN_DESCRIPTION + " TEXT," +
                    Constants.COLUMN_SUPPLIER_ID + " TEXT," +
                    Constants.COLUMN_CREATED_AT + " INTEGER," +
                    Constants.COLUMN_UPDATED_AT + " INTEGER" +
                    ")";

    // 商品表删除SQL
    public static final String SQL_DROP_TABLE_PRODUCTS =
            "DROP TABLE IF EXISTS " + Constants.TABLE_PRODUCTS;

    // 库存事务表创建SQL
    public static final String SQL_CREATE_TABLE_STOCK_TRANSACTIONS =
            "CREATE TABLE " + Constants.TABLE_STOCK_TRANSACTIONS + " (" +
                    Constants.COLUMN_STOCK_TX_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_STOCK_TX_PRODUCT_ID + " TEXT NOT NULL," +
                    Constants.COLUMN_STOCK_TX_USER_ID + " TEXT," +
                    Constants.COLUMN_STOCK_TX_USER_ROLE + " TEXT," +
                    Constants.COLUMN_STOCK_TX_TYPE + " TEXT NOT NULL," +
                    Constants.COLUMN_STOCK_TX_QUANTITY + " INTEGER NOT NULL," +
                    Constants.COLUMN_STOCK_TX_BEFORE + " INTEGER," +
                    Constants.COLUMN_STOCK_TX_AFTER + " INTEGER," +
                    Constants.COLUMN_STOCK_TX_REASON + " TEXT," +
                    Constants.COLUMN_STOCK_TX_TIMESTAMP + " INTEGER" +
                    ")";

    // 库存事务表删除SQL
    public static final String SQL_DROP_TABLE_STOCK_TRANSACTIONS =
            "DROP TABLE IF EXISTS " + Constants.TABLE_STOCK_TRANSACTIONS;
}