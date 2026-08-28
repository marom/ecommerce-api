package com.marom.ecommerce.api.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void should_createReview_when_productAndCustomerExistAndNoDuplicate() {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().rating(5).comment("Great product.").build();
        Product product = Product.builder().id(1L).name("Wireless Mouse").build();
        Customer customer = Customer.builder().id(3L).firstName("Ravi").lastName("Kumar").build();
        Review saved = Review.builder().id(10L).product(product).customer(customer).rating(5)
                .comment("Great product.").build();
        when(reviewRepository.existsByProductIdAndCustomerId(1L, 3L)).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(customerRepository.findById(3L)).thenReturn(Optional.of(customer));
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        // Act
        ReviewResponse response = reviewService.createReview(1L, 3L, request);

        // Assert
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getCustomerId()).isEqualTo(3L);
        assertThat(response.getCustomerName()).isEqualTo("Ravi Kumar");
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Great product.");
    }

    @Test
    void should_throwDuplicateResourceException_when_customerAlreadyReviewedProduct() {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().rating(4).comment("Solid.").build();
        when(reviewRepository.existsByProductIdAndCustomerId(1L, 3L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(1L, 3L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("1")
                .hasMessageContaining("3");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void should_throwResourceNotFoundException_when_productDoesNotExistOnCreate() {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().rating(4).comment("Solid.").build();
        when(reviewRepository.existsByProductIdAndCustomerId(404L, 3L)).thenReturn(false);
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(404L, 3L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
        verify(customerRepository, never()).findById(any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void should_throwResourceNotFoundException_when_customerDoesNotExistOnCreate() {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().rating(4).comment("Solid.").build();
        Product product = Product.builder().id(1L).name("Wireless Mouse").build();
        when(reviewRepository.existsByProductIdAndCustomerId(1L, 404L)).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(customerRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(1L, 404L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void should_returnReviewsForProduct_when_reviewsExist() {
        // Arrange
        Product product = Product.builder().id(1L).name("Wireless Mouse").build();
        Customer first = Customer.builder().id(1L).firstName("John").lastName("Doe").build();
        Customer second = Customer.builder().id(2L).firstName("Jane").lastName("Smith").build();
        Review review1 = Review.builder().id(1L).product(product).customer(first).rating(5).comment("Great.")
                .build();
        Review review2 = Review.builder().id(2L).product(product).customer(second).rating(4).comment("Good.")
                .build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of(review1, review2));

        // Act
        List<ReviewResponse> responses = reviewService.getReviewsForProduct(1L);

        // Assert
        assertThat(responses).extracting(ReviewResponse::getCustomerName).containsExactly("John Doe", "Jane Smith");
    }

    @Test
    void should_returnEmptyList_when_productHasNoReviews() {
        // Arrange
        Product product = Product.builder().id(1L).name("Wireless Mouse").build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of());

        // Act
        List<ReviewResponse> responses = reviewService.getReviewsForProduct(1L);

        // Assert
        assertThat(responses).isEmpty();
    }

    @Test
    void should_throwResourceNotFoundException_when_listingReviewsForNonexistentProduct() {
        // Arrange
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.getReviewsForProduct(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
        verify(reviewRepository, never()).findByProductId(any());
    }
}
