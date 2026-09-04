package com.marom.ecommerce.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marom.ecommerce.api.dto.ErrorResponse;
import com.marom.ecommerce.api.dto.ReviewSummary;
import com.marom.ecommerce.api.service.ReviewSummaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products/{id}/review-summary")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "AI-generated review summaries")
public class ReviewSummaryController {

    private final ReviewSummaryService reviewSummaryService;

    @GetMapping
    @Operation(summary = "Get an AI summary of a product's reviews",
            description = "Summarizes the product's reviews via a local Ollama model. When the product "
                    + "has no reviews yet it returns a summary without calling the model.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review summary generated"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ReviewSummary> getReviewSummary(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        return ResponseEntity.ok(reviewSummaryService.getSummary(id));
    }
}
