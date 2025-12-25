package com.example.android_development.services;

import android.content.Context;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.model.Product;
import java.util.List;

/**
 * 库存服务类
 * 处理库存相关的业务逻辑
 */
public class InventoryService {

    private ProductDAO productDAO;

    public InventoryService(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        this.productDAO = new ProductDAO(dbHelper.getReadableDatabase());
    }

    /**
     * 获取所有低库存商品（货架库存低于最低预警值）
     * @return 低库存商品列表
     */
    public List<Product> getLowStockAlerts() {
        return productDAO.getLowStockProducts();
    }

    /**
     * 检查是否有低库存商品
     * @return true如果有低库存商品，false否则
     */
    public boolean hasLowStockAlerts() {
        List<Product> lowStockProducts = getLowStockAlerts();
        return lowStockProducts != null && !lowStockProducts.isEmpty();
    }

    /**
     * 获取低库存商品数量
     * @return 低库存商品的数量
     */
    public int getLowStockAlertCount() {
        List<Product> lowStockProducts = getLowStockAlerts();
        return lowStockProducts != null ? lowStockProducts.size() : 0;
    }
}