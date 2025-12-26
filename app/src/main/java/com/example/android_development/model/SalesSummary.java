package com.example.android_development.model;

/**
 * 销售汇总条目（按日/按月）。
 * <p>
 * 用于报表统计：periodLabel 表示统计周期，total 为销售总额，count 为销售笔数。
 * </p>
 */
public class SalesSummary {
    private String periodLabel; // 例如：2025-12-24（按日）或 2025-12（按月）
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
