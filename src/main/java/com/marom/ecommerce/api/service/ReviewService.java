package com.marom.ecommerce.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marom.ecommerce.api.dto.ReviewRequest;
import com.marom.ecommerce.api.dto.ReviewResponse;
import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.entity.Product;
import com.marom.ecommerce.api.entity.Review;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.CustomerRepository;
import com.marom.ecommerce.api.repository.ProductRepository;
import com.marom.ecommerce.api.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public ReviewResponse createReview(Long productId, ReviewRequest request) {
        assertNoDuplicateReview(productId, request.getCustomerId());
        Product product = findProductOrThrow(productId);
        Customer customer = findCustomerOrThrow(request.getCustomerId());

        Review review = Review.builder()
                .product(product)
                .customer(customer)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        return toResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForProduct(Long productId) {
        findProductOrThrow(productId);
        return reviewRepository.findByProductId(productId).stream().map(this::toResponse).toList();
    }

    private void assertNoDuplicateReview(Long productId, Long customerId) {
        if (reviewRepository.existsByProductIdAndCustomerId(productId, customerId)) {
            throw new DuplicateResourceException(
                    "Customer with id " + customerId + " has already reviewed product with id " + productId);
        }
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getFirstName() + " " + review.getCustomer().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
