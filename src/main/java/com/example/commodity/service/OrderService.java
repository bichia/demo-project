package com.example.commodity.service;

import com.example.commodity.entity.CartItem;
import com.example.commodity.entity.Order;
import com.example.commodity.entity.OrderItem;
import com.example.commodity.entity.Product;
import com.example.commodity.entity.User;
import com.example.commodity.repository.OrderRepository;
import com.example.commodity.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务
 * 状态流转（参考淘宝）：
 * 下单 PENDING（待付款）--付款--> PAID（待发货）--卖家发货--> SHIPPED（待收货）--确认收货--> COMPLETED（已完成）
 * 待付款可取消 --> CANCELLED（已取消，并回补库存）
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CartService cartService,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productRepository = productRepository;
    }

    /**
     * 从购物车结算，生成待付款订单
     */
    @Transactional
    public Order checkout(User user, String address) {
        List<CartItem> cartItems = cartService.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("购物车为空，无法下单");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        order.setAddress(address);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            if (p.getStatus() == null || p.getStatus() != 1) {
                throw new IllegalStateException("水果「" + p.getName() + "」已下架，请移除后重试");
            }
            if (p.getStock() < ci.getQuantity()) {
                throw new IllegalStateException("水果「" + p.getName() + "」库存不足（剩余 " + p.getStock() + " 件）");
            }

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setProductImage(p.getImageUrl());
            oi.setPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            oi.setSubtotal(subtotal);
            order.getItems().add(oi);
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        // 扣减库存、增加销量
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            p.setStock(p.getStock() - ci.getQuantity());
            p.setSales(p.getSales() + ci.getQuantity());
            productRepository.save(p);
        }

        cartService.clearCart(user);
        return saved;
    }

    public List<Order> findByUser(User user) {
        return orderRepository.findByUserIdOrderByCreateTimeDesc(user.getId());
    }

    public List<Order> findAll() {
        return orderRepository.findAllByOrderByCreateTimeDesc();
    }

    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatusOrderByCreateTimeDesc(status);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在，id=" + id));
    }

    /**
     * 买家付款：待付款 -> 待发货
     */
    @Transactional
    public void pay(User user, Long orderId) {
        Order order = findById(orderId);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("无权操作他人订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("仅待付款订单可支付");
        }
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * 卖家发货：待发货 -> 待收货（仅管理员）
     */
    @Transactional
    public void ship(Long orderId) {
        Order order = findById(orderId);
        if (!"PAID".equals(order.getStatus())) {
            throw new IllegalStateException("仅待发货订单可发货");
        }
        order.setStatus("SHIPPED");
        order.setShipTime(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * 买家确认收货：待收货 -> 已完成
     */
    @Transactional
    public void confirm(User user, Long orderId) {
        Order order = findById(orderId);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("无权操作他人订单");
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new IllegalStateException("仅待收货订单可确认收货");
        }
        order.setStatus("COMPLETED");
        order.setCompleteTime(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * 取消订单（仅待付款状态可取消），回补库存与销量
     */
    @Transactional
    public void cancel(User user, Long orderId) {
        Order order = findById(orderId);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("无权操作他人订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("仅待付款订单可取消");
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        // 回补库存和销量
        for (OrderItem oi : order.getItems()) {
            if (oi.getProductId() != null) {
                productRepository.findById(oi.getProductId()).ifPresent(p -> {
                    p.setStock(p.getStock() + oi.getQuantity());
                    p.setSales(Math.max(0, p.getSales() - oi.getQuantity()));
                    productRepository.save(p);
                });
            }
        }
    }

    public long countByStatus(String status) {
        return orderRepository.countByStatus(status);
    }
}
