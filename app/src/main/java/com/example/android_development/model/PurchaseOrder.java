package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

/**
 * 采购单主表实体。
 * <p>
 * 对应采购单的基础信息：供应商、单名/备注、状态、创建时间、预计到货时间、合计金额等。
 * </p>
 */
public class PurchaseOrder {
    private String id;
    private String supplierId;
    private String name;
    private String status;
    private long createdAt;
    private long expectedAt;
    private double total;

    public PurchaseOrder() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getExpectedAt() { return expectedAt; }
    public void setExpectedAt(long expectedAt) { this.expectedAt = expectedAt; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    /**
     * 转换为 ContentValues（用于插入/更新采购单主表）。
     * <p>
     * 当 id 为空时会自动生成 UUID；当 createdAt 为 0 时会自动写入当前时间戳。
     * </p>
     *
     * @return ContentValues
     */
    public ContentValues toContentValues() {
        ContentValues v = new ContentValues();
        if (id == null) id = java.util.UUID.randomUUID().toString();
        v.put(Constants.COLUMN_PO_ID, id);
        v.put(Constants.COLUMN_PO_SUPPLIER_ID, supplierId);
        v.put(Constants.COLUMN_PO_NAME, name);
        v.put(Constants.COLUMN_PO_STATUS, status);
        v.put(Constants.COLUMN_PO_CREATED_AT, createdAt == 0 ? System.currentTimeMillis() : createdAt);
        v.put(Constants.COLUMN_PO_EXPECTED_AT, expectedAt);
        v.put(Constants.COLUMN_PO_TOTAL, total);
        return v;
    }

    /**
     * 从查询游标构建采购单主表对象。
     *
     * @param c 数据库游标
     * @return 采购单对象；c 为 null 时返回 null
     */
    public static PurchaseOrder fromCursor(Cursor c) {
        if (c == null) return null;
        PurchaseOrder p = new PurchaseOrder();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_PO_ID);
        if (idx != -1) p.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_SUPPLIER_ID);
        if (idx != -1) p.setSupplierId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_NAME);
        if (idx != -1) p.setName(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_STATUS);
        if (idx != -1) p.setStatus(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_CREATED_AT);
        if (idx != -1) p.setCreatedAt(c.getLong(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_EXPECTED_AT);
        if (idx != -1) p.setExpectedAt(c.getLong(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PO_TOTAL);
        if (idx != -1) p.setTotal(c.getDouble(idx));
        return p;
    }
}
