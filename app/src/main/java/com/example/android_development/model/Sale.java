package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private String id;
    private double total;
    private double paid;
    private String userId;
    private long timestamp;
    private List<SaleLine> lines = new ArrayList<>();

    public Sale() { this.timestamp = System.currentTimeMillis(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public double getPaid() { return paid; }
    public void setPaid(double paid) { this.paid = paid; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public List<SaleLine> getLines() { return lines; }
    public void setLines(List<SaleLine> lines) { this.lines = lines; }

    public ContentValues toContentValues() {
        ContentValues v = new ContentValues();
        if (id != null) v.put(Constants.COLUMN_SALE_ID, id);
        v.put(Constants.COLUMN_SALE_TOTAL, total);
        v.put(Constants.COLUMN_SALE_PAID, paid);
        v.put(Constants.COLUMN_SALE_USER_ID, userId);
        v.put(Constants.COLUMN_SALE_TIMESTAMP, timestamp);
        return v;
    }

    public static Sale fromCursor(Cursor c) {
        if (c == null) return null;
        Sale s = new Sale();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_SALE_ID); if (idx != -1) s.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_TOTAL); if (idx != -1) s.setTotal(c.getDouble(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_PAID); if (idx != -1) s.setPaid(c.getDouble(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_USER_ID); if (idx != -1) s.setUserId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_TIMESTAMP); if (idx != -1) s.setTimestamp(c.getLong(idx));
        return s;
    }
}
