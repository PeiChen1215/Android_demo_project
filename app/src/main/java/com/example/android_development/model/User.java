package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

/**
 * 用户实体。
 * <p>
 * 用于本地登录/权限：包含用户名、密码（明文存储并不安全，仅适用于本地演示/离线场景）、角色等。
 * role 字段会被权限工具类用于功能入口与关键操作的授权判断。
 * </p>
 */
public class User {
    private String id;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String phone;
    private String email;
    private long createdAt;

    /**
     * 创建空用户对象。
     */
    public User() {}

    public User(String id, String username, String password, String role, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.createdAt = System.currentTimeMillis();
    }

    // 访问器/修改器
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

    /**
     * 从查询游标构建用户对象。
     *
     * @param c 数据库游标
     * @return 用户对象；c 为 null 时返回 null
     */
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

    /**
     * 转换为 ContentValues（用于插入/更新用户表）。
     *
     * @return ContentValues
     */
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