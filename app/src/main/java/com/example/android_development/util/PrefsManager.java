package com.example.android_development.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences 的轻量封装。
 * <p>
 * 主要用于保存登录态（是否已登录、用户ID、用户角色）以及一些简单的本地设置项。
 * </p>
 */
public class PrefsManager {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    /**
     * 创建偏好管理器。
     *
     * @param context 上下文（用于获取 SharedPreferences）
     */
    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * 保存登录状态。
     *
     * @param isLoggedIn 是否已登录
     */
    public void saveLoginStatus(boolean isLoggedIn) {
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    /**
     * 获取登录状态。
     *
     * @return 是否已登录
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    /**
     * 保存用户ID。
     *
     * @param userId 用户ID
     */
    public void saveUserId(String userId) {
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.apply();
    }

    /**
     * 获取用户ID。
     *
     * @return 用户ID（缺省为空字符串）
     */
    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, "");
    }

    /**
     * 保存用户角色。
     *
     * @param role 角色标识（例如 admin/cashier/manager 等）
     */
    public void saveUserRole(String role) {
        editor.putString(Constants.KEY_USER_ROLE, role);
        editor.apply();
    }

    /**
     * 获取用户角色。
     *
     * @return 角色标识（缺省为空字符串）
     */
    public String getUserRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, "");
    }

    /**
     * 清除所有登录信息（用于退出登录）。
     */
    public void clearLoginInfo() {
        editor.remove(Constants.KEY_IS_LOGGED_IN);
        editor.remove(Constants.KEY_USER_ID);
        editor.remove(Constants.KEY_USER_ROLE);
        editor.apply();
    }

    /**
     * 保存字符串配置。
     *
     * @param key   键
     * @param value 值
     */
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 读取字符串配置。
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 读取到的值
     */
    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    /**
     * 保存布尔配置。
     *
     * @param key   键
     * @param value 值
     */
    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * 读取布尔配置。
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 读取到的值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    /**
     * 保存整型配置。
     *
     * @param key   键
     * @param value 值
     */
    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 读取整型配置。
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 读取到的值
     */
    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }
}
