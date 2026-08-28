package com.marom.ecommerce.api.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marom.ecommerce.api.dto.ProductRequest;
import com.marom.ecommerce.api.dto.ProductResponse;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.service.ProductService;
import com.marom.ecommerce.api.support.SecurityTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityTestSupport.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WithMockUser(roles = "ADMIN")
class ProductControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    ProductControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private ProductRequest validProductRequest() {
        return ProductRequest.builder()
                .name("Wireless Mouse")
                .description("A wireless mouse with ergonomic design")
                .price(BigDecimal.valueOf(29.99))
                .sku("WM-1000")
                .stockQuantity(150)
                .active(true)
                .categoryId(1L)
                .build();
    }

    private ProductResponse sampleProductResponse() {
        return ProductResponse.builder()
                .id(1L)
                .name("Wireless Mouse")
                .description("A wireless mouse with ergonomic design")
                .price(BigDecimal.valueOf(29.99))
                .sku("WM-1000")
                .stockQuantity(150)
                .active(true)
                .categoryId(1L)
                .categoryName("Electronics")
                .build();
    }

    // ----- POST /api/v1/products -----

    @Test
    void should_returnCreated_when_productRequestIsValid() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        ProductResponse response = sampleProductResponse();
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Wireless Mouse"))
                .andExpect(jsonPath("$.sku").value("WM-1000"))
                .andExpect(jsonPath("$.price").value(29.99));
    }

    @Test
    void should_returnBadRequest_when_nameIsBlank() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setName("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_nameExceedsMaxLength() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setName("a".repeat(201));

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_priceIsMissing() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setPrice(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_priceIsNotGreaterThanZero() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setPrice(BigDecimal.ZERO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_skuIsBlank() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setSku("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_skuExceedsMaxLength() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setSku("a".repeat(101));

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_stockQuantityIsMissing() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setStockQuantity(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_stockQuantityIsNegative() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setStockQuantity(-1);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_activeIsMissing() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setActive(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_categoryIdIsMissing() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setCategoryId(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnNotFound_when_categoryDoesNotExistOnCreate() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        when(productService.createProduct(any(ProductRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found with id 1"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_returnConflict_when_skuAlreadyExistsOnCreate() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        when(productService.createProduct(any(ProductRequest.class)))
                .thenThrow(new DuplicateResourceException("Product with SKU 'WM-1000' already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithAnonymousUser
    void should_return401_when_noAuthOnCreate() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void should_return403_when_customerRoleOnCreate() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();

        // Act & Assert
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ----- GET /api/v1/products/{id} -----

    @Test
    void should_returnOk_when_productExists() throws Exception {
        // Arrange
        when(productService.getProduct(1L)).thenReturn(sampleProductResponse());

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("WM-1000"));
    }

    @Test
    void should_returnNotFound_when_productDoesNotExistOnGet() throws Exception {
        // Arrange
        when(productService.getProduct(404L)).thenThrow(new ResourceNotFoundException("Product not found with id 404"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/404"))
                .andExpect(status().isNotFound());
    }

    // ----- GET /api/v1/products -----

    @Test
    void should_returnOk_when_listingAllProducts() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(List.of(sampleProductResponse()));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].sku").value("WM-1000"));
    }

    // ----- PUT /api/v1/products/{id} -----

    @Test
    void should_returnOk_when_productUpdateIsValid() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        ProductResponse response = sampleProductResponse();
        response.setStockQuantity(200);
        when(productService.updateProduct(eq(1L), any(ProductRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stockQuantity").value(200));
    }

    @Test
    void should_returnBadRequest_when_nameIsBlankOnUpdate() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        request.setName("");

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnNotFound_when_productDoesNotExistOnUpdate() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        when(productService.updateProduct(eq(404L), any(ProductRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found with id 404"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/404").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_returnNotFound_when_categoryDoesNotExistOnUpdate() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found with id 1"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_returnConflict_when_skuAlreadyExistsOnUpdate() throws Exception {
        // Arrange
        ProductRequest request = validProductRequest();
        when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                .thenThrow(new DuplicateResourceException("Product with SKU 'WM-1000' already exists"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ----- DELETE /api/v1/products/{id} -----

    @Test
    void should_returnNoContent_when_productIsDeleted() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_returnNotFound_when_productDoesNotExistOnDelete() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Product not found with id 404"))
                .when(productService).deleteProduct(404L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/404"))
                .andExpect(status().isNotFound());
    }
}
