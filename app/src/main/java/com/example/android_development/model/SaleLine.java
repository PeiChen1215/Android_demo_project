package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

/**
 * 销售单明细行实体。
 * <p>
 * 对应一次销售中的某一条商品：商品ID/名称、数量、成交价等。
 * </p>
 */
public class SaleLine {
    private String id;
    private String saleId;
    private String productId;
    private String productName;
    private int qty;
    private double price;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSaleId() { return saleId; }
    public void setSaleId(String saleId) { this.saleId = saleId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    /**
     * 转换为 ContentValues（用于插入/更新销售明细表）。
     *
     * @return ContentValues
     */
    public ContentValues toContentValues() {
        ContentValues v = new ContentValues();
        if (id != null) v.put(Constants.COLUMN_SALE_LINE_ID, id);
        v.put(Constants.COLUMN_SALE_LINE_SALE_ID, saleId);
        v.put(Constants.COLUMN_SALE_LINE_PRODUCT_ID, productId);
        v.put(Constants.COLUMN_SALE_LINE_PRODUCT_NAME, productName);
        v.put(Constants.COLUMN_SALE_LINE_QTY, qty);
        v.put(Constants.COLUMN_SALE_LINE_PRICE, price);
        return v;
    }

    /**
     * 从查询游标构建销售明细对象。
     *
     * @param c 数据库游标
     * @return 明细对象；c 为 null 时返回 null
     */
    public static SaleLine fromCursor(Cursor c) {
        if (c == null) return null;
        SaleLine l = new SaleLine();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_SALE_LINE_ID); if (idx != -1) l.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_LINE_SALE_ID); if (idx != -1) l.setSaleId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_LINE_PRODUCT_ID); if (idx != -1) l.setProductId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_LINE_PRODUCT_NAME); if (idx != -1) l.setProductName(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_LINE_QTY); if (idx != -1) l.setQty(c.getInt(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_LINE_PRICE); if (idx != -1) l.setPrice(c.getDouble(idx));
        return l;
    }
}
