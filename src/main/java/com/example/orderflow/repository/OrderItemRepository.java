package com.example.orderflow.repository;


import com.example.orderflow.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    long countByProductId(Long productId);
}