package com.example.android_development.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_development.model.Product;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;
import com.example.android_development.model.StockTransaction;
import java.util.UUID;

public class ProductDAO {

    private SQLiteDatabase db;

    public ProductDAO(SQLiteDatabase db) {
        this.db = db;
    }

    // 添加商品
    public long addProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PRODUCT_ID, product.getId());
        values.put(Constants.COLUMN_PRODUCT_NAME, product.getName());
        values.put(Constants.COLUMN_CATEGORY, product.getCategory());
        values.put(Constants.COLUMN_BRAND, product.getBrand());
        values.put(Constants.COLUMN_PRICE, product.getPrice());
        values.put(Constants.COLUMN_COST, product.getCost());
        values.put(Constants.COLUMN_STOCK, product.getStock());
        values.put(Constants.COLUMN_MIN_STOCK, product.getMinStock());
        values.put(Constants.COLUMN_UNIT, product.getUnit());
        values.put(Constants.COLUMN_BARCODE, product.getBarcode());
        values.put(Constants.COLUMN_DESCRIPTION, product.getDescription());
        values.put(Constants.COLUMN_SUPPLIER_ID, product.getSupplierId());
        values.put(Constants.COLUMN_CREATED_AT, product.getCreatedAt());
        values.put(Constants.COLUMN_UPDATED_AT, product.getUpdatedAt());

        return db.insert(Constants.TABLE_PRODUCTS, null, values);
    }

    // 更新商品
    public int updateProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PRODUCT_NAME, product.getName());
        values.put(Constants.COLUMN_CATEGORY, product.getCategory());
        values.put(Constants.COLUMN_BRAND, product.getBrand());
        values.put(Constants.COLUMN_PRICE, product.getPrice());
        values.put(Constants.COLUMN_COST, product.getCost());
        values.put(Constants.COLUMN_STOCK, product.getStock());
        values.put(Constants.COLUMN_MIN_STOCK, product.getMinStock());
        values.put(Constants.COLUMN_UNIT, product.getUnit());
        values.put(Constants.COLUMN_BARCODE, product.getBarcode());
        values.put(Constants.COLUMN_DESCRIPTION, product.getDescription());
        values.put(Constants.COLUMN_SUPPLIER_ID, product.getSupplierId());
        values.put(Constants.COLUMN_UPDATED_AT, System.currentTimeMillis());

        String whereClause = Constants.COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = {product.getId()};

        return db.update(Constants.TABLE_PRODUCTS, values, whereClause, whereArgs);
    }

    // 删除商品
    public int deleteProduct(String productId) {
        String whereClause = Constants.COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = {productId};

        return db.delete(Constants.TABLE_PRODUCTS, whereClause, whereArgs);
    }

    // 根据ID获取商品
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

    // 获取所有商品
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

    // 根据分类获取商品
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

    // 搜索商品（按名称或条码）
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

    // 分页获取商品（limit, offset）
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

    // 分页搜索（按名称或条码）
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

    // 获取低库存商品
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

    // 更新库存数量
    public int updateStock(String productId, int newStock) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_STOCK, newStock);
        values.put(Constants.COLUMN_UPDATED_AT, System.currentTimeMillis());

        String whereClause = Constants.COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = {productId};

        return db.update(Constants.TABLE_PRODUCTS, values, whereClause, whereArgs);
    }

    // 增加库存（入库）
    public int increaseStock(String productId, int quantity) {
        Product product = getProductById(productId);
        if (product == null) return 0;

        int newStock = product.getStock() + quantity;
        return updateStock(productId, newStock);
    }

    // 减少库存（出库/销售）
    public int decreaseStock(String productId, int quantity) {
        Product product = getProductById(productId);
        if (product == null) return 0;

        int newStock = product.getStock() - quantity;
        if (newStock < 0) newStock = 0;

        return updateStock(productId, newStock);
    }

    // 添加库存事务记录
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
            return db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, values);
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
                return db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, values);
            } catch (Exception ex) {
                ex.printStackTrace();
                return -1;
            }
        }
    }

    // 调整库存并写入事务（在事务中执行）
    public boolean adjustStockWithTransaction(String productId, int quantity, String type, String userId, String userRole, String reason) {
        if (productId == null || type == null) return false;
        db.beginTransaction();
        try {
            Product product = getProductById(productId);
            if (product == null) return false;

            int before = product.getStock();
            int after = before;
            if ("IN".equalsIgnoreCase(type)) {
                after = before + quantity;
            } else if ("OUT".equalsIgnoreCase(type)) {
                after = before - quantity;
                if (after < 0) after = 0;
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

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // 获取商品的库存事务历史（按时间倒序）
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

    // 获取所有库存事务（全局历史）
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

    // 回填历史表中的 product_name 字段（从 products 表拷贝），用于修复老数据
    public void backfillStockTransactionProductNames() {
        try {
            String sql = "UPDATE " + Constants.TABLE_STOCK_TRANSACTIONS + " SET " + Constants.COLUMN_STOCK_TX_PRODUCT_NAME + " = (SELECT " + Constants.COLUMN_PRODUCT_NAME + " FROM " + Constants.TABLE_PRODUCTS + " p WHERE p." + Constants.COLUMN_PRODUCT_ID + " = " + Constants.TABLE_STOCK_TRANSACTIONS + "." + Constants.COLUMN_STOCK_TX_PRODUCT_ID + ") WHERE " + Constants.COLUMN_STOCK_TX_PRODUCT_NAME + " IS NULL";
            db.execSQL(sql);
        } catch (Exception e) {
            // 忽略任何错误，回填为最佳努力
            e.printStackTrace();
        }
    }

    // 按产品名称搜索库存事务（支持模糊匹配）
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
                Constants.COLUMN_CATEGORY,
                Constants.COLUMN_BRAND,
                Constants.COLUMN_PRICE,
                Constants.COLUMN_COST,
                Constants.COLUMN_STOCK,
                Constants.COLUMN_MIN_STOCK,
                Constants.COLUMN_UNIT,
                Constants.COLUMN_BARCODE,
                Constants.COLUMN_DESCRIPTION,
                Constants.COLUMN_SUPPLIER_ID,
                Constants.COLUMN_CREATED_AT,
                Constants.COLUMN_UPDATED_AT
        };
    }

    // 将Cursor转换为Product对象
    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();

        product.setId(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PRODUCT_ID)));
        product.setName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PRODUCT_NAME)));
        product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_CATEGORY)));
        product.setBrand(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_BRAND)));
        product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.COLUMN_PRICE)));
        product.setCost(cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.COLUMN_COST)));
        product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COLUMN_STOCK)));
        product.setMinStock(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COLUMN_MIN_STOCK)));
        product.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_UNIT)));
        product.setBarcode(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_BARCODE)));
        product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_DESCRIPTION)));
        product.setSupplierId(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_SUPPLIER_ID)));
        product.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_CREATED_AT)));
        product.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_UPDATED_AT)));

        return product;
    }
}