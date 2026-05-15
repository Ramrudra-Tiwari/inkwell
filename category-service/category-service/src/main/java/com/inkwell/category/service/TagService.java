package com.inkwell.category.service;

import com.inkwell.category.dto.TagDTO;
import com.inkwell.category.dto.TaxonomyRequest;

import java.util.List;

/**
 * Service interface for Tag operations.
 * Handles CRUD, post count tracking, and trending tags.
 */
public interface TagService {

    /**
     * Create a new tag.
     *
     * @param request The tag creation request
     * @return The created tag DTO
     */
    TagDTO createTag(TaxonomyRequest request);

    /**
     * Get a tag by ID.
     *
     * @param tagId The tag ID
     * @return The tag DTO
     */
    TagDTO getTagById(Integer tagId);

    /**
     * Get a tag by slug.
     *
     * @param slug The tag slug
     * @return The tag DTO
     */
    TagDTO getTagBySlug(String slug);

    /**
     * Get all tags.
     *
     * @return List of all tags
     */
    List<TagDTO> getAllTags();

    /**
     * Get the top N trending tags ordered by post count.
     *
     * @param limit The number of tags to return
     * @return List of trending tags
     */
    List<TagDTO> getTrendingTags(int limit);

    /**
     * Update a tag.
     *
     * @param tagId The tag ID
     * @param request The update request
     * @return The updated tag DTO
     */
    TagDTO updateTag(Integer tagId, TaxonomyRequest request);

    /**
     * Delete a tag.
     *
     * @param tagId The tag ID
     */
    void deleteTag(Integer tagId);

    /**
     * Associate a post with a tag (increment post count).
     *
     * @param tagId The tag ID
     * @param postId The post ID
     */
    void assignPostToTag(Integer tagId, Integer postId);

    /**
     * Remove post association from a tag (decrement post count).
     *
     * @param tagId The tag ID
     * @param postId The post ID
     */
    void removePostFromTag(Integer tagId, Integer postId);

    /**
     * Search tags by name.
     *
     * @param namePattern The search pattern
     * @return List of matching tags
     */
    List<TagDTO> searchByName(String namePattern);
}

