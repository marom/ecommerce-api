package com.marom.ecommerce.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.marom.ecommerce.api.dto.ReviewSummary;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.service.ReviewSummaryService;
import com.marom.ecommerce.api.support.SecurityTestSupport;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewSummaryController.class)
@Import(SecurityTestSupport.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ReviewSummaryControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private ReviewSummaryService reviewSummaryService;

    ReviewSummaryControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void should_returnReviewSummary_when_productHasReviews() throws Exception {
        // Arrange
        ReviewSummary summary = ReviewSummary.builder()
                .summary("Comfortable but the battery life disappoints.")
                .pros(java.util.List.of("Comfortable"))
                .cons(java.util.List.of("Short battery life"))
                .sentiment(ReviewSummary.Sentiment.NEUTRAL)
                .build();
        when(reviewSummaryService.getSummary(1L)).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/1/review-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Comfortable but the battery life disappoints."))
                .andExpect(jsonPath("$.pros[0]").value("Comfortable"))
                .andExpect(jsonPath("$.cons[0]").value("Short battery life"))
                .andExpect(jsonPath("$.sentiment").value("neutral"));
    }

    @Test
    void should_returnNoReviewsResponse_when_productHasNoReviews() throws Exception {
        // Arrange
        when(reviewSummaryService.getSummary(1L)).thenReturn(ReviewSummary.noReviews());

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/1/review-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(ReviewSummary.NO_REVIEWS_MESSAGE))
                .andExpect(jsonPath("$.pros").isEmpty())
                .andExpect(jsonPath("$.cons").isEmpty());
    }

    @Test
    void should_returnNotFound_when_productDoesNotExist() throws Exception {
        // Arrange
        when(reviewSummaryService.getSummary(404L))
                .thenThrow(new ResourceNotFoundException("Product not found with id 404"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/404/review-summary"))
                .andExpect(status().isNotFound());
    }
}
