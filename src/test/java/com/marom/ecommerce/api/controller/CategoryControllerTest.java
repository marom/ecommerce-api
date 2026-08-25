package com.marom.ecommerce.api.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marom.ecommerce.api.dto.CategoryRequest;
import com.marom.ecommerce.api.dto.CategoryResponse;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.service.CategoryService;

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

@WebMvcTest(CategoryController.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CategoryControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    CategoryControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private CategoryRequest validCategoryRequest() {
        return CategoryRequest.builder()
                .name("Electronics")
                .slug("electronics")
                .description("Devices, gadgets and accessories")
                .build();
    }

    private CategoryResponse sampleCategoryResponse() {
        return CategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .description("Devices, gadgets and accessories")
                .build();
    }

    // ----- POST /api/v1/categories -----

    @Test
    void should_returnCreated_when_categoryRequestIsValid() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        CategoryResponse response = sampleCategoryResponse();
        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.slug").value("electronics"));
    }

    @Test
    void should_returnBadRequest_when_nameIsBlank() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        request.setName("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_nameExceedsMaxSize() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        request.setName("a".repeat(101));

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_slugIsBlank() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        request.setSlug("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_slugExceedsMaxSize() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        request.setSlug("a".repeat(101));

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnConflict_when_nameOrSlugAlreadyExistsOnCreate() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenThrow(new DuplicateResourceException("Category with name 'Electronics' already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ----- GET /api/v1/categories/{id} -----

    @Test
    void should_returnOk_when_categoryExists() throws Exception {
        // Arrange
        when(categoryService.getCategory(1L)).thenReturn(sampleCategoryResponse());

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void should_returnNotFound_when_categoryDoesNotExistOnGet() throws Exception {
        // Arrange
        when(categoryService.getCategory(404L))
                .thenThrow(new ResourceNotFoundException("Category not found with id 404"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/404"))
                .andExpect(status().isNotFound());
    }

    // ----- GET /api/v1/categories -----

    @Test
    void should_returnOk_when_listingAllCategories() throws Exception {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(List.of(sampleCategoryResponse()));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    // ----- PUT /api/v1/categories/{id} -----

    @Test
    void should_returnOk_when_categoryUpdateIsValid() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        CategoryResponse response = sampleCategoryResponse();
        response.setDescription("Updated description");
        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void should_returnBadRequest_when_nameIsBlankOnUpdate() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        request.setName("");

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_slugIsBlankOnUpdate() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        request.setSlug("");

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnNotFound_when_categoryDoesNotExistOnUpdate() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        when(categoryService.updateCategory(eq(404L), any(CategoryRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found with id 404"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/404").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_returnConflict_when_nameOrSlugAlreadyExistsOnUpdate() throws Exception {
        // Arrange
        CategoryRequest request = validCategoryRequest();
        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class)))
                .thenThrow(new DuplicateResourceException("Category with slug 'electronics' already exists"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ----- DELETE /api/v1/categories/{id} -----

    @Test
    void should_returnNoContent_when_categoryIsDeleted() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_returnNotFound_when_categoryDoesNotExistOnDelete() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Category not found with id 404"))
                .when(categoryService).deleteCategory(404L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/404"))
                .andExpect(status().isNotFound());
    }
}
