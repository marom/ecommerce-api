package com.marom.ecommerce.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
import com.marom.ecommerce.api.repository.ProductPictureRepository;
import com.marom.ecommerce.api.repository.ProductPictureView;
import com.marom.ecommerce.api.repository.ProductRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductPictureRepository productPictureRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void stubEmptyPicturesByDefault() {
        lenient().when(productPictureRepository.findViewsByProductId(anyLong())).thenReturn(List.of());
        lenient().when(productPictureRepository.findViewsByProductIdIn(any())).thenReturn(List.of());
    }

    private static Category category() {
        return Category.builder().id(1L).name("Electronics").slug("electronics").build();
    }

    private static ProductPictureView view(long id, long productId, int displayOrder) {
        return new ProductPictureView() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public Long getProductId() {
                return productId;
            }

            @Override
            public String getAltText() {
                return null;
            }

            @Override
            public Integer getDisplayOrder() {
                return displayOrder;
            }

            @Override
            public String getContentType() {
                return "image/jpeg";
            }

            @Override
            public Long getSizeBytes() {
                return 3L;
            }

            @Override
            public String getOriginalFilename() {
                return "pic-" + id + ".jpg";
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return LocalDateTime.now();
            }
        };
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
    void should_updateProduct_when_requestIsValid() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Wireless Mouse Pro")
                .price(new BigDecimal("34.99"))
                .sku("ELEC-MOU-002")
                .stockQuantity(200)
                .active(true)
                .categoryId(1L)
                .build();
        Product existing = Product.builder().id(1L).name("Wireless Mouse").sku("ELEC-MOU-001")
                .price(new BigDecimal("24.99")).stockQuantity(150).active(true).category(category()).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findBySku("ELEC-MOU-002")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category()));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.updateProduct(1L, request);

        // Assert
        assertThat(response.getName()).isEqualTo("Wireless Mouse Pro");
        assertThat(response.getSku()).isEqualTo("ELEC-MOU-002");
        assertThat(response.getStockQuantity()).isEqualTo(200);
    }

    @Test
    void should_throwResourceNotFoundException_when_productDoesNotExistOnUpdate() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Wireless Mouse Pro")
                .price(new BigDecimal("34.99"))
                .sku("ELEC-MOU-002")
                .stockQuantity(200)
                .active(true)
                .categoryId(1L)
                .build();
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(404L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
        verify(productRepository, never()).save(any());
    }

    @Test
    void should_updateProduct_when_skuIsUnchanged() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Wireless Mouse Pro")
                .price(new BigDecimal("34.99"))
                .sku("ELEC-MOU-001")
                .stockQuantity(200)
                .active(true)
                .categoryId(1L)
                .build();
        Product existing = Product.builder().id(1L).name("Wireless Mouse").sku("ELEC-MOU-001")
                .price(new BigDecimal("24.99")).stockQuantity(150).active(true).category(category()).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findBySku("ELEC-MOU-001")).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category()));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.updateProduct(1L, request);

        // Assert
        assertThat(response.getName()).isEqualTo("Wireless Mouse Pro");
        assertThat(response.getSku()).isEqualTo("ELEC-MOU-001");
    }

    @Test
    void should_throwDuplicateResourceException_when_skuBelongsToAnotherProductOnUpdate() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Wireless Mouse Pro")
                .price(new BigDecimal("34.99"))
                .sku("ELEC-KEY-001")
                .stockQuantity(200)
                .active(true)
                .categoryId(1L)
                .build();
        Product existing = Product.builder().id(1L).sku("ELEC-MOU-001").category(category()).build();
        Product other = Product.builder().id(2L).sku("ELEC-KEY-001").category(category()).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findBySku("ELEC-KEY-001")).thenReturn(Optional.of(other));

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ELEC-KEY-001");
        verify(categoryRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void should_deleteProduct_when_productExists() {
        // Arrange
        Product product = Product.builder().id(1L).category(category()).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productPictureRepository).deleteByProductId(1L);
        verify(productRepository).delete(product);
    }

    @Test
    void should_includePictures_when_gettingProduct() {
        // Arrange
        Product product = Product.builder().id(1L).name("Wireless Mouse").category(category()).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productPictureRepository.findViewsByProductId(1L))
                .thenReturn(List.of(view(5L, 1L, 0), view(6L, 1L, 1)));

        // Act
        ProductResponse response = productService.getProduct(1L);

        // Assert
        assertThat(response.getPictures()).extracting(p -> p.getId(), p -> p.getUrl())
                .containsExactly(
                        tuple(5L, "/api/v1/products/1/pictures/5/content"),
                        tuple(6L, "/api/v1/products/1/pictures/6/content"));
    }

    @Test
    void should_batchFetchPictures_when_listingAllProducts() {
        // Arrange
        Product first = Product.builder().id(1L).name("Wireless Mouse").category(category()).build();
        Product second = Product.builder().id(2L).name("Mechanical Keyboard").category(category()).build();
        when(productRepository.findAll()).thenReturn(List.of(first, second));
        when(productPictureRepository.findViewsByProductIdIn(any()))
                .thenReturn(List.of(view(10L, 1L, 0), view(11L, 2L, 0), view(12L, 2L, 1)));

        // Act
        List<ProductResponse> responses = productService.getAllProducts();

        // Assert
        verify(productPictureRepository, never()).findViewsByProductId(anyLong());
        assertThat(responses).extracting(r -> r.getName(), r -> r.getPictures().size())
                .containsExactly(tuple("Wireless Mouse", 1), tuple("Mechanical Keyboard", 2));
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
