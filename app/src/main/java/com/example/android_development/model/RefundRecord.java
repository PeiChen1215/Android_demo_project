package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

public class RefundRecord {
    private String id;
    private String saleId;
    private double amount;
    private String userId;
    private String userRole;
    private String reason;
    private long timestamp;

    public RefundRecord() { this.timestamp = System.currentTimeMillis(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSaleId() { return saleId; }
    public void setSaleId(String saleId) { this.saleId = saleId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public ContentValues toContentValues() {
        ContentValues v = new ContentValues();
        if (id != null) v.put(Constants.COLUMN_REFUND_ID, id);
        v.put(Constants.COLUMN_REFUND_SALE_ID, saleId);
        v.put(Constants.COLUMN_REFUND_AMOUNT, amount);
        v.put(Constants.COLUMN_REFUND_USER_ID, userId);
        v.put(Constants.COLUMN_REFUND_USER_ROLE, userRole);
        v.put(Constants.COLUMN_REFUND_REASON, reason);
        v.put(Constants.COLUMN_REFUND_TIMESTAMP, timestamp);
        return v;
    }

    public static RefundRecord fromCursor(Cursor c) {
        if (c == null) return null;
        RefundRecord r = new RefundRecord();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_REFUND_ID); if (idx != -1) r.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_REFUND_SALE_ID); if (idx != -1) r.setSaleId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_REFUND_AMOUNT); if (idx != -1) r.setAmount(c.getDouble(idx));
        idx = c.getColumnIndex(Constants.COLUMN_REFUND_USER_ID); if (idx != -1) r.setUserId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_REFUND_USER_ROLE); if (idx != -1) r.setUserRole(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_REFUND_REASON); if (idx != -1) r.setReason(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_REFUND_TIMESTAMP); if (idx != -1) r.setTimestamp(c.getLong(idx));
        return r;
    }
}
