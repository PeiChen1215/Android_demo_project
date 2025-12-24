package com.example.android_development.model;

public class SalesSummary {
    private String periodLabel; // e.g., 2025-12-24 or 2025-12
    private double total;
    private int count;

    public SalesSummary(String periodLabel, double total, int count) {
        this.periodLabel = periodLabel;
        this.total = total;
        this.count = count;
    }

    public String getPeriodLabel() { return periodLabel; }
    public double getTotal() { return total; }
    public int getCount() { return count; }
}
