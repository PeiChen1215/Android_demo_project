package com.example.android_development.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.model.StockCount;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    private SQLiteDatabase db;
    public InventoryDAO(SQLiteDatabase db) { this.db = db; }

    public long createStockCount(StockCount sc) {
        if (sc == null) return -1;
        ContentValues v = sc.toContentValues();
        return db.insert(Constants.TABLE_STOCK_COUNTS, null, v);
    }

    public StockCount getStockCountById(String id) {
        if (id == null) return null;
        Cursor c = db.query(Constants.TABLE_STOCK_COUNTS, null, Constants.COLUMN_STOCK_COUNT_ID + " = ?", new String[]{id}, null, null, null);
        if (c != null && c.moveToFirst()) {
            StockCount sc = StockCount.fromCursor(c);
            c.close();
            return sc;
        }
        return null;
    }

    public List<StockCount> getAllStockCounts() {
        List<StockCount> list = new ArrayList<>();
        Cursor c = db.query(Constants.TABLE_STOCK_COUNTS, null, null, null, null, null, Constants.COLUMN_STOCK_COUNT_CREATED_AT + " DESC");
        if (c != null && c.moveToFirst()) {
            do {
                list.add(StockCount.fromCursor(c));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    // 内部补货：仓库 -> 货架
    public boolean restockShelf(String productId, int qty) {
        db.beginTransaction();
        try {
            // 1. 检查仓库库存是否足够
            Cursor c = db.rawQuery("SELECT " + Constants.COLUMN_WAREHOUSE_STOCK + " FROM " + Constants.TABLE_PRODUCTS + " WHERE " + Constants.COLUMN_PRODUCT_ID + " = ?", new String[]{productId});
            if (c != null && c.moveToFirst()) {
                int currentWarehouse = c.getInt(0);
                c.close();
                if (currentWarehouse < qty) return false; // 库存不足
            } else {
                if (c != null) c.close();
                return false;
            }

            // 2. 扣减仓库库存
            db.execSQL("UPDATE " + Constants.TABLE_PRODUCTS + 
                       " SET " + Constants.COLUMN_WAREHOUSE_STOCK + " = " + Constants.COLUMN_WAREHOUSE_STOCK + " - " + qty + 
                       " WHERE " + Constants.COLUMN_PRODUCT_ID + " = ?", new Object[]{productId});

            // 3. 增加货架库存
            db.execSQL("UPDATE " + Constants.TABLE_PRODUCTS + 
                       " SET " + Constants.COLUMN_STOCK + " = " + Constants.COLUMN_STOCK + " + " + qty + 
                       " WHERE " + Constants.COLUMN_PRODUCT_ID + " = ?", new Object[]{productId});

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // 外部采购入库：供应商 -> 仓库
    public boolean receivePurchase(String productId, int qty) {
        if (productId == null) return false;
        try {
            // 1. 查询当前仓库库存
            Cursor c = db.rawQuery("SELECT " + Constants.COLUMN_WAREHOUSE_STOCK + " FROM " + Constants.TABLE_PRODUCTS + " WHERE " + Constants.COLUMN_PRODUCT_ID + " = ?", new String[]{productId});
            if (c == null) return false;
            try {
                if (!c.moveToFirst()) return false; // product not found
                int current = c.getInt(0);
                int updated = current + qty;
                android.content.ContentValues v = new android.content.ContentValues();
                v.put(Constants.COLUMN_WAREHOUSE_STOCK, updated);
                int rows = db.update(Constants.TABLE_PRODUCTS, v, Constants.COLUMN_PRODUCT_ID + " = ?", new String[]{productId});
                return rows > 0;
            } finally {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
