package com.example.commodity.service;

import com.example.commodity.entity.Brand;
import com.example.commodity.repository.BrandRepository;
import com.example.commodity.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public BrandService(BrandRepository brandRepository, ProductRepository productRepository) {
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    public List<Brand> findAll() {
        return brandRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    public Brand findById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("品牌不存在，id=" + id));
    }

    public Brand save(Brand brand) {
        if (brand.getId() == null && brand.getCreateTime() == null) {
            brand.setCreateTime(LocalDateTime.now());
        }
        if (brand.getSortOrder() == null) {
            brand.setSortOrder(99);
        }
        if (brand.getLogo() == null || brand.getLogo().isBlank()) {
            brand.setLogo("🏷️");
        }
        return brandRepository.save(brand);
    }

    /**
     * 删除品牌；品牌下仍有商品时禁止删除
     */
    @Transactional
    public String deleteById(Long id) {
        long count = productRepository.countByBrandId(id);
        if (count > 0) {
            return "该品牌下还有 " + count + " 件商品，请先迁移或删除商品后再删除品牌";
        }
        brandRepository.deleteById(id);
        return null;
    }
}
