package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

/**
 * 采购单明细行实体。
 * <p>
 * 对应采购单中的某一条商品：商品ID/SKU、采购数量、采购单价等。
 * </p>
 */
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

    /**
     * 转换为 ContentValues（用于插入/更新采购单明细表）。
     * <p>
     * 当 id 为空时会自动生成 UUID。
     * </p>
     *
     * @return ContentValues
     */
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

    /**
     * 从查询游标构建采购单明细对象。
     *
     * @param c 数据库游标
     * @return 明细对象；c 为 null 时返回 null
     */
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
