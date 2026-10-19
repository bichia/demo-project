package com.example.commodity.controller;

import com.example.commodity.entity.Order;
import com.example.commodity.entity.User;
import com.example.commodity.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 卖家（管理员）订单管理：查看全部订单、按状态筛选、发货
 */
@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       HttpSession session, Model model) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        if (status != null && !status.isBlank()) {
            model.addAttribute("orders", orderService.findByStatus(status));
        } else {
            model.addAttribute("orders", orderService.findAll());
        }
        model.addAttribute("status", status);
        model.addAttribute("pendingPay", orderService.countByStatus("PENDING"));
        model.addAttribute("paidCount", orderService.countByStatus("PAID"));
        model.addAttribute("shippedCount", orderService.countByStatus("SHIPPED"));
        model.addAttribute("completedCount", orderService.countByStatus("COMPLETED"));
        model.addAttribute("totalOrders", orderService.findAll().size());
        model.addAttribute("loginUser", user);
        return "admin-orders";
    }

    @PostMapping("/{id}/ship")
    public String ship(@PathVariable Long id,
                       @RequestParam(required = false) String status,
                       RedirectAttributes ra) {
        try {
            orderService.ship(id);
            ra.addFlashAttribute("message", "发货成功");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return status != null && !status.isBlank()
                ? "redirect:/admin/orders?status=" + status
                : "redirect:/admin/orders";
    }
}
