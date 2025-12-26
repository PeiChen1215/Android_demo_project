package com.example.android_development.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.SaleDAO;
import com.example.android_development.model.Sale;
import com.example.android_development.model.RefundRecord;
import com.example.android_development.model.User;
import com.example.android_development.model.SaleLine;
import com.example.android_development.activities.adapters.SaleLineAdapter;
import java.util.List;

/**
 * 收据详情页面。
 *
 * <p>展示指定销售单（sale_id）的收据信息：时间、支付方式、合计、明细行。
 * 在具备退单权限且未退单时，允许执行退单并恢复库存；支持将收据文本分享给其他应用。</p>
 */
public class ReceiptDetailActivity extends AppCompatActivity {

    private TextView tvHeader, tvMeta;
    private ListView listLines;
    private Button btnShare;
    private Button btnRefund;

    private DatabaseHelper dbHelper;
    private SaleDAO saleDAO;
    private Sale currentSale;

    /**
     * Activity 创建：读取 sale_id，加载销售单并渲染头部信息与明细列表；按权限控制退单按钮显示。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_detail);

        tvHeader = findViewById(R.id.tv_receipt_header);
        tvMeta = findViewById(R.id.tv_receipt_meta);
        listLines = findViewById(R.id.list_receipt_lines);
        btnShare = findViewById(R.id.btn_share_receipt);

        dbHelper = new DatabaseHelper(this);
        saleDAO = new SaleDAO(dbHelper.getReadableDatabase());

        String saleId = getIntent().getStringExtra("sale_id");
        if (saleId == null) {
            finish();
            return;
        }

        currentSale = saleDAO.getSaleById(saleId);
        if (currentSale == null) {
            finish();
            return;
        }

        tvHeader.setText("收据: " + currentSale.getId());
        StringBuilder metaSb = new StringBuilder();
        metaSb.append(java.text.SimpleDateFormat.getDateTimeInstance().format(new java.util.Date(currentSale.getTimestamp())));
        metaSb.append("  |  支付:").append(currentSale.getPaymentMethod() == null ? "未指定" : currentSale.getPaymentMethod());
        metaSb.append("  |  合计:").append(String.format("%.2f", currentSale.getTotal()));
        // 显示操作人（如果有）
        try {
            if (currentSale.getUserId() != null) {
                User u = dbHelper.getUserByIdObject(currentSale.getUserId());
                if (u != null) metaSb.append("  |  操作人:").append(u.getFullName() != null ? u.getFullName() : u.getUsername());
                else metaSb.append("  |  操作人:").append(currentSale.getUserId());
            }
        } catch (Exception ignored) {}

        // 如果已退单，查找退款记录并显示退款处理人和原因
        if (currentSale.isRefunded()) {
            try {
                android.database.Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + com.example.android_development.util.Constants.TABLE_REFUNDS + " WHERE " + com.example.android_development.util.Constants.COLUMN_REFUND_SALE_ID + " = ? ORDER BY " + com.example.android_development.util.Constants.COLUMN_REFUND_TIMESTAMP + " DESC LIMIT 1", new String[]{currentSale.getId()});
                if (c != null && c.moveToFirst()) {
                    RefundRecord rr = RefundRecord.fromCursor(c);
                    c.close();
                    metaSb.append("  |  已退单");
                    if (rr.getUserId() != null) {
                        User ru = dbHelper.getUserByIdObject(rr.getUserId());
                        String rname = ru != null ? (ru.getFullName() != null ? ru.getFullName() : ru.getUsername()) : rr.getUserId();
                        metaSb.append(" by ").append(rname);
                    }
                    metaSb.append(" @ ").append(java.text.SimpleDateFormat.getDateTimeInstance().format(new java.util.Date(rr.getTimestamp())));
                    if (rr.getReason() != null && !rr.getReason().isEmpty()) metaSb.append("  原因:").append(rr.getReason());
                }
            } catch (Exception ignored) {}
        }

        tvMeta.setText(metaSb.toString());

        List<SaleLine> lines = currentSale.getLines();
        SaleLineAdapter adapter = new SaleLineAdapter(this, lines);
        listLines.setAdapter(adapter);

        btnShare.setOnClickListener(v -> shareReceipt());

        btnRefund = findViewById(R.id.btn_refund_receipt);
        boolean canRefund = com.example.android_development.security.Auth.hasPermission(this, com.example.android_development.util.Constants.PERM_REFUND);
        if (btnRefund != null) {
            btnRefund.setVisibility(canRefund && !currentSale.isRefunded() ? android.view.View.VISIBLE : android.view.View.GONE);
            btnRefund.setOnClickListener(v -> doRefund());
        }
    }

    /**
     * 执行退单流程。
     *
     * <p>包含二次确认与“退单原因”输入；最终调用 {@link SaleDAO#refundSale(String, String)}
     * 完成退单、恢复库存与记录退款信息，并刷新 UI 状态。</p>
     */
    private void doRefund() {
        // 二次确认：退单会恢复货架库存
        new android.app.AlertDialog.Builder(this)
                .setTitle("确认退单")
                .setMessage("是否确认对该收据进行退单并恢复库存？此操作可恢复货架库存。")
                .setPositiveButton("确定", (dialog, which) -> {
                // 弹出原因输入框（必填）
                    android.widget.EditText et = new android.widget.EditText(this);
                    et.setHint("请输入退单原因（必填）");
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("退单原因")
                            .setView(et)
                            .setPositiveButton("提交", (d2, w2) -> {
                                String reason = et.getText() == null ? "" : et.getText().toString().trim();
                                if (reason.isEmpty()) {
                                    android.widget.Toast.makeText(this, "请填写退单原因", android.widget.Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                SaleDAO dao = new SaleDAO(dbHelper.getWritableDatabase(), this);
                                boolean ok = dao.refundSale(currentSale.getId(), reason);
                                if (ok) {
                                    android.widget.Toast.makeText(this, "退单成功", android.widget.Toast.LENGTH_SHORT).show();
                                    // 刷新 UI：重新读取销售单并更新按钮状态
                                    currentSale = dao.getSaleById(currentSale.getId());
                                    tvMeta.setText(java.text.SimpleDateFormat.getDateTimeInstance().format(new java.util.Date(currentSale.getTimestamp()))
                                            + "  |  支付:" + (currentSale.getPaymentMethod() == null ? "未指定" : currentSale.getPaymentMethod())
                                            + "  |  合计:" + String.format("%.2f", currentSale.getTotal())
                                            + (currentSale.isRefunded() ? "  |  已退单" : ""));
                                    btnRefund.setVisibility(android.view.View.GONE);
                                } else {
                                    android.widget.Toast.makeText(this, "退单失败，请检查日志或权限", android.widget.Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 分享收据：将收据信息与明细行拼接为纯文本，并通过系统分享。
     */
    private void shareReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("收据 ").append(currentSale.getId()).append("\n");
        sb.append("时间: ").append(java.text.SimpleDateFormat.getDateTimeInstance().format(new java.util.Date(currentSale.getTimestamp()))).append("\n");
        sb.append("支付: ").append(currentSale.getPaymentMethod()).append("\n");
        sb.append("合计: ").append(String.format("%.2f", currentSale.getTotal())).append("\n\n");
        sb.append("明细:\n");
        for (SaleLine l : currentSale.getLines()) {
            sb.append(l.getProductName()).append("  ").append(l.getQty()).append(" x ").append(String.format("%.2f", l.getPrice())).append(" = ").append(String.format("%.2f", l.getQty() * l.getPrice())).append("\n");
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, "收据 " + currentSale.getId());
        share.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(share, "分享收据"));
    }
}
