package com.example.android_development.model;

import android.database.Cursor;
import com.example.android_development.util.Constants;

public class StockTransaction {
    private String id;
    private String productId;
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
        tx.setId(c.getString(c.getColumnIndexOrThrow(Constants.COLUMN_STOCK_TX_ID)));
        tx.setProductId(c.getString(c.getColumnIndexOrThrow(Constants.COLUMN_STOCK_TX_PRODUCT_ID)));
        tx.setUserId(c.getString(c.getColumnIndexOrThrow(Constants.COLUMN_STOCK_TX_USER_ID)));
        tx.setType(c.getString(c.getColumnIndexOrThrow(Constants.COLUMN_STOCK_TX_TYPE)));
        tx.setQuantity(c.getInt(c.getColumnIndexOrThrow(Constants.COLUMN_STOCK_TX_QUANTITY)));
        tx.setStockBefore(c.getInt(c.getColumnIndexOrThrow(Constants.COLUMN_STOCK_TX_BEFORE)));
        tx.setStockAfter(c.getInt(c.getColumnIndexOrThrow(Constants.COLUMN_STOCK_TX_AFTER)));
        tx.setReason(c.getString(c.getColumnIndexOrThrow(Constants.COLUMN_STOCK_TX_REASON)));
        int idxRole = c.getColumnIndex(Constants.COLUMN_STOCK_TX_USER_ROLE);
        if (idxRole != -1) tx.setUserRole(c.getString(idxRole));
        tx.setTimestamp(c.getLong(c.getColumnIndexOrThrow(Constants.COLUMN_STOCK_TX_TIMESTAMP)));
        return tx;
    }
}
