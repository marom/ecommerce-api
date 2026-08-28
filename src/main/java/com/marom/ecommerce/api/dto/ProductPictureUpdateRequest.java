package com.marom.ecommerce.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Request payload to update a product picture's metadata")
public class ProductPictureUpdateRequest {

    @Schema(description = "Alternative text for accessibility", example = "Front view of the wireless mouse")
    @Size(max = 255)
    private String altText;

    @Schema(description = "Ordering position; the lowest value is the primary picture", example = "0")
    @NotNull
    @Min(0)
    private Integer displayOrder;
}
