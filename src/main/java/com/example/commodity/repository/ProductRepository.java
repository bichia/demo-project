package com.example.commodity.repository;

import com.example.commodity.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    long countByCategoryId(Long categoryId);

    long countByBrandId(Long brandId);

    long countByStatus(Integer status);

    List<Product> findTop5ByStatusAndStockLessThanOrderByStockAsc(Integer status, Integer stock);

    @Query("select coalesce(sum(p.stock), 0) from Product p")
    long sumStock();

    @Query("select coalesce(sum(p.sales), 0) from Product p")
    long sumSales();

    @Query("select coalesce(sum(p.price * p.stock), 0) from Product p")
    BigDecimal inventoryValue();

    /**
     * 按分类统计：[分类id, 分类名, 分类图标, 商品数, 库存合计]
     */
    @Query("select p.category.id, p.category.name, p.category.icon, count(p), coalesce(sum(p.stock), 0) " +
            "from Product p group by p.category.id, p.category.name, p.category.icon " +
            "order by count(p) desc")
    List<Object[]> categoryStats();

    /**
     * 按品牌统计：[品牌id, 品牌名, 品牌logo, 商品数, 库存合计]
     */
    @Query("select p.brand.id, p.brand.name, p.brand.logo, count(p), coalesce(sum(p.stock), 0) " +
            "from Product p where p.brand is not null " +
            "group by p.brand.id, p.brand.name, p.brand.logo " +
            "order by count(p) desc")
    List<Object[]> brandStats();
}
