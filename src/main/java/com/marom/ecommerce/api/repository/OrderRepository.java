package com.marom.ecommerce.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.marom.ecommerce.api.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @Override
    @EntityGraph(attributePaths = {"customer", "payment"})
    List<Order> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer", "payment"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"customer", "payment"})
    List<Order> findByCustomerId(Long customerId);
}
