package com.example.android_development.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.model.Product;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;
import com.example.android_development.model.StockTransaction;
import com.example.android_development.util.Audit;
import java.util.UUID;

/**
 * 商品相关 DAO。
 *
 * <p>负责商品（products）表的增删改查、分页/搜索、低库存查询，以及库存事务（stock_transactions）记录。
 * 注意：该类多数方法不做权限校验；权限控制通常由上层 Activity/业务流程完成。</p>
 */
public class ProductDAO {

    private SQLiteDatabase db;

    public ProductDAO(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * 添加商品。
     *
     * @param product 商品对象
     * @return 插入结果；失败返回 -1
     */
    public long addProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PRODUCT_ID, product.getId());
        values.put(Constants.COLUMN_PRODUCT_NAME, product.getName());
        values.put(Constants.COLUMN_THUMB_URL, product.getThumbUrl());
        values.put(Constants.COLUMN_CATEGORY, product.getCategory());
        values.put(Constants.COLUMN_BRAND, product.getBrand());
        values.put(Constants.COLUMN_PRICE, product.getPrice());
        values.put(Constants.COLUMN_COST, product.getCost());
        values.put(Constants.COLUMN_STOCK, product.getStock());
        values.put(Constants.COLUMN_WAREHOUSE_STOCK, product.getWarehouseStock());
        values.put(Constants.COLUMN_MIN_STOCK, product.getMinStock());
        values.put(Constants.COLUMN_MIN_WAREHOUSE_STOCK, product.getMinWarehouseStock());
        values.put(Constants.COLUMN_UNIT, product.getUnit());
        values.put(Constants.COLUMN_PRODUCTION_DATE, product.getProductionDate());
        values.put(Constants.COLUMN_EXPIRATION_DATE, product.getExpirationDate());
        values.put(Constants.COLUMN_BARCODE, product.getBarcode());
        values.put(Constants.COLUMN_DESCRIPTION, product.getDescription());
        values.put(Constants.COLUMN_SUPPLIER_ID, product.getSupplierId());
        values.put(Constants.COLUMN_CREATED_AT, product.getCreatedAt());
        values.put(Constants.COLUMN_UPDATED_AT, product.getUpdatedAt());

        return db.insert(Constants.TABLE_PRODUCTS, null, values);
    }

    /**
     * 更新商品信息（以商品 id 为主键）。
     *
     * <p>会刷新 updated_at 字段为当前时间。</p>
     *
     * @param product 商品对象
     * @return 受影响行数
     */
    public int updateProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PRODUCT_NAME, product.getName());
        values.put(Constants.COLUMN_THUMB_URL, product.getThumbUrl());
        values.put(Constants.COLUMN_CATEGORY, product.getCategory());
        values.put(Constants.COLUMN_BRAND, product.getBrand());
        values.put(Constants.COLUMN_PRICE, product.getPrice());
        values.put(Constants.COLUMN_COST, product.getCost());
        values.put(Constants.COLUMN_STOCK, product.getStock());
        values.put(Constants.COLUMN_WAREHOUSE_STOCK, product.getWarehouseStock());
        values.put(Constants.COLUMN_MIN_STOCK, product.getMinStock());
        values.put(Constants.COLUMN_MIN_WAREHOUSE_STOCK, product.getMinWarehouseStock());
        values.put(Constants.COLUMN_UNIT, product.getUnit());
        values.put(Constants.COLUMN_PRODUCTION_DATE, product.getProductionDate());
        values.put(Constants.COLUMN_EXPIRATION_DATE, product.getExpirationDate());
        values.put(Constants.COLUMN_BARCODE, product.getBarcode());
        values.put(Constants.COLUMN_DESCRIPTION, product.getDescription());
        values.put(Constants.COLUMN_SUPPLIER_ID, product.getSupplierId());
        values.put(Constants.COLUMN_UPDATED_AT, System.currentTimeMillis());

        String whereClause = Constants.COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = {product.getId()};

        return db.update(Constants.TABLE_PRODUCTS, values, whereClause, whereArgs);
    }

    /**
     * 删除商品。
     *
     * @param productId 商品 id
     * @return 删除行数
     */
    public int deleteProduct(String productId) {
        String whereClause = Constants.COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = {productId};

        return db.delete(Constants.TABLE_PRODUCTS, whereClause, whereArgs);
    }

    /**
     * 根据商品 id 获取商品。
     *
     * @param productId 商品 id
     * @return 商品对象；未找到返回 null
     */
    public Product getProductById(String productId) {
        String[] columns = getAllColumns();
        String selection = Constants.COLUMN_PRODUCT_ID + " = ?";
        String[] selectionArgs = {productId};

        Cursor cursor = db.query(
                Constants.TABLE_PRODUCTS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            product = cursorToProduct(cursor);
            cursor.close();
        }

        return product;
    }

    /**
     * 根据商品名称精确匹配获取商品。
     *
     * <p>如果存在多个同名商品，返回第一条。</p>
     *
     * @param name 商品名称
     * @return 商品对象；未找到返回 null
     */
    public Product getProductByName(String name) {
        if (name == null || name.isEmpty()) return null;
        String[] columns = getAllColumns();
        String selection = Constants.COLUMN_PRODUCT_NAME + " = ?";
        String[] selectionArgs = {name};

        Cursor cursor = db.query(
                Constants.TABLE_PRODUCTS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            product = cursorToProduct(cursor);
            cursor.close();
        }
        return product;
    }

    /**
     * 模糊匹配商品名称，返回匹配的商品列表（用于联想提示）。
     *
     * @param q 关键字
     * @return 最多返回 50 条
     */
    public List<Product> getProductsByNameLike(String q) {
        List<Product> products = new ArrayList<>();
        if (q == null) return products;
        String like = "%" + q + "%";
        String[] columns = getAllColumns();
        String selection = Constants.COLUMN_PRODUCT_NAME + " LIKE ?";
        String[] selectionArgs = {like};
        Cursor cursor = db.query(
                Constants.TABLE_PRODUCTS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                Constants.COLUMN_PRODUCT_NAME + " ASC",
                "50"
        );
        if (cursor != null && cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return products;
    }

    /**
     * 获取所有商品列表。
     *
     * @return 商品列表（按名称升序）
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        String[] columns = getAllColumns();
        String orderBy = Constants.COLUMN_PRODUCT_NAME + " ASC";

        Cursor cursor = db.query(
                Constants.TABLE_PRODUCTS,
                columns,
                null,
                null,
                null,
                null,
                orderBy
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return products;
    }

    /**
     * 根据分类获取商品。
     *
     * @param category 分类
     * @return 商品列表（按名称升序）
     */
    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();

        String[] columns = getAllColumns();
        String selection = Constants.COLUMN_CATEGORY + " = ?";
        String[] selectionArgs = {category};
        String orderBy = Constants.COLUMN_PRODUCT_NAME + " ASC";

        Cursor cursor = db.query(
                Constants.TABLE_PRODUCTS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return products;
    }

    /**
     * 搜索商品（按名称或条码模糊匹配）。
     *
     * @param keyword 关键字
     * @return 匹配列表（按名称升序）
     */
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();

        String[] columns = getAllColumns();
        String selection = Constants.COLUMN_PRODUCT_NAME + " LIKE ? OR " +
                Constants.COLUMN_BARCODE + " LIKE ?";
        String[] selectionArgs = {"%" + keyword + "%", "%" + keyword + "%"};
        String orderBy = Constants.COLUMN_PRODUCT_NAME + " ASC";

        Cursor cursor = db.query(
                Constants.TABLE_PRODUCTS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return products;
    }

    /**
     * 分页获取商品。
     *
     * @param limit 每页条数
     * @param offset 偏移量
     * @return 商品列表
     */
    public List<Product> getProductsPage(int limit, int offset) {
        List<Product> products = new ArrayList<>();

        String[] columns = getAllColumns();
        String orderBy = Constants.COLUMN_PRODUCT_NAME + " ASC";
        // SQLite 的 limit 参数可使用 "offset,limit" 格式
        String limitStr = offset + "," + limit;

        Cursor cursor = db.query(
                Constants.TABLE_PRODUCTS,
                columns,
                null,
                null,
                null,
                null,
                orderBy,
                limitStr
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return products;
    }

    /**
     * 分页搜索商品（按名称或条码模糊匹配）。
     *
     * @param keyword 关键字
     * @param limit 每页条数
     * @param offset 偏移量
     * @return 商品列表
     */
    public List<Product> searchProductsPage(String keyword, int limit, int offset) {
        List<Product> products = new ArrayList<>();
        String[] columns = getAllColumns();
        String selection = Constants.COLUMN_PRODUCT_NAME + " LIKE ? OR " +
                Constants.COLUMN_BARCODE + " LIKE ?";
        String[] selectionArgs = {"%" + keyword + "%", "%" + keyword + "%"};
        String orderBy = Constants.COLUMN_PRODUCT_NAME + " ASC";
        String limitStr = offset + "," + limit;

        Cursor cursor = db.query(
                Constants.TABLE_PRODUCTS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                orderBy,
                limitStr
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return products;
    }

    /**
     * 获取低库存商品（货架库存 <= 货架预警值，且预警值 > 0）。
     */
    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();

        String[] columns = getAllColumns();
        String selection = Constants.COLUMN_STOCK + " <= " + Constants.COLUMN_MIN_STOCK +
                " AND " + Constants.COLUMN_MIN_STOCK + " > 0";
        String orderBy = Constants.COLUMN_STOCK + " ASC";

        Cursor cursor = db.query(
                Constants.TABLE_PRODUCTS,
                columns,
                selection,
                null,
                null,
                null,
                orderBy
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return products;
    }

    /**
     * 获取低仓库库存商品（仓库库存 <= 仓库预警值，且预警值 > 0）。
     */
    public List<Product> getLowWarehouseStockProducts() {
        List<Product> products = new ArrayList<>();

        String[] columns = getAllColumns();
        String selection = Constants.COLUMN_WAREHOUSE_STOCK + " <= " + Constants.COLUMN_MIN_WAREHOUSE_STOCK +
                " AND " + Constants.COLUMN_MIN_WAREHOUSE_STOCK + " > 0";
        String orderBy = Constants.COLUMN_WAREHOUSE_STOCK + " ASC";

        Cursor cursor = db.query(
                Constants.TABLE_PRODUCTS,
                columns,
                selection,
                null,
                null,
                null,
                orderBy
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return products;
    }

    /**
     * 直接更新货架库存数量。
     *
     * @param productId 商品 id
     * @param newStock 新货架库存
     * @return 受影响行数
     */
    public int updateStock(String productId, int newStock) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_STOCK, newStock);
        values.put(Constants.COLUMN_UPDATED_AT, System.currentTimeMillis());

        String whereClause = Constants.COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = {productId};

        return db.update(Constants.TABLE_PRODUCTS, values, whereClause, whereArgs);
    }

    /**
     * 直接更新仓库库存数量。
     *
     * @param productId 商品 id
     * @param newWarehouseStock 新仓库库存
     * @return 受影响行数
     */
    public int updateWarehouseStock(String productId, int newWarehouseStock) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_WAREHOUSE_STOCK, newWarehouseStock);
        values.put(Constants.COLUMN_UPDATED_AT, System.currentTimeMillis());

        String whereClause = Constants.COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = {productId};

        return db.update(Constants.TABLE_PRODUCTS, values, whereClause, whereArgs);
    }

    /**
     * 增加货架库存（简单 +quantity）。
     */
    public int increaseStock(String productId, int quantity) {
        Product product = getProductById(productId);
        if (product == null) return 0;

        int newStock = product.getStock() + quantity;
        return updateStock(productId, newStock);
    }

    /**
     * 减少货架库存（简单 -quantity，结果不小于 0）。
     */
    public int decreaseStock(String productId, int quantity) {
        Product product = getProductById(productId);
        if (product == null) return 0;

        int newStock = product.getStock() - quantity;
        if (newStock < 0) newStock = 0;

        return updateStock(productId, newStock);
    }

    /**
     * 添加一条库存事务记录。
     *
     * <p>兼容：如果旧数据库 schema 缺少 product_name 列，会尝试 ALTER TABLE 补列后重试插入。</p>
     *
     * @param tx 库存事务
     * @return 插入结果；失败返回 -1
     */
    public long addStockTransaction(StockTransaction tx) {
        if (tx == null) return -1;
        ContentValues values = new ContentValues();
        if (tx.getId() == null || tx.getId().isEmpty()) tx.setId(UUID.randomUUID().toString());
        values.put(Constants.COLUMN_STOCK_TX_ID, tx.getId());
        values.put(Constants.COLUMN_STOCK_TX_PRODUCT_ID, tx.getProductId());
        values.put(Constants.COLUMN_STOCK_TX_PRODUCT_NAME, tx.getProductName());
        values.put(Constants.COLUMN_STOCK_TX_USER_ID, tx.getUserId());
        values.put(Constants.COLUMN_STOCK_TX_USER_ROLE, tx.getUserRole());
        values.put(Constants.COLUMN_STOCK_TX_TYPE, tx.getType());
        values.put(Constants.COLUMN_STOCK_TX_QUANTITY, tx.getQuantity());
        values.put(Constants.COLUMN_STOCK_TX_BEFORE, tx.getStockBefore());
        values.put(Constants.COLUMN_STOCK_TX_AFTER, tx.getStockAfter());
        values.put(Constants.COLUMN_STOCK_TX_REASON, tx.getReason());
        values.put(Constants.COLUMN_STOCK_TX_TIMESTAMP, tx.getTimestamp() == 0 ? System.currentTimeMillis() : tx.getTimestamp());

        try {
            long res = db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, values);
            if (res > 0) {
                try { Audit.writeSystemAudit(db, tx.getUserId(), tx.getUserRole(), "product:" + tx.getProductId(), tx.getType() != null ? tx.getType().toLowerCase() : "tx", tx.getReason()); } catch (Exception ignored) {}
            }
            return res;
        } catch (android.database.sqlite.SQLiteException e) {
            // 可能缺少 product_name 列或其它列，尝试添加列后重试
            try {
                android.database.Cursor c = db.rawQuery("PRAGMA table_info(" + Constants.TABLE_STOCK_TRANSACTIONS + ")", null);
                boolean has = false;
                if (c != null) {
                    while (c.moveToNext()) {
                        String name = c.getString(c.getColumnIndexOrThrow("name"));
                        if (Constants.COLUMN_STOCK_TX_PRODUCT_NAME.equals(name)) { has = true; break; }
                    }
                    c.close();
                }
                if (!has) {
                    try { db.execSQL("ALTER TABLE " + Constants.TABLE_STOCK_TRANSACTIONS + " ADD COLUMN " + Constants.COLUMN_STOCK_TX_PRODUCT_NAME + " TEXT"); } catch (Exception ignored) {}
                }
                long res = db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, values);
                if (res > 0) {
                    try { Audit.writeSystemAudit(db, tx.getUserId(), tx.getUserRole(), "product:" + tx.getProductId(), tx.getType() != null ? tx.getType().toLowerCase() : "tx", tx.getReason()); } catch (Exception ignored) {}
                }
                return res;
            } catch (Exception ex) {
                ex.printStackTrace();
                return -1;
            }
        }
    }

    /**
     * 调整货架库存并写入事务记录（事务内执行）。
     *
     * <p>规则：
     * - type=IN：库存增加；
     * - type=OUT：库存减少，若货架库存不足则返回 false；
     * - OUT 场景会同步把数量加到“仓库库存”（表示从货架下架回仓库），并写入第二条事务记录。</p>
     */
    public boolean adjustStockWithTransaction(String productId, int quantity, String type, String userId, String userRole, String reason) {
        if (productId == null || type == null) return false;
        db.beginTransaction();
        try {
            Product product = getProductById(productId);
            if (product == null) return false;

            int before = product.getStock();
            int after;
            if ("IN".equalsIgnoreCase(type)) {
                after = before + quantity;
            } else if ("OUT".equalsIgnoreCase(type)) {
                // 如果货架库存不足，拒绝出库
                if (before < quantity) {
                    return false;
                }
                after = before - quantity;
            } else {
                return false;
            }

            int updated = updateStock(productId, after);
            if (updated <= 0) return false;

            StockTransaction tx = new StockTransaction();
            tx.setProductId(productId);
            // 尝试设置产品名称，便于删除商品后仍保留可读历史
            tx.setProductName(product.getName());
            tx.setUserId(userId);
            tx.setUserRole(userRole);
            tx.setType(type.toUpperCase());
            tx.setQuantity(quantity);
            tx.setStockBefore(before);
            tx.setStockAfter(after);
            tx.setReason(reason);
            tx.setTimestamp(System.currentTimeMillis());

            addStockTransaction(tx);

            // 当货架下架（即把货从货架移回仓库）时，同时增加仓库库存并写入对应事务记录
            if ("OUT".equalsIgnoreCase(type)) {
                int whBefore = product.getWarehouseStock();
                int whAfter = whBefore + quantity;
                int updWh = updateWarehouseStock(productId, whAfter);
                if (updWh <= 0) {
                    return false;
                }

                StockTransaction whTx = new StockTransaction();
                whTx.setProductId(productId);
                whTx.setProductName(product.getName());
                whTx.setUserId(userId);
                whTx.setUserRole(userRole);
                whTx.setType("WAREHOUSE_IN_FROM_SHELF");
                whTx.setQuantity(quantity);
                whTx.setStockBefore(whBefore);
                whTx.setStockAfter(whAfter);
                whTx.setReason("货架下架: " + (reason == null ? "" : reason));
                whTx.setTimestamp(System.currentTimeMillis());

                addStockTransaction(whTx);
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 调整仓库库存并写入事务记录（事务内执行）。
     *
     * <p>规则：
     * - type=IN：仓库库存增加；
     * - type=OUT：仓库库存减少，若仓库库存不足则返回 false；
     * - OUT 场景会同步把数量加到“货架库存”（表示从仓库补到货架），并写入第二条事务记录。</p>
     */
    public boolean adjustWarehouseWithTransaction(String productId, int quantity, String type, String userId, String userRole, String reason) {
        if (productId == null || type == null) return false;
        db.beginTransaction();
        try {
            Product product = getProductById(productId);
            if (product == null) return false;

            int before = product.getWarehouseStock();
            int after;
            if ("IN".equalsIgnoreCase(type)) {
                after = before + quantity;
            } else if ("OUT".equalsIgnoreCase(type)) {
                // 如果仓库库存不足，拒绝仓库出库
                if (before < quantity) {
                    return false;
                }
                after = before - quantity;
            } else {
                return false;
            }

            int updated = updateWarehouseStock(productId, after);
            if (updated <= 0) return false;
            // 写入仓库库存事务
            StockTransaction tx = new StockTransaction();
            tx.setProductId(productId);
            tx.setProductName(product.getName());
            tx.setUserId(userId);
            tx.setUserRole(userRole);
            tx.setType(("IN".equalsIgnoreCase(type) ? "WAREHOUSE_IN" : "WAREHOUSE_OUT"));
            tx.setQuantity(quantity);
            tx.setStockBefore(before);
            tx.setStockAfter(after);
            tx.setReason(reason);
            tx.setTimestamp(System.currentTimeMillis());

            addStockTransaction(tx);

            // 当仓库出库（即把货从仓库放到货架）时，同时增加货架库存并写入另一条事务记录
            if ("OUT".equalsIgnoreCase(type)) {
                int shelfBefore = product.getStock();
                int shelfAfter = shelfBefore + quantity;
                int updShelf = updateStock(productId, shelfAfter);
                if (updShelf <= 0) {
                    return false;
                }

                StockTransaction shelfTx = new StockTransaction();
                shelfTx.setProductId(productId);
                shelfTx.setProductName(product.getName());
                shelfTx.setUserId(userId);
                shelfTx.setUserRole(userRole);
                shelfTx.setType("IN_FROM_WAREHOUSE");
                shelfTx.setQuantity(quantity);
                shelfTx.setStockBefore(shelfBefore);
                shelfTx.setStockAfter(shelfAfter);
                shelfTx.setReason("来自仓库: " + (reason == null ? "" : reason));
                shelfTx.setTimestamp(System.currentTimeMillis());

                addStockTransaction(shelfTx);
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 获取指定商品的库存事务历史（按时间倒序）。
     */
    public List<StockTransaction> getStockHistory(String productId) {
        List<StockTransaction> list = new ArrayList<>();
        String selection = Constants.COLUMN_STOCK_TX_PRODUCT_ID + " = ?";
        String[] selectionArgs = new String[]{productId};
        String orderBy = Constants.COLUMN_STOCK_TX_TIMESTAMP + " DESC";

        Cursor cursor = db.query(Constants.TABLE_STOCK_TRANSACTIONS, null, selection, selectionArgs, null, null, orderBy);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                StockTransaction tx = StockTransaction.fromCursor(cursor);
                list.add(tx);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    /**
     * 获取所有库存事务（全局历史，按时间倒序）。
     *
     * <p>兼容：旧 schema 缺列时会降级为 rawQuery，失败则返回空列表，避免 UI 闪退。</p>
     */
    public List<StockTransaction> getAllStockHistory() {
        List<StockTransaction> list = new ArrayList<>();
        String orderBy = Constants.COLUMN_STOCK_TX_TIMESTAMP + " DESC";
        try {
            Cursor cursor = db.query(Constants.TABLE_STOCK_TRANSACTIONS, null, null, null, null, null, orderBy);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    StockTransaction tx = StockTransaction.fromCursor(cursor);
                    list.add(tx);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (android.database.sqlite.SQLiteException e) {
            // 可能是旧 schema 缺少某列，尝试使用安全的原生查询（只选择存在的列）或返回空列表
            try {
                String sql = "SELECT * FROM " + Constants.TABLE_STOCK_TRANSACTIONS + " ORDER BY " + Constants.COLUMN_STOCK_TX_TIMESTAMP + " DESC";
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        StockTransaction tx = StockTransaction.fromCursor(cursor);
                        list.add(tx);
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            } catch (Exception ex) {
                // 最后兜底，不抛出异常，返回空列表
                ex.printStackTrace();
            }
        }

        return list;
    }

    /**
     * 回填库存事务表中的 product_name 字段（从 products 表拷贝），用于修复老数据。
     *
     * <p>该操作为“最佳努力”，失败会被吞掉并打印日志，不影响主流程。</p>
     */
    public void backfillStockTransactionProductNames() {
        try {
            String sql = "UPDATE " + Constants.TABLE_STOCK_TRANSACTIONS + " SET " + Constants.COLUMN_STOCK_TX_PRODUCT_NAME + " = (SELECT " + Constants.COLUMN_PRODUCT_NAME + " FROM " + Constants.TABLE_PRODUCTS + " p WHERE p." + Constants.COLUMN_PRODUCT_ID + " = " + Constants.TABLE_STOCK_TRANSACTIONS + "." + Constants.COLUMN_STOCK_TX_PRODUCT_ID + ") WHERE " + Constants.COLUMN_STOCK_TX_PRODUCT_NAME + " IS NULL";
            db.execSQL(sql);
        } catch (Exception e) {
            // 忽略任何错误，回填为最佳努力
            e.printStackTrace();
        }
    }

    /**
     * 按产品名称搜索库存事务（支持模糊匹配）。
     *
     * <p>兼容：
     * - 若事务表包含 product_name 列，直接按该列 LIKE；
     * - 否则通过 JOIN products 表按商品名称过滤。</p>
     *
     * @param productName 商品名称关键字
     * @return 事务列表（按时间倒序）
     */
    public List<StockTransaction> searchStockHistoryByProductName(String productName) {
        List<StockTransaction> list = new ArrayList<>();
        String orderBy = Constants.COLUMN_STOCK_TX_TIMESTAMP + " DESC";
        try {
            // 先尝试直接在事务表上按 product_name 搜索（如果列存在）
            Cursor check = db.rawQuery("PRAGMA table_info(" + Constants.TABLE_STOCK_TRANSACTIONS + ")", null);
            boolean hasProductNameCol = false;
            if (check != null && check.moveToFirst()) {
                do {
                    String colName = check.getString(check.getColumnIndexOrThrow("name"));
                    if (Constants.COLUMN_STOCK_TX_PRODUCT_NAME.equals(colName)) {
                        hasProductNameCol = true;
                        break;
                    }
                } while (check.moveToNext());
                check.close();
            }

            if (hasProductNameCol) {
                String selection = Constants.COLUMN_STOCK_TX_PRODUCT_NAME + " LIKE ?";
                String[] selectionArgs = new String[]{"%" + productName + "%"};
                Cursor cursor = db.query(Constants.TABLE_STOCK_TRANSACTIONS, null, selection, selectionArgs, null, null, orderBy);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        StockTransaction tx = StockTransaction.fromCursor(cursor);
                        list.add(tx);
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            } else {
                // 如果事务表没有 product_name 列，尝试通过关联 products 表来按名称搜索
                String sql = "SELECT st.* FROM " + Constants.TABLE_STOCK_TRANSACTIONS + " st JOIN " + Constants.TABLE_PRODUCTS + " p ON st." + Constants.COLUMN_STOCK_TX_PRODUCT_ID + " = p." + Constants.COLUMN_PRODUCT_ID + " WHERE p." + Constants.COLUMN_PRODUCT_NAME + " LIKE ? ORDER BY st." + Constants.COLUMN_STOCK_TX_TIMESTAMP + " DESC";
                Cursor cursor = db.rawQuery(sql, new String[]{"%" + productName + "%"});
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        StockTransaction tx = StockTransaction.fromCursor(cursor);
                        list.add(tx);
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 返回空列表，不抛出，避免 UI 闪退
        }

        return list;
    }

    // 获取所有列名
    private String[] getAllColumns() {
        return new String[] {
                Constants.COLUMN_PRODUCT_ID,
                Constants.COLUMN_PRODUCT_NAME,
                Constants.COLUMN_THUMB_URL,
                Constants.COLUMN_CATEGORY,
                Constants.COLUMN_BRAND,
                Constants.COLUMN_PRICE,
                Constants.COLUMN_COST,
                Constants.COLUMN_STOCK,
                Constants.COLUMN_WAREHOUSE_STOCK,
                Constants.COLUMN_MIN_STOCK,
                Constants.COLUMN_MIN_WAREHOUSE_STOCK,
                Constants.COLUMN_UNIT,
                Constants.COLUMN_PRODUCTION_DATE,
                Constants.COLUMN_EXPIRATION_DATE,
                Constants.COLUMN_BARCODE,
                Constants.COLUMN_DESCRIPTION,
                Constants.COLUMN_SUPPLIER_ID,
                Constants.COLUMN_CREATED_AT,
                Constants.COLUMN_UPDATED_AT
        };
    }

    /**
     * 将 Cursor 转换为 Product 对象。
     *
     * <p>委托给 {@link Product#fromCursor(Cursor)}，以便统一处理缺列/旧 schema 兼容逻辑。</p>
     */
    private Product cursorToProduct(Cursor cursor) {
        // 委托给 Product.fromCursor：内部会兼容 thumb_url 以及缺列等老版本 schema 场景
        return Product.fromCursor(cursor);
    }
}