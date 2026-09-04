package com.marom.ecommerce.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.marom.ecommerce.api.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.customer WHERE r.product.id = :productId")
    List<Review> findByProductId(Long productId);

    List<Review> findByProductIdOrderByCreatedAtAsc(Long productId);

    boolean existsByProductIdAndCustomerId(Long productId, Long customerId);
}
