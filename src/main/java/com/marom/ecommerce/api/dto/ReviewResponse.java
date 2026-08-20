package com.marom.ecommerce.api.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A review posted on a product")
public class ReviewResponse {

    @Schema(description = "Review ID", example = "1")
    private Long id;
    @Schema(description = "ID of the reviewed product", example = "1")
    private Long productId;
    @Schema(description = "ID of the customer who posted the review", example = "1")
    private Long customerId;
    @Schema(description = "Name of the customer who posted the review", example = "John Doe")
    private String customerName;
    @Schema(description = "Star rating from 1 to 5", example = "5")
    private Integer rating;
    @Schema(description = "Review comment", example = "Great mouse, very responsive and comfortable to use daily.")
    private String comment;
    @Schema(description = "When the review was created")
    private LocalDateTime createdAt;
}
