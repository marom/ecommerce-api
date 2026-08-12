package com.marom.ecommerce.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marom.ecommerce.api.dto.ProductRequest;
import com.marom.ecommerce.api.dto.ProductResponse;
import com.marom.ecommerce.api.entity.Category;
import com.marom.ecommerce.api.entity.Product;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.CategoryRepository;
import com.marom.ecommerce.api.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductResponse createProduct(ProductRequest request) {
        assertSkuAvailable(request.getSku(), null);
        Category category = findCategoryOrThrow(request.getCategoryId());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .sku(request.getSku())
                .stockQuantity(request.getStockQuantity())
                .active(request.getActive())
                .category(category)
                .build();
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return toResponse(findProductOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);
        assertSkuAvailable(request.getSku(), id);
        Category category = findCategoryOrThrow(request.getCategoryId());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setStockQuantity(request.getStockQuantity());
        product.setActive(request.getActive());
        product.setCategory(category);
        return toResponse(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        productRepository.delete(findProductOrThrow(id));
    }

    // Business rule: stock is reduced when the order is placed, not at shipment — called from
    // OrderService while building a new order.
    Product reduceStock(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new BusinessRuleException(
                    "Insufficient stock for product '" + product.getName() + "': requested " + quantity
                            + ", available " + product.getStockQuantity());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        return productRepository.save(product);
    }

    // Business rule: cancelling an order restores stock for all of its items — called from
    // OrderService when an order transitions to CANCELLED.
    void restoreStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
    }

    private void assertSkuAvailable(String sku, Long selfId) {
        productRepository.findBySku(sku)
                .filter(existing -> !existing.getId().equals(selfId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Product with SKU '" + sku + "' already exists");
                });
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .sku(product.getSku())
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
