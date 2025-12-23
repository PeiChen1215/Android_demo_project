package com.example.android_development.util;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.UUID;

public class Audit {

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
