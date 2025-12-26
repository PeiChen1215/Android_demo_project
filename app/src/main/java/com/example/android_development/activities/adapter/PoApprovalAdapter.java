package com.example.android_development.activities.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.android_development.R;
import com.example.android_development.util.Constants;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * 采购单审批历史 ListView 适配器。
 *
 * <p>数据源为 {@link ContentValues} 列表，字段名由 {@link Constants} 中的列常量提供。
 * 主要展示：决策（approve/reject）、审批人、角色、时间与备注。</p>
 */
public class PoApprovalAdapter extends BaseAdapter {
    private Context ctx;
    private List<ContentValues> items;
    private LayoutInflater inflater;

    public PoApprovalAdapter(Context ctx, List<ContentValues> items) {
        this.ctx = ctx;
        this.items = items;
        this.inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() { return items == null ? 0 : items.size(); }

    @Override
    public Object getItem(int position) { return items.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    /**
     * 渲染审批历史行。
     *
     * <p>第一行：DECISION + 审批人 + 角色；第二行：时间 + 备注。</p>
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            h = new ViewHolder();
            h.tv1 = convertView.findViewById(android.R.id.text1);
            h.tv2 = convertView.findViewById(android.R.id.text2);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        ContentValues cv = items.get(position);
        String who = cv.getAsString(Constants.COLUMN_PO_APPROVAL_APPROVER_ID);
        String role = cv.getAsString(Constants.COLUMN_PO_APPROVAL_APPROVER_ROLE);
        String decision = cv.getAsString(Constants.COLUMN_PO_APPROVAL_DECISION);
        String comment = cv.getAsString(Constants.COLUMN_PO_APPROVAL_COMMENT);
        String tsStr = cv.getAsString(Constants.COLUMN_PO_APPROVAL_TIMESTAMP);
        long ts = 0;
        try { ts = Long.parseLong(tsStr == null ? "0" : tsStr); } catch (Exception ignored) {}
        String t = ts == 0 ? "" : DateFormat.getDateTimeInstance().format(new Date(ts));

        h.tv1.setText((decision == null ? "" : decision.toUpperCase()) + " by " + (who == null ? "?" : who) + (role == null ? "" : " (" + role + ")"));
        String detail = (t.isEmpty() ? "" : t + " - ") + (comment == null ? "" : comment);
        h.tv2.setText(detail);
        return convertView;
    }

    static class ViewHolder { TextView tv1; TextView tv2; }
}
