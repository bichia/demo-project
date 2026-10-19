package com.example.commodity.controller;

import com.example.commodity.entity.Product;
import com.example.commodity.entity.User;
import com.example.commodity.service.BrandService;
import com.example.commodity.service.CategoryService;
import com.example.commodity.service.DashboardService;
import com.example.commodity.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final DashboardService dashboardService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             BrandService brandService,
                             DashboardService dashboardService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.dashboardService = dashboardService;
    }

    /**
     * 后台商品列表：分类 + 品牌 + 关键字 + 排序
     */
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Long brandId,
                       @RequestParam(required = false, defaultValue = "createTime") String sort,
                       @RequestParam(required = false, defaultValue = "desc") String dir,
                       HttpSession session,
                       Model model) {
        List<Product> products = productService.list(keyword, categoryId, brandId, sort, dir);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", brandService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("productCount", dashboardService.productCount());
        model.addAttribute("onSaleCount", dashboardService.onSaleCount());
        model.addAttribute("offSaleCount", dashboardService.offSaleCount());
        model.addAttribute("totalStock", dashboardService.totalStock());
        model.addAttribute("inventoryValue", dashboardService.inventoryValue());
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "products";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/products";
        }
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", brandService.findAll());
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "product-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/products";
        }
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", brandService.findAll());
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "product-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam String name,
                       @RequestParam Long categoryId,
                       @RequestParam(required = false) Long brandId,
                       @RequestParam(required = false) String imageUrl,
                       @RequestParam BigDecimal price,
                       @RequestParam Integer stock,
                       @RequestParam(defaultValue = "0") Integer sales,
                       @RequestParam(defaultValue = "1") Integer status,
                       @RequestParam(required = false) String description,
                       HttpSession session,
                       RedirectAttributes ra) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/products";
        }
        productService.assemble(id, name, categoryId, brandId,
                imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim(),
                price, stock, sales, status, description);
        ra.addFlashAttribute("message", (id == null ? "新增" : "修改") + "商品成功");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/products";
        }
        productService.deleteById(id);
        ra.addFlashAttribute("message", "删除商品成功");
        return "redirect:/products";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long brandId,
                         @RequestParam(required = false) String sort,
                         @RequestParam(required = false) String dir,
                         HttpSession session) {
        if (currentUser(session).isAdmin()) {
            productService.toggleStatus(id);
        }
        return "redirect:/products?" + buildQuery(keyword, categoryId, brandId, sort, dir);
    }

    private String buildQuery(String keyword, Long categoryId, Long brandId, String sort, String dir) {
        StringBuilder sb = new StringBuilder();
        if (keyword != null && !keyword.isBlank()) {
            sb.append("keyword=").append(keyword.trim()).append('&');
        }
        if (categoryId != null) {
            sb.append("categoryId=").append(categoryId).append('&');
        }
        if (brandId != null) {
            sb.append("brandId=").append(brandId).append('&');
        }
        if (sort != null) {
            sb.append("sort=").append(sort).append('&');
        }
        if (dir != null) {
            sb.append("dir=").append(dir);
        }
        String q = sb.toString();
        if (q.endsWith("&")) {
            q = q.substring(0, q.length() - 1);
        }
        return q;
    }

    private User currentUser(HttpSession session) {
        return (User) session.getAttribute(LoginController.SESSION_USER);
    }
}
