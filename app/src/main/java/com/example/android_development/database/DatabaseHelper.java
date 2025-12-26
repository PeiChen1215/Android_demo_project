package com.example.android_development.database;

import android.content.Context;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android_development.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.android_development.model.Product;
import com.example.android_development.model.User;
import com.example.android_development.util.Audit;

/**
 * SQLite 数据库帮助类。
 *
 * <p>负责：建表、版本升级迁移（onUpgrade）、以及部分基于 ContentValues/POJO 的便捷 CRUD。
 * 业务权限本身不在此类强制保证（权限校验更多由上层 DAO/业务层负责）。</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

        /**
         * 创建数据库帮助类。
         *
         * @param context 应用上下文
         */
    public DatabaseHelper(Context context) {
                super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
                this.context = context;
    }

                /**
                 * 确保表中存在指定列；如缺失则尝试执行 ALTER TABLE ADD COLUMN（用于兼容旧版本数据库结构）。
                 *
                 * <p>该方法为“最佳努力”：遇到不支持的 SQLite 场景会吞掉异常，避免升级/运行时直接崩溃。</p>
                 *
                 * @param db 数据库连接
                 * @param tableName 表名
                 * @param columnName 列名
                 * @param columnDef 列定义（例如 "INTEGER DEFAULT 0"）
                 */
    private void ensureColumnExists(SQLiteDatabase db, String tableName, String columnName, String columnDef) {
            try {
                    android.database.Cursor c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
                    boolean found = false;
                    if (c != null) {
                            while (c.moveToNext()) {
                                    String name = c.getString(c.getColumnIndexOrThrow("name"));
                                    if (columnName.equals(name)) { found = true; break; }
                            }
                            c.close();
                    }
                    if (!found) {
                            try {
                                    db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDef);
                            } catch (Exception ignored) {
                                    // 某些 SQLite 版本/场景不允许 ALTER TABLE ADD COLUMN；忽略即可（兼容处理）
                            }
                    }
            } catch (Exception ignored) {}
    }

        private Context context;

    @Override
        /**
         * 首次创建数据库时建表并写入演示数据。
         *
         * <p>注意：该项目包含“测试用户/测试商品”初始化逻辑，便于首次安装直接体验。</p>
         */
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        db.execSQL(DbContract.SQL_CREATE_TABLE_USERS);

        db.execSQL(DbContract.SQL_CREATE_TABLE_PRODUCTS);

        // 创建库存事务表
        db.execSQL(DbContract.SQL_CREATE_TABLE_STOCK_TRANSACTIONS);

                // 创建供应商/采购/盘点相关表（模块脚手架）
                try {
                        db.execSQL(DbContract.SQL_CREATE_TABLE_SUPPLIERS);
                } catch (Exception ignored) {}
                        try {
                                db.execSQL(DbContract.SQL_CREATE_TABLE_SALES);
                        } catch (Exception ignored) {}
                        try {
                                db.execSQL(DbContract.SQL_CREATE_TABLE_SALE_LINES);
                        } catch (Exception ignored) {}
                        try {
                                db.execSQL(DbContract.SQL_CREATE_TABLE_REFUNDS);
                        } catch (Exception ignored) {}
                try {
                        db.execSQL(DbContract.SQL_CREATE_TABLE_PURCHASE_ORDERS);
                } catch (Exception ignored) {}
                try {
                        db.execSQL(DbContract.SQL_CREATE_TABLE_PURCHASE_LINES);
                                try { db.execSQL(DbContract.SQL_CREATE_TABLE_PO_APPROVALS); } catch (Exception ignored) {}
                } catch (Exception ignored) {}
                try {
                        db.execSQL(DbContract.SQL_CREATE_TABLE_STOCK_COUNTS);
                } catch (Exception ignored) {}
                try {
                        db.execSQL(DbContract.SQL_CREATE_TABLE_STOCK_COUNT_LINES);
                } catch (Exception ignored) {}

                // 创建商品相关索引以提高查询性能
                try {
                        db.execSQL(DbContract.SQL_CREATE_INDEX_PRODUCTS_NAME);
                        db.execSQL(DbContract.SQL_CREATE_INDEX_PRODUCTS_BARCODE);
                        db.execSQL(DbContract.SQL_CREATE_INDEX_PRODUCTS_CATEGORY);
                } catch (Exception e) {
                        e.printStackTrace();
                }

        // 插入不同角色的测试用户
        insertTestUsers(db);

        // 插入测试商品
        insertTestProducts(db);
    }

        /**
         * 写入演示/测试用户数据。
         *
         * <p>包含管理员、采购员、收银员、库存管理员、财务等示例账号。</p>
         */
    private void insertTestUsers(SQLiteDatabase db) {
        // 1. 管理员
        String adminId = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_USERS + " (" +
                Constants.COLUMN_USER_ID + ", " +
                Constants.COLUMN_USERNAME + ", " +
                Constants.COLUMN_PASSWORD + ", " +
                Constants.COLUMN_ROLE + ", " +
                Constants.COLUMN_FULL_NAME + ", " +
                Constants.COLUMN_CREATED_AT +
                ") VALUES ('" + adminId + "', 'admin', 'admin123', '" +
                Constants.ROLE_ADMIN + "', '系统管理员', " + System.currentTimeMillis() + ")");

        // 2. 采购员
        String buyerId = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_USERS + " (" +
                Constants.COLUMN_USER_ID + ", " +
                Constants.COLUMN_USERNAME + ", " +
                Constants.COLUMN_PASSWORD + ", " +
                Constants.COLUMN_ROLE + ", " +
                Constants.COLUMN_FULL_NAME + ", " +
                Constants.COLUMN_CREATED_AT +
                ") VALUES ('" + buyerId + "', 'buyer1', '123456', '" +
                Constants.ROLE_BUYER + "', '采购员张三', " + System.currentTimeMillis() + ")");

        // 3. 收银员
        String cashierId = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_USERS + " (" +
                Constants.COLUMN_USER_ID + ", " +
                Constants.COLUMN_USERNAME + ", " +
                Constants.COLUMN_PASSWORD + ", " +
                Constants.COLUMN_ROLE + ", " +
                Constants.COLUMN_FULL_NAME + ", " +
                Constants.COLUMN_CREATED_AT +
                ") VALUES ('" + cashierId + "', 'cashier1', '123456', '" +
                Constants.ROLE_CASHIER + "', '收银员李四', " + System.currentTimeMillis() + ")");

        // 4. 库存管理员
        String stockId = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_USERS + " (" +
                Constants.COLUMN_USER_ID + ", " +
                Constants.COLUMN_USERNAME + ", " +
                Constants.COLUMN_PASSWORD + ", " +
                Constants.COLUMN_ROLE + ", " +
                Constants.COLUMN_FULL_NAME + ", " +
                Constants.COLUMN_CREATED_AT +
                ") VALUES ('" + stockId + "', 'stock1', '123456', '" +
                Constants.ROLE_STOCK + "', '库存管理员王五', " + System.currentTimeMillis() + ")");

        // （盘点员已并入库存管理员，故不再插入单独示例用户）
        // 6. 财务/出纳（测试）
        String financeId = java.util.UUID.randomUUID().toString();
        try {
            db.execSQL("INSERT INTO " + Constants.TABLE_USERS + " (" +
                    Constants.COLUMN_USER_ID + ", " +
                    Constants.COLUMN_USERNAME + ", " +
                    Constants.COLUMN_PASSWORD + ", " +
                    Constants.COLUMN_ROLE + ", " +
                    Constants.COLUMN_FULL_NAME + ", " +
                    Constants.COLUMN_CREATED_AT +
                    ") VALUES ('" + financeId + "', 'finance1', '123456', '" +
                    Constants.ROLE_FINANCE + "', '财务张三', " + System.currentTimeMillis() + ")");
        } catch (Exception ignored) {}
    }
    // 添加测试商品数据
        /**
         * 写入演示/测试商品数据。
         */
    private void insertTestProducts(SQLiteDatabase db) {
        // 商品1：矿泉水
        String productId1 = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_PRODUCTS + " (" +
                Constants.COLUMN_PRODUCT_ID + ", " +
                Constants.COLUMN_PRODUCT_NAME + ", " +
                Constants.COLUMN_CATEGORY + ", " +
                Constants.COLUMN_BRAND + ", " +
                Constants.COLUMN_PRICE + ", " +
                Constants.COLUMN_COST + ", " +
                Constants.COLUMN_STOCK + ", " +
                Constants.COLUMN_MIN_STOCK + ", " +
                Constants.COLUMN_UNIT + ", " +
                Constants.COLUMN_BARCODE + ", " +
                Constants.COLUMN_DESCRIPTION + ", " +
                Constants.COLUMN_CREATED_AT + ", " +
                Constants.COLUMN_UPDATED_AT +
                ") VALUES ('" + productId1 + "', '矿泉水', '" +
                Constants.CATEGORY_DRINK + "', '农夫山泉', 2.0, 1.2, 100, 20, '瓶', '6901234567890', '500ml瓶装矿泉水', " +
                System.currentTimeMillis() + ", " + System.currentTimeMillis() + ")");

        // 商品2：方便面
        String productId2 = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_PRODUCTS + " (" +
                Constants.COLUMN_PRODUCT_ID + ", " +
                Constants.COLUMN_PRODUCT_NAME + ", " +
                Constants.COLUMN_CATEGORY + ", " +
                Constants.COLUMN_BRAND + ", " +
                Constants.COLUMN_PRICE + ", " +
                Constants.COLUMN_COST + ", " +
                Constants.COLUMN_STOCK + ", " +
                Constants.COLUMN_MIN_STOCK + ", " +
                Constants.COLUMN_UNIT + ", " +
                Constants.COLUMN_BARCODE + ", " +
                Constants.COLUMN_DESCRIPTION + ", " +
                Constants.COLUMN_CREATED_AT + ", " +
                Constants.COLUMN_UPDATED_AT +
                ") VALUES ('" + productId2 + "', '方便面', '" +
                Constants.CATEGORY_FOOD + "', '康师傅', 4.5, 2.8, 50, 15, '袋', '6912345678901', '红烧牛肉面120g', " +
                System.currentTimeMillis() + ", " + System.currentTimeMillis() + ")");

        // 商品3：纸巾
        String productId3 = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_PRODUCTS + " (" +
                Constants.COLUMN_PRODUCT_ID + ", " +
                Constants.COLUMN_PRODUCT_NAME + ", " +
                Constants.COLUMN_CATEGORY + ", " +
                Constants.COLUMN_BRAND + ", " +
                Constants.COLUMN_PRICE + ", " +
                Constants.COLUMN_COST + ", " +
                Constants.COLUMN_STOCK + ", " +
                Constants.COLUMN_MIN_STOCK + ", " +
                Constants.COLUMN_UNIT + ", " +
                Constants.COLUMN_BARCODE + ", " +
                Constants.COLUMN_DESCRIPTION + ", " +
                Constants.COLUMN_CREATED_AT + ", " +
                Constants.COLUMN_UPDATED_AT +
                ") VALUES ('" + productId3 + "', '纸巾', '" +
                Constants.CATEGORY_DAILY + "', '心相印', 8.0, 5.0, 30, 10, '包', '6923456789012', '200抽软抽纸巾', " +
                System.currentTimeMillis() + ", " + System.currentTimeMillis() + ")");

        // 商品4：可乐
        String productId4 = java.util.UUID.randomUUID().toString();
        db.execSQL("INSERT INTO " + Constants.TABLE_PRODUCTS + " (" +
                Constants.COLUMN_PRODUCT_ID + ", " +
                Constants.COLUMN_PRODUCT_NAME + ", " +
                Constants.COLUMN_CATEGORY + ", " +
                Constants.COLUMN_BRAND + ", " +
                Constants.COLUMN_PRICE + ", " +
                Constants.COLUMN_COST + ", " +
                Constants.COLUMN_STOCK + ", " +
                Constants.COLUMN_MIN_STOCK + ", " +
                Constants.COLUMN_UNIT + ", " +
                Constants.COLUMN_BARCODE + ", " +
                Constants.COLUMN_DESCRIPTION + ", " +
                Constants.COLUMN_CREATED_AT + ", " +
                Constants.COLUMN_UPDATED_AT +
                ") VALUES ('" + productId4 + "', '可乐', '" +
                Constants.CATEGORY_DRINK + "', '可口可乐', 3.0, 1.8, 80, 25, '瓶', '6934567890123', '500ml瓶装可乐', " +
                System.currentTimeMillis() + ", " + System.currentTimeMillis() + ")");
    }

        // ---------- 商品 CRUD（ContentValues） ----------
        /**
         * 新增/替换商品记录（ContentValues 形式）。
         */
        public long addProduct(ContentValues values) {
                SQLiteDatabase db = getWritableDatabase();
                return db.insertWithOnConflict(Constants.TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        /**
         * 根据商品 id 查询（返回 Cursor）。
         */
        public Cursor getProductById(String productId) {
                SQLiteDatabase db = getReadableDatabase();
                String selection = Constants.COLUMN_PRODUCT_ID + " = ?";
                String[] selectionArgs = new String[]{productId};
                return db.query(Constants.TABLE_PRODUCTS, null, selection, selectionArgs, null, null, null);
        }

        /**
         * 查询全部商品（返回 Cursor）。
         */
        public Cursor getAllProducts() {
                SQLiteDatabase db = getReadableDatabase();
                return db.query(Constants.TABLE_PRODUCTS, null, null, null, null, null, Constants.COLUMN_PRODUCT_NAME + " ASC");
        }

        /**
         * 更新商品（ContentValues 形式）。
         */
        public int updateProduct(String productId, ContentValues values) {
                SQLiteDatabase db = getWritableDatabase();
                String where = Constants.COLUMN_PRODUCT_ID + " = ?";
                String[] whereArgs = new String[]{productId};
                return db.update(Constants.TABLE_PRODUCTS, values, where, whereArgs);
        }

        /**
         * 删除商品。
         */
        public int deleteProduct(String productId) {
                SQLiteDatabase db = getWritableDatabase();
                String where = Constants.COLUMN_PRODUCT_ID + " = ?";
                String[] whereArgs = new String[]{productId};
                return db.delete(Constants.TABLE_PRODUCTS, where, whereArgs);
        }

        // ---------- 用户 CRUD（ContentValues） ----------
        /**
         * 新增/替换用户记录（ContentValues 形式）。
         */
        public long addUser(ContentValues values) {
                SQLiteDatabase db = getWritableDatabase();
                return db.insertWithOnConflict(Constants.TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        /**
         * 根据用户 id 查询（返回 Cursor）。
         */
        public Cursor getUserById(String userId) {
                SQLiteDatabase db = getReadableDatabase();
                String selection = Constants.COLUMN_USER_ID + " = ?";
                String[] selectionArgs = new String[]{userId};
                return db.query(Constants.TABLE_USERS, null, selection, selectionArgs, null, null, null);
        }

        /**
         * 查询全部用户（返回 Cursor）。
         */
        public Cursor getAllUsers() {
                SQLiteDatabase db = getReadableDatabase();
                return db.query(Constants.TABLE_USERS, null, null, null, null, null, Constants.COLUMN_USERNAME + " ASC");
        }

        /**
         * 更新用户（ContentValues 形式）。
         */
        public int updateUser(String userId, ContentValues values) {
                SQLiteDatabase db = getWritableDatabase();
                String where = Constants.COLUMN_USER_ID + " = ?";
                String[] whereArgs = new String[]{userId};
                return db.update(Constants.TABLE_USERS, values, where, whereArgs);
        }

        /**
         * 删除用户。
         */
        public int deleteUser(String userId) {
                SQLiteDatabase db = getWritableDatabase();
                String where = Constants.COLUMN_USER_ID + " = ?";
                String[] whereArgs = new String[]{userId};
                return db.delete(Constants.TABLE_USERS, where, whereArgs);
        }

        // ---------- POJO 读取封装 ----------
        /**
         * 获取全部商品列表（POJO 形式）。
         */
        public List<Product> getAllProductsList() {
                List<Product> list = new ArrayList<>();
                Cursor c = getAllProducts();
                if (c != null) {
                        while (c.moveToNext()) {
                                Product p = Product.fromCursor(c);
                                list.add(p);
                        }
                        c.close();
                }
                return list;
        }

        /**
         * 根据商品 id 获取商品对象（POJO 形式）。
         */
        public Product getProductByIdObject(String productId) {
                Cursor c = getProductById(productId);
                Product p = null;
                if (c != null) {
                        if (c.moveToFirst()) p = Product.fromCursor(c);
                        c.close();
                }
                return p;
        }

        public List<User> getAllUsersList() {
                List<User> list = new ArrayList<>();
                Cursor c = getAllUsers();
                if (c != null) {
                        while (c.moveToNext()) {
                                User u = User.fromCursor(c);
                                list.add(u);
                        }
                        c.close();
                }
                return list;
        }

        public User getUserByIdObject(String userId) {
                Cursor c = getUserById(userId);
                User u = null;
                if (c != null) {
                        if (c.moveToFirst()) u = User.fromCursor(c);
                        c.close();
                }
                return u;
        }

        // ---------- POJO 写入封装（add/update） ----------
        public long addProduct(Product product) {
                if (product == null) return -1;
                if (product.getId() == null || product.getId().isEmpty()) {
                        product.setId(UUID.randomUUID().toString());
                }
                long now = System.currentTimeMillis();
                if (product.getCreatedAt() == 0) product.setCreatedAt(now);
                product.setUpdatedAt(now);
                ContentValues v = product.toContentValues();
                return addProduct(v);
        }

        public int updateProduct(Product product) {
                if (product == null || product.getId() == null) return 0;
                product.setUpdatedAt(System.currentTimeMillis());
                return updateProduct(product.getId(), product.toContentValues());
        }

        public long addUser(User user) {
                if (user == null) return -1;
                if (user.getId() == null || user.getId().isEmpty()) {
                        user.setId(UUID.randomUUID().toString());
                }
                if (user.getCreatedAt() == 0) user.setCreatedAt(System.currentTimeMillis());
                ContentValues v = user.toContentValues();
                return addUser(v);
        }

        public int updateUser(User user) {
                if (user == null || user.getId() == null) return 0;
                return updateUser(user.getId(), user.toContentValues());
        }

        // ---------- 基于角色的权限校验辅助方法（Role-checked helpers） ----------
        /**
         * 判断角色是否为管理员。
         */
        private boolean isAdminRole(String role) {
                return role != null && role.equals(Constants.ROLE_ADMIN);
        }

        /**
         * 根据 userId 从 users 表查询角色并判断是否为管理员。
         */
        public boolean isUserAdminById(String userId) {
                if (userId == null) return false;
                Cursor c = getUserById(userId);
                boolean isAdmin = false;
                if (c != null) {
                        if (c.moveToFirst()) {
                                int idx = c.getColumnIndex(Constants.COLUMN_ROLE);
                                if (idx != -1) {
                                        String role = c.getString(idx);
                                        isAdmin = Constants.ROLE_ADMIN.equals(role);
                                }
                        }
                        c.close();
                }
                return isAdmin;
        }

        // 基于 userId 的受限封装：add/update/delete
        public long addProductAsUser(String userId, Product product) {
                if (!isUserAdminById(userId)) return -1;
                // 执行插入并在成功后写入历史记录（类型为 ADD）
                long res = addProduct(product);
                try {
                        if (res > 0) {
                                SQLiteDatabase db = getWritableDatabase();
                                // 插入事务记录前，确保 stock_transactions 表存在 product_name 列（兼容老版本 schema）
                                ensureColumnExists(db, Constants.TABLE_STOCK_TRANSACTIONS, Constants.COLUMN_STOCK_TX_PRODUCT_NAME, "TEXT");
                                ContentValues tx = new ContentValues();
                                tx.put(Constants.COLUMN_STOCK_TX_ID, java.util.UUID.randomUUID().toString());
                                tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_ID, product.getId());
                                tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_NAME, product.getName());
                                tx.put(Constants.COLUMN_STOCK_TX_USER_ID, userId);
                                String role = null;
                                if (userId != null) {
                                        User uobj = getUserByIdObject(userId);
                                        if (uobj != null) role = uobj.getRole();
                                }
                                tx.put(Constants.COLUMN_STOCK_TX_USER_ROLE, role);
                                tx.put(Constants.COLUMN_STOCK_TX_TYPE, "ADD");
                                tx.put(Constants.COLUMN_STOCK_TX_QUANTITY, product != null ? product.getStock() : 0);
                                tx.put(Constants.COLUMN_STOCK_TX_BEFORE, 0);
                                tx.put(Constants.COLUMN_STOCK_TX_AFTER, product != null ? product.getStock() : 0);
                                tx.put(Constants.COLUMN_STOCK_TX_REASON, "添加商品");
                                tx.put(Constants.COLUMN_STOCK_TX_TIMESTAMP, System.currentTimeMillis());
                                try { db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, tx); } catch (Exception ignored) {}
                                try { Audit.writeSystemAudit(db, userId, role, "product:" + product.getId(), "add", "添加商品"); } catch (Exception ignored) {}
                        }
                } catch (Exception ignored) {}

                return res;
        }

        public int updateProductAsUser(String userId, Product product) {
                if (!isUserAdminById(userId)) return 0;
                if (product == null || product.getId() == null) return 0;

                SQLiteDatabase db = getWritableDatabase();
                int rows = 0;
                db.beginTransaction();
                try {
                        // 获取修改前的库存
                        Product before = getProductByIdObject(product.getId());
                        int beforeStock = before != null ? before.getStock() : 0;
                        // 执行更新
                        ContentValues v = product.toContentValues();
                        String where = Constants.COLUMN_PRODUCT_ID + " = ?";
                        String[] whereArgs = new String[]{product.getId()};
                        product.setUpdatedAt(System.currentTimeMillis());
                        rows = (int) db.update(Constants.TABLE_PRODUCTS, v, where, whereArgs);

                        // 如果库存发生变化，记录事务
                        int afterStock = product.getStock();
                        if (rows > 0 && beforeStock != afterStock) {
                                ContentValues tx = new ContentValues();
                                // 插入事务记录前，确保 stock_transactions 表存在 product_name 列（兼容老版本 schema）
                                ensureColumnExists(db, Constants.TABLE_STOCK_TRANSACTIONS, Constants.COLUMN_STOCK_TX_PRODUCT_NAME, "TEXT");
                                tx.put(Constants.COLUMN_STOCK_TX_ID, java.util.UUID.randomUUID().toString());
                                tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_ID, product.getId());
                                tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_NAME, product.getName());
                                tx.put(Constants.COLUMN_STOCK_TX_USER_ID, userId);
                                // 查询用户角色并写入
                                String role = null;
                                if (userId != null) {
                                        User uobj = getUserByIdObject(userId);
                                        if (uobj != null) role = uobj.getRole();
                                }
                                tx.put(Constants.COLUMN_STOCK_TX_USER_ROLE, role);
                                String type = afterStock > beforeStock ? "IN" : "OUT";
                                tx.put(Constants.COLUMN_STOCK_TX_TYPE, type);
                                tx.put(Constants.COLUMN_STOCK_TX_QUANTITY, Math.abs(afterStock - beforeStock));
                                tx.put(Constants.COLUMN_STOCK_TX_BEFORE, beforeStock);
                                tx.put(Constants.COLUMN_STOCK_TX_AFTER, afterStock);
                                tx.put(Constants.COLUMN_STOCK_TX_REASON, "管理员修改库存");
                                tx.put(Constants.COLUMN_STOCK_TX_TIMESTAMP, System.currentTimeMillis());
                                try { db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, tx); } catch (Exception ignored) {}
                                try { Audit.writeSystemAudit(db, userId, role, "product:" + product.getId(), type.toLowerCase(), "管理员修改库存"); } catch (Exception ignored) {}
                        }

                        db.setTransactionSuccessful();
                } catch (Exception e) {
                        e.printStackTrace();
                } finally {
                        db.endTransaction();
                }

                return rows;
        }

        public int deleteProductAsUser(String userId, String productId) {
                if (!isUserAdminById(userId)) return 0;
                // 先读取商品信息以便记录名称/库存
                Product p = getProductByIdObject(productId);
                int rows = deleteProduct(productId);
                try {
                        if (rows > 0) {
                                SQLiteDatabase db = getWritableDatabase();
                                // 插入事务记录前，确保 stock_transactions 表存在 product_name 列（兼容老版本 schema）
                                ensureColumnExists(db, Constants.TABLE_STOCK_TRANSACTIONS, Constants.COLUMN_STOCK_TX_PRODUCT_NAME, "TEXT");
                                ContentValues tx = new ContentValues();
                                tx.put(Constants.COLUMN_STOCK_TX_ID, java.util.UUID.randomUUID().toString());
                                tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_ID, productId);
                                tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_NAME, p != null ? p.getName() : null);
                                tx.put(Constants.COLUMN_STOCK_TX_USER_ID, userId);
                                String role = null;
                                if (userId != null) {
                                        User uobj = getUserByIdObject(userId);
                                        if (uobj != null) role = uobj.getRole();
                                }
                                tx.put(Constants.COLUMN_STOCK_TX_USER_ROLE, role);
                                tx.put(Constants.COLUMN_STOCK_TX_TYPE, "DELETE");
                                int beforeStock = p != null ? p.getStock() : 0;
                                tx.put(Constants.COLUMN_STOCK_TX_QUANTITY, beforeStock);
                                tx.put(Constants.COLUMN_STOCK_TX_BEFORE, beforeStock);
                                tx.put(Constants.COLUMN_STOCK_TX_AFTER, 0);
                                tx.put(Constants.COLUMN_STOCK_TX_REASON, "删除商品");
                                tx.put(Constants.COLUMN_STOCK_TX_TIMESTAMP, System.currentTimeMillis());
                                try { db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, tx); } catch (Exception ignored) {}
                                try { Audit.writeSystemAudit(db, userId, role, "product:" + productId, "delete", "删除商品"); } catch (Exception ignored) {}
                        }
                } catch (Exception ignored) {}

                return rows;
        }

        /**
         * 在调用方传入当前用户角色时，只有管理员可新增商品。
         * 返回 -1 表示未授权或失败，>0 为插入行 id。
         */
        public long addProductAsRole(String userRole, Product product) {
                if (!isAdminRole(userRole)) return -1;
                return addProduct(product);
        }

        /**
         * 管理员更新商品。
         * 返回 0 表示未授权或未更新，>0 表示更新的行数。
         */
        public int updateProductAsRole(String userRole, Product product) {
                if (!isAdminRole(userRole)) return 0;
                if (product == null || product.getId() == null) return 0;

                // 使用空的 userId 表示通过角色修改（未知具体用户）
                String roleUserId = null;
                SQLiteDatabase db = getWritableDatabase();
                int rows = 0;
                db.beginTransaction();
                try {
                        Product before = getProductByIdObject(product.getId());
                        int beforeStock = before != null ? before.getStock() : 0;

                        ContentValues v = product.toContentValues();
                        String where = Constants.COLUMN_PRODUCT_ID + " = ?";
                        String[] whereArgs = new String[]{product.getId()};
                        product.setUpdatedAt(System.currentTimeMillis());
                        rows = (int) db.update(Constants.TABLE_PRODUCTS, v, where, whereArgs);

                        int afterStock = product.getStock();
                        if (rows > 0 && beforeStock != afterStock) {
                                        ContentValues tx = new ContentValues();
                                        tx.put(Constants.COLUMN_STOCK_TX_ID, java.util.UUID.randomUUID().toString());
                                        tx.put(Constants.COLUMN_STOCK_TX_PRODUCT_ID, product.getId());
                                        tx.put(Constants.COLUMN_STOCK_TX_USER_ID, roleUserId);
                                        // 写入库存事务时同时记录当前角色（此处按角色修改，userId 为空）
                                        tx.put(Constants.COLUMN_STOCK_TX_USER_ROLE, userRole);
                                        String type = afterStock > beforeStock ? "IN" : "OUT";
                                        tx.put(Constants.COLUMN_STOCK_TX_TYPE, type);
                                        tx.put(Constants.COLUMN_STOCK_TX_QUANTITY, Math.abs(afterStock - beforeStock));
                                        tx.put(Constants.COLUMN_STOCK_TX_BEFORE, beforeStock);
                                        tx.put(Constants.COLUMN_STOCK_TX_AFTER, afterStock);
                                        tx.put(Constants.COLUMN_STOCK_TX_REASON, "通过角色修改库存");
                                        tx.put(Constants.COLUMN_STOCK_TX_TIMESTAMP, System.currentTimeMillis());
                                        db.insert(Constants.TABLE_STOCK_TRANSACTIONS, null, tx);
                                        try { Audit.writeSystemAudit(db, roleUserId, userRole, "product:" + product.getId(), type.toLowerCase(), "通过角色修改库存"); } catch (Exception ignored) {}
                        }

                        db.setTransactionSuccessful();
                } catch (Exception e) {
                        e.printStackTrace();
                } finally {
                        db.endTransaction();
                }

                return rows;
        }

        /**
         * 管理员删除商品。
         * 返回 0 表示未授权或未删除，>0 表示删除的行数。
         */
        public int deleteProductAsRole(String userRole, String productId) {
                if (!isAdminRole(userRole)) return 0;
                return deleteProduct(productId);
        }

    @Override
        /**
         * 数据库升级回调。
         *
         * <p>采用增量迁移策略：尽量保留数据，仅在必要时补列/建表/迁移历史数据。</p>
         */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                // 使用增量迁移策略，尽量保留数据。仅在必要时添加新列或创建缺失表。
                android.util.Log.d("DEBUG", "数据库从版本 " + oldVersion + " 升级到 " + newVersion);

                if (oldVersion < 4) {
                    // 增加“仓库库存”相关字段
                    ensureColumnExists(db, Constants.TABLE_PRODUCTS, Constants.COLUMN_WAREHOUSE_STOCK, "INTEGER DEFAULT 0");
                    ensureColumnExists(db, Constants.TABLE_PRODUCTS, Constants.COLUMN_MIN_WAREHOUSE_STOCK, "INTEGER DEFAULT 0");
                }

                if (oldVersion < 5) {
                    // 增加“生产日期/保质期（到期日）”字段
                    ensureColumnExists(db, Constants.TABLE_PRODUCTS, Constants.COLUMN_PRODUCTION_DATE, "INTEGER");
                    ensureColumnExists(db, Constants.TABLE_PRODUCTS, Constants.COLUMN_EXPIRATION_DATE, "INTEGER");
                }

                                if (oldVersion < 6) {
                                                // 保障 purchase_orders 表存在 status 列，并创建 approvals 审批记录表
                                                ensureColumnExists(db, Constants.TABLE_PURCHASE_ORDERS, Constants.COLUMN_PO_STATUS, "TEXT DEFAULT '" + Constants.PO_STATUS_CREATED + "'");
                                                try { db.execSQL(DbContract.SQL_CREATE_TABLE_PO_APPROVALS); } catch (Exception ignored) {}
                                }

                                if (oldVersion < 7) {
                                        // 增加采购单名称（PO name）字段
                                        ensureColumnExists(db, Constants.TABLE_PURCHASE_ORDERS, Constants.COLUMN_PO_NAME, "TEXT");
                                        // 对历史数据补默认名称：为空则填充 PO-<id 前 8 位>
                                        try {
                                                db.execSQL("UPDATE " + Constants.TABLE_PURCHASE_ORDERS + " SET " + Constants.COLUMN_PO_NAME + " = ('PO-' || substr(" + Constants.COLUMN_PO_ID + ",1,8)) WHERE " + Constants.COLUMN_PO_NAME + " IS NULL OR trim(" + Constants.COLUMN_PO_NAME + ") = ''");
                                        } catch (Exception ignored) {}
                                }

                // 保障 users 和 products 表存在（如果是非常旧的版本）
                if (oldVersion < 2) {
                        try {
                                db.execSQL(DbContract.SQL_CREATE_TABLE_USERS);
                        } catch (Exception ignored) {}
                        try {
                                db.execSQL(DbContract.SQL_CREATE_TABLE_PRODUCTS);
                        } catch (Exception ignored) {}
                        try {
                                db.execSQL(DbContract.SQL_CREATE_TABLE_STOCK_TRANSACTIONS);
                        } catch (Exception ignored) {}
                }

                if (oldVersion < 8) {
                        // 为 sales 表增加支付方式字段（payment_method）
                        try { ensureColumnExists(db, Constants.TABLE_SALES, Constants.COLUMN_SALE_PAYMENT_METHOD, "TEXT"); } catch (Exception ignored) {}
                }

                                // 如果数据库中存在旧的 'inventory' 角色，将其迁移为 'stock'，以合并角色权限
                                try {
                                        db.execSQL("UPDATE " + Constants.TABLE_USERS + " SET " + Constants.COLUMN_ROLE + " = '" + Constants.ROLE_STOCK + "' WHERE " + Constants.COLUMN_ROLE + " = 'inventory'");
                                } catch (Exception ignored) {}

                // 从版本 <3 升级：使用安全迁移脚本保留数据并为每条记录填充 user_role（如能从 users 表推断）
                if (oldVersion < 3) {
                        Cursor c = null;
                        boolean tableExists = false;
                        try {
                                c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + Constants.TABLE_STOCK_TRANSACTIONS + "'", null);
                                if (c != null && c.moveToFirst()) tableExists = true;
                        } catch (Exception e) {
                                e.printStackTrace();
                        } finally {
                                if (c != null) c.close();
                        }

                        if (!tableExists) {
                                // 表不存在，直接创建
                                try {
                                        db.execSQL(DbContract.SQL_CREATE_TABLE_STOCK_TRANSACTIONS);
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        } else {
                                // 表存在但可能缺少 user_role，执行更安全的拷贝迁移：
                                // 1) 创建新表 stock_transactions_new（带 user_role）
                                // 2) 将旧表数据拷贝过来，同时通过 users 表填充 user_role
                                // 3) 重命名表并删除旧表
                                db.beginTransaction();
                                try {
                                        String newTable = Constants.TABLE_STOCK_TRANSACTIONS + "_new";
                                        String createNew = "CREATE TABLE " + newTable + " (" +
                                                Constants.COLUMN_STOCK_TX_ID + " TEXT PRIMARY KEY," +
                                                Constants.COLUMN_STOCK_TX_PRODUCT_ID + " TEXT NOT NULL," +
                                                Constants.COLUMN_STOCK_TX_PRODUCT_NAME + " TEXT," +
                                                Constants.COLUMN_STOCK_TX_USER_ID + " TEXT," +
                                                Constants.COLUMN_STOCK_TX_USER_ROLE + " TEXT," +
                                                Constants.COLUMN_STOCK_TX_TYPE + " TEXT NOT NULL," +
                                                Constants.COLUMN_STOCK_TX_QUANTITY + " INTEGER NOT NULL," +
                                                Constants.COLUMN_STOCK_TX_BEFORE + " INTEGER," +
                                                Constants.COLUMN_STOCK_TX_AFTER + " INTEGER," +
                                                Constants.COLUMN_STOCK_TX_REASON + " TEXT," +
                                                Constants.COLUMN_STOCK_TX_TIMESTAMP + " INTEGER" +
                                                ")";

                                        db.execSQL(createNew);

                                        // 将旧表数据拷贝到新表，同时尝试从 users 表获取 role
                                        String insertSQL = "INSERT INTO " + newTable + " (" +
                                                Constants.COLUMN_STOCK_TX_ID + "," +
                                                Constants.COLUMN_STOCK_TX_PRODUCT_ID + "," +
                                                Constants.COLUMN_STOCK_TX_PRODUCT_NAME + "," +
                                                Constants.COLUMN_STOCK_TX_USER_ID + "," +
                                                Constants.COLUMN_STOCK_TX_USER_ROLE + "," +
                                                Constants.COLUMN_STOCK_TX_TYPE + "," +
                                                Constants.COLUMN_STOCK_TX_QUANTITY + "," +
                                                Constants.COLUMN_STOCK_TX_BEFORE + "," +
                                                Constants.COLUMN_STOCK_TX_AFTER + "," +
                                                Constants.COLUMN_STOCK_TX_REASON + "," +
                                                Constants.COLUMN_STOCK_TX_TIMESTAMP + 
                                                ") SELECT " +
                                                Constants.COLUMN_STOCK_TX_ID + "," +
                                                Constants.COLUMN_STOCK_TX_PRODUCT_ID + "," +
                                                "(SELECT " + Constants.COLUMN_PRODUCT_NAME + " FROM " + Constants.TABLE_PRODUCTS + " p WHERE p." + Constants.COLUMN_PRODUCT_ID + " = " + Constants.TABLE_STOCK_TRANSACTIONS + "." + Constants.COLUMN_STOCK_TX_PRODUCT_ID + ") as " + Constants.COLUMN_STOCK_TX_PRODUCT_NAME + "," +
                                                Constants.COLUMN_STOCK_TX_USER_ID + "," +
                                                "(SELECT " + Constants.COLUMN_ROLE + " FROM " + Constants.TABLE_USERS + " u WHERE u." + Constants.COLUMN_USER_ID + " = " + Constants.TABLE_STOCK_TRANSACTIONS + "." + Constants.COLUMN_STOCK_TX_USER_ID + ") as " + Constants.COLUMN_STOCK_TX_USER_ROLE + "," +
                                                Constants.COLUMN_STOCK_TX_TYPE + "," +
                                                Constants.COLUMN_STOCK_TX_QUANTITY + "," +
                                                Constants.COLUMN_STOCK_TX_BEFORE + "," +
                                                Constants.COLUMN_STOCK_TX_AFTER + "," +
                                                Constants.COLUMN_STOCK_TX_REASON + "," +
                                                Constants.COLUMN_STOCK_TX_TIMESTAMP + 
                                                " FROM " + Constants.TABLE_STOCK_TRANSACTIONS;

                                        db.execSQL(insertSQL);

                                        // 将旧表重命名并替换
                                        String oldTemp = Constants.TABLE_STOCK_TRANSACTIONS + "_old";
                                        db.execSQL("ALTER TABLE " + Constants.TABLE_STOCK_TRANSACTIONS + " RENAME TO " + oldTemp);
                                        db.execSQL("ALTER TABLE " + newTable + " RENAME TO " + Constants.TABLE_STOCK_TRANSACTIONS);
                                        db.execSQL("DROP TABLE IF EXISTS " + oldTemp);

                                        db.setTransactionSuccessful();
                                } catch (Exception e) {
                                        e.printStackTrace();
                                        // 兜底：如果迁移失败，确保目标表存在
                                        try {
                                                db.execSQL(DbContract.SQL_CREATE_TABLE_STOCK_TRANSACTIONS);
                                        } catch (Exception ex) {
                                                ex.printStackTrace();
                                        }
                                } finally {
                                        db.endTransaction();
                                }
                }

                // 确保在任何升级路径完成后，索引存在以提升查询性能
                try {
                        db.execSQL(DbContract.SQL_CREATE_INDEX_PRODUCTS_NAME);
                        db.execSQL(DbContract.SQL_CREATE_INDEX_PRODUCTS_BARCODE);
                        db.execSQL(DbContract.SQL_CREATE_INDEX_PRODUCTS_CATEGORY);
                } catch (Exception e) {
                        e.printStackTrace();
                }
                }

                // 兜底：确保采购/盘点/销售等脚手架表在任何升级路径后都存在，防止旧数据库缺失表导致运行时异常
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_SUPPLIERS); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_PURCHASE_ORDERS); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_PO_APPROVALS); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_SYSTEM_AUDIT); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_PURCHASE_LINES); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_SYSTEM_AUDIT); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_STOCK_COUNTS); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_STOCK_COUNT_LINES); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_SALES); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_SALE_LINES); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_REFUNDS); } catch (Exception ignored) {}
    }

        @Override
        /**
         * 数据库配置回调（在打开数据库时调用）。
         *
         * <p>用于在运行时兜底补齐可能缺失的列/表，提升旧数据库兼容性。</p>
         */
        public void onConfigure(SQLiteDatabase db) {
                super.onConfigure(db);
                // 在打开数据库时保障产品表包含 thumb_url 列（兼容老版本）
                try {
                        ensureColumnExists(db, Constants.TABLE_PRODUCTS, Constants.COLUMN_THUMB_URL, "TEXT");
                } catch (Exception ignored) {}
                try {
                        ensureColumnExists(db, Constants.TABLE_PURCHASE_ORDERS, Constants.COLUMN_PO_NAME, "TEXT");
                } catch (Exception ignored) {}
                try { ensureColumnExists(db, Constants.TABLE_SALES, Constants.COLUMN_SALE_PAYMENT_METHOD, "TEXT"); } catch (Exception ignored) {}
                try { ensureColumnExists(db, Constants.TABLE_SALES, Constants.COLUMN_SALE_REFUNDED, "INTEGER"); } catch (Exception ignored) {}
                try { ensureColumnExists(db, Constants.TABLE_SALES, Constants.COLUMN_SALE_REFUNDED_AT, "INTEGER"); } catch (Exception ignored) {}
                try { db.execSQL(DbContract.SQL_CREATE_TABLE_REFUNDS); } catch (Exception ignored) {}
        }

        /**
         * 根据条码查询商品（返回 Product 对象）。
         *
         * @param barcode 条码
         * @return 商品对象；未找到返回 null
         */
        public Product getProductByBarcodeObject(String barcode) {
                if (barcode == null || barcode.isEmpty()) return null;
                SQLiteDatabase db = getReadableDatabase();
                String selection = Constants.COLUMN_BARCODE + " = ?";
                String[] selectionArgs = new String[]{barcode};
                Cursor c = db.query(Constants.TABLE_PRODUCTS, null, selection, selectionArgs, null, null, null);
                Product p = null;
                if (c != null) {
                        if (c.moveToFirst()) {
                                p = Product.fromCursor(c);
                        }
                        c.close();
                }
                return p;
        }
}