package com.inkwell.category.controller;

import com.inkwell.category.dto.CategoryDTO;
import com.inkwell.category.dto.PostAssociationRequest;
import com.inkwell.category.dto.TagDTO;
import com.inkwell.category.dto.TaxonomyRequest;
import com.inkwell.category.service.CategoryService;
import com.inkwell.category.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Category and Tag operations.
 * Provides endpoints for managing hierarchical categories and flat tags.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category & Tag Service", description = "API endpoints for managing categories and tags")
public class CategoryResource {

    private final CategoryService categoryService;
    private final TagService tagService;

    // ==========================================
    // CATEGORY ENDPOINTS
    // ==========================================

    @PostMapping("/categories")
    @Operation(summary = "Create a new category", description = "Creates a new category with optional parent for hierarchy")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Category slug already exists")
    })
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody TaxonomyRequest request) {
        log.info("Creating category: {}", request.getName());
        CategoryDTO category = categoryService.createCategory(request);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    @GetMapping("/categories/{categoryId}")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by its unique ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Integer categoryId) {
        log.debug("Fetching category by ID: {}", categoryId);
        CategoryDTO category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/categories/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieves a category by its unique slug")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryDTO> getCategoryBySlug(
            @Parameter(description = "Category slug") @PathVariable String slug) {
        log.debug("Fetching category by slug: {}", slug);
        CategoryDTO category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieves all categories in the system")
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        log.debug("Fetching all categories");
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/top-level")
    @Operation(summary = "Get top-level categories", description = "Retrieves categories that have no parent (root categories)")
    @ApiResponse(responseCode = "200", description = "Top-level categories retrieved successfully")
    public ResponseEntity<List<CategoryDTO>> getTopLevelCategories() {
        log.debug("Fetching top-level categories");
        List<CategoryDTO> categories = categoryService.getTopLevelCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{parentCategoryId}/children")
    @Operation(summary = "Get child categories", description = "Retrieves all child categories of a specific parent")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Child categories retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Parent category not found")
    })
    public ResponseEntity<List<CategoryDTO>> getChildCategories(
            @Parameter(description = "Parent category ID") @PathVariable Integer parentCategoryId) {
        log.debug("Fetching child categories for parent: {}", parentCategoryId);
        List<CategoryDTO> categories = categoryService.getChildCategories(parentCategoryId);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/categories/{categoryId}")
    @Operation(summary = "Update category", description = "Updates an existing category's name, description, or parent")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category slug already exists")
    })
    public ResponseEntity<CategoryDTO> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Integer categoryId,
            @Valid @RequestBody TaxonomyRequest request) {
        log.info("Updating category: {}", categoryId);
        CategoryDTO category = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/categories/{categoryId}")
    @Operation(summary = "Delete category", description = "Deletes a category from the system")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Integer categoryId) {
        log.info("Deleting category: {}", categoryId);
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories/search")
    @Operation(summary = "Search categories", description = "Searches categories by name pattern")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    public ResponseEntity<List<CategoryDTO>> searchCategories(
            @Parameter(description = "Name search pattern") @RequestParam String name) {
        log.debug("Searching categories by name: {}", name);
        List<CategoryDTO> categories = categoryService.searchByName(name);
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/categories/{categoryId}/posts")
    @Operation(summary = "Assign post to category", description = "Associates a post with a category and increments post count")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Post assigned successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> assignPostToCategory(
            @Parameter(description = "Category ID") @PathVariable Integer categoryId,
            @Valid @RequestBody PostAssociationRequest request) {
        log.info("Assigning post {} to category {}", request.getPostId(), categoryId);
        categoryService.assignPostToCategory(categoryId, request.getPostId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/categories/{categoryId}/posts/{postId}")
    @Operation(summary = "Remove post from category", description = "Removes post association from category and decrements post count")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Post removed successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> removePostFromCategory(
            @Parameter(description = "Category ID") @PathVariable Integer categoryId,
            @Parameter(description = "Post ID") @PathVariable Integer postId) {
        log.info("Removing post {} from category {}", postId, categoryId);
        categoryService.removePostFromCategory(categoryId, postId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // TAG ENDPOINTS
    // ==========================================

    @PostMapping("/tags")
    @Operation(summary = "Create a new tag", description = "Creates a new tag for post tagging")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tag created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Tag slug already exists")
    })
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TaxonomyRequest request) {
        log.info("Creating tag: {}", request.getName());
        TagDTO tag = tagService.createTag(request);
        return new ResponseEntity<>(tag, HttpStatus.CREATED);
    }

    @GetMapping("/tags/{tagId}")
    @Operation(summary = "Get tag by ID", description = "Retrieves a tag by its unique ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag found"),
        @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public ResponseEntity<TagDTO> getTagById(
            @Parameter(description = "Tag ID") @PathVariable Integer tagId) {
        log.debug("Fetching tag by ID: {}", tagId);
        TagDTO tag = tagService.getTagById(tagId);
        return ResponseEntity.ok(tag);
    }

    @GetMapping("/tags/slug/{slug}")
    @Operation(summary = "Get tag by slug", description = "Retrieves a tag by its unique slug")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag found"),
        @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public ResponseEntity<TagDTO> getTagBySlug(
            @Parameter(description = "Tag slug") @PathVariable String slug) {
        log.debug("Fetching tag by slug: {}", slug);
        TagDTO tag = tagService.getTagBySlug(slug);
        return ResponseEntity.ok(tag);
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all tags", description = "Retrieves all tags in the system")
    @ApiResponse(responseCode = "200", description = "Tags retrieved successfully")
    public ResponseEntity<List<TagDTO>> getAllTags() {
        log.debug("Fetching all tags");
        List<TagDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/tags/trending")
    @Operation(summary = "Get trending tags", description = "Retrieves the top trending tags ordered by post count")
    @ApiResponse(responseCode = "200", description = "Trending tags retrieved successfully")
    public ResponseEntity<List<TagDTO>> getTrendingTags(
            @Parameter(description = "Number of tags to return", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Fetching trending tags, limit: {}", limit);
        List<TagDTO> tags = tagService.getTrendingTags(limit);
        return ResponseEntity.ok(tags);
    }

    @PutMapping("/tags/{tagId}")
    @Operation(summary = "Update tag", description = "Updates an existing tag's name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Tag not found"),
        @ApiResponse(responseCode = "409", description = "Tag slug already exists")
    })
    public ResponseEntity<TagDTO> updateTag(
            @Parameter(description = "Tag ID") @PathVariable Integer tagId,
            @Valid @RequestBody TaxonomyRequest request) {
        log.info("Updating tag: {}", tagId);
        TagDTO tag = tagService.updateTag(tagId, request);
        return ResponseEntity.ok(tag);
    }

    @DeleteMapping("/tags/{tagId}")
    @Operation(summary = "Delete tag", description = "Deletes a tag from the system")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "Tag ID") @PathVariable Integer tagId) {
        log.info("Deleting tag: {}", tagId);
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tags/search")
    @Operation(summary = "Search tags", description = "Searches tags by name pattern")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    public ResponseEntity<List<TagDTO>> searchTags(
            @Parameter(description = "Name search pattern") @RequestParam String name) {
        log.debug("Searching tags by name: {}", name);
        List<TagDTO> tags = tagService.searchByName(name);
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/tags/{tagId}/posts")
    @Operation(summary = "Assign post to tag", description = "Associates a post with a tag and increments post count")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Post assigned successfully"),
        @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public ResponseEntity<Void> assignPostToTag(
            @Parameter(description = "Tag ID") @PathVariable Integer tagId,
            @Valid @RequestBody PostAssociationRequest request) {
        log.info("Assigning post {} to tag {}", request.getPostId(), tagId);
        tagService.assignPostToTag(tagId, request.getPostId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/tags/{tagId}/posts/{postId}")
    @Operation(summary = "Remove post from tag", description = "Removes post association from tag and decrements post count")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Post removed successfully"),
        @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public ResponseEntity<Void> removePostFromTag(
            @Parameter(description = "Tag ID") @PathVariable Integer tagId,
            @Parameter(description = "Post ID") @PathVariable Integer postId) {
        log.info("Removing post {} from tag {}", postId, tagId);
        tagService.removePostFromTag(tagId, postId);
        return ResponseEntity.noContent().build();
    }
}

