package com.example.android_development.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_development.R;
import com.example.android_development.model.Product;

import java.util.List;
import androidx.recyclerview.widget.DiffUtil;
import com.bumptech.glide.Glide;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final Context context;
    private final List<Product> data;
    private OnItemClickListener listener;
    private boolean showActions = true;

    public interface OnItemClickListener {
        void onItemClick(int position, Product product);
        boolean onItemLongClick(int position, Product product);
        void onActionEdit(int position, Product product);
        void onActionDelete(int position, Product product);
        void onActionAdjustStock(int position, Product product);
    }

    public ProductAdapter(Context context, List<Product> data) {
        this.context = context;
        this.data = data;
        this.showActions = true;
    }

    public ProductAdapter(Context context, List<Product> data, boolean showActions) {
        this.context = context;
        this.data = data;
        this.showActions = showActions;
    }

    public void submitList(List<Product> newList) {
        if (this.data == null) {
            // set directly
            this.data.clear();
            if (newList != null) this.data.addAll(newList);
            notifyDataSetChanged();
            return;
        }
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return data == null ? 0 : data.size(); }

            @Override
            public int getNewListSize() { return newList == null ? 0 : newList.size(); }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                String oldId = data.get(oldItemPosition).getId();
                String newId = newList.get(newItemPosition).getId();
                if (oldId == null || newId == null) return oldId == newId;
                return oldId.equals(newId);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Product o = data.get(oldItemPosition);
                Product n = newList.get(newItemPosition);
                return o.getName().equals(n.getName()) && o.getPrice() == n.getPrice() && o.getStock() == n.getStock();
            }
        });

        // apply
        this.data.clear();
        if (newList != null) this.data.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = data.get(position);
        if (p == null) return;
        holder.name.setText(p.getName());
        holder.price.setText(String.format("￥%.2f", p.getPrice()));
        holder.stock.setText(String.format("库存: %d", p.getStock()));
        holder.category.setText(p.getCategory() == null ? "未分类" : p.getCategory());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position, p);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) return listener.onItemLongClick(position, p);
            return false;
        });

        // 操作按钮弹出菜单（根据权限显示/隐藏）
        if (holder.actionButton != null) {
            holder.actionButton.setVisibility(showActions ? View.VISIBLE : View.GONE);
            // 如果有缩略图 URL 则使用 Glide 加载，否则显示系统占位图
            if (holder.thumb != null) {
                String url = p.getThumbUrl();
                if (url != null && !url.isEmpty()) {
                    try {
                        Glide.with(holder.thumb.getContext())
                                .load(url)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .centerCrop()
                                .into(holder.thumb);
                    } catch (Exception ex) {
                        holder.thumb.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    holder.thumb.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }

            holder.actionButton.setOnClickListener(v -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(context, holder.actionButton, android.view.Gravity.END);
                popup.getMenuInflater().inflate(R.menu.menu_product_item, popup.getMenu());
                // 强制显示图标（MenuPopupHelper 反射）
                try {
                    Field[] fields = popup.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popup);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                            Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                            setForceShowIcon.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception ignored) {}

                popup.setOnMenuItemClickListener(item -> {
                    if (listener == null) return false;
                    int id = item.getItemId();
                    if (id == R.id.action_edit) {
                        listener.onActionEdit(position, p);
                        return true;
                    } else if (id == R.id.action_delete) {
                        listener.onActionDelete(position, p);
                        return true;
                    } else if (id == R.id.action_adjust_stock) {
                        listener.onActionAdjustStock(position, p);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, stock, category;
        ImageView thumb;
        ImageButton actionButton;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewProductName);
            price = itemView.findViewById(R.id.textViewProductPrice);
            stock = itemView.findViewById(R.id.textViewProductStock);
            category = itemView.findViewById(R.id.textViewProductCategory);
            thumb = itemView.findViewById(R.id.imageViewThumb);
            actionButton = itemView.findViewById(R.id.buttonProductAction);
        }
    }
}
