package com.example.android_development.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.EditText;
import com.example.android_development.R;
import com.example.android_development.util.PrefsManager;
import com.example.android_development.security.Auth;
import com.example.android_development.util.Constants;
import com.example.android_development.activities.adapter.PoLineAdapter;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.database.PurchaseDAO;
import com.example.android_development.database.SupplierDAO;
import com.example.android_development.model.Product;
import com.example.android_development.model.PurchaseLine;
import com.example.android_development.model.PurchaseOrder;
import com.example.android_development.model.Supplier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 采购单详情页面。
 *
 * <p>用于查看与编辑采购单头信息与明细行，并根据采购单状态与当前用户权限显示不同操作：
 * 保存/提交/批准/拒绝/收货入库/查看审批历史等。
 * 明细行变更会触发合计实时计算；关键写操作通常在事务中执行以保证一致性。</p>
 */
public class PurchaseDetailActivity extends AppCompatActivity {
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private PurchaseDAO purchaseDAO;
    private ProductDAO productDAO;
    private SupplierDAO supplierDAO;

    private Spinner spSupplier;
    private ListView lvLines;
    private Button btnSave, btnApprove, btnReceive, btnAddLine;
    private Button btnSubmit, btnHistory;
    private Button btnReject;

    private PurchaseOrder po;
    private android.widget.TextView tvPoId;
    private android.widget.EditText etPoName;
    private android.widget.TextView tvPoStatus;
    private android.widget.TextView tvTotal;
    private List<PurchaseLine> lines = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private PoLineAdapter adapter;
    private Set<String> originalLineIds = new HashSet<>();

    /**
     * Activity 创建：加载采购单、初始化供应商与明细行数据，按权限与状态配置按钮，并绑定各操作事件。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_detail);
        String poId = getIntent().getStringExtra("po_id");

        dbh = new DatabaseHelper(this);
        db = dbh.getWritableDatabase();
        purchaseDAO = new PurchaseDAO(db, this);
        productDAO = new ProductDAO(db);
        supplierDAO = new SupplierDAO(db);

        spSupplier = findViewById(R.id.sp_supplier);
        lvLines = findViewById(R.id.lv_po_lines);
        btnSave = findViewById(R.id.btn_save_po);
        btnApprove = findViewById(R.id.btn_approve_po);
        btnReceive = findViewById(R.id.btn_receive_po);
        btnReject = findViewById(R.id.btn_reject_po);
        btnAddLine = findViewById(R.id.btn_add_line);
        btnSubmit = findViewById(R.id.btn_submit_po);
        btnHistory = findViewById(R.id.btn_history_po);

        po = purchaseDAO.getPurchaseOrderById(poId);
        if (po == null) { Toast.makeText(this, "采购单不存在", Toast.LENGTH_SHORT).show(); finish(); return; }

        List<Supplier> supList = supplierDAO.getAllSuppliers();
        List<String> names = new ArrayList<>();
        int sel = -1;
        for (int i=0;i<supList.size();i++) { names.add(supList.get(i).getName()); if (supList.get(i).getId().equals(po.getSupplierId())) sel = i; }
        spSupplier.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
        if (sel != -1) spSupplier.setSelection(sel);

        tvPoId = findViewById(R.id.tv_po_id);
        etPoName = findViewById(R.id.et_po_name);
        tvPoStatus = findViewById(R.id.tv_po_status);
        tvTotal = findViewById(R.id.tv_total);

        // 填充采购单头部信息
        tvPoId.setText("PO: " + (po.getId() == null ? "-" : po.getId()));
        etPoName.setText(po.getName() == null ? "" : po.getName());
        tvPoStatus.setText(po.getStatus() == null ? "" : po.getStatus().toUpperCase());
        tvTotal.setText(String.format("%.2f", po.getTotal()));

        // 按状态设置状态标签颜色
        try { applyStatusBadge(tvPoStatus, po.getStatus()); } catch (Exception ignored) {}

        // 加载采购明细行
        lines = purchaseDAO.getLinesForPo(po.getId());
        products.clear();
        originalLineIds.clear();
        for (PurchaseLine l : lines) {
            if (l.getId() != null) originalLineIds.add(l.getId());
            Product p = productDAO.getProductById(l.getProductId());
            products.add(p == null ? new Product() : p);
        }

        // 根据明细行计算初始合计，确保 UI 与当前明细一致
        double initialTotal = 0.0;
        for (PurchaseLine l : lines) initialTotal += l.getQty() * l.getPrice();
        po.setTotal(initialTotal);

        boolean editableLines = !(po.getStatus() != null && po.getStatus().equalsIgnoreCase("received"));
        adapter = new com.example.android_development.activities.adapter.PoLineAdapter(this, lines, products, editableLines, () -> {
            // 当适配器回调“行已变更”时实时重算合计
            double t = 0.0;
            for (PurchaseLine l : lines) t += l.getQty() * l.getPrice();
            po.setTotal(t);
            if (tvTotal != null) tvTotal.setText(String.format("%.2f", po.getTotal()));
        });
        lvLines.setAdapter(adapter);

        // 角色权限控制（使用 Auth）
        PrefsManager prefs = new PrefsManager(this);
        android.util.Log.d("PurchaseDetail", "current userId=" + prefs.getUserId() + " role=" + prefs.getUserRole() + " po.status=" + po.getStatus());
        boolean canApprove = Auth.hasPermission(this, Constants.PERM_APPROVE_PO);
        boolean canReceive = Auth.hasPermission(this, Constants.PERM_RECEIVE_PO);
        boolean canSave = Auth.hasPermission(this, Constants.PERM_CREATE_PO);
        boolean canSubmit = Auth.hasPermission(this, Constants.PERM_SUBMIT_PO);
        boolean canViewHistory = Auth.hasPermission(this, Constants.PERM_VIEW_AUDIT);
        btnApprove.setEnabled(canApprove);
        btnReject.setEnabled(canApprove);
        btnReceive.setEnabled(canReceive);
        btnSave.setEnabled(canSave);
        btnSubmit.setEnabled(canSubmit);
        btnHistory.setEnabled(canViewHistory);

        // 动态显示/隐藏按钮，基于采购单状态和权限
        String status = po.getStatus() == null ? "" : po.getStatus().toLowerCase();
        // 默认隐藏所有操作按钮，再按状态/权限打开相应按钮
        btnSave.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        btnApprove.setVisibility(View.GONE);
        btnReject.setVisibility(View.GONE);
        btnReceive.setVisibility(View.GONE);
        btnAddLine.setVisibility(View.GONE);

        if ("".equals(status) || "created".equalsIgnoreCase(status) || "draft".equalsIgnoreCase(status) || "open".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status)) {
            // 新建/草稿：可保存、添加行、可提交
            if (canSave) btnSave.setVisibility(View.VISIBLE);
            btnAddLine.setVisibility(View.VISIBLE);
            if (canSubmit) btnSubmit.setVisibility(View.VISIBLE);
        } else if ("submitted".equalsIgnoreCase(status)) {
            // 已提交：审批角色可批准/拒绝
            if (canApprove) {
                btnApprove.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
            }
            // 提交后不允许编辑行
        } else if ("approved".equalsIgnoreCase(status)) {
            // 批准后可收货
            if (canReceive) btnReceive.setVisibility(View.VISIBLE);
        } else if ("received".equalsIgnoreCase(status)) {
            // 已入库，不显示操作按钮
            // 保持全部隐藏
        } else {
            // 其它状态，允许查看历史
        }

        // 如果为 open 状态且当前用户有批准权限，则同时展示批准/拒绝（在某些流程中 admin 可直接批准 open PO）
        if ("open".equalsIgnoreCase(status) && canApprove) {
            btnApprove.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);
        }

        // 如果采购单已完成（received），禁用所有编辑操作以防止被修改
        if (po.getStatus() != null && po.getStatus().equalsIgnoreCase("received")) {
            btnSave.setEnabled(false);
            btnApprove.setEnabled(false);
            btnReceive.setEnabled(false);
            btnAddLine.setEnabled(false);
            Toast.makeText(this, getString(R.string.po_completed_cannot_edit_long), Toast.LENGTH_LONG).show();
        }

        btnSave.setOnClickListener(v -> {
            if (po.getStatus() != null && po.getStatus().equalsIgnoreCase("received")) {
                Toast.makeText(this, getString(R.string.po_completed_cannot_edit), Toast.LENGTH_SHORT).show();
                return;
            }
            int idx = spSupplier.getSelectedItemPosition();
            if (idx >= 0 && idx < supList.size()) po.setSupplierId(supList.get(idx).getId());
            // 从输入框保存采购单名称
            if (etPoName != null) po.setName(etPoName.getText() == null ? null : etPoName.getText().toString().trim());

            // 提交列表中“正在编辑但未失焦”的输入（强制失焦触发监听器保存值）
            try { findViewById(android.R.id.content).requestFocus(); } catch (Exception ignored) {}

            // 校验：每一行必须有商品与有效数量
            for (int i = 0; i < lines.size(); i++) {
                PurchaseLine l = lines.get(i);
                if (l.getProductId() == null || l.getProductId().trim().isEmpty()) {
                    Toast.makeText(this, getString(R.string.po_line_missing_product, i+1), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (l.getQty() <= 0) {
                    Toast.makeText(this, "第 " + (i+1) + " 行数量必须大于0", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // 事务内持久化明细（新增/更新），并同步删除已移除的行
            db.beginTransaction();
            try {
                // 校验采购单名称（若配置为必填）
                String nameInput = etPoName.getText() == null ? "" : etPoName.getText().toString().trim();
                if (com.example.android_development.util.Constants.PO_NAME_REQUIRED && (nameInput.isEmpty())) {
                    Toast.makeText(this, "采购单名称为必填项", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (nameInput.length() > com.example.android_development.util.Constants.PO_NAME_MAX_LENGTH) {
                    Toast.makeText(this, "采购单名称过长（最大 " + com.example.android_development.util.Constants.PO_NAME_MAX_LENGTH + " 字符）", Toast.LENGTH_SHORT).show();
                    return;
                }
                double total = 0;
                Set<String> currentIds = new HashSet<>();
                for (PurchaseLine l : lines) {
                    if (l.getId() == null || l.getId().trim().isEmpty()) {
                        purchaseDAO.addPurchaseLine(l);
                    } else {
                        purchaseDAO.updatePurchaseLine(l);
                    }
                    if (l.getId() != null) currentIds.add(l.getId());
                    total += l.getQty() * l.getPrice();
                }

                // 删除已从界面移除的旧明细行
                for (String oldId : new ArrayList<>(originalLineIds)) {
                    if (!currentIds.contains(oldId)) purchaseDAO.deletePurchaseLine(oldId);
                }

                po.setTotal(total);
                po.setName(etPoName.getText() == null ? null : etPoName.getText().toString().trim());
                purchaseDAO.updatePurchaseOrder(po);

                db.setTransactionSuccessful();
            } catch (Exception e) {
                android.util.Log.e("PurchaseDetail", "保存采购单出错", e);
            } finally {
                db.endTransaction();
            }
            // 刷新 UI 合计（确保以数据库中最新明细为准）
            double liveTotal = 0.0;
            for (PurchaseLine l : purchaseDAO.getLinesForPo(po.getId())) liveTotal += l.getQty() * l.getPrice();
            po.setTotal(liveTotal);
            tvTotal.setText(String.format("%.2f", po.getTotal()));
            // 刷新 originalLineIds（用于后续对比删除）
            originalLineIds.clear();
            for (PurchaseLine l : purchaseDAO.getLinesForPo(po.getId())) if (l.getId() != null) originalLineIds.add(l.getId());

            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
        });

        btnApprove.setOnClickListener(v -> {
            String uid = prefs.getUserId();
            showCommentDialog("批准采购单", comment -> {
                boolean ok = purchaseDAO.approvePo(po.getId(), uid, comment == null ? "" : comment);
                if (ok) {
                    Toast.makeText(this, "已批准", Toast.LENGTH_SHORT).show();
                } else {
                    String msg = com.example.android_development.util.DaoResult.getMessage();
                    if (msg == null || msg.isEmpty()) msg = "批准失败";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnReject.setOnClickListener(v -> {
            String uid = prefs.getUserId();
            showCommentDialog("拒绝并备注", comment -> {
                boolean ok = purchaseDAO.rejectPo(po.getId(), uid, comment == null ? "" : comment);
                if (ok) {
                    Toast.makeText(this, "已拒绝", Toast.LENGTH_SHORT).show();
                } else {
                    String msg = com.example.android_development.util.DaoResult.getMessage();
                    if (msg == null || msg.isEmpty()) msg = "拒绝失败";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnSubmit.setOnClickListener(v -> {
            // UI 侧拦截：空采购单不允许提交
            if (lines == null || lines.isEmpty()) {
                Toast.makeText(this, "空的采购单不能提交", Toast.LENGTH_SHORT).show();
                return;
            }
            // 提交前校验采购单名称
            String nameInput = etPoName.getText() == null ? "" : etPoName.getText().toString().trim();
            if (com.example.android_development.util.Constants.PO_NAME_REQUIRED && (nameInput.isEmpty())) {
                Toast.makeText(this, "采购单名称为必填项", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nameInput.length() > com.example.android_development.util.Constants.PO_NAME_MAX_LENGTH) {
                Toast.makeText(this, "采购单名称过长（最大 " + com.example.android_development.util.Constants.PO_NAME_MAX_LENGTH + " 字符）", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean ok = purchaseDAO.submitPo(po.getId());
            android.util.Log.d("PurchaseDetail", "submitPo returned=" + ok + " for po=" + po.getId());
            PurchaseOrder updated = purchaseDAO.getPurchaseOrderById(po.getId());
            android.util.Log.d("PurchaseDetail", "after submit, po.status=" + (updated == null ? "<null>" : updated.getStatus()));
            if (ok) {
                Toast.makeText(this, "已提交", Toast.LENGTH_SHORT).show();
            } else {
                String msg = com.example.android_development.util.DaoResult.getMessage();
                if (msg == null || msg.isEmpty()) msg = "提交失败";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        btnHistory.setOnClickListener(v -> {
            List<android.content.ContentValues> history = purchaseDAO.getApprovalHistory(po.getId());
            android.view.View dlgView = getLayoutInflater().inflate(R.layout.dialog_approval_history, null);
            android.widget.ListView lv = dlgView.findViewById(R.id.lv_approval_history);
            com.example.android_development.activities.adapter.PoApprovalAdapter adapter = new com.example.android_development.activities.adapter.PoApprovalAdapter(this, history);
            lv.setAdapter(adapter);
            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
            b.setTitle("审批历史");
            b.setView(dlgView);
            b.setPositiveButton("关闭", null);
            b.show();
        });

        btnReceive.setOnClickListener(v -> {
            boolean ok = false;
            db.beginTransaction();
            try {
                ok = purchaseDAO.receiveAndMatchPo(po.getId());
                if (ok) db.setTransactionSuccessful();
            } catch (Exception e) {
                android.util.Log.e("PurchaseDetail", "入库出错", e);
                ok = false;
            } finally {
                db.endTransaction();
            }
            if (ok) Toast.makeText(this, getString(R.string.purchase_receive_success), Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, getString(R.string.receive_failed), Toast.LENGTH_SHORT).show();
            finish();
        });

        btnAddLine.setOnClickListener(v -> {
            PurchaseLine nl = new PurchaseLine();
            nl.setPoId(po.getId());
            nl.setProductId("");
            nl.setQty(1);
            nl.setPrice(0.0);
            lines.add(nl);
            products.add(new Product());
            adapter.notifyDataSetChanged();
            // 通知变更：触发合计重新计算
            if (adapter != null) adapter.setOnLinesChangeListener(() -> {
                double t = 0.0; for (PurchaseLine l : lines) t += l.getQty() * l.getPrice(); po.setTotal(t); if (tvTotal != null) tvTotal.setText(String.format("%.2f", po.getTotal()));
            });
        });
    }

    /**
     * 审批操作回调：用于提交“备注/审批意见”等文本。
     */
    private interface ApprovalCallback {
        void onSubmit(String comment);
    }

    /**
     * 弹出备注输入对话框，并将输入内容回传给回调。
     */
    private void showCommentDialog(String title, ApprovalCallback cb) {
        final EditText input = new EditText(this);
        input.setSingleLine(false);
        input.setLines(3);
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
        b.setTitle(title);
        b.setView(input);
        b.setPositiveButton("确定", (dialog, which) -> {
            String comment = input.getText() == null ? "" : input.getText().toString();
            try { cb.onSubmit(comment); } catch (Exception ignored) {}
        });
        b.setNegativeButton("取消", null);
        b.show();
    }

    /**
     * 根据采购单状态设置状态标签的背景/前景色。
     *
     * <p>仅影响 UI 展示，不改变业务状态。</p>
     */
    private void applyStatusBadge(android.widget.TextView v, String status) {
        if (v == null) return;
        if (status == null) status = "";
        status = status.toLowerCase();
        int bg = android.graphics.Color.DKGRAY;
        int fg = android.graphics.Color.WHITE;
        switch (status) {
            case "approved": bg = android.graphics.Color.parseColor("#C8E6C9"); fg = android.graphics.Color.parseColor("#1B5E20"); break;
            case "received": bg = android.graphics.Color.parseColor("#BBDEFB"); fg = android.graphics.Color.parseColor("#0D47A1"); break;
            case "rejected": bg = android.graphics.Color.parseColor("#FFCDD2"); fg = android.graphics.Color.parseColor("#B71C1C"); break;
            case "submitted": bg = android.graphics.Color.parseColor("#FFE0B2"); fg = android.graphics.Color.parseColor("#E65100"); break;
            default: bg = android.graphics.Color.LTGRAY; fg = android.graphics.Color.DKGRAY; break;
        }
        try {
            v.setBackgroundColor(bg);
            v.setTextColor(fg);
        } catch (Exception ignored) {}
    }
}
