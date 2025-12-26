package com.example.android_development.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.content.Intent;
import com.example.android_development.adapters.PurchaseAdapter;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;

/**
 * 采购单列表页面。
 *
 * <p>提供采购单的查询与管理入口：
 * 支持关键字搜索、按状态筛选、按供应商筛选、排序，以及分页加载更多。
 * 点击某条采购单进入 {@link PurchaseDetailActivity} 查看/编辑/提交/审批/入库等流程。</p>
 */
public class PurchaseListActivity extends AppCompatActivity {
    /**
     * Activity 创建：初始化控件与数据源，并加载采购单列表。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_list);
        initViews();
        loadPurchaseOrders();
    }

    private RecyclerView listViewPurchases;
    private android.widget.EditText etSearch;
    private android.widget.Spinner spStatus;
    private android.widget.ImageButton buttonMore;
    private String supplierSelected = "全部";
    private String sortSelected = "按日期";
    private java.util.List<String> supNames = new java.util.ArrayList<>();
    private java.util.List<String> sorts = new java.util.ArrayList<>();
    private final java.util.Map<String,String> supIdByName = new java.util.HashMap<>();
    private android.widget.Button buttonLoadMore;
    private android.widget.TextView textViewNoPO;
    private android.widget.ImageButton buttonNewPurchase;
    private com.example.android_development.database.PurchaseDAO purchaseDAO;
    private com.example.android_development.database.SupplierDAO supplierDAO;
    private java.util.List<com.example.android_development.model.PurchaseOrder> allPOs = new java.util.ArrayList<>();
    private java.util.List<com.example.android_development.model.PurchaseOrder> filteredPOs = new java.util.ArrayList<>();
    private PurchaseAdapter adapter;
    private int pageSize = 20;
    private int currentOffset = 0;

    /**
     * 初始化页面控件与事件。
     *
     * <p>包含：RecyclerView、搜索框、状态筛选、更多筛选/排序弹窗、分页按钮。
     * 同时初始化 DAO 与筛选项数据源（供应商列表、排序列表）。</p>
     */
    private void initViews() {
        listViewPurchases = findViewById(R.id.listViewPurchases);
        textViewNoPO = findViewById(R.id.textViewNoPO);
        buttonNewPurchase = findViewById(R.id.buttonNewPurchase);
        etSearch = findViewById(R.id.etSearch);
        spStatus = findViewById(R.id.spStatus);
        buttonMore = findViewById(R.id.buttonMore);
        buttonLoadMore = findViewById(R.id.buttonLoadMore);

        if (listViewPurchases != null && listViewPurchases.getLayoutManager() == null) {
            listViewPurchases.setLayoutManager(new LinearLayoutManager(this));
        }

        DatabaseHelper db = new DatabaseHelper(this);
        purchaseDAO = new com.example.android_development.database.PurchaseDAO(db.getWritableDatabase());
        // supplierDAO 仅用于筛选弹窗的供应商列表；列表展示主要依赖采购单自身字段
        supplierDAO = new com.example.android_development.database.SupplierDAO(db.getWritableDatabase());

        // 初始化状态筛选下拉框
        java.util.List<String> statuses = new java.util.ArrayList<>();
        statuses.add("全部");
        statuses.add("OPEN"); statuses.add("SUBMITTED"); statuses.add("APPROVED"); statuses.add("RECEIVED"); statuses.add("REJECTED"); statuses.add("DRAFT"); statuses.add("PENDING");
        if (spStatus != null) {
            spStatus.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses));
        }

        sorts.clear();
        sorts.add("按日期"); sorts.add("按名称"); sorts.add("按状态");

        // 准备供应商筛选列表：'全部' + 供应商名称（用于“更多”弹窗）
        supNames.clear();
        supNames.add("全部");
        java.util.List<com.example.android_development.model.Supplier> allSups = supplierDAO.getAllSuppliers();
        for (com.example.android_development.model.Supplier s : allSups) {
            String n = s.getName() == null || s.getName().trim().isEmpty() ? s.getId() : s.getName();
            supNames.add(n);
            supIdByName.put(n, s.getId());
        }

        if (buttonNewPurchase != null) {
            buttonNewPurchase.setOnClickListener(v -> {
                // 创建一个最小采购单（OPEN）并刷新列表
                com.example.android_development.model.PurchaseOrder po = new com.example.android_development.model.PurchaseOrder();
                po.setStatus("OPEN");
                long res = purchaseDAO.addPurchaseOrder(po);
                if (res != -1) loadPurchaseOrders();
            });
        }

        // 搜索/筛选变更时重新应用过滤条件
        android.widget.AdapterView.OnItemSelectedListener reloadListener = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) { applyFiltersAndShow(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        };
        if (spStatus != null) {
            spStatus.setOnItemSelectedListener(reloadListener);
        }

        // “更多”按钮：弹出供应商筛选/排序方式对话框
        if (buttonMore != null) {
            buttonMore.setOnClickListener(v -> {
            android.widget.PopupMenu pm = new android.widget.PopupMenu(PurchaseListActivity.this, buttonMore);
            pm.getMenu().add(0, 1, 0, "供应商筛选");
            pm.getMenu().add(0, 2, 1, "排序");
            pm.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    // 供应商单选对话框
                    androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(PurchaseListActivity.this);
                    b.setTitle("选择供应商");
                    String[] arr = supNames.toArray(new String[0]);
                    int checked = Math.max(0, supNames.indexOf(supplierSelected));
                    b.setSingleChoiceItems(arr, checked, (dialog, which) -> {
                        supplierSelected = arr[which];
                    });
                    b.setPositiveButton("应用", (dialog, which) -> applyFiltersAndShow());
                    b.setNegativeButton("取消", null);
                    b.show();
                    return true;
                } else if (item.getItemId() == 2) {
                    androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(PurchaseListActivity.this);
                    b.setTitle("排序方式");
                    String[] arr = sorts.toArray(new String[0]);
                    int checked = Math.max(0, sorts.indexOf(sortSelected));
                    b.setSingleChoiceItems(arr, checked, (dialog, which) -> { sortSelected = arr[which]; });
                    b.setPositiveButton("应用", (dialog, which) -> applyFiltersAndShow());
                    b.setNegativeButton("取消", null);
                    b.show();
                    return true;
                }
                return false;
            });
            pm.show();
        });
        }

        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> { applyFiltersAndShow(); return false; });
        }

        if (buttonLoadMore != null) {
            buttonLoadMore.setOnClickListener(v -> {
            // 加载下一页
            int nextOffset = currentOffset + pageSize;
            if (nextOffset < filteredPOs.size()) {
                java.util.List<com.example.android_development.model.PurchaseOrder> more = filteredPOs.subList(nextOffset, Math.min(nextOffset + pageSize, filteredPOs.size()));
                adapter.appendData(more);
                currentOffset = nextOffset;
                if (currentOffset + pageSize >= filteredPOs.size()) buttonLoadMore.setVisibility(android.view.View.GONE);
            } else {
                buttonLoadMore.setVisibility(android.view.View.GONE);
            }
        });
        }
    }

    /**
     * 从数据库加载采购单列表。
     *
     * <p>会初始化 Adapter（若尚未创建），并在加载完成后重置分页并应用当前筛选条件。</p>
     */
    private void loadPurchaseOrders() {
        allPOs = purchaseDAO.getAllPurchaseOrders();
        if (allPOs == null || allPOs.isEmpty()) {
            if (listViewPurchases != null) listViewPurchases.setVisibility(android.view.View.GONE);
            if (textViewNoPO != null) textViewNoPO.setVisibility(android.view.View.VISIBLE);
            return;
        }
        if (listViewPurchases != null) listViewPurchases.setVisibility(android.view.View.VISIBLE);
        if (textViewNoPO != null) textViewNoPO.setVisibility(android.view.View.GONE);

        if (adapter == null && listViewPurchases != null) {
            adapter = new PurchaseAdapter(this, new java.util.ArrayList<>());
            adapter.setOnItemClickListener((position, po) -> {
                if (po != null && po.getId() != null) {
                    Intent it = new Intent(PurchaseListActivity.this, PurchaseDetailActivity.class);
                    it.putExtra("po_id", po.getId());
                    startActivity(it);
                }
            });
            listViewPurchases.setAdapter(adapter);
        }
        // 重置分页并应用当前筛选条件
        currentOffset = 0;
        applyFiltersAndShow();
    }

    /**
     * 根据搜索/状态/供应商/排序条件过滤并刷新列表展示。
     *
     * <p>注意：Spinner/搜索事件可能早于 Adapter 初始化触发，因此这里需要判空保护，
     * 防止初始化时序导致的空指针崩溃。</p>
     */
    private void applyFiltersAndShow() {
        if (adapter == null) {
            return; // 适配器尚未初始化（Spinner/搜索事件可能早于 adapter 创建）
        }
        
        String q = etSearch != null && etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase() : "";
        String statusSel = spStatus != null && spStatus.getSelectedItem() != null ? spStatus.getSelectedItem().toString() : "全部";
        String supplierSel = supplierSelected == null ? "全部" : supplierSelected;
        String sortSel = sortSelected == null ? "按日期" : sortSelected;

        // 过滤
        filteredPOs.clear();
        for (com.example.android_development.model.PurchaseOrder po : allPOs) {
            if (po == null) continue;
            if (!q.isEmpty()) {
                String name = po.getName() == null ? "" : po.getName();
                String id = po.getId() == null ? "" : po.getId();
                if (!(name.toLowerCase().contains(q) || id.toLowerCase().contains(q))) continue;
            }
            if (!"全部".equals(statusSel)) {
                if (po.getStatus() == null || !po.getStatus().equalsIgnoreCase(statusSel)) continue;
            }
            if (!"全部".equals(supplierSel)) {
                // 供应商筛选：按 supplierId（或名称映射）尽力匹配
                String sid = po.getSupplierId() == null ? "" : po.getSupplierId();
                if (!sid.equals(supplierSel) && (po.getSupplierId() == null || !po.getSupplierId().equals(supplierSel)) ) {
                    // 尽力而为：当筛选项为名称而采购单存的是 id 时，可能无法严格匹配
                }
            }
            filteredPOs.add(po);
        }

        // 排序
        if ("按名称".equals(sortSel)) {
            java.util.Collections.sort(filteredPOs, (a,b) -> {
                String an = a.getName() == null ? "" : a.getName();
                String bn = b.getName() == null ? "" : b.getName();
                return an.compareToIgnoreCase(bn);
            });
        } else if ("按状态".equals(sortSel)) {
            java.util.Collections.sort(filteredPOs, (a,b) -> {
                String as = a.getStatus() == null ? "" : a.getStatus();
                String bs = b.getStatus() == null ? "" : b.getStatus();
                return as.compareToIgnoreCase(bs);
            });
        } else { // 按日期
            java.util.Collections.sort(filteredPOs, (a,b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        }

        // 首屏展示（分页）
        adapter.updateData(new java.util.ArrayList<>());
        currentOffset = 0;
        int end = Math.min(pageSize, filteredPOs.size());
        if (end > 0) adapter.appendData(filteredPOs.subList(0, end));
        currentOffset = end - 1 < 0 ? 0 : end - 1;
        if (buttonLoadMore != null) {
            if (filteredPOs.size() > end) {
                buttonLoadMore.setVisibility(android.view.View.VISIBLE);
            } else {
                buttonLoadMore.setVisibility(android.view.View.GONE);
            }
        }
    }
}
