package com.marom.ecommerce.api.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.marom.ecommerce.api.dto.ProductPictureResponse;
import com.marom.ecommerce.api.dto.ProductPictureUpdateRequest;
import com.marom.ecommerce.api.entity.Product;
import com.marom.ecommerce.api.entity.ProductPicture;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.ProductPictureRepository;
import com.marom.ecommerce.api.repository.ProductPictureView;
import com.marom.ecommerce.api.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductPictureService {

    static final int MAX_PICTURES_PER_PRODUCT = 10;
    static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final ProductPictureRepository productPictureRepository;
    private final ProductRepository productRepository;

    public List<ProductPictureResponse> addPictures(Long productId, List<MultipartFile> files) {
        Product product = findProductOrThrow(productId);
        if (files == null || files.isEmpty() || files.stream().allMatch(file -> file == null || file.isEmpty())) {
            throw new BusinessRuleException("At least one picture file is required");
        }
        long existing = productPictureRepository.countByProductId(productId);
        if (existing + files.size() > MAX_PICTURES_PER_PRODUCT) {
            throw new BusinessRuleException("A product may have at most " + MAX_PICTURES_PER_PRODUCT
                    + " pictures; " + existing + " already present, " + files.size() + " more requested");
        }
        files.forEach(ProductPictureService::assertValidImage);

        int nextOrder = productPictureRepository.findMaxDisplayOrder(productId) + 1;
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            productPictureRepository.save(ProductPicture.builder()
                    .product(product)
                    .data(readBytes(file))
                    .contentType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .originalFilename(file.getOriginalFilename())
                    .displayOrder(nextOrder + i)
                    .build());
        }
        return getPictures(productId);
    }

    @Transactional(readOnly = true)
    public List<ProductPictureResponse> getPictures(Long productId) {
        findProductOrThrow(productId);
        return productPictureRepository.findViewsByProductId(productId).stream()
                .map(ProductPictureService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PictureContent getPictureContent(Long productId, Long pictureId) {
        ProductPicture picture = productPictureRepository.findByIdAndProductId(pictureId, productId)
                .orElseThrow(() -> pictureNotFound(productId, pictureId));
        return new PictureContent(picture.getData(), picture.getContentType(),
                picture.getOriginalFilename(), picture.getSizeBytes());
    }

    public ProductPictureResponse updatePicture(Long productId, Long pictureId, ProductPictureUpdateRequest request) {
        ProductPicture picture = productPictureRepository.findByIdAndProductId(pictureId, productId)
                .orElseThrow(() -> pictureNotFound(productId, pictureId));
        picture.setAltText(request.getAltText());
        picture.setDisplayOrder(request.getDisplayOrder());
        productPictureRepository.save(picture);
        return productPictureRepository.findViewsByProductId(productId).stream()
                .filter(view -> view.getId().equals(pictureId))
                .map(ProductPictureService::toResponse)
                .findFirst()
                .orElseThrow(() -> pictureNotFound(productId, pictureId));
    }

    public void deletePicture(Long productId, Long pictureId) {
        if (productPictureRepository.deleteByIdAndProductId(pictureId, productId) == 0) {
            throw pictureNotFound(productId, pictureId);
        }
    }

    private static void assertValidImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessRuleException("Picture file '" + file.getOriginalFilename() + "' is empty");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessRuleException("Unsupported picture content type '" + file.getContentType()
                    + "'; allowed types are " + ALLOWED_CONTENT_TYPES);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleException("Picture file '" + file.getOriginalFilename() + "' exceeds the "
                    + MAX_FILE_SIZE_BYTES + " byte limit");
        }
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new BusinessRuleException("Could not read picture file '" + file.getOriginalFilename() + "'");
        }
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    private static ResourceNotFoundException pictureNotFound(Long productId, Long pictureId) {
        return new ResourceNotFoundException(
                "Picture not found with id " + pictureId + " for product with id " + productId);
    }

    public static ProductPictureResponse toResponse(ProductPictureView view) {
        return ProductPictureResponse.builder()
                .id(view.getId())
                .productId(view.getProductId())
                .url("/api/v1/products/" + view.getProductId() + "/pictures/" + view.getId() + "/content")
                .altText(view.getAltText())
                .displayOrder(view.getDisplayOrder())
                .contentType(view.getContentType())
                .sizeBytes(view.getSizeBytes())
                .originalFilename(view.getOriginalFilename())
                .createdAt(view.getCreatedAt())
                .build();
    }

    public record PictureContent(byte[] data, String contentType, String filename, Long sizeBytes) {
    }
}
