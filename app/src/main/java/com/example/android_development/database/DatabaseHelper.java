package com.example.android_development.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android_development.util.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        db.execSQL(DbContract.SQL_CREATE_TABLE_USERS);

        db.execSQL(DbContract.SQL_CREATE_TABLE_PRODUCTS);

        // 插入不同角色的测试用户
        insertTestUsers(db);

        // 插入测试商品
        insertTestProducts(db);
    }

    private void insertTestUsers(SQLiteDatabase db) {
        // 1. 管理员
        String adminId = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_USERS + " (" +
                Constants.COLUMN_USER_ID + ", " +
                Constants.COLUMN_USERNAME + ", " +
                Constants.COLUMN_PASSWORD + ", " +
                Constants.COLUMN_ROLE + ", " +
                Constants.COLUMN_FULL_NAME + ", " +
                Constants.COLUMN_CREATED_AT +
                ") VALUES ('" + adminId + "', 'admin', 'admin123', '" +
                Constants.ROLE_ADMIN + "', '系统管理员', " + System.currentTimeMillis() + ")");

        // 2. 采购员
        String buyerId = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_USERS + " (" +
                Constants.COLUMN_USER_ID + ", " +
                Constants.COLUMN_USERNAME + ", " +
                Constants.COLUMN_PASSWORD + ", " +
                Constants.COLUMN_ROLE + ", " +
                Constants.COLUMN_FULL_NAME + ", " +
                Constants.COLUMN_CREATED_AT +
                ") VALUES ('" + buyerId + "', 'buyer1', '123456', '" +
                Constants.ROLE_BUYER + "', '采购员张三', " + System.currentTimeMillis() + ")");

        // 3. 收银员
        String cashierId = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_USERS + " (" +
                Constants.COLUMN_USER_ID + ", " +
                Constants.COLUMN_USERNAME + ", " +
                Constants.COLUMN_PASSWORD + ", " +
                Constants.COLUMN_ROLE + ", " +
                Constants.COLUMN_FULL_NAME + ", " +
                Constants.COLUMN_CREATED_AT +
                ") VALUES ('" + cashierId + "', 'cashier1', '123456', '" +
                Constants.ROLE_CASHIER + "', '收银员李四', " + System.currentTimeMillis() + ")");

        // 4. 库存管理员
        String stockId = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_USERS + " (" +
                Constants.COLUMN_USER_ID + ", " +
                Constants.COLUMN_USERNAME + ", " +
                Constants.COLUMN_PASSWORD + ", " +
                Constants.COLUMN_ROLE + ", " +
                Constants.COLUMN_FULL_NAME + ", " +
                Constants.COLUMN_CREATED_AT +
                ") VALUES ('" + stockId + "', 'stock1', '123456', '" +
                Constants.ROLE_STOCK + "', '库存管理员王五', " + System.currentTimeMillis() + ")");

        // 5. 盘点员
        String inventoryId = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_USERS + " (" +
                Constants.COLUMN_USER_ID + ", " +
                Constants.COLUMN_USERNAME + ", " +
                Constants.COLUMN_PASSWORD + ", " +
                Constants.COLUMN_ROLE + ", " +
                Constants.COLUMN_FULL_NAME + ", " +
                Constants.COLUMN_CREATED_AT +
                ") VALUES ('" + inventoryId + "', 'inventory1', '123456', '" +
                Constants.ROLE_INVENTORY + "', '盘点员赵六', " + System.currentTimeMillis() + ")");
    }
    // 添加测试商品数据
    private void insertTestProducts(SQLiteDatabase db) {
        // 商品1：矿泉水
        String productId1 = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_PRODUCTS + " (" +
                Constants.COLUMN_PRODUCT_ID + ", " +
                Constants.COLUMN_PRODUCT_NAME + ", " +
                Constants.COLUMN_CATEGORY + ", " +
                Constants.COLUMN_BRAND + ", " +
                Constants.COLUMN_PRICE + ", " +
                Constants.COLUMN_COST + ", " +
                Constants.COLUMN_STOCK + ", " +
                Constants.COLUMN_MIN_STOCK + ", " +
                Constants.COLUMN_UNIT + ", " +
                Constants.COLUMN_BARCODE + ", " +
                Constants.COLUMN_DESCRIPTION + ", " +
                Constants.COLUMN_CREATED_AT + ", " +
                Constants.COLUMN_UPDATED_AT +
                ") VALUES ('" + productId1 + "', '矿泉水', '" +
                Constants.CATEGORY_DRINK + "', '农夫山泉', 2.0, 1.2, 100, 20, '瓶', '6901234567890', '500ml瓶装矿泉水', " +
                System.currentTimeMillis() + ", " + System.currentTimeMillis() + ")");

        // 商品2：方便面
        String productId2 = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_PRODUCTS + " (" +
                Constants.COLUMN_PRODUCT_ID + ", " +
                Constants.COLUMN_PRODUCT_NAME + ", " +
                Constants.COLUMN_CATEGORY + ", " +
                Constants.COLUMN_BRAND + ", " +
                Constants.COLUMN_PRICE + ", " +
                Constants.COLUMN_COST + ", " +
                Constants.COLUMN_STOCK + ", " +
                Constants.COLUMN_MIN_STOCK + ", " +
                Constants.COLUMN_UNIT + ", " +
                Constants.COLUMN_BARCODE + ", " +
                Constants.COLUMN_DESCRIPTION + ", " +
                Constants.COLUMN_CREATED_AT + ", " +
                Constants.COLUMN_UPDATED_AT +
                ") VALUES ('" + productId2 + "', '方便面', '" +
                Constants.CATEGORY_FOOD + "', '康师傅', 4.5, 2.8, 50, 15, '袋', '6912345678901', '红烧牛肉面120g', " +
                System.currentTimeMillis() + ", " + System.currentTimeMillis() + ")");

        // 商品3：纸巾
        String productId3 = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_PRODUCTS + " (" +
                Constants.COLUMN_PRODUCT_ID + ", " +
                Constants.COLUMN_PRODUCT_NAME + ", " +
                Constants.COLUMN_CATEGORY + ", " +
                Constants.COLUMN_BRAND + ", " +
                Constants.COLUMN_PRICE + ", " +
                Constants.COLUMN_COST + ", " +
                Constants.COLUMN_STOCK + ", " +
                Constants.COLUMN_MIN_STOCK + ", " +
                Constants.COLUMN_UNIT + ", " +
                Constants.COLUMN_BARCODE + ", " +
                Constants.COLUMN_DESCRIPTION + ", " +
                Constants.COLUMN_CREATED_AT + ", " +
                Constants.COLUMN_UPDATED_AT +
                ") VALUES ('" + productId3 + "', '纸巾', '" +
                Constants.CATEGORY_DAILY + "', '心相印', 8.0, 5.0, 30, 10, '包', '6923456789012', '200抽软抽纸巾', " +
                System.currentTimeMillis() + ", " + System.currentTimeMillis() + ")");

        // 商品4：可乐
        String productId4 = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_PRODUCTS + " (" +
                Constants.COLUMN_PRODUCT_ID + ", " +
                Constants.COLUMN_PRODUCT_NAME + ", " +
                Constants.COLUMN_CATEGORY + ", " +
                Constants.COLUMN_BRAND + ", " +
                Constants.COLUMN_PRICE + ", " +
                Constants.COLUMN_COST + ", " +
                Constants.COLUMN_STOCK + ", " +
                Constants.COLUMN_MIN_STOCK + ", " +
                Constants.COLUMN_UNIT + ", " +
                Constants.COLUMN_BARCODE + ", " +
                Constants.COLUMN_DESCRIPTION + ", " +
                Constants.COLUMN_CREATED_AT + ", " +
                Constants.COLUMN_UPDATED_AT +
                ") VALUES ('" + productId4 + "', '可乐', '" +
                Constants.CATEGORY_DRINK + "', '可口可乐', 3.0, 1.8, 80, 25, '瓶', '6934567890123', '500ml瓶装可乐', " +
                System.currentTimeMillis() + ", " + System.currentTimeMillis() + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级数据库时，删除旧表，创建新表
        db.execSQL(DbContract.SQL_DROP_TABLE_USERS);
        db.execSQL(DbContract.SQL_DROP_TABLE_PRODUCTS);
        onCreate(db);

        android.util.Log.d("DEBUG", "数据库从版本 " + oldVersion + " 升级到 " + newVersion);
    }
}