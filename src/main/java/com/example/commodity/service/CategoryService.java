package com.example.commodity.service;

import com.example.commodity.entity.Category;
import com.example.commodity.repository.CategoryRepository;
import com.example.commodity.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在，id=" + id));
    }

    public Category save(Category category) {
        if (category.getId() == null && category.getCreateTime() == null) {
            category.setCreateTime(LocalDateTime.now());
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(99);
        }
        if (category.getIcon() == null || category.getIcon().isBlank()) {
            category.setIcon("🏷️");
        }
        return categoryRepository.save(category);
    }

    /**
     * 删除分类；分类下仍有商品时禁止删除
     *
     * @return null 表示删除成功，否则返回错误提示
     */
    @Transactional
    public String deleteById(Long id) {
        long count = productRepository.countByCategoryId(id);
        if (count > 0) {
            return "该分类下还有 " + count + " 件商品，请先迁移或删除商品后再删除分类";
        }
        categoryRepository.deleteById(id);
        return null;
    }
}
