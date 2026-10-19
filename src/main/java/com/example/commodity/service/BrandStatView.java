package com.example.commodity.service;

/**
 * 品牌统计视图
 */
public class BrandStatView {

    private final Long id;
    private final String name;
    private final String logo;
    private final long productCount;
    private final long stock;
    private final int percent;

    public BrandStatView(Long id, String name, String logo, long productCount, long stock, int percent) {
        this.id = id;
        this.name = name;
        this.logo = logo;
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

    public String getLogo() {
        return logo;
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
