package com.example.android_development.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.model.Supplier;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {
    private SQLiteDatabase db;
    public SupplierDAO(SQLiteDatabase db) { this.db = db; }

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
