package com.example.android_development.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.android_development.util.Constants;

public class Product {
    private String id;
    private String name;
    private String category;
    private String brand;
    private double price;
    private double cost;
    private int stock; // Shelf stock
    private int warehouseStock; // Warehouse stock
    private int minStock; // Shelf min stock
    private int minWarehouseStock; // Warehouse min stock
    private String unit;
    private long productionDate;
    private long expirationDate;
    private String barcode;
    private String description;
    private String thumbUrl;
    private String supplierId;
    private long createdAt;
    private long updatedAt;

    // 构造方法
    public Product() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Product(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = category;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) {
        this.brand = brand;
        this.updatedAt = System.currentTimeMillis();
    }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        this.price = price;
        this.updatedAt = System.currentTimeMillis();
    }

    public double getCost() { return cost; }
    public void setCost(double cost) {
        this.cost = cost;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getStock() { return stock; }
    public void setStock(int stock) {
        this.stock = stock;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getWarehouseStock() { return warehouseStock; }
    public void setWarehouseStock(int warehouseStock) {
        this.warehouseStock = warehouseStock;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) {
        this.minStock = minStock;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getMinWarehouseStock() { return minWarehouseStock; }
    public void setMinWarehouseStock(int minWarehouseStock) {
        this.minWarehouseStock = minWarehouseStock;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getUnit() { return unit; }
    public void setUnit(String unit) {
        this.unit = unit;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getProductionDate() { return productionDate; }
    public void setProductionDate(long productionDate) {
        this.productionDate = productionDate;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getExpirationDate() { return expirationDate; }
    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // 辅助方法
    public boolean isLowStock() {
        return minStock > 0 && stock <= minStock;
    }

    public double getTotalValue() {
        return cost * stock;
    }

    public double getProfitMargin() {
        if (cost == 0) return 0;
        return ((price - cost) / cost) * 100;
    }

    // 转换：从 Cursor 创建对象
    public static Product fromCursor(Cursor c) {
        if (c == null) return null;
        Product p = new Product();
        int idx;
        idx = c.getColumnIndex(Constants.COLUMN_PRODUCT_ID); if (idx != -1) p.setId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PRODUCT_NAME); if (idx != -1) p.setName(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_CATEGORY); if (idx != -1) p.setCategory(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_BRAND); if (idx != -1) p.setBrand(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PRICE); if (idx != -1) p.setPrice(c.getDouble(idx));
        idx = c.getColumnIndex(Constants.COLUMN_COST); if (idx != -1) p.setCost(c.getDouble(idx));
        idx = c.getColumnIndex(Constants.COLUMN_STOCK); if (idx != -1) p.setStock(c.getInt(idx));
        idx = c.getColumnIndex(Constants.COLUMN_WAREHOUSE_STOCK); if (idx != -1) p.setWarehouseStock(c.getInt(idx));
        idx = c.getColumnIndex(Constants.COLUMN_MIN_STOCK); if (idx != -1) p.setMinStock(c.getInt(idx));
        idx = c.getColumnIndex(Constants.COLUMN_MIN_WAREHOUSE_STOCK); if (idx != -1) p.setMinWarehouseStock(c.getInt(idx));
        idx = c.getColumnIndex(Constants.COLUMN_UNIT); if (idx != -1) p.setUnit(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_PRODUCTION_DATE); if (idx != -1) p.setProductionDate(c.getLong(idx));
        idx = c.getColumnIndex(Constants.COLUMN_EXPIRATION_DATE); if (idx != -1) p.setExpirationDate(c.getLong(idx));
        idx = c.getColumnIndex(Constants.COLUMN_BARCODE); if (idx != -1) p.setBarcode(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_DESCRIPTION); if (idx != -1) p.setDescription(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_THUMB_URL); if (idx != -1) p.setThumbUrl(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_SUPPLIER_ID); if (idx != -1) p.setSupplierId(c.getString(idx));
        idx = c.getColumnIndex(Constants.COLUMN_CREATED_AT); if (idx != -1) p.setCreatedAt(c.getLong(idx));
        idx = c.getColumnIndex(Constants.COLUMN_UPDATED_AT); if (idx != -1) p.setUpdatedAt(c.getLong(idx));
        return p;
    }

    // 转换：对象 -> ContentValues
    public ContentValues toContentValues() {
        ContentValues v = new ContentValues();
        if (id != null) v.put(Constants.COLUMN_PRODUCT_ID, id);
        v.put(Constants.COLUMN_PRODUCT_NAME, name);
        if (thumbUrl != null) v.put(Constants.COLUMN_THUMB_URL, thumbUrl);
        v.put(Constants.COLUMN_CATEGORY, category);
        v.put(Constants.COLUMN_BRAND, brand);
        v.put(Constants.COLUMN_PRICE, price);
        v.put(Constants.COLUMN_COST, cost);
        v.put(Constants.COLUMN_STOCK, stock);
        v.put(Constants.COLUMN_WAREHOUSE_STOCK, warehouseStock);
        v.put(Constants.COLUMN_MIN_STOCK, minStock);
        v.put(Constants.COLUMN_MIN_WAREHOUSE_STOCK, minWarehouseStock);
        v.put(Constants.COLUMN_UNIT, unit);
        v.put(Constants.COLUMN_PRODUCTION_DATE, productionDate);
        v.put(Constants.COLUMN_EXPIRATION_DATE, expirationDate);
        v.put(Constants.COLUMN_BARCODE, barcode);
        v.put(Constants.COLUMN_DESCRIPTION, description);
        v.put(Constants.COLUMN_SUPPLIER_ID, supplierId);
        v.put(Constants.COLUMN_CREATED_AT, createdAt);
        v.put(Constants.COLUMN_UPDATED_AT, updatedAt);
        return v;
    }
}
