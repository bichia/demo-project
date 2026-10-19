package com.example.commodity.repository;

import com.example.commodity.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdOrderByCreateTimeDesc(Long userId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    long deleteByUserId(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
