package com.example.android_development.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.model.PurchaseLine;
import com.example.android_development.model.PurchaseOrder;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PurchaseDAO {
    private SQLiteDatabase db;
    private InventoryDAO inventoryDAO;

    public PurchaseDAO(SQLiteDatabase db) {
        this.db = db;
        this.inventoryDAO = new InventoryDAO(db);
    }

    public long addPurchaseOrder(PurchaseOrder po) {
        if (po == null) return -1;
        if (po.getId() == null) po.setId(UUID.randomUUID().toString());
        ContentValues v = po.toContentValues();
        return db.insert(Constants.TABLE_PURCHASE_ORDERS, null, v);
    }

    public long addPurchaseLine(PurchaseLine line) {
        if (line == null) return -1;
        if (line.getId() == null) line.setId(UUID.randomUUID().toString());
        ContentValues v = line.toContentValues();
        long r = db.insert(Constants.TABLE_PURCHASE_LINES, null, v);
        return r;
    }

    public PurchaseOrder getPurchaseOrderById(String id) {
        if (id == null) return null;
        Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, null, Constants.COLUMN_PO_ID + " = ?", new String[]{id}, null, null, null);
        if (c != null && c.moveToFirst()) { PurchaseOrder p = PurchaseOrder.fromCursor(c); c.close(); return p; }
        return null;
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        List<PurchaseOrder> list = new ArrayList<>();
        Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, null, null, null, null, null, Constants.COLUMN_PO_CREATED_AT + " DESC");
        if (c != null && c.moveToFirst()) {
            do { list.add(PurchaseOrder.fromCursor(c)); } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    public List<PurchaseLine> getLinesForPo(String poId) {
        List<PurchaseLine> list = new ArrayList<>();
        if (poId == null) return list;
        Cursor c = db.query(Constants.TABLE_PURCHASE_LINES, null, Constants.COLUMN_PO_LINE_PO_ID + " = ?", new String[]{poId}, null, null, null);
        if (c != null && c.moveToFirst()) {
            do { list.add(PurchaseLine.fromCursor(c)); } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    public List<PurchaseOrder> getPendingPurchaseOrders() {
        List<PurchaseOrder> list = new ArrayList<>();
        Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, null, Constants.COLUMN_PO_STATUS + " = ?", new String[]{"pending"}, null, null, Constants.COLUMN_PO_CREATED_AT + " DESC");
        if (c != null && c.moveToFirst()) {
            do { list.add(PurchaseOrder.fromCursor(c)); } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    public int updatePurchaseLine(PurchaseLine line) {
        if (line == null || line.getId() == null) return 0;
        ContentValues v = line.toContentValues();
        v.remove(Constants.COLUMN_PO_LINE_ID);
        return db.update(Constants.TABLE_PURCHASE_LINES, v, Constants.COLUMN_PO_LINE_ID + " = ?", new String[]{line.getId()});
    }

    public int deletePurchaseLine(String lineId) {
        if (lineId == null) return 0;
        return db.delete(Constants.TABLE_PURCHASE_LINES, Constants.COLUMN_PO_LINE_ID + " = ?", new String[]{lineId});
    }

    public int updatePurchaseOrder(PurchaseOrder po) {
        if (po == null || po.getId() == null) return 0;
        ContentValues v = po.toContentValues();
        v.remove(Constants.COLUMN_PO_ID);
        return db.update(Constants.TABLE_PURCHASE_ORDERS, v, Constants.COLUMN_PO_ID + " = ?", new String[]{po.getId()});
    }

    // Transactional receive: for each line increase warehouse stock, then mark PO received
    public boolean receiveAndMatchPo(String poId) {
        if (poId == null) return false;
        db.beginTransaction();
        try {
            List<PurchaseLine> lines = getLinesForPo(poId);
            for (PurchaseLine l : lines) {
                boolean ok = inventoryDAO.receivePurchase(l.getProductId(), l.getQty());
                if (!ok) { db.endTransaction(); return false; }
            }
            ContentValues v = new ContentValues();
            v.put(Constants.COLUMN_PO_STATUS, "received");
            db.update(Constants.TABLE_PURCHASE_ORDERS, v, Constants.COLUMN_PO_ID + " = ?", new String[]{poId});
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; } finally { try { db.endTransaction(); } catch (Exception ignored) {} }
    }
}
