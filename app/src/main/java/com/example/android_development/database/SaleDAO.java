package com.example.android_development.database;

import android.content.ContentValues;
import android.content.Context;
import com.example.android_development.util.PrefsManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.model.Sale;
import com.example.android_development.model.SaleLine;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SaleDAO {
    private SQLiteDatabase db;
    private PrefsManager prefsManager;

    public SaleDAO(SQLiteDatabase db) { this.db = db; }

    public SaleDAO(SQLiteDatabase db, Context ctx) {
        this.db = db;
        if (ctx != null) this.prefsManager = new PrefsManager(ctx);
    }

    public long addSale(Sale sale) {
        if (sale == null) return -1;
        db.beginTransaction();
        try {
            if (sale.getId() == null || sale.getId().isEmpty()) sale.setId(UUID.randomUUID().toString());
            long now = System.currentTimeMillis();
            sale.setTimestamp(now);

            ContentValues v = sale.toContentValues();
            long res = db.insert(Constants.TABLE_SALES, null, v);
            if (res == -1) throw new Exception("Failed to insert sale");

            // insert lines
            if (sale.getLines() != null) {
                for (SaleLine l : sale.getLines()) {
                    if (l.getId() == null || l.getId().isEmpty()) l.setId(UUID.randomUUID().toString());
                    l.setSaleId(sale.getId());
                    db.insert(Constants.TABLE_SALE_LINES, null, l.toContentValues());

                    // Update Shelf Stock (Real-time deduction) and write stock transaction (use before/after)
                    int beforeStock = 0;
                    Cursor pc = db.rawQuery("SELECT " + Constants.COLUMN_STOCK + " FROM " + Constants.TABLE_PRODUCTS + " WHERE " + Constants.COLUMN_PRODUCT_ID + " = ?", new String[]{l.getProductId()});
                    if (pc != null) {
                        if (pc.moveToFirst()) {
                            beforeStock = pc.getInt(0);
                        }
                        pc.close();
                    }
                    int afterStock = beforeStock - l.getQty();
                    if (afterStock < 0) afterStock = 0;
                    ContentValues pv = new ContentValues();
                    pv.put(Constants.COLUMN_STOCK, afterStock);
                    db.update(Constants.TABLE_PRODUCTS, pv, Constants.COLUMN_PRODUCT_ID + " = ?", new String[]{l.getProductId()});

                    // insert stock transaction audit
                    try {
                        ContentValues tx = new ContentValues();
                        tx.put(Constants.COLUMN_STOCK_TX_ID, UUID.randomUUID().toString());
                        tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_ID, l.getProductId());
                        tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_NAME, (String) null);
                        String uid = null;
                        String urole = null;
                        if (prefsManager != null) {
                            try { uid = prefsManager.getUserId(); } catch (Exception ignored) {}
                            try { urole = prefsManager.getUserRole(); } catch (Exception ignored) {}
                        }
                        tx.put(Constants.COLUMN_STOCK_TX_USER_ID, uid);
                        tx.put(Constants.COLUMN_STOCK_TX_USER_ROLE, urole);
                        tx.put(Constants.COLUMN_STOCK_TX_TYPE, "OUT");
                        tx.put(Constants.COLUMN_STOCK_TX_QUANTITY, l.getQty());
                        tx.put(Constants.COLUMN_STOCK_TX_BEFORE, beforeStock);
                        tx.put(Constants.COLUMN_STOCK_TX_AFTER, afterStock);
                        tx.put(Constants.COLUMN_STOCK_TX_REASON, "sale");
                        tx.put(Constants.COLUMN_STOCK_TX_TIMESTAMP, System.currentTimeMillis());
                        try { db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, tx); } catch (Exception ignored) {}
                    } catch (Exception ignored) {}
                }
            }
            db.setTransactionSuccessful();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    public Sale getSaleById(String saleId) {
        String sel = Constants.COLUMN_SALE_ID + " = ?";
        String[] args = new String[]{saleId};
        Cursor c = db.query(Constants.TABLE_SALES, null, sel, args, null, null, null);
        Sale s = null;
        if (c != null) {
            if (c.moveToFirst()) s = Sale.fromCursor(c);
            c.close();
        }
        if (s != null) {
            s.setLines(getLinesForSale(s.getId()));
        }
        return s;
    }

    public List<SaleLine> getLinesForSale(String saleId) {
        List<SaleLine> list = new ArrayList<>();
        String sel = Constants.COLUMN_SALE_LINE_SALE_ID + " = ?";
        String[] args = new String[]{saleId};
        Cursor c = db.query(Constants.TABLE_SALE_LINES, null, sel, args, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(SaleLine.fromCursor(c));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    public List<Sale> getRecentSales(int limit) {
        List<Sale> list = new ArrayList<>();
        String order = Constants.COLUMN_SALE_TIMESTAMP + " DESC";
        String lim = String.valueOf(limit);
        Cursor c = db.query(Constants.TABLE_SALES, null, null, null, null, null, order, lim);
        if (c != null && c.moveToFirst()) {
            do {
                Sale s = Sale.fromCursor(c);
                s.setLines(getLinesForSale(s.getId()));
                list.add(s);
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }
}
