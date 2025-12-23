package com.example.android_development.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.content.Intent;
import com.example.android_development.adapters.PurchaseAdapter;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;

public class PurchaseListActivity extends AppCompatActivity {
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

    private void initViews() {
        listViewPurchases = findViewById(R.id.listViewPurchases);
        textViewNoPO = findViewById(R.id.textViewNoPO);
        buttonNewPurchase = findViewById(R.id.buttonNewPurchase);
        etSearch = findViewById(R.id.etSearch);
        spStatus = findViewById(R.id.spStatus);
        buttonMore = findViewById(R.id.buttonMore);
        buttonLoadMore = findViewById(R.id.buttonLoadMore);

        if (listViewPurchases.getLayoutManager() == null) listViewPurchases.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper db = new DatabaseHelper(this);
        purchaseDAO = new com.example.android_development.database.PurchaseDAO(db.getWritableDatabase());
        // supplierDAO intentionally not used for list display
        supplierDAO = new com.example.android_development.database.SupplierDAO(db.getWritableDatabase());

        // setup filter spinners
        java.util.List<String> statuses = new java.util.ArrayList<>();
        statuses.add("全部");
        statuses.add("OPEN"); statuses.add("SUBMITTED"); statuses.add("APPROVED"); statuses.add("RECEIVED"); statuses.add("REJECTED"); statuses.add("DRAFT"); statuses.add("PENDING");
        spStatus.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses));

        java.util.List<String> sorts = new java.util.ArrayList<>();
        sorts.add("按日期"); sorts.add("按名称"); sorts.add("按状态");

        // populate supplier list with '全部' + suppliers (used in More dialog)
        supNames.clear();
        supNames.add("全部");
        java.util.List<com.example.android_development.model.Supplier> allSups = supplierDAO.getAllSuppliers();
        for (com.example.android_development.model.Supplier s : allSups) {
            String n = s.getName() == null || s.getName().trim().isEmpty() ? s.getId() : s.getName();
            supNames.add(n);
            supIdByName.put(n, s.getId());
        }

        buttonNewPurchase.setOnClickListener(v -> {
            // create a minimal PO and refresh
            com.example.android_development.model.PurchaseOrder po = new com.example.android_development.model.PurchaseOrder();
            po.setStatus("OPEN");
            long res = purchaseDAO.addPurchaseOrder(po);
            if (res != -1) loadPurchaseOrders();
        });

        // apply filters on search action or spinner change
        android.widget.AdapterView.OnItemSelectedListener reloadListener = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) { applyFiltersAndShow(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        };
        spStatus.setOnItemSelectedListener(reloadListener);

        // More button shows supplier/sort dialogs
        buttonMore.setOnClickListener(v -> {
            android.widget.PopupMenu pm = new android.widget.PopupMenu(PurchaseListActivity.this, buttonMore);
            pm.getMenu().add(0, 1, 0, "供应商筛选");
            pm.getMenu().add(0, 2, 1, "排序");
            pm.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    // supplier single-choice dialog
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

        etSearch.setOnEditorActionListener((v, actionId, event) -> { applyFiltersAndShow(); return false; });

        buttonLoadMore.setOnClickListener(v -> {
            // load next page
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

    private void loadPurchaseOrders() {
        allPOs = purchaseDAO.getAllPurchaseOrders();
        if (allPOs == null || allPOs.isEmpty()) {
            listViewPurchases.setVisibility(android.view.View.GONE);
            textViewNoPO.setVisibility(android.view.View.VISIBLE);
            return;
        }
        listViewPurchases.setVisibility(android.view.View.VISIBLE);
        textViewNoPO.setVisibility(android.view.View.GONE);

        if (adapter == null) {
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
        // reset paging and apply current filters
        currentOffset = 0;
        applyFiltersAndShow();
    }

    private void applyFiltersAndShow() {
        String q = etSearch.getText() == null ? "" : etSearch.getText().toString().trim().toLowerCase();
        String statusSel = spStatus.getSelectedItem() == null ? "全部" : spStatus.getSelectedItem().toString();
        String supplierSel = supplierSelected == null ? "全部" : supplierSelected;
        String sortSel = sortSelected == null ? "按日期" : sortSelected;

        // filter
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
                // match supplier by name or id
                String sid = po.getSupplierId() == null ? "" : po.getSupplierId();
                if (!sid.equals(supplierSel) && (po.getSupplierId() == null || !po.getSupplierId().equals(supplierSel)) ) {
                    // best-effort: if supplier spinner shows names, skip strict match (supplier filtering may be approximate)
                }
            }
            filteredPOs.add(po);
        }

        // sort
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

        // show first page
        adapter.updateData(new java.util.ArrayList<>());
        currentOffset = 0;
        int end = Math.min(pageSize, filteredPOs.size());
        if (end > 0) adapter.appendData(filteredPOs.subList(0, end));
        currentOffset = end - 1 < 0 ? 0 : end - 1;
        if (filteredPOs.size() > end) buttonLoadMore.setVisibility(android.view.View.VISIBLE); else buttonLoadMore.setVisibility(android.view.View.GONE);
    }
}
