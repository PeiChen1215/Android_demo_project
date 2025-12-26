package com.example.android_development.database;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.model.User;
import com.example.android_development.util.Constants;

/**
 * 用户相关 DAO。
 *
 * <p>提供 users 表的基础读写与登录鉴权查询。
 * 注意：当前实现使用明文密码字段进行匹配，仅适用于演示/测试环境；生产环境应对密码做哈希加盐存储并使用安全校验。</p>
 */
public class UserDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public UserDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * 打开数据库连接（获取可写数据库）。
     */
    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    /**
     * 关闭数据库连接。
     */
    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    /**
     * 添加用户。
     *
     * <p>注意：password 字段当前直接写入数据库（明文），仅供演示使用。</p>
     *
     * @param user 用户对象
     * @return 插入结果；失败返回 -1
     */
    public long addUser(User user) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_ID, user.getId());
        values.put(Constants.COLUMN_USERNAME, user.getUsername());
        values.put(Constants.COLUMN_PASSWORD, user.getPassword()); // 注意：实际应用中密码应该加密
        values.put(Constants.COLUMN_ROLE, user.getRole());
        values.put(Constants.COLUMN_FULL_NAME, user.getFullName());
        values.put(Constants.COLUMN_PHONE, user.getPhone());
        values.put(Constants.COLUMN_EMAIL, user.getEmail());
        values.put(Constants.COLUMN_CREATED_AT, user.getCreatedAt());

        return db.insert(Constants.TABLE_USERS, null, values);
    }

    /**
     * 根据用户名和密码验证用户（登录鉴权）。
     *
     * @param username 用户名
     * @param password 密码（当前为明文匹配）
     * @return 验证通过返回用户对象，否则返回 null
     */
    public User authenticateUser(String username, String password) {
        User user = null;

        String[] columns = {
                Constants.COLUMN_USER_ID,
                Constants.COLUMN_USERNAME,
                Constants.COLUMN_PASSWORD,
                Constants.COLUMN_ROLE,
                Constants.COLUMN_FULL_NAME,
                Constants.COLUMN_PHONE,
                Constants.COLUMN_EMAIL,
                Constants.COLUMN_CREATED_AT
        };

        String selection = Constants.COLUMN_USERNAME + " = ? AND " +
                Constants.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(
                Constants.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }

        if (cursor != null) {
            cursor.close();
        }

        return user;
    }

    /**
     * 根据用户 id 获取用户。
     *
     * @param userId 用户 id
     * @return 用户对象；未找到返回 null
     */
    public User getUserById(String userId) {
        User user = null;

        String[] columns = {
                Constants.COLUMN_USER_ID,
                Constants.COLUMN_USERNAME,
                Constants.COLUMN_PASSWORD,
                Constants.COLUMN_ROLE,
                Constants.COLUMN_FULL_NAME,
                Constants.COLUMN_PHONE,
                Constants.COLUMN_EMAIL,
                Constants.COLUMN_CREATED_AT
        };

        String selection = Constants.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {userId};

        Cursor cursor = db.query(
                Constants.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }

        if (cursor != null) {
            cursor.close();
        }

        return user;
    }

    /**
     * 检查用户名是否存在。
     *
     * @param username 用户名
     * @return 存在返回 true，否则 false
     */
    public boolean isUsernameExists(String username) {
        String[] columns = {Constants.COLUMN_USER_ID};
        String selection = Constants.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(
                Constants.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor != null && cursor.getCount() > 0;

        if (cursor != null) {
            cursor.close();
        }

        return exists;
    }

    /**
     * 将 Cursor 转换为 User 对象。
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();

        user.setId(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USERNAME)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PASSWORD)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_ROLE)));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_FULL_NAME)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PHONE)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EMAIL)));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_CREATED_AT)));

        return user;
    }
}
