package com.example.android_development.database;

import android.content.ContentValues;
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

    public SaleDAO(SQLiteDatabase db) { this.db = db; }

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

                    // Update Shelf Stock (Real-time deduction)
                    db.execSQL("UPDATE " + Constants.TABLE_PRODUCTS + 
                               " SET " + Constants.COLUMN_STOCK + " = " + Constants.COLUMN_STOCK + " - " + l.getQty() + 
                               " WHERE " + Constants.COLUMN_PRODUCT_ID + " = ?", new Object[]{l.getProductId()});
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
