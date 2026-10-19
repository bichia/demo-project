package com.example.commodity.controller;

import com.example.commodity.entity.User;
import com.example.commodity.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        if (user != null && !user.isAdmin()) {
            return "redirect:/shop";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        model.addAttribute("productCount", dashboardService.productCount());
        model.addAttribute("onSaleCount", dashboardService.onSaleCount());
        model.addAttribute("totalStock", dashboardService.totalStock());
        model.addAttribute("totalSales", dashboardService.totalSales());
        model.addAttribute("inventoryValue", dashboardService.inventoryValue());
        model.addAttribute("lowStockProducts", dashboardService.lowStockProducts());
        model.addAttribute("categoryStats", dashboardService.categoryStats());
        model.addAttribute("brandStats", dashboardService.brandStats());
        model.addAttribute("loginUser", session.getAttribute(LoginController.SESSION_USER));
        return "dashboard";
    }
}
