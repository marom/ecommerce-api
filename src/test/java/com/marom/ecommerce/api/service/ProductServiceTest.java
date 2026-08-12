package com.marom.ecommerce.api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marom.ecommerce.api.dto.ProductRequest;
import com.marom.ecommerce.api.dto.ProductResponse;
import com.marom.ecommerce.api.entity.Category;
import com.marom.ecommerce.api.entity.Product;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.CategoryRepository;
import com.marom.ecommerce.api.repository.ProductRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private static Category category() {
        return Category.builder().id(1L).name("Electronics").slug("electronics").build();
    }

    @Test
    void should_createProduct_when_skuIsAvailable() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Wireless Mouse")
                .price(new BigDecimal("24.99"))
                .sku("ELEC-MOU-001")
                .stockQuantity(150)
                .active(true)
                .categoryId(1L)
                .build();
        Product saved = Product.builder()
                .id(1L)
                .name("Wireless Mouse")
                .price(new BigDecimal("24.99"))
                .sku("ELEC-MOU-001")
                .stockQuantity(150)
                .active(true)
                .category(category())
                .build();
        when(productRepository.findBySku("ELEC-MOU-001")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category()));
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertThat(response.getSku()).isEqualTo("ELEC-MOU-001");
        assertThat(response.getCategoryId()).isEqualTo(1L);
    }

    @Test
    void should_throwDuplicateResourceException_when_skuAlreadyExists() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Wireless Mouse")
                .price(new BigDecimal("24.99"))
                .sku("ELEC-MOU-001")
                .stockQuantity(150)
                .active(true)
                .categoryId(1L)
                .build();
        Product existing = Product.builder().id(9L).sku("ELEC-MOU-001").category(category()).build();
        when(productRepository.findBySku("ELEC-MOU-001")).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ELEC-MOU-001");
        verify(categoryRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void should_throwResourceNotFoundException_when_categoryDoesNotExist() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Wireless Mouse")
                .price(new BigDecimal("24.99"))
                .sku("ELEC-MOU-001")
                .stockQuantity(150)
                .active(true)
                .categoryId(404L)
                .build();
        when(productRepository.findBySku("ELEC-MOU-001")).thenReturn(Optional.empty());
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
        verify(productRepository, never()).save(any());
    }

    @Test
    void should_returnProduct_when_productExists() {
        // Arrange
        Product product = Product.builder().id(1L).name("Wireless Mouse").sku("ELEC-MOU-001").category(category()).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        ProductResponse response = productService.getProduct(1L);

        // Assert
        assertThat(response.getName()).isEqualTo("Wireless Mouse");
    }

    @Test
    void should_throwResourceNotFoundException_when_productDoesNotExist() {
        // Arrange
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProduct(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void should_returnAllProducts_when_productsExist() {
        // Arrange
        Product first = Product.builder().id(1L).name("Wireless Mouse").category(category()).build();
        Product second = Product.builder().id(2L).name("Mechanical Keyboard").category(category()).build();
        when(productRepository.findAll()).thenReturn(List.of(first, second));

        // Act
        List<ProductResponse> responses = productService.getAllProducts();

        // Assert
        assertThat(responses).extracting(ProductResponse::getName).containsExactly("Wireless Mouse", "Mechanical Keyboard");
    }

    @Test
    void should_deleteProduct_when_productExists() {
        // Arrange
        Product product = Product.builder().id(1L).category(category()).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository).delete(product);
    }

    @Test
    void should_reduceStock_when_sufficientStockAvailable() {
        // Arrange
        Product product = Product.builder().id(1L).name("Wireless Mouse").stockQuantity(150).build();
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product result = productService.reduceStock(product, 10);

        // Assert
        assertThat(result.getStockQuantity()).isEqualTo(140);
    }

    @Test
    void should_throwBusinessRuleException_when_stockInsufficient() {
        // Arrange
        Product product = Product.builder().id(1L).name("Wireless Mouse").stockQuantity(5).build();

        // Act & Assert
        assertThatThrownBy(() -> productService.reduceStock(product, 10))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");
        verify(productRepository, never()).save(any());
    }

    @Test
    void should_restoreStock_when_orderIsCancelled() {
        // Arrange
        Product product = Product.builder().id(1L).name("Wireless Mouse").stockQuantity(140).build();
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        productService.restoreStock(product, 10);

        // Assert
        assertThat(product.getStockQuantity()).isEqualTo(150);
    }
}
