package com.marom.ecommerce.api.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

import com.marom.ecommerce.api.dto.ReviewSummary;
import com.marom.ecommerce.api.entity.Product;
import com.marom.ecommerce.api.entity.Review;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.ProductRepository;
import com.marom.ecommerce.api.repository.ReviewRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewSummaryServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewSummaryService reviewSummaryService;

    @Test
    void should_returnAiGeneratedSummary_when_productHasReviews() {
        // Arrange
        Review review1 = Review.builder().rating(5).comment("Great mouse, very comfortable.").build();
        Review review2 = Review.builder().rating(2).comment("Battery dies too quickly.").build();
        when(productRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByProductIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(review1, review2));

        ReviewSummary aiSummary = ReviewSummary.builder()
                .summary("Comfortable but the battery life disappoints.")
                .pros(List.of("Comfortable"))
                .cons(List.of("Short battery life"))
                .sentiment(ReviewSummary.Sentiment.NEUTRAL)
                .build();
        stubChatClientToReturn(aiSummary);

        // Act
        ReviewSummary result = reviewSummaryService.getSummary(1L);

        // Assert
        assertThat(result).isEqualTo(aiSummary);
    }

    @Test
    void should_returnNoReviews_when_productHasNoReviews() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByProductIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        // Act
        ReviewSummary result = reviewSummaryService.getSummary(1L);

        // Assert
        assertThat(result.getSummary()).isEqualTo(ReviewSummary.NO_REVIEWS_MESSAGE);
        assertThat(result.getPros()).isEmpty();
        assertThat(result.getCons()).isEmpty();
        assertThat(result.getSentiment()).isNull();
        verify(chatClient, never()).prompt();
    }

    @Test
    void should_throwResourceNotFoundException_when_productDoesNotExist() {
        // Arrange
        when(productRepository.existsById(404L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> reviewSummaryService.getSummary(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
        verify(reviewRepository, never()).findByProductIdOrderByCreatedAtAsc(anyLong());
        verify(chatClient, never()).prompt();
    }

    private void stubChatClientToReturn(ReviewSummary summary) {
        ChatClientRequestSpec requestSpec = mock(ChatClientRequestSpec.class);
        CallResponseSpec responseSpec = mock(CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(ReviewSummary.class)).thenReturn(summary);
    }
}
