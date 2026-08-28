package com.marom.ecommerce.api.repository;

import java.time.LocalDateTime;

/**
 * Read-only projection of {@link com.marom.ecommerce.api.entity.ProductPicture} that omits the
 * {@code data} BLOB, so listing a product's pictures never loads the image bytes.
 */
public interface ProductPictureView {

    Long getId();

    Long getProductId();

    String getAltText();

    Integer getDisplayOrder();

    String getContentType();

    Long getSizeBytes();

    String getOriginalFilename();

    LocalDateTime getCreatedAt();
}
