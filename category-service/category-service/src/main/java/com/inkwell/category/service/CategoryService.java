package com.inkwell.category.service;

import com.inkwell.category.dto.CategoryDTO;
import com.inkwell.category.dto.TaxonomyRequest;

import java.util.List;

/**
 * Service interface for Category operations.
 * Handles CRUD, hierarchy management, and post count tracking.
 */
public interface CategoryService {

    /**
     * Create a new category.
     *
     * @param request The category creation request
     * @return The created category DTO
     */
    CategoryDTO createCategory(TaxonomyRequest request);

    /**
     * Get a category by ID.
     *
     * @param categoryId The category ID
     * @return The category DTO
     */
    CategoryDTO getCategoryById(Integer categoryId);

    /**
     * Get a category by slug.
     *
     * @param slug The category slug
     * @return The category DTO
     */
    CategoryDTO getCategoryBySlug(String slug);

    /**
     * Get all top-level categories (no parent).
     *
     * @return List of top-level categories
     */
    List<CategoryDTO> getTopLevelCategories();

    /**
     * Get all child categories of a parent.
     *
     * @param parentCategoryId The parent category ID
     * @return List of child categories
     */
    List<CategoryDTO> getChildCategories(Integer parentCategoryId);

    /**
     * Get all categories.
     *
     * @return List of all categories
     */
    List<CategoryDTO> getAllCategories();

    /**
     * Update a category.
     *
     * @param categoryId The category ID
     * @param request The update request
     * @return The updated category DTO
     */
    CategoryDTO updateCategory(Integer categoryId, TaxonomyRequest request);

    /**
     * Delete a category.
     *
     * @param categoryId The category ID
     */
    void deleteCategory(Integer categoryId);

    /**
     * Associate a post with a category (increment post count).
     *
     * @param categoryId The category ID
     * @param postId The post ID
     */
    void assignPostToCategory(Integer categoryId, Integer postId);

    /**
     * Remove post association from a category (decrement post count).
     *
     * @param categoryId The category ID
     * @param postId The post ID
     */
    void removePostFromCategory(Integer categoryId, Integer postId);

    /**
     * Search categories by name.
     *
     * @param namePattern The search pattern
     * @return List of matching categories
     */
    List<CategoryDTO> searchByName(String namePattern);

    /**
     * Validate hierarchical structure (prevent circular references).
     *
     * @param categoryId The category ID
     * @param parentCategoryId The proposed parent category ID
     * @return true if valid, false otherwise
     */
    boolean validateHierarchy(Integer categoryId, Integer parentCategoryId);
}

