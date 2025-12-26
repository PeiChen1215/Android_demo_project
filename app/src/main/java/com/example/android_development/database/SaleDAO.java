package com.example.android_development.database;

import android.content.ContentValues;
import android.content.Context;
import com.example.android_development.util.PrefsManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.model.Sale;
import com.example.android_development.model.SaleLine;
import com.example.android_development.util.Constants;
import com.example.android_development.util.Audit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.android_development.model.SalesSummary;
import java.util.HashMap;
import java.util.Map;

/**
 * 销售/收银相关 DAO。
 *
 * <p>负责销售单（sales）与销售明细（sale_lines）的读写，支持退款、报表明细与日/月汇总查询。
 * 涉及库存变更的操作会调用 {@link InventoryDAO} 写入库存事务与审计记录。</p>
 */
public class SaleDAO {
    private SQLiteDatabase db;
    private PrefsManager prefsManager;
    private android.content.Context ctx;

    public SaleDAO(SQLiteDatabase db) { this.db = db; }

    public SaleDAO(SQLiteDatabase db, Context ctx) {
        this.db = db;
        this.ctx = ctx;
        if (ctx != null) this.prefsManager = new PrefsManager(ctx);
    }

    /**
     * 新增一笔销售（含销售明细行）。
     *
     * <p>行为：
     * 1) 会在事务中写入 sales 与 sale_lines；
     * 2) 对每一条明细行调用库存扣减（货架库存），并写入审计；
     * 3) 需要具备调整库存权限（PERM_ADJUST_STOCK）。</p>
     *
     * @param sale 销售单对象（包含 lines）
     * @return 插入结果；失败返回 -1
     */
    public long addSale(Sale sale) {
        if (sale == null) return -1;
        // 权限检查：销售创建需要调整库存权限（收银员/管理员）
        if (ctx != null && !com.example.android_development.security.Auth.hasPermission(ctx, com.example.android_development.util.Constants.PERM_ADJUST_STOCK)) {
            com.example.android_development.util.DaoResult.setError(com.example.android_development.util.DaoResult.ERR_PERMISSION, "no permission to create sale");
            return -1;
        }
        db.beginTransaction();
        try {
            if (sale.getId() == null || sale.getId().isEmpty()) sale.setId(UUID.randomUUID().toString());
            long now = System.currentTimeMillis();
            sale.setTimestamp(now);

            ContentValues v = sale.toContentValues();
            long res = db.insert(Constants.TABLE_SALES, null, v);
            if (res == -1) throw new Exception("Failed to insert sale");

            // 插入销售明细行（sale_lines），并对每行执行扣减库存（货架库存）
            if (sale.getLines() != null) {
                for (SaleLine l : sale.getLines()) {
                    if (l.getId() == null || l.getId().isEmpty()) l.setId(UUID.randomUUID().toString());
                    l.setSaleId(sale.getId());
                    db.insert(Constants.TABLE_SALE_LINES, null, l.toContentValues());

                    // 使用 InventoryDAO 提供的并发保护调整并写审计
                    boolean ok = InventoryDAO.adjustShelfStock(db, prefsManager, l.getProductId(), -l.getQty(), "sale", "OUT");
                    if (!ok) {
                        throw new Exception("Failed to adjust stock for product " + l.getProductId());
                    }
                }
            }
            db.setTransactionSuccessful();
            // 写入操作审计（记录销售单创建）
            try {
                String uid = null, urole = null;
                if (prefsManager != null) { uid = prefsManager.getUserId(); urole = prefsManager.getUserRole(); }
                Audit.writeSystemAudit(db, uid, urole, "sale:" + sale.getId(), "create", "create_sale");
            } catch (Exception ignored) {}
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 根据销售单 id 获取销售单（包含明细行）。
     *
     * @param saleId 销售单 id
     * @return 销售单对象；未找到返回 null
     */
    public Sale getSaleById(String saleId) {
        String sel = Constants.COLUMN_SALE_ID + " = ?";
        String[] args = new String[]{saleId};
        Cursor c = db.query(Constants.TABLE_SALES, null, sel, args, null, null, null);
        Sale s = null;
        if (c != null) {
            if (c.moveToFirst()) s = Sale.fromCursor(c);
            c.close();
        }
        if (s != null) {
            s.setLines(getLinesForSale(s.getId()));
        }
        return s;
    }

    /**
     * 获取指定销售单的明细行。
     *
     * @param saleId 销售单 id
     * @return 明细行列表
     */
    public List<SaleLine> getLinesForSale(String saleId) {
        List<SaleLine> list = new ArrayList<>();
        String sel = Constants.COLUMN_SALE_LINE_SALE_ID + " = ?";
        String[] args = new String[]{saleId};
        Cursor c = db.query(Constants.TABLE_SALE_LINES, null, sel, args, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(SaleLine.fromCursor(c));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    /**
     * 获取最近的销售单列表（包含明细行）。
     *
     * @param limit 返回条数上限
     * @return 最近销售单列表（按时间倒序）
     */
    public List<Sale> getRecentSales(int limit) {
        List<Sale> list = new ArrayList<>();
        String order = Constants.COLUMN_SALE_TIMESTAMP + " DESC";
        String lim = String.valueOf(limit);
        Cursor c = db.query(Constants.TABLE_SALES, null, null, null, null, null, order, lim);
        if (c != null && c.moveToFirst()) {
            do {
                Sale s = Sale.fromCursor(c);
                s.setLines(getLinesForSale(s.getId()));
                list.add(s);
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    /**
     * 返回指定时间段内的明细条目。
     *
     * <p>口径：
     * - 销售：正数；
     * - 退款：按“原销售时间”计入（refunds JOIN sales），金额为负数；
     * - 采购：按 created_at 计入该时间段，作为支出金额为负数。</p>
     *
     * @param startMillis 开始时间（毫秒时间戳）
     * @param endMillis 结束时间（毫秒时间戳）
     * @return 明细列表（ContentValues，包含 type/id/amount/ts 等字段）
     */
    public List<android.content.ContentValues> getDetailedEntriesForPeriod(long startMillis, long endMillis) {
        List<android.content.ContentValues> list = new ArrayList<>();
        // 1) 销售：正数
        Cursor sc = db.query(Constants.TABLE_SALES, null, Constants.COLUMN_SALE_TIMESTAMP + " BETWEEN ? AND ?", new String[]{String.valueOf(startMillis), String.valueOf(endMillis)}, null, null, Constants.COLUMN_SALE_TIMESTAMP + " DESC");
        if (sc != null && sc.moveToFirst()) {
            do {
                android.content.ContentValues cv = new android.content.ContentValues();
                String sid = sc.getString(sc.getColumnIndexOrThrow(Constants.COLUMN_SALE_ID));
                double total = sc.getDouble(sc.getColumnIndexOrThrow(Constants.COLUMN_SALE_TOTAL));
                long ts = sc.getLong(sc.getColumnIndexOrThrow(Constants.COLUMN_SALE_TIMESTAMP));
                cv.put("type", "sale");
                cv.put("id", sid);
                cv.put("amount", total);
                cv.put("ts", ts);
                list.add(cv);
            } while (sc.moveToNext());
            sc.close();
        }

        // 2) 退款：按“原销售时间”计入该时间段（refunds JOIN sales），金额取负
        String rsql = "SELECT r.* , s." + Constants.COLUMN_SALE_TIMESTAMP + " as sale_ts FROM " + Constants.TABLE_REFUNDS + " r JOIN " + Constants.TABLE_SALES + " s ON r." + Constants.COLUMN_REFUND_SALE_ID + " = s." + Constants.COLUMN_SALE_ID + " WHERE s." + Constants.COLUMN_SALE_TIMESTAMP + " BETWEEN ? AND ? ORDER BY s." + Constants.COLUMN_SALE_TIMESTAMP + " DESC";
        Cursor rc = db.rawQuery(rsql, new String[]{String.valueOf(startMillis), String.valueOf(endMillis)});
        if (rc != null && rc.moveToFirst()) {
            do {
                android.content.ContentValues cv = new android.content.ContentValues();
                String rid = rc.getString(rc.getColumnIndexOrThrow(Constants.COLUMN_REFUND_ID));
                double amt = rc.getDouble(rc.getColumnIndexOrThrow(Constants.COLUMN_REFUND_AMOUNT));
                long saleTs = rc.getLong(rc.getColumnIndexOrThrow("sale_ts"));
                String reason = null;
                int idx = rc.getColumnIndex(Constants.COLUMN_REFUND_REASON);
                if (idx != -1) reason = rc.getString(idx);
                cv.put("type", "refund");
                cv.put("id", rid);
                cv.put("amount", -amt);
                cv.put("ts", saleTs);
                if (reason != null) cv.put("reason", reason);
                list.add(cv);
            } while (rc.moveToNext());
            rc.close();
        }

        // 3) 采购：按 created_at 计入该时间段，作为支出金额取负
        Cursor pc = db.query(Constants.TABLE_PURCHASE_ORDERS, null, Constants.COLUMN_PO_CREATED_AT + " BETWEEN ? AND ?", new String[]{String.valueOf(startMillis), String.valueOf(endMillis)}, null, null, Constants.COLUMN_PO_CREATED_AT + " DESC");
        if (pc != null && pc.moveToFirst()) {
            do {
                android.content.ContentValues cv = new android.content.ContentValues();
                String pid = pc.getString(pc.getColumnIndexOrThrow(Constants.COLUMN_PO_ID));
                double total = 0.0;
                int idx = pc.getColumnIndex(Constants.COLUMN_PO_TOTAL);
                if (idx != -1) total = pc.getDouble(idx);
                long ts = pc.getLong(pc.getColumnIndexOrThrow(Constants.COLUMN_PO_CREATED_AT));
                cv.put("type", "purchase");
                cv.put("id", pid);
                cv.put("amount", -total);
                cv.put("ts", ts);
                list.add(cv);
            } while (pc.moveToNext());
            pc.close();
        }

        return list;
    }

    /**
     * 退单（退款/撤销销售）。
     *
     * <p>行为：恢复库存（货架库存）并将销售单标记为已退款，同时写入退款记录与审计。</p>
     *
     * @param saleId 销售单 id
     * @param reason 退款原因（可为空）
     * @return 成功返回 true；失败返回 false（含已退款的重复退款）
     */
    public boolean refundSale(String saleId, String reason) {
        if (saleId == null || saleId.isEmpty()) return false;
        // 权限检查
        if (ctx != null && !com.example.android_development.security.Auth.hasPermission(ctx, com.example.android_development.util.Constants.PERM_REFUND)) {
            com.example.android_development.util.DaoResult.setError(com.example.android_development.util.DaoResult.ERR_PERMISSION, "no permission to refund");
            return false;
        }
        db.beginTransaction();
        try {
            Sale s = getSaleById(saleId);
            if (s == null) return false;
            if (s.isRefunded()) return false; // 已退款则不允许重复退款

            // 恢复库存：对每一行进行入库（货架）操作
            for (SaleLine l : s.getLines()) {
                boolean ok = InventoryDAO.adjustShelfStock(db, prefsManager, l.getProductId(), l.getQty(), "refund", "IN");
                if (!ok) throw new Exception("Failed to restore stock for product " + l.getProductId());
            }

            // 标记已退款
            android.content.ContentValues v = new android.content.ContentValues();
            v.put(com.example.android_development.util.Constants.COLUMN_SALE_REFUNDED, 1);
            long now = System.currentTimeMillis();
            v.put(com.example.android_development.util.Constants.COLUMN_SALE_REFUNDED_AT, now);
            int updated = db.update(com.example.android_development.util.Constants.TABLE_SALES, v, com.example.android_development.util.Constants.COLUMN_SALE_ID + " = ?", new String[]{saleId});
            if (updated <= 0) throw new Exception("Failed to mark sale refunded");

            try {
                String uid = null, urole = null;
                if (prefsManager != null) { uid = prefsManager.getUserId(); urole = prefsManager.getUserRole(); }
                // 写退款记录
                com.example.android_development.model.RefundRecord rr = new com.example.android_development.model.RefundRecord();
                rr.setId(java.util.UUID.randomUUID().toString());
                rr.setSaleId(saleId);
                // 退款金额应为顾客实际支付的销售金额（不包括找零），即使用销售总额 total
                rr.setAmount(s.getTotal());
                rr.setUserId(uid);
                rr.setUserRole(urole);
                rr.setReason(reason);
                rr.setTimestamp(now);
                db.insert(com.example.android_development.util.Constants.TABLE_REFUNDS, null, rr.toContentValues());

                com.example.android_development.util.Audit.writeSystemAudit(db, uid, urole, "sale:" + saleId, "refund", "refund_sale");
            } catch (Exception ignored) {}

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 按天汇总。
     *
     * <p>periodLabel 格式：yyyy-MM-dd。汇总口径：销售 +（退款为负）+（采购为负）。</p>
     *
     * @param startMillis 开始时间（毫秒时间戳）
     * @param endMillis 结束时间（毫秒时间戳）
     * @return 按天汇总列表（倒序）
     */
    public List<SalesSummary> getDailySalesSummary(long startMillis, long endMillis) {
        List<SalesSummary> list = new ArrayList<>();
        // 合并销售和退款，但退款按原销售时间计入（使用 refunds JOIN sales，退款金额取负）
        String sql = "SELECT period, SUM(amount) as total, SUM(cnt) as cnt FROM (" +
            "  SELECT strftime('%Y-%m-%d', s." + Constants.COLUMN_SALE_TIMESTAMP + "/1000, 'unixepoch', 'localtime') as period, SUM(s." + Constants.COLUMN_SALE_TOTAL + ") as amount, COUNT(*) as cnt FROM " + Constants.TABLE_SALES + " s WHERE s." + Constants.COLUMN_SALE_TIMESTAMP + " BETWEEN ? AND ? GROUP BY period" +
            "  UNION ALL" +
            "  SELECT strftime('%Y-%m-%d', s." + Constants.COLUMN_SALE_TIMESTAMP + "/1000, 'unixepoch', 'localtime') as period, -SUM(r." + Constants.COLUMN_REFUND_AMOUNT + ") as amount, 0 as cnt FROM " + Constants.TABLE_REFUNDS + " r JOIN " + Constants.TABLE_SALES + " s ON r." + Constants.COLUMN_REFUND_SALE_ID + " = s." + Constants.COLUMN_SALE_ID + " WHERE s." + Constants.COLUMN_SALE_TIMESTAMP + " BETWEEN ? AND ? GROUP BY period" +
            "  UNION ALL" +
            "  SELECT strftime('%Y-%m-%d', po." + Constants.COLUMN_PO_CREATED_AT + "/1000, 'unixepoch', 'localtime') as period, -SUM(po." + Constants.COLUMN_PO_TOTAL + ") as amount, 0 as cnt FROM " + Constants.TABLE_PURCHASE_ORDERS + " po WHERE po." + Constants.COLUMN_PO_CREATED_AT + " BETWEEN ? AND ? GROUP BY period" +
            ") GROUP BY period ORDER BY period DESC";
        String[] args = new String[]{String.valueOf(startMillis), String.valueOf(endMillis), String.valueOf(startMillis), String.valueOf(endMillis), String.valueOf(startMillis), String.valueOf(endMillis)};
        Cursor c = db.rawQuery(sql, args);
        if (c != null && c.moveToFirst()) {
            do {
                String period = c.getString(c.getColumnIndexOrThrow("period"));
                double total = c.getDouble(c.getColumnIndexOrThrow("total"));
                int cnt = c.getInt(c.getColumnIndexOrThrow("cnt"));
                list.add(new SalesSummary(period, total, cnt));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    /**
     * 按月汇总。
     *
     * <p>periodLabel 格式：yyyy-MM。汇总口径：销售 +（退款为负）+（采购为负）。</p>
     *
     * @param startMillis 开始时间（毫秒时间戳）
     * @param endMillis 结束时间（毫秒时间戳）
     * @return 按月汇总列表（倒序）
     */
    public List<SalesSummary> getMonthlySalesSummary(long startMillis, long endMillis) {
        List<SalesSummary> list = new ArrayList<>();
        String sql = "SELECT period, SUM(amount) as total, SUM(cnt) as cnt FROM (" +
            "  SELECT strftime('%Y-%m', s." + Constants.COLUMN_SALE_TIMESTAMP + "/1000, 'unixepoch', 'localtime') as period, SUM(s." + Constants.COLUMN_SALE_TOTAL + ") as amount, COUNT(*) as cnt FROM " + Constants.TABLE_SALES + " s WHERE s." + Constants.COLUMN_SALE_TIMESTAMP + " BETWEEN ? AND ? GROUP BY period" +
            "  UNION ALL" +
            "  SELECT strftime('%Y-%m', s." + Constants.COLUMN_SALE_TIMESTAMP + "/1000, 'unixepoch', 'localtime') as period, -SUM(r." + Constants.COLUMN_REFUND_AMOUNT + ") as amount, 0 as cnt FROM " + Constants.TABLE_REFUNDS + " r JOIN " + Constants.TABLE_SALES + " s ON r." + Constants.COLUMN_REFUND_SALE_ID + " = s." + Constants.COLUMN_SALE_ID + " WHERE s." + Constants.COLUMN_SALE_TIMESTAMP + " BETWEEN ? AND ? GROUP BY period" +
            "  UNION ALL" +
            "  SELECT strftime('%Y-%m', po." + Constants.COLUMN_PO_CREATED_AT + "/1000, 'unixepoch', 'localtime') as period, -SUM(po." + Constants.COLUMN_PO_TOTAL + ") as amount, 0 as cnt FROM " + Constants.TABLE_PURCHASE_ORDERS + " po WHERE po." + Constants.COLUMN_PO_CREATED_AT + " BETWEEN ? AND ? GROUP BY period" +
            ") GROUP BY period ORDER BY period DESC";
        String[] args = new String[]{String.valueOf(startMillis), String.valueOf(endMillis), String.valueOf(startMillis), String.valueOf(endMillis), String.valueOf(startMillis), String.valueOf(endMillis)};
        Cursor c = db.rawQuery(sql, args);
        if (c != null && c.moveToFirst()) {
            do {
                String period = c.getString(c.getColumnIndexOrThrow("period"));
                double total = c.getDouble(c.getColumnIndexOrThrow("total"));
                int cnt = c.getInt(c.getColumnIndexOrThrow("cnt"));
                list.add(new SalesSummary(period, total, cnt));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }
}
