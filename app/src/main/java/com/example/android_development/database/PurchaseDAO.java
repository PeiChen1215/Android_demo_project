package com.example.android_development.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.model.PurchaseOrder;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {
    private SQLiteDatabase db;

    public PurchaseDAO(SQLiteDatabase db) { this.db = db; }

    public long addPurchaseOrder(PurchaseOrder po) {
        if (po == null) return -1;
        ContentValues v = po.toContentValues();
        return db.insert(Constants.TABLE_PURCHASE_ORDERS, null, v);
    }

    public PurchaseOrder getPurchaseOrderById(String id) {
        if (id == null) return null;
        String sel = Constants.COLUMN_PO_ID + " = ?";
        String[] args = new String[]{id};
        Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, null, sel, args, null, null, null);
        PurchaseOrder po = null;
        if (c != null && c.moveToFirst()) {
            po = PurchaseOrder.fromCursor(c);
            c.close();
        }
        return po;
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        List<PurchaseOrder> list = new ArrayList<>();
        Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, null, null, null, null, null, Constants.COLUMN_PO_CREATED_AT + " DESC");
        if (c != null && c.moveToFirst()) {
            do {
                list.add(PurchaseOrder.fromCursor(c));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }
}
