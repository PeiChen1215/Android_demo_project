package com.example.android_development.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.model.Supplier;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 * 供应商相关 DAO。
 *
 * <p>当前主要用于读取供应商列表，供采购模块筛选/展示使用。</p>
 */
public class SupplierDAO {
    private SQLiteDatabase db;
    public SupplierDAO(SQLiteDatabase db) { this.db = db; }

    /**
     * 获取所有供应商。
     *
     * @return 供应商列表
     */
    public List<Supplier> getAllSuppliers() {
        List<Supplier> list = new ArrayList<>();
        Cursor c = db.query(Constants.TABLE_SUPPLIERS, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            do { list.add(Supplier.fromCursor(c)); } while (c.moveToNext());
            c.close();
        }
        return list;
    }
}
