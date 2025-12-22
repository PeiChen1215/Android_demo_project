package com.example.android_development.model;

import android.database.Cursor;
import com.example.android_development.util.Constants;

public class StockTransaction {
    private String id;
    private String productId;
    private String productName;
    private String userId;
    private String userRole;
    private String type; // IN or OUT
    private int quantity;
    private int stockBefore;
    private int stockAfter;
    private String reason;
    private long timestamp;

    public StockTransaction() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getStockBefore() { return stockBefore; }
    public void setStockBefore(int stockBefore) { this.stockBefore = stockBefore; }
    public int getStockAfter() { return stockAfter; }
    public void setStockAfter(int stockAfter) { this.stockAfter = stockAfter; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public static StockTransaction fromCursor(Cursor c) {
        StockTransaction tx = new StockTransaction();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_ID);
        if (idx != -1) tx.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_PRODUCT_ID);
        if (idx != -1) tx.setProductId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_PRODUCT_NAME);
        if (idx != -1) tx.setProductName(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_USER_ID);
        if (idx != -1) tx.setUserId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_USER_ROLE);
        if (idx != -1) tx.setUserRole(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_TYPE);
        if (idx != -1) tx.setType(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_QUANTITY);
        if (idx != -1) tx.setQuantity(c.getInt(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_BEFORE);
        if (idx != -1) tx.setStockBefore(c.getInt(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_AFTER);
        if (idx != -1) tx.setStockAfter(c.getInt(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_REASON);
        if (idx != -1) tx.setReason(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_TX_TIMESTAMP);
        if (idx != -1) tx.setTimestamp(c.getLong(idx));
        return tx;
    }
}
