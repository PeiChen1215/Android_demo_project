package com.example.android_development.services;

import android.content.Context;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.ProductDAO;
import com.example.android_development.model.Product;
import java.util.List;

/**
 * 库存领域服务（业务层封装）。
 *
 * <p>对外提供“低库存预警”等与库存相关的业务查询能力，内部通过 DAO 访问数据库。
 * 该类不负责 UI，也不直接处理权限；权限控制应由调用方（Activity/Service）负责。</p>
 */
public class InventoryService {

    private ProductDAO productDAO;

    /**
     * 构造函数：创建 DAO 并使用只读数据库连接。
     */
    public InventoryService(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        this.productDAO = new ProductDAO(dbHelper.getReadableDatabase());
    }

    /**
     * 获取所有低库存商品（货架库存低于最低预警值）。
     *
     * @return 低库存商品列表；为空表示无低库存商品
     */
    public List<Product> getLowStockAlerts() {
        return productDAO.getLowStockProducts();
    }

    /**
     * 是否存在低库存预警。
     *
     * @return true 表示存在低库存商品
     */
    public boolean hasLowStockAlerts() {
        List<Product> lowStockProducts = getLowStockAlerts();
        return lowStockProducts != null && !lowStockProducts.isEmpty();
    }

    /**
     * 获取低库存商品数量。
     *
     * @return 低库存商品的数量
     */
    public int getLowStockAlertCount() {
        List<Product> lowStockProducts = getLowStockAlerts();
        return lowStockProducts != null ? lowStockProducts.size() : 0;
    }
}