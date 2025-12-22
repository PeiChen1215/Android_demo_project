package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

public class PurchaseLine {
    private String id;
    private String poId;
    private String productId;
    private String sku;
    private int qty;
    private double price;

    public PurchaseLine() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public ContentValues toContentValues() {
        ContentValues v = new ContentValues();
        if (id == null) id = java.util.UUID.randomUUID().toString();
        v.put(Constants.COLUMN_PO_LINE_ID, id);
        v.put(Constants.COLUMN_PO_LINE_PO_ID, poId);
        v.put(Constants.COLUMN_PO_LINE_PRODUCT_ID, productId);
        v.put(Constants.COLUMN_PO_LINE_SKU, sku);
        v.put(Constants.COLUMN_PO_LINE_QTY, qty);
        v.put(Constants.COLUMN_PO_LINE_PRICE, price);
        return v;
    }

    public static PurchaseLine fromCursor(Cursor c) {
        if (c == null) return null;
        PurchaseLine l = new PurchaseLine();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_PO_LINE_ID);
        if (idx != -1) l.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_LINE_PO_ID);
        if (idx != -1) l.setPoId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_LINE_PRODUCT_ID);
        if (idx != -1) l.setProductId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_LINE_SKU);
        if (idx != -1) l.setSku(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_LINE_QTY);
        if (idx != -1) l.setQty(c.getInt(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_LINE_PRICE);
        if (idx != -1) l.setPrice(c.getDouble(idx));
        return l;
    }
}
