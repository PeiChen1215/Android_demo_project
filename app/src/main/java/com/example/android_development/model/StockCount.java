package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

public class StockCount {
    private String id;
    private String status;
    private String createdBy;
    private long createdAt;

    public StockCount() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public ContentValues toContentValues() {
        ContentValues v = new ContentValues();
        if (id == null) id = java.util.UUID.randomUUID().toString();
        v.put(Constants.COLUMN_STOCK_COUNT_ID, id);
        v.put(Constants.COLUMN_STOCK_COUNT_STATUS, status);
        v.put(Constants.COLUMN_STOCK_COUNT_CREATED_BY, createdBy);
        v.put(Constants.COLUMN_STOCK_COUNT_CREATED_AT, createdAt == 0 ? System.currentTimeMillis() : createdAt);
        return v;
    }

    public static StockCount fromCursor(Cursor c) {
        if (c == null) return null;
        StockCount s = new StockCount();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_COUNT_ID);
        if (idx != -1) s.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_COUNT_STATUS);
        if (idx != -1) s.setStatus(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_COUNT_CREATED_BY);
        if (idx != -1) s.setCreatedBy(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK_COUNT_CREATED_AT);
        if (idx != -1) s.setCreatedAt(c.getLong(idx));
        return s;
    }
}
