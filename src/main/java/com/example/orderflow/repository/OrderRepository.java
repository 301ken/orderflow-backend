package com.example.orderflow.repository;

import com.example.orderflow.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByClientId(Long clientId);
    List<Order> findByClientId(Long clientId);
}