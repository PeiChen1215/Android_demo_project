package com.example.android_development.model;

public class Product {
    private String id;
    private String name;
    private String category;
    private String brand;
    private double price;
    private double cost;
    private int stock;
    private int minStock;
    private String unit;
    private String barcode;
    private String description;
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

    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) {
        this.minStock = minStock;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getUnit() { return unit; }
    public void setUnit(String unit) {
        this.unit = unit;
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
}
