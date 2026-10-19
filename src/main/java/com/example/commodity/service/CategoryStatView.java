package com.example.commodity.service;

/**
 * 仪表盘分类统计视图
 */
public class CategoryStatView {

    private final Long id;
    private final String name;
    private final String icon;
    private final long productCount;
    private final long stock;
    private final int percent;

    public CategoryStatView(Long id, String name, String icon, long productCount, long stock, int percent) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.productCount = productCount;
        this.stock = stock;
        this.percent = percent;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public long getProductCount() {
        return productCount;
    }

    public long getStock() {
        return stock;
    }

    public int getPercent() {
        return percent;
    }
}
