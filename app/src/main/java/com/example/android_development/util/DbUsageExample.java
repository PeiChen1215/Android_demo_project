package com.example.android_development.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.model.Product;

public class DbUsageExample {

    // 演示如何从 SharedPreferences 获取当前用户角色并调用受限方法
    public static void exampleAddProduct(Context ctx, Product product) {
        SharedPreferences prefs = ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String role = prefs.getString(Constants.KEY_USER_ROLE, null);

        DatabaseHelper db = new DatabaseHelper(ctx);
        long res = db.addProductAsRole(role, product);
        if (res == -1) {
            // 未授权或失败
            android.util.Log.w("DbUsageExample", "当前用户无权限新增商品");
        } else {
            android.util.Log.d("DbUsageExample", "商品已添加: " + res);
        }
    }
}
