package com.marom.ecommerce.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.marom.ecommerce.api.entity.ProductPicture;

public interface ProductPictureRepository extends JpaRepository<ProductPicture, Long> {

    String VIEW_SELECT = "SELECT p.id AS id, p.product.id AS productId, p.altText AS altText, "
            + "p.displayOrder AS displayOrder, p.contentType AS contentType, p.sizeBytes AS sizeBytes, "
            + "p.originalFilename AS originalFilename, p.createdAt AS createdAt FROM ProductPicture p ";

    @Query(VIEW_SELECT + "WHERE p.product.id = :productId ORDER BY p.displayOrder ASC, p.id ASC")
    List<ProductPictureView> findViewsByProductId(Long productId);

    @Query(VIEW_SELECT + "WHERE p.product.id IN :productIds "
            + "ORDER BY p.product.id ASC, p.displayOrder ASC, p.id ASC")
    List<ProductPictureView> findViewsByProductIdIn(Collection<Long> productIds);

    Optional<ProductPicture> findByIdAndProductId(Long id, Long productId);

    @Query("SELECT COALESCE(MAX(p.displayOrder), -1) FROM ProductPicture p WHERE p.product.id = :productId")
    int findMaxDisplayOrder(Long productId);

    long countByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM ProductPicture p WHERE p.id = :id AND p.product.id = :productId")
    int deleteByIdAndProductId(Long id, Long productId);

    @Modifying
    @Query("DELETE FROM ProductPicture p WHERE p.product.id = :productId")
    void deleteByProductId(Long productId);
}
