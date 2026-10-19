package com.example.commodity.service;

import com.example.commodity.entity.Brand;
import com.example.commodity.entity.Category;
import com.example.commodity.entity.Product;
import com.example.commodity.repository.ProductRepository;
import com.example.commodity.repository.ProductSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductService {

    /** 允许排序的字段（白名单，防止非法字段名注入排序） */
    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "name", "category", "brand", "price", "stock", "sales", "status", "createTime");

    /** 排序字段 -> JPA 属性路径 */
    private static final Map<String, String> FIELD_PATHS = Map.of(
            "id", "id",
            "name", "name",
            "category", "category.name",
            "brand", "brand.name",
            "price", "price",
            "stock", "stock",
            "sales", "sales",
            "status", "status",
            "createTime", "createTime"
    );

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService,
                          BrandService brandService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    /**
     * 后台商品列表（分类 + 品牌 + 关键字 + 排序，含下架商品）
     */
    public List<Product> list(String keyword, Long categoryId, Long brandId, String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir);
        return productRepository.findAll(ProductSpecifications.filter(categoryId, brandId, keyword, null), sort);
    }

    /**
     * 商城浏览（仅在售商品）
     */
    public List<Product> shopList(String keyword, Long categoryId, Long brandId, String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir);
        return productRepository.findAll(ProductSpecifications.filter(categoryId, brandId, keyword, 1), sort);
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String field = (sortBy != null && SORTABLE_FIELDS.contains(sortBy)) ? sortBy : "createTime";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, FIELD_PATHS.get(field));
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在，id=" + id));
    }

    public Product save(Product product) {
        if (product.getId() == null && product.getCreateTime() == null) {
            product.setCreateTime(LocalDateTime.now());
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        return productRepository.save(product);
    }

    /**
     * 新增/修改商品的统一装配
     */
    public Product assemble(Long id, String name, Long categoryId, Long brandId, String imageUrl,
                            BigDecimal price, Integer stock, Integer sales, Integer status,
                            String description) {
        Product product = id == null ? new Product() : findById(id);
        Category category = categoryService.findById(categoryId);
        product.setName(name);
        product.setCategory(category);
        if (brandId != null) {
            Brand brand = brandService.findById(brandId);
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }
        product.setImageUrl(imageUrl);
        product.setPrice(price);
        product.setStock(stock);
        product.setSales(sales == null ? 0 : sales);
        product.setStatus(status == null ? 1 : status);
        product.setDescription(description);
        return save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * 切换上下架状态
     */
    public void toggleStatus(Long id) {
        Product product = findById(id);
        product.setStatus(product.getStatus() != null && product.getStatus() == 1 ? 0 : 1);
        productRepository.save(product);
    }
}
