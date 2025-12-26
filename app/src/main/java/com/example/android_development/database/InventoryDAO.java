package com.example.android_development.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.util.PrefsManager;
import com.example.android_development.util.Audit;
import com.example.android_development.model.StockCount;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 * 库存相关 DAO。
 *
 * <p>包含货架库存与仓库库存的调整、补货、采购入库，以及盘点记录的读写。
 * 部分方法通过“条件更新（CAS）”实现并发保护，并写入库存事务与系统审计。</p>
 */
public class InventoryDAO {
    private SQLiteDatabase db;
    private PrefsManager prefsManager;
    private android.content.Context ctx;

    public InventoryDAO(SQLiteDatabase db) { this.db = db; }

    public InventoryDAO(SQLiteDatabase db, Context ctx) {
        this.db = db;
        this.ctx = ctx;
        if (ctx != null) this.prefsManager = new PrefsManager(ctx);
    }

    /**
     * 并发保护的货架库存调整（可用于销售/退款等场景）。
     *
     * <p>实现方式：读取 before 值后，以 (product_id + before) 作为条件更新（CAS），避免并发覆盖。
     * 该方法在需要时会开启本地事务，并写入库存事务与系统审计。</p>
     *
     * @param db 数据库连接
     * @param prefsManager 用于读取当前用户信息（可为空）
     * @param productId 商品 id
     * @param delta 变更量（可为负）
     * @param reason 原因/来源（用于审计）
     * @param type 事务类型（例如 IN/OUT）
     * @return 成功返回 true；并发冲突或失败返回 false
     */
    public static boolean adjustShelfStock(SQLiteDatabase db, PrefsManager prefsManager, String productId, int delta, String reason, String type) {
        if (productId == null) return false;
        if (delta == 0) return true;

        boolean localTxStarted = false;
        Cursor c = null;
        try {
            if (!db.inTransaction()) { db.beginTransaction(); localTxStarted = true; }

            c = db.rawQuery("SELECT " + Constants.COLUMN_STOCK + " FROM " + Constants.TABLE_PRODUCTS + " WHERE " + Constants.COLUMN_PRODUCT_ID + " = ?", new String[]{productId});
            if (c == null) return false;
            if (!c.moveToFirst()) return false;
            int before = c.getInt(0);
            c.close();
            c = null;

            int after = before + delta;
            if (after < 0) after = 0;

            android.content.ContentValues pv = new android.content.ContentValues();
            pv.put(Constants.COLUMN_STOCK, after);
            int rows = db.update(Constants.TABLE_PRODUCTS, pv, Constants.COLUMN_PRODUCT_ID + " = ? AND " + Constants.COLUMN_STOCK + " = ?", new String[]{productId, String.valueOf(before)});
            if (rows == 0) return false;

            // 写审计记录
            try {
                ContentValues tx = new ContentValues();
                tx.put(Constants.COLUMN_STOCK_TX_ID, java.util.UUID.randomUUID().toString());
                tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_ID, productId);
                tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_NAME, (String) null);
                String uid = null;
                String urole = null;
                if (prefsManager != null) {
                    try { uid = prefsManager.getUserId(); } catch (Exception ignored) {}
                    try { urole = prefsManager.getUserRole(); } catch (Exception ignored) {}
                }
                tx.put(Constants.COLUMN_STOCK_TX_USER_ID, uid);
                tx.put(Constants.COLUMN_STOCK_TX_USER_ROLE, urole);
                tx.put(Constants.COLUMN_STOCK_TX_TYPE, type);
                tx.put(Constants.COLUMN_STOCK_TX_QUANTITY, Math.abs(delta));
                tx.put(Constants.COLUMN_STOCK_TX_BEFORE, before);
                tx.put(Constants.COLUMN_STOCK_TX_AFTER, after);
                tx.put(Constants.COLUMN_STOCK_TX_REASON, reason);
                tx.put(Constants.COLUMN_STOCK_TX_TIMESTAMP, System.currentTimeMillis());
                    try { db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, tx); } catch (Exception ignored) {}
                    try { Audit.writeSystemAudit(db, uid, urole, "product:" + productId, type.toLowerCase(), reason); } catch (Exception ignored) {}
            } catch (Exception ignored) {}

            if (localTxStarted) db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (c != null) try { c.close(); } catch (Exception ignored) {}
            if (localTxStarted) {
                try { db.endTransaction(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * 新建一条盘点记录（主表）。
     *
     * @param sc 盘点对象
     * @return 插入结果；失败返回 -1
     */
    public long createStockCount(StockCount sc) {
        if (sc == null) return -1;
        ContentValues v = sc.toContentValues();
        return db.insert(Constants.TABLE_STOCK_COUNTS, null, v);
    }

    /**
     * 根据 id 获取盘点记录。
     *
     * @param id 盘点 id
     * @return 盘点对象；未找到返回 null
     */
    public StockCount getStockCountById(String id) {
        if (id == null) return null;
        Cursor c = db.query(Constants.TABLE_STOCK_COUNTS, null, Constants.COLUMN_STOCK_COUNT_ID + " = ?", new String[]{id}, null, null, null);
        if (c != null && c.moveToFirst()) {
            StockCount sc = StockCount.fromCursor(c);
            c.close();
            return sc;
        }
        return null;
    }

    /**
     * 获取所有盘点记录。
     *
     * @return 盘点列表（按创建时间倒序）
     */
    public List<StockCount> getAllStockCounts() {
        List<StockCount> list = new ArrayList<>();
        Cursor c = db.query(Constants.TABLE_STOCK_COUNTS, null, null, null, null, null, Constants.COLUMN_STOCK_COUNT_CREATED_AT + " DESC");
        if (c != null && c.moveToFirst()) {
            do {
                list.add(StockCount.fromCursor(c));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    /**
     * 内部补货：仓库 -> 货架（read→update，带并发保护与审计）。
     *
     * <p>需要 ADJUST_STOCK 权限；会在必要时开启本地事务。</p>
     *
     * @param productId 商品 id
     * @param qty 补货数量（>0）
     * @return 成功返回 true；仓库库存不足/并发冲突/失败返回 false
     */
    public boolean restockShelf(String productId, int qty) {
        if (productId == null) return false;
        if (qty <= 0) return false;

        // 权限检查：调整库存需要 ADJUST_STOCK
        if (ctx != null && !com.example.android_development.security.Auth.hasPermission(ctx, com.example.android_development.util.Constants.PERM_ADJUST_STOCK)) {
            com.example.android_development.util.DaoResult.setError(com.example.android_development.util.DaoResult.ERR_PERMISSION, "no permission to adjust stock");
            return false;
        }

        boolean localTxStarted = false;
        Cursor c = null;
        try {
            if (!db.inTransaction()) { db.beginTransaction(); localTxStarted = true; }

            // 1. 读取当前仓库和货架库存
            c = db.rawQuery("SELECT " + Constants.COLUMN_WAREHOUSE_STOCK + ", " + Constants.COLUMN_STOCK + " FROM " + Constants.TABLE_PRODUCTS + " WHERE " + Constants.COLUMN_PRODUCT_ID + " = ?", new String[]{productId});
            if (c == null) return false;
            if (!c.moveToFirst()) return false;
            int currentWarehouse = c.getInt(0);
            int currentShelf = c.getInt(1);
            c.close();
            c = null;

            if (currentWarehouse < qty) return false; // 仓库库存不足

            int newWarehouse = currentWarehouse - qty;
            int newShelf = currentShelf + qty;

            // 2. 条件更新仓库库存（以当前值做并发保护）
            android.content.ContentValues v1 = new android.content.ContentValues();
            v1.put(Constants.COLUMN_WAREHOUSE_STOCK, newWarehouse);
            int rows1 = db.update(Constants.TABLE_PRODUCTS, v1, Constants.COLUMN_PRODUCT_ID + " = ? AND " + Constants.COLUMN_WAREHOUSE_STOCK + " = ?", new String[]{productId, String.valueOf(currentWarehouse)});
            if (rows1 == 0) return false; // 并发冲突或商品被删除

            // 3. 条件更新货架库存（以当前值做并发保护）
            android.content.ContentValues v2 = new android.content.ContentValues();
            v2.put(Constants.COLUMN_STOCK, newShelf);
            int rows2 = db.update(Constants.TABLE_PRODUCTS, v2, Constants.COLUMN_PRODUCT_ID + " = ? AND " + Constants.COLUMN_STOCK + " = ?", new String[]{productId, String.valueOf(currentShelf)});
            if (rows2 == 0) return false; // 并发冲突

            // 4. 写入审计记录（补货）
            try {
                ContentValues tx = new ContentValues();
                tx.put(Constants.COLUMN_STOCK_TX_ID, java.util.UUID.randomUUID().toString());
                tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_ID, productId);
                tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_NAME, (String) null);
                String uid = null;
                String urole = null;
                if (prefsManager != null) {
                    try { uid = prefsManager.getUserId(); } catch (Exception ignored) {}
                    try { urole = prefsManager.getUserRole(); } catch (Exception ignored) {}
                }
                tx.put(Constants.COLUMN_STOCK_TX_USER_ID, uid);
                tx.put(Constants.COLUMN_STOCK_TX_USER_ROLE, urole);
                tx.put(Constants.COLUMN_STOCK_TX_TYPE, "IN");
                tx.put(Constants.COLUMN_STOCK_TX_QUANTITY, qty);
                tx.put(Constants.COLUMN_STOCK_TX_BEFORE, currentWarehouse);
                tx.put(Constants.COLUMN_STOCK_TX_AFTER, newWarehouse);
                tx.put(Constants.COLUMN_STOCK_TX_REASON, "补货");
                tx.put(Constants.COLUMN_STOCK_TX_TIMESTAMP, System.currentTimeMillis());
                try { db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, tx); } catch (Exception ignored) {}
                try { Audit.writeSystemAudit(db, uid, urole, "product:" + productId, "restock", "补货"); } catch (Exception ignored) {}
            } catch (Exception ignored) {}

            if (localTxStarted) db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (c != null) try { c.close(); } catch (Exception ignored) {}
            if (localTxStarted) {
                try { db.endTransaction(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * 外部采购入库：供应商 -> 仓库。
     *
     * <p>需要 RECEIVE_PO 权限；会在必要时开启本地事务，并写入库存事务与系统审计。</p>
     *
     * @param productId 商品 id
     * @param qty 入库数量（>0）
     * @return 成功返回 true；商品不存在/失败返回 false
     */
    public boolean receivePurchase(String productId, int qty) {
        if (productId == null) return false;
        if (qty <= 0) return false; // 不接受 0 或负数入库

        // 权限检查：接收 PO 需要 RECEIVE_PO
        if (ctx != null && !com.example.android_development.security.Auth.hasPermission(ctx, com.example.android_development.util.Constants.PERM_RECEIVE_PO)) {
            com.example.android_development.util.DaoResult.setError(com.example.android_development.util.DaoResult.ERR_PERMISSION, "no permission to receive purchase");
            return false;
        }

        boolean localTxStarted = false;
        try {
            if (!db.inTransaction()) { db.beginTransaction(); localTxStarted = true; }

            // 1. 查询当前仓库库存
            Cursor c = db.rawQuery("SELECT " + Constants.COLUMN_WAREHOUSE_STOCK + " FROM " + Constants.TABLE_PRODUCTS + " WHERE " + Constants.COLUMN_PRODUCT_ID + " = ?", new String[]{productId});
            if (c == null) return false;
            try {
                if (!c.moveToFirst()) return false; // 未找到商品
                int current = c.getInt(0);
                int updated = current + qty;
                android.content.ContentValues v = new android.content.ContentValues();
                v.put(Constants.COLUMN_WAREHOUSE_STOCK, updated);
                int rows = db.update(Constants.TABLE_PRODUCTS, v, Constants.COLUMN_PRODUCT_ID + " = ?", new String[]{productId});
                if (rows > 0) {
                    // 写审计记录（尽量不影响主流程）
                    try {
                        ContentValues tx = new ContentValues();
                        tx.put(Constants.COLUMN_STOCK_TX_ID, java.util.UUID.randomUUID().toString());
                        tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_ID, productId);
                        tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_NAME, (String) null);
                        String uid = null;
                        String urole = null;
                        if (prefsManager != null) {
                            try { uid = prefsManager.getUserId(); } catch (Exception ignored) {}
                            try { urole = prefsManager.getUserRole(); } catch (Exception ignored) {}
                        }
                        tx.put(Constants.COLUMN_STOCK_TX_USER_ID, uid);
                        tx.put(Constants.COLUMN_STOCK_TX_USER_ROLE, urole);
                        tx.put(Constants.COLUMN_STOCK_TX_TYPE, "IN");
                        tx.put(Constants.COLUMN_STOCK_TX_QUANTITY, qty);
                        tx.put(Constants.COLUMN_STOCK_TX_BEFORE, current);
                        tx.put(Constants.COLUMN_STOCK_TX_AFTER, updated);
                        tx.put(Constants.COLUMN_STOCK_TX_REASON, "采购入库");
                        tx.put(Constants.COLUMN_STOCK_TX_TIMESTAMP, System.currentTimeMillis());
                            try { db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, tx); } catch (Exception ignored) {}
                            try { Audit.writeSystemAudit(db, uid, urole, "product:" + productId, "in", "采购入库"); } catch (Exception ignored) {}
                    } catch (Exception ignored) {}

                    if (localTxStarted) db.setTransactionSuccessful();
                    return true;
                }
                return false;
            } finally {
                if (c != null) c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (localTxStarted) {
                try { db.endTransaction(); } catch (Exception ignored) {}
            }
        }
    }
}
