package com.example.android_development.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.SaleDAO;
import com.example.android_development.model.SalesSummary;
import android.app.AlertDialog;
import android.content.ContentValues;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.List;

public class RevenueReportActivity extends AppCompatActivity {

    private Spinner spPeriod;
    private Button btnGenerate;
    private TextView tvTotalSummary;
    private ListView listSummary;
    private Button btnExport;
    private java.util.List<SalesSummary> currentItems = new java.util.ArrayList<>();

    private DatabaseHelper dbHelper;
    private SaleDAO saleDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue_report);

        spPeriod = findViewById(R.id.sp_period);
        btnGenerate = findViewById(R.id.btn_generate);
        tvTotalSummary = findViewById(R.id.tv_total_summary);
        listSummary = findViewById(R.id.list_summary);

        dbHelper = new DatabaseHelper(this);
        saleDAO = new SaleDAO(dbHelper.getReadableDatabase());

        ArrayAdapter<String> pa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"按日", "按月"});
        pa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPeriod.setAdapter(pa);

        btnGenerate.setOnClickListener(v -> generateReport());
        btnExport = findViewById(R.id.btn_export);
        boolean canExport = com.example.android_development.security.Auth.hasPermission(this, com.example.android_development.util.Constants.PERM_EXPORT_REVENUE);
        if (btnExport != null) {
            btnExport.setVisibility(canExport ? android.view.View.VISIBLE : android.view.View.GONE);
            btnExport.setOnClickListener(v -> exportCsv());
        }

        // generate default report
        generateReport();
    }

    private void generateReport() {
        // 默认最近30天
        long now = System.currentTimeMillis();
        long start = now - 30L * 24 * 60 * 60 * 1000;
        List<SalesSummary> items;
        if (spPeriod.getSelectedItemPosition() == 1) {
            items = saleDAO.getMonthlySalesSummary(start, now);
        } else {
            items = saleDAO.getDailySalesSummary(start, now);
        }
        // cache items for export
        currentItems.clear();
        currentItems.addAll(items);
        double total = 0;
        String[] display = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            SalesSummary s = items.get(i);
            total += s.getTotal();
            display[i] = s.getPeriodLabel() + "  —  " + String.format("%.2f", s.getTotal()) + "（笔数:" + s.getCount() + ")";
        }
        tvTotalSummary.setText(String.format("总营收: %.2f", total));
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, display);
        listSummary.setAdapter(ad);

        // 点击某一行显示该 period 的明细（销售/退款/采购）
        listSummary.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= currentItems.size()) return;
            SalesSummary s = currentItems.get(position);
            String label = s.getPeriodLabel();
            boolean monthly = (spPeriod.getSelectedItemPosition() == 1);
            long periodStart = 0, periodEnd = 0;
            try {
                if (monthly) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                    Calendar c = Calendar.getInstance();
                    c.setTime(sdf.parse(label));
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
                    periodStart = c.getTimeInMillis();
                    c.add(Calendar.MONTH, 1);
                    periodEnd = c.getTimeInMillis() - 1;
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar c = Calendar.getInstance();
                    c.setTime(sdf.parse(label));
                    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
                    periodStart = c.getTimeInMillis();
                    c.add(Calendar.DAY_OF_MONTH, 1);
                    periodEnd = c.getTimeInMillis() - 1;
                }
            } catch (Exception e) { e.printStackTrace(); return; }

            List<ContentValues> details = saleDAO.getDetailedEntriesForPeriod(periodStart, periodEnd);
            if (details == null || details.isEmpty()) {
                new AlertDialog.Builder(this).setTitle(label).setMessage("没有明细").setPositiveButton("关闭", null).show();
                return;
            }
            String[] lines = new String[details.size()];
            for (int i = 0; i < details.size(); i++) {
                ContentValues cv = details.get(i);
                String type = cv.getAsString("type");
                String idv = cv.getAsString("id");
                double amt = 0.0; try { amt = cv.getAsDouble("amount"); } catch (Exception ignored) {}
                long ts = 0; try { ts = cv.getAsLong("ts"); } catch (Exception ignored) {}
                String reason = cv.getAsString("reason");
                String timeStr = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", ts).toString();
                String desc = type + " — " + idv + "  " + String.format(Locale.getDefault(), "%.2f", amt) + "  " + timeStr;
                if (reason != null && !reason.isEmpty()) desc += "  原因:" + reason;
                lines[i] = desc;
            }
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle(label + " 明细");
            b.setItems(lines, null);
            b.setPositiveButton("关闭", null);
            b.show();
        });
    }

    private void exportCsv() {
        if (currentItems == null || currentItems.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        // summary section
        sb.append("period,total,count\n");
        for (SalesSummary s : currentItems) {
            sb.append(s.getPeriodLabel()).append(',').append(String.format("%.2f", s.getTotal())).append(',').append(s.getCount()).append('\n');
        }

        // details section header
        sb.append('\n');
        sb.append("period,type,id,amount,timestamp,reason\n");
        // for each period, compute period bounds and fetch details
        for (SalesSummary s : currentItems) {
            String label = s.getPeriodLabel();
            boolean monthly = (spPeriod.getSelectedItemPosition() == 1);
            long periodStart = 0, periodEnd = 0;
            try {
                if (monthly) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTime(sdf.parse(label));
                    c.set(java.util.Calendar.DAY_OF_MONTH, 1);
                    c.set(java.util.Calendar.HOUR_OF_DAY, 0); c.set(java.util.Calendar.MINUTE, 0); c.set(java.util.Calendar.SECOND, 0); c.set(java.util.Calendar.MILLISECOND, 0);
                    periodStart = c.getTimeInMillis();
                    c.add(java.util.Calendar.MONTH, 1);
                    periodEnd = c.getTimeInMillis() - 1;
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTime(sdf.parse(label));
                    c.set(java.util.Calendar.HOUR_OF_DAY, 0); c.set(java.util.Calendar.MINUTE, 0); c.set(java.util.Calendar.SECOND, 0); c.set(java.util.Calendar.MILLISECOND, 0);
                    periodStart = c.getTimeInMillis();
                    c.add(java.util.Calendar.DAY_OF_MONTH, 1);
                    periodEnd = c.getTimeInMillis() - 1;
                }
            } catch (Exception e) { e.printStackTrace(); continue; }

            List<ContentValues> details = saleDAO.getDetailedEntriesForPeriod(periodStart, periodEnd);
            if (details == null || details.isEmpty()) continue;
            for (ContentValues cv : details) {
                String type = cv.getAsString("type");
                String idv = cv.getAsString("id");
                double amt = 0.0; try { amt = cv.getAsDouble("amount"); } catch (Exception ignored) {}
                long ts = 0; try { ts = cv.getAsLong("ts"); } catch (Exception ignored) {}
                String reason = cv.getAsString("reason");
                sb.append(label).append(',').append(type).append(',').append(idv == null ? "" : idv).append(',').append(String.format("%.2f", amt)).append(',').append(ts).append(',').append(reason == null ? "" : reason).append('\n');
            }
        }
        try {
            java.io.File dir = getExternalCacheDir();
            if (dir == null) dir = getCacheDir();
            java.io.File f = new java.io.File(dir, "revenue_report.csv");
            java.io.FileWriter fw = new java.io.FileWriter(f);
            fw.write(sb.toString());
            fw.close();

            android.net.Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", f);
            android.content.Intent share = new android.content.Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/csv");
            share.putExtra(android.content.Intent.EXTRA_STREAM, uri);
            share.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(android.content.Intent.createChooser(share, "分享报表"));
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "导出失败: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
