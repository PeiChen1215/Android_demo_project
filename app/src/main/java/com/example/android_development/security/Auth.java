package com.example.android_development.security;

import android.content.Context;
import com.example.android_development.util.Constants;
import com.example.android_development.util.PrefsManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 本地权限检查工具类。
 *
 * <p>将“角色”映射到一组权限常量（见 {@link Constants}），用于：
 * 1）控制 UI 入口显示/隐藏；2）在本地关键操作前做兜底拦截。</p>
 *
 * <p>重要：客户端权限仅是体验层控制手段；若存在真正的安全边界（联网/多用户/外部输入），
 * 关键权限仍应在受信任层（例如后端/服务端）再次校验。</p>
 */
public class Auth {
    private static final Map<String, Set<String>> ROLE_PERMS;

    static {
        Map<String, Set<String>> m = new HashMap<>();

        // 管理员：所有权限
        Set<String> admin = new HashSet<>();
        admin.add(Constants.PERM_CREATE_PO);
        admin.add(Constants.PERM_SUBMIT_PO);
        admin.add(Constants.PERM_APPROVE_PO);
        admin.add(Constants.PERM_RECEIVE_PO);
        admin.add(Constants.PERM_ADJUST_STOCK);
        admin.add(Constants.PERM_CREATE_RETURN);
        admin.add(Constants.PERM_APPROVE_RETURN);
        admin.add(Constants.PERM_VIEW_AUDIT);
        admin.add(Constants.PERM_RUN_INVENTORY);
        // 管理员应具备查看/导出营收与退款权限
        admin.add(Constants.PERM_VIEW_REVENUE);
        admin.add(Constants.PERM_EXPORT_REVENUE);
        admin.add(Constants.PERM_REFUND);
        m.put(Constants.ROLE_ADMIN, Collections.unmodifiableSet(admin));

        // 采购员：创建/提交/查看采购
        Set<String> purchaser = new HashSet<>();
        purchaser.add(Constants.PERM_CREATE_PO);
        purchaser.add(Constants.PERM_SUBMIT_PO);
        purchaser.add(Constants.PERM_VIEW_AUDIT);
        m.put(Constants.ROLE_BUYER, Collections.unmodifiableSet(purchaser));
        m.put(Constants.ROLE_PURCHASER, Collections.unmodifiableSet(purchaser));

        // 仓库/库存：接收/调整/盘点
        Set<String> warehouse = new HashSet<>();
        warehouse.add(Constants.PERM_RECEIVE_PO);
        warehouse.add(Constants.PERM_ADJUST_STOCK);
        warehouse.add(Constants.PERM_RUN_INVENTORY);
        warehouse.add(Constants.PERM_VIEW_AUDIT);
        m.put(Constants.ROLE_WAREHOUSE, Collections.unmodifiableSet(warehouse));
        m.put(Constants.ROLE_STOCK, Collections.unmodifiableSet(warehouse));

        // 出纳/收银：销售相关权限
        Set<String> cashier = new HashSet<>();
        cashier.add(Constants.PERM_ADJUST_STOCK); // POS 出库视为库存调整
        cashier.add(Constants.PERM_VIEW_AUDIT);
        m.put(Constants.ROLE_CASHIER, Collections.unmodifiableSet(cashier));

        // 财务/出纳：报表与导出权限
        Set<String> finance = new HashSet<>();
        finance.add(Constants.PERM_VIEW_REVENUE);
        finance.add(Constants.PERM_EXPORT_REVENUE);
        finance.add(Constants.PERM_VIEW_AUDIT);
        // 财务可能也需要退款权限 (可选)
        finance.add(Constants.PERM_REFUND);
        m.put(Constants.ROLE_FINANCE, Collections.unmodifiableSet(finance));

        ROLE_PERMS = Collections.unmodifiableMap(m);
    }

    /**
     * 按角色检查是否具备指定权限。
     */
    public static boolean hasPermission(String role, String permission) {
        if (role == null || permission == null) return false;
        Set<String> perms = ROLE_PERMS.get(role);
        if (perms == null) return false;
        return perms.contains(permission);
    }

    /**
     * 从本地登录态读取当前用户角色，并检查是否具备指定权限。
     */
    public static boolean hasPermission(Context ctx, String permission) {
        if (ctx == null) return false;
        try {
            PrefsManager pm = new PrefsManager(ctx);
            String role = pm.getUserRole();
            return hasPermission(role, permission);
        } catch (Exception e) {
            return false;
        }
    }
}
