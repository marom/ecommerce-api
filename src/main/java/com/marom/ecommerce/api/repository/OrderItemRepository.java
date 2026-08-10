package com.marom.ecommerce.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marom.ecommerce.api.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
