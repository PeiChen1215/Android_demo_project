package com.example.android_development.util;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.UUID;

/**
 * 审计日志写入工具（系统审计）。
 * <p>
 * 该工具用于将关键操作写入本地 SQLite 的系统审计表，用于问题追溯与操作留痕。
 * 写入失败时会吞掉异常（best-effort），避免审计影响主业务流程。
 * </p>
 */
public class Audit {

    /**
     * 写入一条系统审计记录。
     * <p>
     * 注意：该方法为 best-effort；db 为 null 或插入失败时将直接返回，不抛出异常。
     * </p>
     *
     * @param db       可写数据库连接
     * @param userId   操作人用户ID（可为空）
     * @param userRole 操作人角色（可为空）
     * @param entity   实体/模块标识（例如 Product/Purchase/Sale 等）
     * @param action   动作标识（例如 CREATE/UPDATE/DELETE/APPROVE 等）
     * @param detail   详细描述（可为空，建议包含关键字段）
     */
    public static void writeSystemAudit(SQLiteDatabase db, String userId, String userRole, String entity, String action, String detail) {
        if (db == null) return;
        try {
            ContentValues v = new ContentValues();
            v.put(Constants.COLUMN_SYSTEM_AUDIT_ID, UUID.randomUUID().toString());
            v.put(Constants.COLUMN_SYSTEM_AUDIT_USER_ID, userId);
            v.put(Constants.COLUMN_SYSTEM_AUDIT_USER_ROLE, userRole);
            v.put(Constants.COLUMN_SYSTEM_AUDIT_ENTITY, entity);
            v.put(Constants.COLUMN_SYSTEM_AUDIT_ACTION, action);
            v.put(Constants.COLUMN_SYSTEM_AUDIT_DETAIL, detail);
            v.put(Constants.COLUMN_SYSTEM_AUDIT_TIMESTAMP, System.currentTimeMillis());
            try { db.insert(Constants.TABLE_SYSTEM_AUDIT, null, v); } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }
}
