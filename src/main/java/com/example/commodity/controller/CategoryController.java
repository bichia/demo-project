package com.example.commodity.controller;

import com.example.commodity.entity.Category;
import com.example.commodity.entity.User;
import com.example.commodity.repository.ProductRepository;
import com.example.commodity.service.CategoryService;
import com.example.commodity.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final DashboardService dashboardService;

    public CategoryController(CategoryService categoryService,
                              ProductRepository productRepository,
                              DashboardService dashboardService) {
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.dashboardService = dashboardService;
    }

    /**
     * 分类管理列表（所有登录用户可查看，仅管理员可增删改）
     */
    @GetMapping
    public String list(HttpSession session, Model model) {
        Map<Long, Long> productCountMap = new HashMap<>();
        for (Category c : categoryService.findAll()) {
            productCountMap.put(c.getId(), productRepository.countByCategoryId(c.getId()));
        }
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("productCountMap", productCountMap);
        model.addAttribute("categoryStats", dashboardService.categoryStats());
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "categories";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/categories";
        }
        model.addAttribute("category", new Category());
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "category-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/categories";
        }
        model.addAttribute("category", categoryService.findById(id));
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "category-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam String name,
                       @RequestParam(required = false) String icon,
                       @RequestParam(required = false) String description,
                       @RequestParam(defaultValue = "99") Integer sortOrder,
                       HttpSession session,
                       RedirectAttributes ra) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/categories";
        }
        Category category = id == null ? new Category() : categoryService.findById(id);
        category.setName(name);
        category.setIcon(icon);
        category.setDescription(description);
        category.setSortOrder(sortOrder);
        categoryService.save(category);
        ra.addFlashAttribute("message", (id == null ? "新增" : "修改") + "分类成功");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/categories";
        }
        String error = categoryService.deleteById(id);
        if (error != null) {
            ra.addFlashAttribute("error", error);
        } else {
            ra.addFlashAttribute("message", "删除分类成功");
        }
        return "redirect:/categories";
    }

    private User currentUser(HttpSession session) {
        return (User) session.getAttribute(LoginController.SESSION_USER);
    }
}
