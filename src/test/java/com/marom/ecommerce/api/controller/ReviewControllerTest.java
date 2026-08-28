package com.marom.ecommerce.api.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marom.ecommerce.api.dto.ReviewRequest;
import com.marom.ecommerce.api.dto.ReviewResponse;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.service.ReviewService;
import com.marom.ecommerce.api.support.SecurityTestSupport;
import com.marom.ecommerce.api.support.WithMockCustomUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import(SecurityTestSupport.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WithMockCustomUser(customerId = 3L)
class ReviewControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ReviewService reviewService;

    ReviewControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void should_returnCreated_when_reviewRequestIsValid() throws Exception {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().rating(5).comment("Great product.").build();
        ReviewResponse response = ReviewResponse.builder().id(10L).productId(1L).customerId(3L)
                .customerName("Ravi Kumar").rating(5).comment("Great product.").build();
        when(reviewService.createReview(eq(1L), eq(3L), any(ReviewRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products/1/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.customerName").value("Ravi Kumar"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void should_returnBadRequest_when_ratingIsMissing() throws Exception {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().comment("Great product.").build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/products/1/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_ratingIsOutOfRange() throws Exception {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().rating(6).comment("Great product.").build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/products/1/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_commentIsBlank() throws Exception {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().rating(5).comment("").build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/products/1/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnConflict_when_customerAlreadyReviewedProduct() throws Exception {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().rating(5).comment("Great product.").build();
        when(reviewService.createReview(eq(1L), eq(3L), any(ReviewRequest.class)))
                .thenThrow(new DuplicateResourceException("Customer with id 3 has already reviewed product with id 1"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/products/1/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void should_returnNotFound_when_productDoesNotExistOnCreate() throws Exception {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().rating(5).comment("Great product.").build();
        when(reviewService.createReview(eq(404L), eq(3L), any(ReviewRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found with id 404"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/products/404/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void should_return401_when_anonymousPostsReview() throws Exception {
        // Arrange
        ReviewRequest request = ReviewRequest.builder().rating(5).comment("Great product.").build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/products/1/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_returnOk_when_listingReviewsForProduct() throws Exception {
        // Arrange
        ReviewResponse response = ReviewResponse.builder().id(1L).productId(1L).customerId(1L)
                .customerName("John Doe").rating(5).comment("Great.").build();
        when(reviewService.getReviewsForProduct(1L)).thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("John Doe"));
    }

    @Test
    void should_returnNotFound_when_listingReviewsForNonexistentProduct() throws Exception {
        // Arrange
        when(reviewService.getReviewsForProduct(anyLong()))
                .thenThrow(new ResourceNotFoundException("Product not found with id 404"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/404/reviews"))
                .andExpect(status().isNotFound());
    }
}
