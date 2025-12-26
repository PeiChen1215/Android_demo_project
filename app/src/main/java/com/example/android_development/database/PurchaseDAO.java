package com.example.android_development.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import com.example.android_development.model.PurchaseLine;
import com.example.android_development.model.PurchaseOrder;
import com.example.android_development.util.Constants;
import com.example.android_development.util.PrefsManager;
import com.example.android_development.security.Auth;
import com.example.android_development.util.Audit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 采购相关 DAO。
 *
 * <p>负责采购单（PO）与采购明细行的增删改查，以及“提交/审批/拒绝/入库”等状态流转。
 * 关键操作会进行权限校验（通过 {@link Auth}）并写入审计记录（{@link Audit}）。</p>
 */
public class PurchaseDAO {
    private SQLiteDatabase db;
    private InventoryDAO inventoryDAO;
    private PrefsManager prefsManager;
    private Context ctx;

    public PurchaseDAO(SQLiteDatabase db) {
        this(db, null);
    }

    public PurchaseDAO(SQLiteDatabase db, Context ctx) {
        this.db = db;
        this.ctx = ctx;
        this.inventoryDAO = new InventoryDAO(db, ctx);
        if (ctx != null) this.prefsManager = new PrefsManager(ctx);
    }

    /**
     * 新增采购单（Purchase Order）。
     *
     * <p>说明：会自动补齐 PO 的 id（若为空），并在有上下文时做权限校验与写入审计。</p>
     *
     * @param po 采购单对象
     * @return 插入结果行 id（SQLite insert 返回值）；失败返回 -1
     */
    public long addPurchaseOrder(PurchaseOrder po) {
        if (po == null) return -1;
        // 权限检查
        if (ctx != null && !Auth.hasPermission(ctx, Constants.PERM_CREATE_PO)) {
            com.example.android_development.util.DaoResult.setError(com.example.android_development.util.DaoResult.ERR_PERMISSION, "no permission to create PO");
            return -1;
        }
        if (po.getId() == null) po.setId(UUID.randomUUID().toString());
        ContentValues v = po.toContentValues();
        long r = db.insert(Constants.TABLE_PURCHASE_ORDERS, null, v);
        if (r > 0) {
            try {
                String uid = null, urole = null;
                if (prefsManager != null) { uid = prefsManager.getUserId(); urole = prefsManager.getUserRole(); }
                Audit.writeSystemAudit(db, uid, urole, "purchase_order:" + po.getId(), "create", "create_po");
            } catch (Exception ignored) {}
        }
        return r;
    }

    /**
     * 新增采购明细行。
     *
     * <p>限制：若父采购单已入库（RECEIVED），则不允许再新增明细行。</p>
     *
     * @param line 采购明细行
     * @return 插入结果；失败返回 -1
     */
    public long addPurchaseLine(PurchaseLine line) {
        if (line == null) return -1;
        if (ctx != null && !Auth.hasPermission(ctx, Constants.PERM_CREATE_PO)) {
            com.example.android_development.util.DaoResult.setError(com.example.android_development.util.DaoResult.ERR_PERMISSION, "no permission to add PO line");
            return -1;
        }
        // 已入库（RECEIVED）的采购单不允许再新增行
        if (line.getPoId() != null) {
            Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, new String[]{Constants.COLUMN_PO_STATUS}, Constants.COLUMN_PO_ID + " = ?", new String[]{line.getPoId()}, null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    int idx = c.getColumnIndex(Constants.COLUMN_PO_STATUS);
                    if (idx != -1) {
                        String st = c.getString(idx);
                        if (Constants.PO_STATUS_RECEIVED.equalsIgnoreCase(st)) {
                            c.close();
                            return -1;
                        }
                    }
                }
                c.close();
            }
        }
        if (line.getId() == null) line.setId(UUID.randomUUID().toString());
        ContentValues v = line.toContentValues();
        long r = db.insert(Constants.TABLE_PURCHASE_LINES, null, v);
        if (r > 0) {
            try {
                String uid = null, urole = null;
                if (prefsManager != null) { uid = prefsManager.getUserId(); urole = prefsManager.getUserRole(); }
                Audit.writeSystemAudit(db, uid, urole, "purchase_line:" + line.getId(), "create", "add_line");
            } catch (Exception ignored) {}
        }
        return r;
    }

    /**
     * 提交采购单：将采购单状态推进到 SUBMITTED（后续进入审批流程）。
     *
     * <p>会校验：权限（SUBMIT_PO）与采购单不为空（至少包含一条明细行）。</p>
     */
    public boolean submitPo(String poId) {
        if (poId == null) return false;
        if (ctx != null && !Auth.hasPermission(ctx, Constants.PERM_SUBMIT_PO)) {
            com.example.android_development.util.DaoResult.setError(com.example.android_development.util.DaoResult.ERR_PERMISSION, "no permission to submit PO");
            return false;
        }
        // 不允许提交空采购单（没有任何明细行）
        try {
            Cursor lc = db.query(Constants.TABLE_PURCHASE_LINES, new String[]{Constants.COLUMN_PO_LINE_ID}, Constants.COLUMN_PO_LINE_PO_ID + " = ?", new String[]{poId}, null, null, null);
            boolean hasLines = false;
            if (lc != null) {
                hasLines = lc.moveToFirst();
                lc.close();
            }
            if (!hasLines) {
                com.example.android_development.util.DaoResult.setError(com.example.android_development.util.DaoResult.ERR_INVALID, "empty purchase order");
                return false;
            }
        } catch (Exception ignored) {}
        ContentValues v = new ContentValues();
        v.put(Constants.COLUMN_PO_STATUS, Constants.PO_STATUS_SUBMITTED);
        // 允许从 CREATED/OPEN/DRAFT/REJECTED 提交，避免历史数据中 open 状态被“卡死”
        String where = Constants.COLUMN_PO_ID + " = ? AND (LOWER(" + Constants.COLUMN_PO_STATUS + ") = ? OR LOWER(" + Constants.COLUMN_PO_STATUS + ") = ? OR LOWER(" + Constants.COLUMN_PO_STATUS + ") = ? OR LOWER(" + Constants.COLUMN_PO_STATUS + ") = ? )";
        int rows = db.update(Constants.TABLE_PURCHASE_ORDERS, v, where, new String[]{poId, Constants.PO_STATUS_CREATED.toLowerCase(), "open", "draft", Constants.PO_STATUS_REJECTED.toLowerCase()});
        boolean ok = rows > 0;
        if (ok) {
            try {
                String uid = null, urole = null;
                if (prefsManager != null) { uid = prefsManager.getUserId(); urole = prefsManager.getUserRole(); }
                Audit.writeSystemAudit(db, uid, urole, "purchase_order:" + poId, "submit", "submit_po");
                // 写入一条“提交”记录到审批历史，便于在历史列表中展示提交动作
                ContentValues ap = new ContentValues();
                ap.put(Constants.COLUMN_PO_APPROVAL_ID, java.util.UUID.randomUUID().toString());
                ap.put(Constants.COLUMN_PO_APPROVAL_PO_ID, poId);
                ap.put(Constants.COLUMN_PO_APPROVAL_APPROVER_ID, uid);
                ap.put(Constants.COLUMN_PO_APPROVAL_APPROVER_ROLE, urole);
                ap.put(Constants.COLUMN_PO_APPROVAL_DECISION, Constants.PO_STATUS_SUBMITTED);
                ap.put(Constants.COLUMN_PO_APPROVAL_COMMENT, "");
                ap.put(Constants.COLUMN_PO_APPROVAL_TIMESTAMP, System.currentTimeMillis());
                db.insert(Constants.TABLE_PO_APPROVALS, null, ap);
            } catch (Exception ignored) {}
        }
        return ok;
    }

    /**
     * 批准采购单：写入审批记录，并将状态置为 APPROVED（事务内执行）。
     */
    public boolean approvePo(String poId, String approverId, String comment) {
        if (poId == null) return false;
        if (ctx != null && !Auth.hasPermission(ctx, Constants.PERM_APPROVE_PO)) {
            com.example.android_development.util.DaoResult.setError(com.example.android_development.util.DaoResult.ERR_PERMISSION, "no permission to approve PO");
            return false;
        }
        boolean localTx = false;
        try {
            if (!db.inTransaction()) { db.beginTransaction(); localTx = true; }

            // 仅允许从 submitted/pending 批准到 approved（并发/非法状态将失败）
            ContentValues v = new ContentValues();
            v.put(Constants.COLUMN_PO_STATUS, Constants.PO_STATUS_APPROVED);
            String where = Constants.COLUMN_PO_ID + " = ? AND (LOWER(" + Constants.COLUMN_PO_STATUS + ") = ? OR LOWER(" + Constants.COLUMN_PO_STATUS + ") = ? )";
            int updated = db.update(Constants.TABLE_PURCHASE_ORDERS, v, where, new String[]{poId, Constants.PO_STATUS_SUBMITTED.toLowerCase(), Constants.PO_STATUS_PENDING.toLowerCase()});
            if (updated == 0) {
                if (localTx) db.endTransaction();
                return false; // 并发冲突或状态不允许
            }

            // 写入审批记录
            ContentValues ap = new ContentValues();
            ap.put(Constants.COLUMN_PO_APPROVAL_ID, java.util.UUID.randomUUID().toString());
            ap.put(Constants.COLUMN_PO_APPROVAL_PO_ID, poId);
            ap.put(Constants.COLUMN_PO_APPROVAL_APPROVER_ID, approverId);
            String role = null;
            if (prefsManager != null) role = prefsManager.getUserRole();
            ap.put(Constants.COLUMN_PO_APPROVAL_APPROVER_ROLE, role);
            ap.put(Constants.COLUMN_PO_APPROVAL_DECISION, Constants.PO_STATUS_APPROVED);
            ap.put(Constants.COLUMN_PO_APPROVAL_COMMENT, comment);
            ap.put(Constants.COLUMN_PO_APPROVAL_TIMESTAMP, System.currentTimeMillis());
            db.insert(Constants.TABLE_PO_APPROVALS, null, ap);
            try { Audit.writeSystemAudit(db, approverId, role, "purchase_order:" + poId, "approve", comment); } catch (Exception ignored) {}

            if (localTx) db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (localTx) try { db.endTransaction(); } catch (Exception ignored) {}
        }
    }

    /**
     * 拒绝采购单：写入审批记录，并将状态置为 REJECTED（事务内执行）。
     *
     * @param poId 采购单 id
     * @param approverId 审批人 id
     * @param comment 审批意见（可为空）
     * @return 成功返回 true；状态不允许/并发冲突/失败返回 false
     */
    public boolean rejectPo(String poId, String approverId, String comment) {
        if (poId == null) return false;
        if (ctx != null && !Auth.hasPermission(ctx, Constants.PERM_APPROVE_PO)) {
            com.example.android_development.util.DaoResult.setError(com.example.android_development.util.DaoResult.ERR_PERMISSION, "no permission to reject PO");
            return false;
        }
        boolean localTx = false;
        try {
            if (!db.inTransaction()) { db.beginTransaction(); localTx = true; }

            ContentValues v = new ContentValues();
            v.put(Constants.COLUMN_PO_STATUS, Constants.PO_STATUS_REJECTED);
            String where = Constants.COLUMN_PO_ID + " = ? AND (LOWER(" + Constants.COLUMN_PO_STATUS + ") = ? OR LOWER(" + Constants.COLUMN_PO_STATUS + ") = ? )";
            int updated = db.update(Constants.TABLE_PURCHASE_ORDERS, v, where, new String[]{poId, Constants.PO_STATUS_SUBMITTED.toLowerCase(), Constants.PO_STATUS_PENDING.toLowerCase()});
            if (updated == 0) {
                if (localTx) db.endTransaction();
                return false;
            }

            ContentValues ap = new ContentValues();
            ap.put(Constants.COLUMN_PO_APPROVAL_ID, java.util.UUID.randomUUID().toString());
            ap.put(Constants.COLUMN_PO_APPROVAL_PO_ID, poId);
            ap.put(Constants.COLUMN_PO_APPROVAL_APPROVER_ID, approverId);
            String role = null;
            if (prefsManager != null) role = prefsManager.getUserRole();
            ap.put(Constants.COLUMN_PO_APPROVAL_APPROVER_ROLE, role);
            ap.put(Constants.COLUMN_PO_APPROVAL_DECISION, Constants.PO_STATUS_REJECTED);
            ap.put(Constants.COLUMN_PO_APPROVAL_COMMENT, comment);
            ap.put(Constants.COLUMN_PO_APPROVAL_TIMESTAMP, System.currentTimeMillis());
            db.insert(Constants.TABLE_PO_APPROVALS, null, ap);
            try { Audit.writeSystemAudit(db, approverId, role, "purchase_order:" + poId, "reject", comment); } catch (Exception ignored) {}

            if (localTx) db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (localTx) try { db.endTransaction(); } catch (Exception ignored) {}
        }
    }

    /**
     * 获取采购单的审批/流转历史。
     *
     * @param poId 采购单 id
     * @return 按时间正序的审批记录列表（ContentValues）
     */
    public List<ContentValues> getApprovalHistory(String poId) {
        List<ContentValues> list = new ArrayList<>();
        if (poId == null) return list;
        Cursor c = db.query(Constants.TABLE_PO_APPROVALS, null, Constants.COLUMN_PO_APPROVAL_PO_ID + " = ?", new String[]{poId}, null, null, Constants.COLUMN_PO_APPROVAL_TIMESTAMP + " ASC");
        if (c != null && c.moveToFirst()) {
            do {
                ContentValues cv = new ContentValues();
                for (String col : c.getColumnNames()) {
                    int idx = c.getColumnIndex(col);
                    if (idx != -1) cv.put(col, c.getString(idx));
                }
                list.add(cv);
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    /**
     * 根据 id 获取采购单。
     *
     * @param id 采购单 id
     * @return 采购单对象；未找到返回 null
     */
    public PurchaseOrder getPurchaseOrderById(String id) {
        if (id == null) return null;
        Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, null, Constants.COLUMN_PO_ID + " = ?", new String[]{id}, null, null, null);
        if (c != null && c.moveToFirst()) { PurchaseOrder p = PurchaseOrder.fromCursor(c); c.close(); return p; }
        return null;
    }

    /**
     * 获取全部采购单。
     *
     * @return 采购单列表（按创建时间倒序）
     */
    public List<PurchaseOrder> getAllPurchaseOrders() {
        List<PurchaseOrder> list = new ArrayList<>();
        Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, null, null, null, null, null, Constants.COLUMN_PO_CREATED_AT + " DESC");
        if (c != null && c.moveToFirst()) {
            do { list.add(PurchaseOrder.fromCursor(c)); } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    /**
     * 获取指定采购单的所有明细行。
     *
     * @param poId 采购单 id
     * @return 明细行列表
     */
    public List<PurchaseLine> getLinesForPo(String poId) {
        List<PurchaseLine> list = new ArrayList<>();
        if (poId == null) return list;
        Cursor c = db.query(Constants.TABLE_PURCHASE_LINES, null, Constants.COLUMN_PO_LINE_PO_ID + " = ?", new String[]{poId}, null, null, null);
        if (c != null && c.moveToFirst()) {
            do { list.add(PurchaseLine.fromCursor(c)); } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    /**
     * 获取待审批/待处理的采购单。
     *
     * @return 状态为 PENDING 的采购单列表（按创建时间倒序）
     */
    public List<PurchaseOrder> getPendingPurchaseOrders() {
        List<PurchaseOrder> list = new ArrayList<>();
        Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, null, Constants.COLUMN_PO_STATUS + " = ?", new String[]{Constants.PO_STATUS_PENDING}, null, null, Constants.COLUMN_PO_CREATED_AT + " DESC");
        if (c != null && c.moveToFirst()) {
            do { list.add(PurchaseOrder.fromCursor(c)); } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    /**
     * 更新采购明细行。
     *
     * <p>限制：若父采购单已入库（RECEIVED），则禁止修改。</p>
     *
     * @param line 采购明细行
     * @return 受影响行数
     */
    public int updatePurchaseLine(PurchaseLine line) {
        if (line == null || line.getId() == null) return 0;
        if (ctx != null && !Auth.hasPermission(ctx, Constants.PERM_CREATE_PO)) return 0;
        // 若父采购单已入库（RECEIVED），则禁止修改明细行
        if (line.getPoId() != null) {
            Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, new String[]{Constants.COLUMN_PO_STATUS}, Constants.COLUMN_PO_ID + " = ?", new String[]{line.getPoId()}, null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    int idx = c.getColumnIndex(Constants.COLUMN_PO_STATUS);
                    if (idx != -1) {
                        String st = c.getString(idx);
                        if (Constants.PO_STATUS_RECEIVED.equalsIgnoreCase(st)) { c.close(); return 0; }
                    }
                }
                c.close();
            }
        }
        ContentValues v = line.toContentValues();
        v.remove(Constants.COLUMN_PO_LINE_ID);
        int rows = db.update(Constants.TABLE_PURCHASE_LINES, v, Constants.COLUMN_PO_LINE_ID + " = ?", new String[]{line.getId()});
        if (rows > 0) {
            try {
                String uid = null, urole = null;
                if (prefsManager != null) { uid = prefsManager.getUserId(); urole = prefsManager.getUserRole(); }
                Audit.writeSystemAudit(db, uid, urole, "purchase_line:" + line.getId(), "update", "update_line");
            } catch (Exception ignored) {}
        }
        return rows;
    }

    /**
     * 删除采购明细行。
     *
     * <p>限制：若父采购单已入库（RECEIVED），则禁止删除。</p>
     *
     * @param lineId 明细行 id
     * @return 删除行数
     */
    public int deletePurchaseLine(String lineId) {
        if (lineId == null) return 0;
        if (ctx != null && !Auth.hasPermission(ctx, Constants.PERM_CREATE_PO)) return 0;
        // 找到父采购单并检查状态：已入库的采购单不允许删除明细
        Cursor c = db.query(Constants.TABLE_PURCHASE_LINES, new String[]{Constants.COLUMN_PO_LINE_PO_ID}, Constants.COLUMN_PO_LINE_ID + " = ?", new String[]{lineId}, null, null, null);
        String poId = null;
        if (c != null) {
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex(Constants.COLUMN_PO_LINE_PO_ID);
                if (idx != -1) poId = c.getString(idx);
            }
            c.close();
        }
        if (poId != null) {
            Cursor pc = db.query(Constants.TABLE_PURCHASE_ORDERS, new String[]{Constants.COLUMN_PO_STATUS}, Constants.COLUMN_PO_ID + " = ?", new String[]{poId}, null, null, null);
            if (pc != null) {
                if (pc.moveToFirst()) {
                    int idx = pc.getColumnIndex(Constants.COLUMN_PO_STATUS);
                    if (idx != -1) {
                        String st = pc.getString(idx);
                        if (Constants.PO_STATUS_RECEIVED.equalsIgnoreCase(st)) { pc.close(); return 0; }
                    }
                }
                pc.close();
            }
        }
        int deleted = db.delete(Constants.TABLE_PURCHASE_LINES, Constants.COLUMN_PO_LINE_ID + " = ?", new String[]{lineId});
        if (deleted > 0) {
            try {
                String uid = null, urole = null;
                if (prefsManager != null) { uid = prefsManager.getUserId(); urole = prefsManager.getUserRole(); }
                Audit.writeSystemAudit(db, uid, urole, "purchase_line:" + lineId, "delete", "delete_line");
            } catch (Exception ignored) {}
        }
        return deleted;
    }

    /**
     * 更新采购单主表信息。
     *
     * <p>限制：已入库（RECEIVED）的采购单不允许再被修改。</p>
     *
     * @param po 采购单
     * @return 受影响行数
     */
    public int updatePurchaseOrder(PurchaseOrder po) {
        if (po == null || po.getId() == null) return 0;
        if (ctx != null && !Auth.hasPermission(ctx, Constants.PERM_CREATE_PO)) return 0;
        // 已入库（RECEIVED）的采购单不允许再被修改
        Cursor c = db.query(Constants.TABLE_PURCHASE_ORDERS, new String[]{Constants.COLUMN_PO_STATUS}, Constants.COLUMN_PO_ID + " = ?", new String[]{po.getId()}, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex(Constants.COLUMN_PO_STATUS);
                    if (idx != -1) {
                        String st = c.getString(idx);
                        if (Constants.PO_STATUS_RECEIVED.equalsIgnoreCase(st)) { c.close(); return 0; }
                    }
            }
            c.close();
        }
        ContentValues v = po.toContentValues();
        v.remove(Constants.COLUMN_PO_ID);
        int rows = db.update(Constants.TABLE_PURCHASE_ORDERS, v, Constants.COLUMN_PO_ID + " = ?", new String[]{po.getId()});
        if (rows > 0) {
            try {
                String uid = null, urole = null;
                if (prefsManager != null) { uid = prefsManager.getUserId(); urole = prefsManager.getUserRole(); }
                Audit.writeSystemAudit(db, uid, urole, "purchase_order:" + po.getId(), "update", "update_po");
            } catch (Exception ignored) {}
        }
        return rows;
    }

    /**
     * 事务化“接收入库”：逐行增加库存，最后将采购单标记为 RECEIVED。
     *
     * <p>注意：
     * 1) 该方法会开启事务并在成功时提交；
     * 2) 会调用 {@link InventoryDAO#receivePurchase(String, int)} 对每一行执行入库；
     * 3) 需要 RECEIVE_PO 权限。</p>
     *
     * @param poId 采购单 id
     * @return 成功返回 true；任何一步失败返回 false
     */
    public boolean receiveAndMatchPo(String poId) {
        if (poId == null) return false;
        // 权限检查：接收 PO 需要 RECEIVE_PO
        if (ctx != null && !Auth.hasPermission(ctx, Constants.PERM_RECEIVE_PO)) return false;
        db.beginTransaction();
        boolean allOk = true;
        try {
            List<PurchaseLine> lines = getLinesForPo(poId);
            for (PurchaseLine l : lines) {
                boolean ok = inventoryDAO.receivePurchase(l.getProductId(), l.getQty());
                if (!ok) { allOk = false; break; }
            }
            if (!allOk) {
                return false;
            }
            ContentValues v = new ContentValues();
            v.put(Constants.COLUMN_PO_STATUS, Constants.PO_STATUS_RECEIVED);
                db.update(Constants.TABLE_PURCHASE_ORDERS, v, Constants.COLUMN_PO_ID + " = ?", new String[]{poId});
                try {
                    String uid = null, urole = null;
                    if (prefsManager != null) { uid = prefsManager.getUserId(); urole = prefsManager.getUserRole(); }
                    Audit.writeSystemAudit(db, uid, urole, "purchase_order:" + poId, "receive", "receive_and_match");
                } catch (Exception ignored) {}
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { db.endTransaction(); } catch (Exception ignored) {}
        }
    }
}
