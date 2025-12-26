package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

/**
 * 供应商实体。
 * <p>
 * 保存供应商基础资料：名称、联系人、电话、邮箱等。
 * </p>
 */
public class Supplier {
    private String id;
    private String name;
    private String contact;
    private String phone;
    private String email;

    public Supplier() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * 转换为 ContentValues（用于插入/更新供应商表）。
     * <p>
     * 当 id 为空时会自动生成 UUID。
     * </p>
     *
     * @return ContentValues
     */
    public ContentValues toContentValues() {
        ContentValues v = new ContentValues();
        if (id == null) id = java.util.UUID.randomUUID().toString();
        v.put(Constants.COLUMN_SUPPLIER_ID, id);
        v.put(Constants.COLUMN_SUPPLIER_NAME, name);
        v.put(Constants.COLUMN_SUPPLIER_CONTACT, contact);
        v.put(Constants.COLUMN_SUPPLIER_PHONE, phone);
        v.put(Constants.COLUMN_SUPPLIER_EMAIL, email);
        return v;
    }

    /**
     * 从查询游标构建供应商对象。
     *
     * @param c 数据库游标
     * @return 供应商对象；c 为 null 时返回 null
     */
    public static Supplier fromCursor(Cursor c) {
        if (c == null) return null;
        Supplier s = new Supplier();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_SUPPLIER_ID);
        if (idx != -1) s.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SUPPLIER_NAME);
        if (idx != -1) s.setName(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SUPPLIER_CONTACT);
        if (idx != -1) s.setContact(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SUPPLIER_PHONE);
        if (idx != -1) s.setPhone(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SUPPLIER_EMAIL);
        if (idx != -1) s.setEmail(c.getString(idx));
        return s;
    }
}
