package com.marom.ecommerce.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI-generated digest of a product's reviews. It is both the structured-output target the
 * model is asked to fill in and the payload returned by the review-summary endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI-generated summary of a product's reviews")
public class ReviewSummary {

    public static final String NO_REVIEWS_MESSAGE = "No reviews yet for this product.";

    @Schema(description = "One-line overall summary of the reviews", example = "Buyers praise the ergonomics but wish the battery lasted longer.")
    private String summary;

    @Schema(description = "Positive points mentioned across the reviews", example = "[\"Comfortable to use all day\", \"Precise tracking\"]")
    private List<String> pros;

    @Schema(description = "Negative points mentioned across the reviews", example = "[\"Short battery life\", \"Software setup is clunky\"]")
    private List<String> cons;

    @Schema(description = "Overall sentiment of the reviews")
    private Sentiment sentiment;

    public static ReviewSummary noReviews() {
        return ReviewSummary.builder()
                .summary(NO_REVIEWS_MESSAGE)
                .pros(List.of())
                .cons(List.of())
                .build();
    }

    public enum Sentiment {
        POSITIVE("positive"),
        NEUTRAL("neutral"),
        NEGATIVE("negative");

        private final String value;

        Sentiment(String value) {
            this.value = value;
        }

        @JsonValue
        public String value() {
            return value;
        }

        @JsonCreator
        public static Sentiment fromValue(String value) {
            for (Sentiment sentiment : values()) {
                if (sentiment.value.equalsIgnoreCase(value)) {
                    return sentiment;
                }
            }
            throw new IllegalArgumentException("Unknown sentiment: " + value);
        }
    }
}
