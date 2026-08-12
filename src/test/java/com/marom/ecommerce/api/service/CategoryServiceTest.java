package com.marom.ecommerce.api.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marom.ecommerce.api.dto.CategoryRequest;
import com.marom.ecommerce.api.dto.CategoryResponse;
import com.marom.ecommerce.api.entity.Category;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.CategoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void should_createCategory_when_nameAndSlugAreAvailable() {
        // Arrange
        CategoryRequest request = CategoryRequest.builder()
                .name("Electronics")
                .slug("electronics")
                .description("Gadgets and devices")
                .build();
        Category saved = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .description("Gadgets and devices")
                .build();
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        // Act
        CategoryResponse response = categoryService.createCategory(request);

        // Assert
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Electronics");
        assertThat(response.getSlug()).isEqualTo("electronics");
    }

    @Test
    void should_throwDuplicateResourceException_when_nameAlreadyExists() {
        // Arrange
        CategoryRequest request = CategoryRequest.builder()
                .name("Electronics")
                .slug("electronics-2")
                .build();
        Category existing = Category.builder().id(9L).name("Electronics").slug("electronics").build();
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Electronics");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void should_throwDuplicateResourceException_when_slugAlreadyExists() {
        // Arrange
        CategoryRequest request = CategoryRequest.builder()
                .name("New Electronics")
                .slug("electronics")
                .build();
        Category existing = Category.builder().id(9L).name("Electronics").slug("electronics").build();
        when(categoryRepository.findByName("New Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("electronics");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void should_returnCategory_when_categoryExists() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").slug("electronics").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        CategoryResponse response = categoryService.getCategory(1L);

        // Assert
        assertThat(response.getName()).isEqualTo("Electronics");
    }

    @Test
    void should_throwResourceNotFoundException_when_categoryDoesNotExist() {
        // Arrange
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategory(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void should_returnAllCategories_when_categoriesExist() {
        // Arrange
        Category first = Category.builder().id(1L).name("Electronics").slug("electronics").build();
        Category second = Category.builder().id(2L).name("Books").slug("books").build();
        when(categoryRepository.findAll()).thenReturn(List.of(first, second));

        // Act
        List<CategoryResponse> responses = categoryService.getAllCategories();

        // Assert
        assertThat(responses).extracting(CategoryResponse::getName).containsExactly("Electronics", "Books");
    }

    @Test
    void should_updateCategory_when_nameAndSlugBelongToSameCategory() {
        // Arrange
        Category existing = Category.builder().id(1L).name("Electronics").slug("electronics").build();
        CategoryRequest request = CategoryRequest.builder()
                .name("Electronics")
                .slug("electronics")
                .description("Updated description")
                .build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(existing));
        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CategoryResponse response = categoryService.updateCategory(1L, request);

        // Assert
        assertThat(response.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void should_deleteCategory_when_categoryExists() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").slug("electronics").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        categoryService.deleteCategory(1L);

        // Assert
        verify(categoryRepository).delete(category);
    }
}
