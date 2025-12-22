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
}
