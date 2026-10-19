package com.example.commodity.service;

import com.example.commodity.entity.Product;
import com.example.commodity.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 仪表盘统计数据
 */
@Service
public class DashboardService {

    /** 库存预警阈值 */
    private static final int LOW_STOCK_THRESHOLD = 50;

    private final ProductRepository productRepository;

    public DashboardService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public long productCount() {
        return productRepository.count();
    }

    public long onSaleCount() {
        return productRepository.countByStatus(1);
    }

    public long totalStock() {
        return productRepository.sumStock();
    }

    public long totalSales() {
        return productRepository.sumSales();
    }

    public BigDecimal inventoryValue() {
        return productRepository.inventoryValue();
    }

    public List<Product> lowStockProducts() {
        return productRepository
                .findTop5ByStatusAndStockLessThanOrderByStockAsc(1, LOW_STOCK_THRESHOLD);
    }

    public List<CategoryStatView> categoryStats() {
        List<Object[]> rows = productRepository.categoryStats();
        long max = rows.stream()
                .mapToLong(r -> (Long) r[3])
                .max()
                .orElse(1L);
        if (max == 0) {
            max = 1;
        }
        List<CategoryStatView> views = new ArrayList<>();
        for (Object[] r : rows) {
            long count = (Long) r[3];
            int percent = (int) Math.round(count * 100.0 / max);
            views.add(new CategoryStatView(
                    (Long) r[0], (String) r[1], (String) r[2],
                    count, (Long) r[4], percent));
        }
        return views;
    }

    public List<BrandStatView> brandStats() {
        List<Object[]> rows = productRepository.brandStats();
        long max = rows.stream()
                .mapToLong(r -> (Long) r[3])
                .max()
                .orElse(1L);
        if (max == 0) {
            max = 1;
        }
        List<BrandStatView> views = new ArrayList<>();
        for (Object[] r : rows) {
            long count = (Long) r[3];
            int percent = (int) Math.round(count * 100.0 / max);
            views.add(new BrandStatView(
                    (Long) r[0], (String) r[1], (String) r[2],
                    count, (Long) r[4], percent));
        }
        return views;
    }

    public long offSaleCount() {
        return productRepository.count() - productRepository.countByStatus(1);
    }

    public long lowStockCount() {
        return productRepository
                .findTop5ByStatusAndStockLessThanOrderByStockAsc(1, LOW_STOCK_THRESHOLD)
                .size();
    }
}
