package com.example.commodity.controller;

import com.example.commodity.entity.User;
import com.example.commodity.service.CartService;
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

/**
 * 购物车控制器
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        model.addAttribute("cartItems", cartService.findByUser(user));
        BigDecimal total = cartService.totalAmount(user);
        model.addAttribute("totalAmount", total);
        model.addAttribute("totalItems", cartService.totalItems(user));
        model.addAttribute("loginUser", user);
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                           @RequestParam(defaultValue = "1") Integer quantity,
                           @RequestParam(defaultValue = "false") String buyNow,
                           HttpSession session,
                           RedirectAttributes ra) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        try {
            cartService.addToCart(user, productId, quantity);
            ra.addFlashAttribute("message", "已加入购物车");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        if ("true".equals(buyNow)) {
            return "redirect:/orders/checkout";
        }
        return "redirect:/shop";
    }

    @PostMapping("/{itemId}/update")
    public String updateQuantity(@PathVariable Long itemId,
                                 @RequestParam Integer quantity,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        try {
            cartService.updateQuantity(user, itemId, quantity);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/{itemId}/remove")
    public String removeItem(@PathVariable Long itemId,
                             HttpSession session,
                             RedirectAttributes ra) {
        User user = (User) session.getAttribute(LoginController.SESSION_USER);
        cartService.removeItem(user, itemId);
        ra.addFlashAttribute("message", "已从购物车移除");
        return "redirect:/cart";
    }
}
