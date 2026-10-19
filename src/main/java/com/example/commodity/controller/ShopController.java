package com.example.commodity.controller;

import com.example.commodity.service.BrandService;
import com.example.commodity.service.CategoryService;
import com.example.commodity.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 淘宝风格商城浏览页（仅展示在售商品）
 */
@Controller
public class ShopController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ShopController(ProductService productService,
                          CategoryService categoryService,
                          BrandService brandService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Long brandId,
                       @RequestParam(required = false, defaultValue = "createTime") String sort,
                       @RequestParam(required = false, defaultValue = "desc") String dir,
                       HttpSession session,
                       Model model) {
        model.addAttribute("products", productService.shopList(keyword, categoryId, brandId, sort, dir));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", brandService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "shop";
    }
}
