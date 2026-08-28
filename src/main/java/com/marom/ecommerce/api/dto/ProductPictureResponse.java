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
@Schema(description = "A picture attached to a product")
public class ProductPictureResponse {

    @Schema(description = "Picture ID", example = "1")
    private Long id;
    @Schema(description = "ID of the product this picture belongs to", example = "1")
    private Long productId;
    @Schema(description = "Relative URL that serves the raw image bytes",
            example = "/api/v1/products/1/pictures/1/content")
    private String url;
    @Schema(description = "Alternative text for accessibility", example = "Front view of the wireless mouse")
    private String altText;
    @Schema(description = "Ordering position; the lowest value is the primary picture", example = "0")
    private Integer displayOrder;
    @Schema(description = "MIME type of the stored image", example = "image/jpeg")
    private String contentType;
    @Schema(description = "Size of the stored image in bytes", example = "20480")
    private Long sizeBytes;
    @Schema(description = "Original filename supplied on upload", example = "mouse-front.jpg")
    private String originalFilename;
    @Schema(description = "When the picture was uploaded")
    private LocalDateTime createdAt;
}
