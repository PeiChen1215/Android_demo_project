package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

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
