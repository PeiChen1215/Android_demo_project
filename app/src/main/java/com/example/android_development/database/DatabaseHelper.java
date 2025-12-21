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

        // 插入不同角色的测试用户
        insertTestUsers(db);
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 删除所有表
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USERS);
        // 重新创建表
        onCreate(db);
        android.util.Log.d("DEBUG", "数据库从版本 " + oldVersion + " 升级到 " + newVersion);
    }
}