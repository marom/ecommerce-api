package com.marom.ecommerce.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.marom.ecommerce.api.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Override
    @EntityGraph(attributePaths = "category")
    List<Product> findAll();
}
