package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 * 销售单主表实体。
 * <p>
 * 表示一次收银/销售行为：总金额、实收金额、支付方式、收银员用户ID、时间戳等。
 * lines 为内存中的明细列表，通常由调用方另行查询/组装（数据库中通常拆为主表+明细表）。
 * </p>
 */
public class Sale {
    private String id;
    private double total;
    private double paid;
    private String paymentMethod;
    private String userId;
    private long timestamp;
    private boolean refunded;
    private long refundedAt;
    private List<SaleLine> lines = new ArrayList<>();

    public Sale() { this.timestamp = System.currentTimeMillis(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public double getPaid() { return paid; }
    public void setPaid(double paid) { this.paid = paid; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public List<SaleLine> getLines() { return lines; }
    public void setLines(List<SaleLine> lines) { this.lines = lines; }

    /**
     * 转换为 ContentValues（用于插入/更新销售单主表）。
     *
     * @return ContentValues
     */
    public ContentValues toContentValues() {
        ContentValues v = new ContentValues();
        if (id != null) v.put(Constants.COLUMN_SALE_ID, id);
        v.put(Constants.COLUMN_SALE_TOTAL, total);
        v.put(Constants.COLUMN_SALE_PAID, paid);
        v.put(Constants.COLUMN_SALE_PAYMENT_METHOD, paymentMethod);
        v.put(Constants.COLUMN_SALE_USER_ID, userId);
        v.put(Constants.COLUMN_SALE_TIMESTAMP, timestamp);
        v.put(Constants.COLUMN_SALE_REFUNDED, refunded ? 1 : 0);
        v.put(Constants.COLUMN_SALE_REFUNDED_AT, refundedAt);
        return v;
    }

    /**
     * 从查询游标构建销售单对象。
     * <p>
     * 仅构建主表字段，不包含明细 lines（需由调用方单独查询销售明细表后填充）。
     * </p>
     *
     * @param c 数据库游标
     * @return 销售单对象；c 为 null 时返回 null
     */
    public static Sale fromCursor(Cursor c) {
        if (c == null) return null;
        Sale s = new Sale();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_SALE_ID); if (idx != -1) s.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_TOTAL); if (idx != -1) s.setTotal(c.getDouble(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_PAID); if (idx != -1) s.setPaid(c.getDouble(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_PAYMENT_METHOD); if (idx != -1) s.setPaymentMethod(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_USER_ID); if (idx != -1) s.setUserId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_TIMESTAMP); if (idx != -1) s.setTimestamp(c.getLong(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SALE_REFUNDED); if (idx != -1) s.setRefunded(c.getInt(idx) == 1);
        idx = c.getColumnIndex(Constants.COLUMN_SALE_REFUNDED_AT); if (idx != -1) s.setRefundedAt(c.getLong(idx));
        return s;
    }

    public boolean isRefunded() { return refunded; }
    public void setRefunded(boolean refunded) { this.refunded = refunded; }
    public long getRefundedAt() { return refundedAt; }
    public void setRefundedAt(long refundedAt) { this.refundedAt = refundedAt; }
}
