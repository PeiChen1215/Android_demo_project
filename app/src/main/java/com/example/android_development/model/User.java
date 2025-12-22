package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

public class User {
    private String id;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String phone;
    private String email;
    private long createdAt;

    // 构造方法
    public User() {}

    public User(String id, String username, String password, String role, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.createdAt = System.currentTimeMillis();
    }

    // Getter 和 Setter 方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // 从 Cursor 创建 User
    public static User fromCursor(Cursor c) {
        if (c == null) return null;
        User u = new User();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_USER_ID); if (idx != -1) u.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_USERNAME); if (idx != -1) u.setUsername(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PASSWORD); if (idx != -1) u.setPassword(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_ROLE); if (idx != -1) u.setRole(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_FULL_NAME); if (idx != -1) u.setFullName(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PHONE); if (idx != -1) u.setPhone(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_EMAIL); if (idx != -1) u.setEmail(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_CREATED_AT); if (idx != -1) u.setCreatedAt(c.getLong(idx));
        return u;
    }

    // 转换为 ContentValues
    public ContentValues toContentValues() {
        ContentValues v = new ContentValues();
        if (id != null) v.put(Constants.COLUMN_USER_ID, id);
        v.put(Constants.COLUMN_USERNAME, username);
        v.put(Constants.COLUMN_PASSWORD, password);
        v.put(Constants.COLUMN_ROLE, role);
        v.put(Constants.COLUMN_FULL_NAME, fullName);
        v.put(Constants.COLUMN_PHONE, phone);
        v.put(Constants.COLUMN_EMAIL, email);
        v.put(Constants.COLUMN_CREATED_AT, createdAt);
        return v;
    }
}