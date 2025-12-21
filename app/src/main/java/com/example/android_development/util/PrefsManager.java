package com.example.android_development.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // 保存登录状态
    public void saveLoginStatus(boolean isLoggedIn) {
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    // 获取登录状态
    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    // 保存用户ID
    public void saveUserId(String userId) {
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.apply();
    }

    // 获取用户ID
    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, "");
    }

    // 保存用户角色
    public void saveUserRole(String role) {
        editor.putString(Constants.KEY_USER_ROLE, role);
        editor.apply();
    }

    // 获取用户角色
    public String getUserRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, "");
    }

    // 清除所有登录信息（退出登录）
    public void clearLoginInfo() {
        editor.remove(Constants.KEY_IS_LOGGED_IN);
        editor.remove(Constants.KEY_USER_ID);
        editor.remove(Constants.KEY_USER_ROLE);
        editor.apply();
    }

    // 保存其他设置
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }
}
