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
 * 买家订单控制器：下单、付款、确认收货、取消、查看自己的订单
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam(required = false) String address,
                           HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        try {
            Order order = orderService.checkout(user, address);
            ra.addFlashAttribute("message", "下单成功，请尽快付款");
            return "redirect:/orders/" + order.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping
    public String listOrders(@RequestParam(required = false) String status,
                             HttpSession session, Model model) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        if (status != null && !status.isBlank()) {
            model.addAttribute("orders", orderService.findByStatus(status).stream()
                    .filter(o -> o.getUser().getId().equals(user.getId()))
                    .toList());
        } else {
            model.addAttribute("orders", orderService.findByUser(user));
        }
        model.addAttribute("status", status);
        model.addAttribute("loginUser", user);
        return "orders";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        Order order = orderService.findById(id);
        // 订单本人或管理员可查看
        if (!order.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("loginUser", user);
        return "order-detail";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        try {
            orderService.pay(user, id);
            ra.addFlashAttribute("message", "付款成功！等待卖家发货");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        try {
            orderService.confirm(user, id);
            ra.addFlashAttribute("message", "确认收货成功，感谢您的购买！");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        try {
            orderService.cancel(user, id);
            ra.addFlashAttribute("message", "订单已取消");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
