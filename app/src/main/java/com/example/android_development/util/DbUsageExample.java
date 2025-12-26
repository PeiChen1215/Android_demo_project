package com.example.android_development.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.model.Product;

/**
 * 数据库权限能力的示例代码。
 * <p>
 * 该类用于演示：如何从 SharedPreferences 读取当前用户角色，并调用需要角色校验的数据库方法。
 * 仅用于示例/开发期验证，不建议在正式业务逻辑中直接依赖该类。
 * </p>
 */
public class DbUsageExample {

    /**
     * 演示：读取当前用户角色，并尝试以该角色新增商品。
     *
     * @param ctx     上下文
     * @param product 待新增商品
     */
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
