package com.marom.ecommerce.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response returned for failed requests")
public class ErrorResponse {

    @Schema(description = "When the error occurred")
    private LocalDateTime timestamp;
    @Schema(description = "HTTP status code", example = "404")
    private int status;
    @Schema(description = "HTTP status reason phrase", example = "Not Found")
    private String error;
    @Schema(description = "Human-readable error message", example = "Product not found with id 1")
    private String message;
    @Schema(description = "Request path that triggered the error", example = "/api/v1/products/1")
    private String path;
    @Schema(description = "Field-level validation errors, present only for validation failures")
    private List<String> details;
}
