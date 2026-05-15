package com.inkwell.category.service;

import com.inkwell.category.dto.CategoryDTO;
import com.inkwell.category.dto.TaxonomyRequest;
import com.inkwell.category.entity.Category;
import com.inkwell.category.exception.TaxonomyNotFoundException;
import com.inkwell.category.mapper.CategoryMapper;
import com.inkwell.category.repository.CategoryRepository;
import com.inkwell.category.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of CategoryService.
 * Manages category creation, updates, deletion, hierarchy, and post associations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDTO createCategory(TaxonomyRequest request) {
        log.debug("Creating category: {}", request.getName());

        // Generate slug from name
        String slug = SlugUtil.generateSlug(request.getName());

        // Check if slug already exists
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Category slug already exists: " + slug);
        }

        // Validate parent category if provided
        if (request.getParentCategoryId() != null) {
            categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> TaxonomyNotFoundException.categoryNotFound(request.getParentCategoryId()));
        }

        // Create and save category
        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .parentCategoryId(request.getParentCategoryId())
                .postCount(0)
                .build();

        category = categoryRepository.save(category);
        log.info("Category created successfully: {} (ID: {})", slug, category.getCategoryId());

        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Integer categoryId) {
        log.debug("Fetching category by ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> TaxonomyNotFoundException.categoryNotFound(categoryId));

        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryBySlug(String slug) {
        log.debug("Fetching category by slug: {}", slug);

        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> TaxonomyNotFoundException.categoryNotFound(slug));

        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getTopLevelCategories() {
        log.debug("Fetching all top-level categories");

        return categoryRepository.findByParentCategoryIdIsNull()
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getChildCategories(Integer parentCategoryId) {
        log.debug("Fetching child categories for parent: {}", parentCategoryId);

        // Verify parent exists
        categoryRepository.findById(parentCategoryId)
                .orElseThrow(() -> TaxonomyNotFoundException.categoryNotFound(parentCategoryId));

        return categoryRepository.findByParentCategoryId(parentCategoryId)
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.debug("Fetching all categories");

        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(Integer categoryId, TaxonomyRequest request) {
        log.debug("Updating category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> TaxonomyNotFoundException.categoryNotFound(categoryId));

        // Update name and slug if changed
        if (!category.getName().equals(request.getName())) {
            String newSlug = SlugUtil.generateSlug(request.getName());
            if (!category.getSlug().equals(newSlug) && categoryRepository.existsBySlug(newSlug)) {
                throw new IllegalArgumentException("Category slug already exists: " + newSlug);
            }
            category.setName(request.getName());
            category.setSlug(newSlug);
        }

        // Update description
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        // Update parent category if provided and different
        if (request.getParentCategoryId() != null && !request.getParentCategoryId().equals(category.getParentCategoryId())) {
            if (!validateHierarchy(categoryId, request.getParentCategoryId())) {
                throw new IllegalArgumentException("Invalid parent category: creates circular reference");
            }
            category.setParentCategoryId(request.getParentCategoryId());
        }

        category = categoryRepository.save(category);
        log.info("Category updated successfully: {}", categoryId);

        return categoryMapper.toDTO(category);
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        log.debug("Deleting category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> TaxonomyNotFoundException.categoryNotFound(categoryId));

        categoryRepository.delete(category);
        log.info("Category deleted successfully: {}", categoryId);
    }

    @Override
    public void assignPostToCategory(Integer categoryId, Integer postId) {
        log.debug("Assigning post {} to category {}", postId, categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> TaxonomyNotFoundException.categoryNotFound(categoryId));

        categoryRepository.incrementPostCount(categoryId);
        log.info("Post {} assigned to category {}", postId, categoryId);
    }

    @Override
    public void removePostFromCategory(Integer categoryId, Integer postId) {
        log.debug("Removing post {} from category {}", postId, categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> TaxonomyNotFoundException.categoryNotFound(categoryId));

        categoryRepository.decrementPostCount(categoryId);
        log.info("Post {} removed from category {}", postId, categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> searchByName(String namePattern) {
        log.debug("Searching categories by name pattern: {}", namePattern);

        return categoryRepository.searchByName(namePattern)
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateHierarchy(Integer categoryId, Integer parentCategoryId) {
        log.debug("Validating hierarchy: category {} with parent {}", categoryId, parentCategoryId);

        if (categoryId.equals(parentCategoryId)) {
            return false; // A category cannot be its own parent
        }

        // Check for circular references by traversing up the hierarchy
        Integer currentParent = parentCategoryId;
        int maxDepth = 100; // Prevent infinite loops
        int depth = 0;

        while (currentParent != null && depth < maxDepth) {
            if (currentParent.equals(categoryId)) {
                return false; // Circular reference detected
            }

            Category parent = categoryRepository.findById(currentParent).orElse(null);
            if (parent == null) {
                break;
            }

            currentParent = parent.getParentCategoryId();
            depth++;
        }

        log.debug("Hierarchy validation passed for category {} with parent {}", categoryId, parentCategoryId);
        return true;
    }
}

