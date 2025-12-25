package com.example.android_development.util;

public class Constants {
    // 数据库常量
    public static final String DATABASE_NAME = "supermarket.db";
    public static final int DATABASE_VERSION = 8;

    // 用户表
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_CREATED_AT = "created_at";

    // SharedPreferences 常量
    public static final String PREFS_NAME = "supermarket_prefs";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_ROLE = "user_role";

    // 用户角色
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CASHIER = "cashier";
    public static final String ROLE_STOCK = "stock";
    public static final String ROLE_BUYER = "buyer";          // 采购员（采购子系统）
    // 将旧的“盘点员”角色合并为“库存管理员”（别名）
    public static final String ROLE_INVENTORY = ROLE_STOCK;  // 盘点员（盘点子系统）别名 -> "stock"

    // 财务/出纳角色
    public static final String ROLE_FINANCE = "finance";     // 财务（营收报表、对账）

    // 标准化角色（别名）
    public static final String ROLE_PURCHASER = "purchaser"; // 采购员别名
    public static final String ROLE_WAREHOUSE = "warehouse"; // 仓库操作员

    // 商品表
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_PRODUCT_NAME = "product_name";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_BRAND = "brand";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_COST = "cost";  // 成本价
    public static final String COLUMN_STOCK = "stock"; // 默认为货架库存
    public static final String COLUMN_WAREHOUSE_STOCK = "warehouse_stock"; // 仓库库存
    public static final String COLUMN_MIN_STOCK = "min_stock";  // 货架最低库存预警
    public static final String COLUMN_MIN_WAREHOUSE_STOCK = "min_warehouse_stock"; // 仓库最低库存预警
    public static final String COLUMN_UNIT = "unit";  // 单位（如：瓶、袋、个）
    public static final String COLUMN_PRODUCTION_DATE = "production_date"; // 生产日期
    public static final String COLUMN_EXPIRATION_DATE = "expiration_date"; // 过期日期
    public static final String COLUMN_BARCODE = "barcode";  // 条码
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_THUMB_URL = "thumb_url";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // 商品分类
    public static final String CATEGORY_DAILY = "daily";      // 日用品
    public static final String CATEGORY_FOOD = "food";        // 食品
    public static final String CATEGORY_DRINK = "drink";      // 饮料
    public static final String CATEGORY_SNACK = "snack";      // 零食
    public static final String CATEGORY_CLEANING = "cleaning"; // 清洁用品
    public static final String CATEGORY_OTHER = "other";      // 其他

    // 库存事务表
    public static final String TABLE_STOCK_TRANSACTIONS = "stock_transactions";
    public static final String COLUMN_STOCK_TX_ID = "tx_id";
    public static final String COLUMN_STOCK_TX_PRODUCT_ID = "product_id";
    public static final String COLUMN_STOCK_TX_PRODUCT_NAME = "product_name";
    public static final String COLUMN_STOCK_TX_USER_ID = "user_id";
    public static final String COLUMN_STOCK_TX_USER_ROLE = "user_role";
    public static final String COLUMN_STOCK_TX_TYPE = "tx_type"; // IN/OUT
    public static final String COLUMN_STOCK_TX_QUANTITY = "quantity";
    public static final String COLUMN_STOCK_TX_BEFORE = "stock_before";
    public static final String COLUMN_STOCK_TX_AFTER = "stock_after";
    public static final String COLUMN_STOCK_TX_REASON = "reason";
    public static final String COLUMN_STOCK_TX_TIMESTAMP = "timestamp";

    // 权限项（用于权限检查）
    public static final String PERM_CREATE_PO = "CREATE_PO";
    public static final String PERM_SUBMIT_PO = "SUBMIT_PO";
    public static final String PERM_APPROVE_PO = "APPROVE_PO";
    public static final String PERM_RECEIVE_PO = "RECEIVE_PO";
    public static final String PERM_ADJUST_STOCK = "ADJUST_STOCK";
    public static final String PERM_CREATE_RETURN = "CREATE_RETURN";
    public static final String PERM_APPROVE_RETURN = "APPROVE_RETURN";
    public static final String PERM_VIEW_AUDIT = "VIEW_AUDIT";
    public static final String PERM_RUN_INVENTORY = "RUN_INVENTORY";
    // 财务相关权限
    public static final String PERM_VIEW_REVENUE = "VIEW_REVENUE"; // 查看营收报表
    public static final String PERM_EXPORT_REVENUE = "EXPORT_REVENUE"; // 导出/下载报表
    public static final String PERM_REFUND = "REFUND"; // 执行退款/退货审批

    // 供应商表
    public static final String TABLE_SUPPLIERS = "suppliers";
    public static final String COLUMN_SUPPLIER_ID = "supplier_id";
    public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
    public static final String COLUMN_SUPPLIER_CONTACT = "contact";
    public static final String COLUMN_SUPPLIER_PHONE = "phone";
    public static final String COLUMN_SUPPLIER_EMAIL = "email";

    // 采购订单（Purchase Orders）
    public static final String TABLE_PURCHASE_ORDERS = "purchase_orders";
    public static final String COLUMN_PO_ID = "po_id";
    public static final String COLUMN_PO_SUPPLIER_ID = "supplier_id";
    public static final String COLUMN_PO_NAME = "po_name";
    public static final String COLUMN_PO_STATUS = "status";
    public static final String COLUMN_PO_CREATED_AT = "created_at";
    public static final String COLUMN_PO_EXPECTED_AT = "expected_at";
    public static final String COLUMN_PO_TOTAL = "total";

    // 采购单状态
    public static final String PO_STATUS_CREATED = "created";
    public static final String PO_STATUS_SUBMITTED = "submitted";
    public static final String PO_STATUS_PENDING = "pending";
    public static final String PO_STATUS_APPROVED = "approved";
    public static final String PO_STATUS_REJECTED = "rejected";
    public static final String PO_STATUS_RECEIVED = "received";

    // PO 名称规则（可配置）
    public static final boolean PO_NAME_REQUIRED = true; // set true to require name before save/submit
    public static final int PO_NAME_MAX_LENGTH = 120;

    // 采购审批表
    public static final String TABLE_PO_APPROVALS = "po_approvals";
    public static final String COLUMN_PO_APPROVAL_ID = "approval_id";
    public static final String COLUMN_PO_APPROVAL_PO_ID = "po_id";
    public static final String COLUMN_PO_APPROVAL_APPROVER_ID = "approver_id";
    public static final String COLUMN_PO_APPROVAL_APPROVER_ROLE = "approver_role";
    public static final String COLUMN_PO_APPROVAL_DECISION = "decision"; // approved/rejected
    public static final String COLUMN_PO_APPROVAL_COMMENT = "comment";
    public static final String COLUMN_PO_APPROVAL_TIMESTAMP = "timestamp";

    // 系统审计表（通用的操作审计）
    public static final String TABLE_SYSTEM_AUDIT = "system_audit";
    public static final String COLUMN_SYSTEM_AUDIT_ID = "audit_id";
    public static final String COLUMN_SYSTEM_AUDIT_ENTITY = "entity"; // e.g., purchase_order
    public static final String COLUMN_SYSTEM_AUDIT_ENTITY_ID = "entity_id";
    public static final String COLUMN_SYSTEM_AUDIT_ACTION = "action"; // submit/approve/reject
    public static final String COLUMN_SYSTEM_AUDIT_USER_ID = "user_id";
    public static final String COLUMN_SYSTEM_AUDIT_USER_ROLE = "user_role";
    public static final String COLUMN_SYSTEM_AUDIT_DETAIL = "detail";
    public static final String COLUMN_SYSTEM_AUDIT_TIMESTAMP = "timestamp";

    // 采购订单行
    public static final String TABLE_PURCHASE_LINES = "purchase_lines";
    public static final String COLUMN_PO_LINE_ID = "po_line_id";
    public static final String COLUMN_PO_LINE_PO_ID = "po_id";
    public static final String COLUMN_PO_LINE_PRODUCT_ID = "product_id";
    public static final String COLUMN_PO_LINE_SKU = "sku";
    public static final String COLUMN_PO_LINE_QTY = "qty";
    public static final String COLUMN_PO_LINE_PRICE = "price";

    // 盘点表
    public static final String TABLE_STOCK_COUNTS = "stock_counts";
    public static final String COLUMN_STOCK_COUNT_ID = "count_id";
    public static final String COLUMN_STOCK_COUNT_STATUS = "status";
    public static final String COLUMN_STOCK_COUNT_CREATED_BY = "created_by";
    public static final String COLUMN_STOCK_COUNT_CREATED_AT = "created_at";

    // 盘点行
    public static final String TABLE_STOCK_COUNT_LINES = "stock_count_lines";
    public static final String COLUMN_STOCK_COUNT_LINE_ID = "count_line_id";
    public static final String COLUMN_STOCK_COUNT_LINE_COUNT_ID = "count_id";
    public static final String COLUMN_STOCK_COUNT_LINE_PRODUCT_ID = "product_id";
    public static final String COLUMN_STOCK_COUNT_LINE_SKU = "sku";
    public static final String COLUMN_STOCK_COUNT_LINE_EXPECTED_QTY = "expected_qty";
    public static final String COLUMN_STOCK_COUNT_LINE_COUNTED_QTY = "counted_qty";

    // 销售/收银表
    public static final String TABLE_SALES = "sales";
    public static final String COLUMN_SALE_ID = "sale_id";
    public static final String COLUMN_SALE_TOTAL = "total";
    public static final String COLUMN_SALE_PAID = "paid";
    public static final String COLUMN_SALE_USER_ID = "user_id";
    public static final String COLUMN_SALE_TIMESTAMP = "timestamp";
    public static final String COLUMN_SALE_PAYMENT_METHOD = "payment_method";
    public static final String COLUMN_SALE_REFUNDED = "refunded"; // 0/1
    public static final String COLUMN_SALE_REFUNDED_AT = "refunded_at"; // timestamp

    // 退款记录表
    public static final String TABLE_REFUNDS = "refunds";
    public static final String COLUMN_REFUND_ID = "refund_id";
    public static final String COLUMN_REFUND_SALE_ID = "sale_id";
    public static final String COLUMN_REFUND_AMOUNT = "amount";
    public static final String COLUMN_REFUND_USER_ID = "user_id";
    public static final String COLUMN_REFUND_USER_ROLE = "user_role";
    public static final String COLUMN_REFUND_REASON = "reason";
    public static final String COLUMN_REFUND_TIMESTAMP = "timestamp";

    public static final String TABLE_SALE_LINES = "sale_lines";
    public static final String COLUMN_SALE_LINE_ID = "line_id";
    public static final String COLUMN_SALE_LINE_SALE_ID = "sale_id";
    public static final String COLUMN_SALE_LINE_PRODUCT_ID = "product_id";
    public static final String COLUMN_SALE_LINE_PRODUCT_NAME = "product_name";
    public static final String COLUMN_SALE_LINE_QTY = "qty";
    public static final String COLUMN_SALE_LINE_PRICE = "price";
}