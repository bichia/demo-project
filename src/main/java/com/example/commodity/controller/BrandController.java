package com.example.commodity.controller;

import com.example.commodity.entity.Brand;
import com.example.commodity.entity.User;
import com.example.commodity.repository.ProductRepository;
import com.example.commodity.service.BrandService;
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
@RequestMapping("/brands")
public class BrandController {

    private final BrandService brandService;
    private final ProductRepository productRepository;
    private final DashboardService dashboardService;

    public BrandController(BrandService brandService, ProductRepository productRepository,
                           DashboardService dashboardService) {
        this.brandService = brandService;
        this.productRepository = productRepository;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        Map<Long, Long> productCountMap = new HashMap<>();
        for (Brand b : brandService.findAll()) {
            productCountMap.put(b.getId(), productRepository.countByBrandId(b.getId()));
        }
        model.addAttribute("brands", brandService.findAll());
        model.addAttribute("productCountMap", productCountMap);
        model.addAttribute("brandStats", dashboardService.brandStats());
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "brands";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/brands";
        }
        model.addAttribute("brand", new Brand());
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "brand-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/brands";
        }
        model.addAttribute("brand", brandService.findById(id));
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "brand-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam String name,
                       @RequestParam(required = false) String logo,
                       @RequestParam(required = false) String description,
                       @RequestParam(defaultValue = "99") Integer sortOrder,
                       HttpSession session,
                       RedirectAttributes ra) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/brands";
        }
        Brand brand = id == null ? new Brand() : brandService.findById(id);
        brand.setName(name);
        brand.setLogo(logo);
        brand.setDescription(description);
        brand.setSortOrder(sortOrder);
        brandService.save(brand);
        ra.addFlashAttribute("message", (id == null ? "新增" : "修改") + "品牌成功");
        return "redirect:/brands";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!currentUser(session).isAdmin()) {
            return "redirect:/brands";
        }
        String error = brandService.deleteById(id);
        if (error != null) {
            ra.addFlashAttribute("error", error);
        } else {
            ra.addFlashAttribute("message", "删除品牌成功");
        }
        return "redirect:/brands";
    }

    private User currentUser(HttpSession session) {
        return (User) session.getAttribute(LoginController.SESSION_USER);
    }
}
