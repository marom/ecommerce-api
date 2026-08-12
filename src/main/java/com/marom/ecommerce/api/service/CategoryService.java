package com.marom.ecommerce.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marom.ecommerce.api.dto.CategoryRequest;
import com.marom.ecommerce.api.dto.CategoryResponse;
import com.marom.ecommerce.api.entity.Category;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponse createCategory(CategoryRequest request) {
        assertNameAndSlugAvailable(request, null);

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .build();
        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return toResponse(findCategoryOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryOrThrow(id);
        assertNameAndSlugAvailable(request, id);

        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        categoryRepository.delete(findCategoryOrThrow(id));
    }

    private void assertNameAndSlugAvailable(CategoryRequest request, Long selfId) {
        categoryRepository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(selfId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
                });
        categoryRepository.findBySlug(request.getSlug())
                .filter(existing -> !existing.getId().equals(selfId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Category with slug '" + request.getSlug() + "' already exists");
                });
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
