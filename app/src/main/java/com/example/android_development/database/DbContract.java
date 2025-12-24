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
                    Constants.COLUMN_WAREHOUSE_STOCK + " INTEGER DEFAULT 0," +
                    Constants.COLUMN_MIN_STOCK + " INTEGER DEFAULT 0," +
                    Constants.COLUMN_MIN_WAREHOUSE_STOCK + " INTEGER DEFAULT 0," +
                    Constants.COLUMN_UNIT + " TEXT," +
                    Constants.COLUMN_PRODUCTION_DATE + " INTEGER," +
                    Constants.COLUMN_EXPIRATION_DATE + " INTEGER," +
                    Constants.COLUMN_BARCODE + " TEXT UNIQUE," +
                    Constants.COLUMN_DESCRIPTION + " TEXT," +
                    Constants.COLUMN_THUMB_URL + " TEXT," +
                    Constants.COLUMN_SUPPLIER_ID + " TEXT," +
                    Constants.COLUMN_CREATED_AT + " INTEGER," +
                    Constants.COLUMN_UPDATED_AT + " INTEGER" +
                    ")";

    // 商品表删除SQL
    public static final String SQL_DROP_TABLE_PRODUCTS =
            "DROP TABLE IF EXISTS " + Constants.TABLE_PRODUCTS;

    // 商品表索引（提升按名称/条码/分类的查询性能）
    public static final String SQL_CREATE_INDEX_PRODUCTS_NAME =
            "CREATE INDEX IF NOT EXISTS idx_products_name ON " + Constants.TABLE_PRODUCTS + "(" + Constants.COLUMN_PRODUCT_NAME + ")";

    public static final String SQL_CREATE_INDEX_PRODUCTS_BARCODE =
            "CREATE INDEX IF NOT EXISTS idx_products_barcode ON " + Constants.TABLE_PRODUCTS + "(" + Constants.COLUMN_BARCODE + ")";

    public static final String SQL_CREATE_INDEX_PRODUCTS_CATEGORY =
            "CREATE INDEX IF NOT EXISTS idx_products_category ON " + Constants.TABLE_PRODUCTS + "(" + Constants.COLUMN_CATEGORY + ")";

    // 库存事务表创建SQL
    public static final String SQL_CREATE_TABLE_STOCK_TRANSACTIONS =
            "CREATE TABLE " + Constants.TABLE_STOCK_TRANSACTIONS + " (" +
                    Constants.COLUMN_STOCK_TX_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_STOCK_TX_PRODUCT_ID + " TEXT NOT NULL," +
                    Constants.COLUMN_STOCK_TX_PRODUCT_NAME + " TEXT," +
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

    // 供应商表创建SQL
    public static final String SQL_CREATE_TABLE_SUPPLIERS =
            "CREATE TABLE " + Constants.TABLE_SUPPLIERS + " (" +
                    Constants.COLUMN_SUPPLIER_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL," +
                    Constants.COLUMN_SUPPLIER_CONTACT + " TEXT," +
                    Constants.COLUMN_SUPPLIER_PHONE + " TEXT," +
                    Constants.COLUMN_SUPPLIER_EMAIL + " TEXT" +
                    ")";

    public static final String SQL_DROP_TABLE_SUPPLIERS =
            "DROP TABLE IF EXISTS " + Constants.TABLE_SUPPLIERS;

    // 采购订单表创建SQL
    public static final String SQL_CREATE_TABLE_PURCHASE_ORDERS =
            "CREATE TABLE " + Constants.TABLE_PURCHASE_ORDERS + " (" +
                    Constants.COLUMN_PO_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_PO_SUPPLIER_ID + " TEXT," +
                    Constants.COLUMN_PO_NAME + " TEXT," +
                    Constants.COLUMN_PO_STATUS + " TEXT," +
                    Constants.COLUMN_PO_CREATED_AT + " INTEGER," +
                    Constants.COLUMN_PO_EXPECTED_AT + " INTEGER," +
                    Constants.COLUMN_PO_TOTAL + " REAL" +
                    ")";

    public static final String SQL_DROP_TABLE_PURCHASE_ORDERS =
            "DROP TABLE IF EXISTS " + Constants.TABLE_PURCHASE_ORDERS;

    // 采购订单行表
    public static final String SQL_CREATE_TABLE_PURCHASE_LINES =
            "CREATE TABLE " + Constants.TABLE_PURCHASE_LINES + " (" +
                    Constants.COLUMN_PO_LINE_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_PO_LINE_PO_ID + " TEXT NOT NULL," +
                    Constants.COLUMN_PO_LINE_PRODUCT_ID + " TEXT," +
                    Constants.COLUMN_PO_LINE_SKU + " TEXT," +
                    Constants.COLUMN_PO_LINE_QTY + " INTEGER," +
                    Constants.COLUMN_PO_LINE_PRICE + " REAL" +
                    ")";

    public static final String SQL_DROP_TABLE_PURCHASE_LINES =
            "DROP TABLE IF EXISTS " + Constants.TABLE_PURCHASE_LINES;

    // 采购审批表
    public static final String SQL_CREATE_TABLE_PO_APPROVALS =
            "CREATE TABLE " + Constants.TABLE_PO_APPROVALS + " (" +
                    Constants.COLUMN_PO_APPROVAL_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_PO_APPROVAL_PO_ID + " TEXT NOT NULL," +
                    Constants.COLUMN_PO_APPROVAL_APPROVER_ID + " TEXT," +
                    Constants.COLUMN_PO_APPROVAL_APPROVER_ROLE + " TEXT," +
                    Constants.COLUMN_PO_APPROVAL_DECISION + " TEXT," +
                    Constants.COLUMN_PO_APPROVAL_COMMENT + " TEXT," +
                    Constants.COLUMN_PO_APPROVAL_TIMESTAMP + " INTEGER" +
                    ")";

    public static final String SQL_DROP_TABLE_PO_APPROVALS =
            "DROP TABLE IF EXISTS " + Constants.TABLE_PO_APPROVALS;

    // 通用系统审计表
    public static final String SQL_CREATE_TABLE_SYSTEM_AUDIT =
            "CREATE TABLE " + Constants.TABLE_SYSTEM_AUDIT + " (" +
                    Constants.COLUMN_SYSTEM_AUDIT_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_SYSTEM_AUDIT_ENTITY + " TEXT," +
                    Constants.COLUMN_SYSTEM_AUDIT_ENTITY_ID + " TEXT," +
                    Constants.COLUMN_SYSTEM_AUDIT_ACTION + " TEXT," +
                    Constants.COLUMN_SYSTEM_AUDIT_USER_ID + " TEXT," +
                    Constants.COLUMN_SYSTEM_AUDIT_USER_ROLE + " TEXT," +
                    Constants.COLUMN_SYSTEM_AUDIT_DETAIL + " TEXT," +
                    Constants.COLUMN_SYSTEM_AUDIT_TIMESTAMP + " INTEGER" +
                    ")";

    public static final String SQL_DROP_TABLE_SYSTEM_AUDIT =
            "DROP TABLE IF EXISTS " + Constants.TABLE_SYSTEM_AUDIT;

    // 盘点表
    public static final String SQL_CREATE_TABLE_STOCK_COUNTS =
            "CREATE TABLE " + Constants.TABLE_STOCK_COUNTS + " (" +
                    Constants.COLUMN_STOCK_COUNT_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_STOCK_COUNT_STATUS + " TEXT," +
                    Constants.COLUMN_STOCK_COUNT_CREATED_BY + " TEXT," +
                    Constants.COLUMN_STOCK_COUNT_CREATED_AT + " INTEGER" +
                    ")";

    public static final String SQL_DROP_TABLE_STOCK_COUNTS =
            "DROP TABLE IF EXISTS " + Constants.TABLE_STOCK_COUNTS;

    // 盘点行表
    public static final String SQL_CREATE_TABLE_STOCK_COUNT_LINES =
            "CREATE TABLE " + Constants.TABLE_STOCK_COUNT_LINES + " (" +
                    Constants.COLUMN_STOCK_COUNT_LINE_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_STOCK_COUNT_LINE_COUNT_ID + " TEXT NOT NULL," +
                    Constants.COLUMN_STOCK_COUNT_LINE_PRODUCT_ID + " TEXT," +
                    Constants.COLUMN_STOCK_COUNT_LINE_SKU + " TEXT," +
                    Constants.COLUMN_STOCK_COUNT_LINE_EXPECTED_QTY + " INTEGER," +
                    Constants.COLUMN_STOCK_COUNT_LINE_COUNTED_QTY + " INTEGER" +
                    ")";

    public static final String SQL_DROP_TABLE_STOCK_COUNT_LINES =
            "DROP TABLE IF EXISTS " + Constants.TABLE_STOCK_COUNT_LINES;

    // 销售/收银表（收据）
    public static final String SQL_CREATE_TABLE_SALES =
            "CREATE TABLE " + Constants.TABLE_SALES + " (" +
                    Constants.COLUMN_SALE_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_SALE_TOTAL + " REAL NOT NULL," +
                    Constants.COLUMN_SALE_PAID + " REAL," +
                    Constants.COLUMN_SALE_PAYMENT_METHOD + " TEXT," +
                    Constants.COLUMN_SALE_USER_ID + " TEXT," +
                    Constants.COLUMN_SALE_TIMESTAMP + " INTEGER," +
                    Constants.COLUMN_SALE_REFUNDED + " INTEGER DEFAULT 0," +
                    Constants.COLUMN_SALE_REFUNDED_AT + " INTEGER" +
                    ")";

    public static final String SQL_CREATE_TABLE_SALE_LINES =
            "CREATE TABLE " + Constants.TABLE_SALE_LINES + " (" +
                    Constants.COLUMN_SALE_LINE_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_SALE_LINE_SALE_ID + " TEXT NOT NULL," +
                    Constants.COLUMN_SALE_LINE_PRODUCT_ID + " TEXT," +
                    Constants.COLUMN_SALE_LINE_PRODUCT_NAME + " TEXT," +
                    Constants.COLUMN_SALE_LINE_QTY + " INTEGER NOT NULL," +
                    Constants.COLUMN_SALE_LINE_PRICE + " REAL NOT NULL" +
                    ")";

    // 退款记录表
    public static final String SQL_CREATE_TABLE_REFUNDS =
            "CREATE TABLE " + Constants.TABLE_REFUNDS + " (" +
                    Constants.COLUMN_REFUND_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_REFUND_SALE_ID + " TEXT NOT NULL," +
                    Constants.COLUMN_REFUND_AMOUNT + " REAL NOT NULL," +
                    Constants.COLUMN_REFUND_USER_ID + " TEXT," +
                    Constants.COLUMN_REFUND_USER_ROLE + " TEXT," +
                    Constants.COLUMN_REFUND_REASON + " TEXT," +
                    Constants.COLUMN_REFUND_TIMESTAMP + " INTEGER" +
                    ")";

    public static final String SQL_DROP_TABLE_SALES =
            "DROP TABLE IF EXISTS " + Constants.TABLE_SALES;

    public static final String SQL_DROP_TABLE_SALE_LINES =
            "DROP TABLE IF EXISTS " + Constants.TABLE_SALE_LINES;
}