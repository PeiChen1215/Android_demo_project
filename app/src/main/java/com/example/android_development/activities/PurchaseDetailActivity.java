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
import com.example.android_development.R;
import com.example.android_development.util.PrefsManager;
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

public class PurchaseDetailActivity extends AppCompatActivity {
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private PurchaseDAO purchaseDAO;
    private ProductDAO productDAO;
    private SupplierDAO supplierDAO;

    private Spinner spSupplier;
    private ListView lvLines;
    private Button btnSave, btnApprove, btnReceive, btnAddLine;

    private PurchaseOrder po;
    private List<PurchaseLine> lines = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private PoLineAdapter adapter;
    private Set<String> originalLineIds = new HashSet<>();

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
        btnAddLine = findViewById(R.id.btn_add_line);

        po = purchaseDAO.getPurchaseOrderById(poId);
        if (po == null) { Toast.makeText(this, "采购单不存在", Toast.LENGTH_SHORT).show(); finish(); return; }

        List<Supplier> supList = supplierDAO.getAllSuppliers();
        List<String> names = new ArrayList<>();
        int sel = -1;
        for (int i=0;i<supList.size();i++) { names.add(supList.get(i).getName()); if (supList.get(i).getId().equals(po.getSupplierId())) sel = i; }
        spSupplier.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
        if (sel != -1) spSupplier.setSelection(sel);

        // load lines
        lines = purchaseDAO.getLinesForPo(po.getId());
        products.clear();
        originalLineIds.clear();
        for (PurchaseLine l : lines) {
            if (l.getId() != null) originalLineIds.add(l.getId());
            Product p = productDAO.getProductById(l.getProductId());
            products.add(p == null ? new Product() : p);
        }

        boolean editableLines = !(po.getStatus() != null && po.getStatus().equalsIgnoreCase("received"));
        adapter = new PoLineAdapter(this, lines, products, editableLines);
        lvLines.setAdapter(adapter);

        // 角色权限控制
        PrefsManager prefs = new PrefsManager(this);
        String role = prefs.getUserRole();
        boolean canApprove = false;
        boolean canReceive = false;
        boolean canSave = false;
        if (role != null) {
            String r = role.trim().toLowerCase();
            if (r.equals("admin") || r.equals("系统管理员")) { canApprove = canReceive = canSave = true; }
            if (r.equals("purchaser") || r.equals("采购员")) { canApprove = false; canReceive = true; canSave = true; }
            if (r.equals("stock_manager") || r.equals("库存管理员")) { canApprove = true; canReceive = true; canSave = true; }
            if (r.equals("seller") || r.equals("售货员")) { canApprove = false; canReceive = false; canSave = true; }
        }
        btnApprove.setEnabled(canApprove);
        btnReceive.setEnabled(canReceive);
        btnSave.setEnabled(canSave);

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

            // Validation
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

            // Persist lines (add/update) inside a DB transaction
            db.beginTransaction();
            try {
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

                // delete removed lines
                for (String oldId : new ArrayList<>(originalLineIds)) {
                    if (!currentIds.contains(oldId)) purchaseDAO.deletePurchaseLine(oldId);
                }

                po.setTotal(total);
                purchaseDAO.updatePurchaseOrder(po);

                db.setTransactionSuccessful();
            } catch (Exception e) {
                android.util.Log.e("PurchaseDetail", "保存采购单出错", e);
            } finally {
                db.endTransaction();
            }
            // refresh original ids
            originalLineIds.clear();
            for (PurchaseLine l : purchaseDAO.getLinesForPo(po.getId())) if (l.getId() != null) originalLineIds.add(l.getId());

            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
        });

        btnApprove.setOnClickListener(v -> {
            po.setStatus("approved");
            db.beginTransaction();
            try {
                purchaseDAO.updatePurchaseOrder(po);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                android.util.Log.e("PurchaseDetail", "批准采购单失败", e);
            } finally {
                db.endTransaction();
            }
            Toast.makeText(this, "已批准", Toast.LENGTH_SHORT).show();
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
            nl.setPrice(0);
            lines.add(nl);
            products.add(new Product());
            adapter.notifyDataSetChanged();
        });
    }
}
