package com.inkwell.category.repository;

import com.inkwell.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity.
 * Provides custom queries for slug lookup, hierarchy, and post count management.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Find a category by its unique slug.
     *
     * @param slug The slug
     * @return Optional containing the category if found
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Find all top-level categories (those without a parent).
     *
     * @return List of top-level categories
     */
    List<Category> findByParentCategoryIdIsNull();

    /**
     * Find all child categories of a specific parent.
     *
     * @param parentCategoryId The parent category ID
     * @return List of child categories
     */
    List<Category> findByParentCategoryId(Integer parentCategoryId);

    /**
     * Check if a category with the given slug exists.
     *
     * @param slug The slug to check
     * @return true if exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Atomically increment the post count for a category.
     * Prevents race conditions during concurrent post assignments.
     *
     * @param categoryId The category ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.postCount = c.postCount + 1 WHERE c.categoryId = :categoryId")
    void incrementPostCount(@Param("categoryId") Integer categoryId);

    /**
     * Atomically decrement the post count for a category.
     * Prevents race conditions during concurrent post removals.
     *
     * @param categoryId The category ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.postCount = c.postCount - 1 WHERE c.categoryId = :categoryId AND c.postCount > 0")
    void decrementPostCount(@Param("categoryId") Integer categoryId);

    /**
     * Find categories by name pattern (case-insensitive).
     *
     * @param namePattern The name pattern to search
     * @return List of matching categories
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :pattern, '%')) " +
           "ORDER BY c.name ASC")
    List<Category> searchByName(@Param("pattern") String namePattern);
}

