package com.example.commodity.service;

import com.example.commodity.entity.CartItem;
import com.example.commodity.entity.Product;
import com.example.commodity.entity.User;
import com.example.commodity.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车服务
 */
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartItemRepository cartItemRepository, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    public List<CartItem> findByUser(User user) {
        return cartItemRepository.findByUserIdOrderByCreateTimeDesc(user.getId());
    }

    /**
     * 加入购物车（若已在购物车则数量+1）
     */
    @Transactional
    public void addToCart(User user, Long productId, Integer quantity) {
        Product product = productService.findById(productId);
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new IllegalStateException("该商品已下架，无法加入购物车");
        }
        int qty = quantity == null || quantity < 1 ? 1 : quantity;
        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setUser(user);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    ci.setCreateTime(LocalDateTime.now());
                    return ci;
                });
        item.setQuantity(item.getQuantity() + qty);
        cartItemRepository.save(item);
    }

    /**
     * 修改购物车数量
     */
    @Transactional
    public void updateQuantity(User user, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("购物车条目不存在"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("无权操作他人购物车");
        }
        if (quantity == null || quantity < 1) {
            quantity = 1;
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    /**
     * 移除购物车条目
     */
    @Transactional
    public void removeItem(User user, Long itemId) {
        cartItemRepository.deleteByIdAndUserId(itemId, user.getId());
    }

    /**
     * 清空购物车
     */
    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUserId(user.getId());
    }

    /**
     * 购物车商品总件数
     */
    public int totalItems(User user) {
        return cartItemRepository.findByUserIdOrderByCreateTimeDesc(user.getId())
                .stream().mapToInt(CartItem::getQuantity).sum();
    }

    /**
     * 购物车总金额
     */
    public BigDecimal totalAmount(User user) {
        return cartItemRepository.findByUserIdOrderByCreateTimeDesc(user.getId())
                .stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
