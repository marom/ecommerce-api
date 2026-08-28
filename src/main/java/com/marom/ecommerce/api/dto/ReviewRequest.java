package com.marom.ecommerce.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to post a review for a product. The customer is taken "
        + "from the authenticated principal, not this body.")
public class ReviewRequest {

    @Schema(description = "Star rating from 1 (worst) to 5 (best)", example = "5")
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Schema(description = "Review comment", example = "Great mouse, very responsive and comfortable to use daily.")
    @NotBlank
    @Size(max = 2000)
    private String comment;
}
