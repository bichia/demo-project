package com.example.commodity.repository;

import com.example.commodity.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreateTimeDesc(Long userId);

    List<Order> findAllByOrderByCreateTimeDesc();

    List<Order> findByStatusOrderByCreateTimeDesc(String status);

    long countByStatus(String status);
}
