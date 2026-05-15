package com.inkwell.category.repository;

import com.inkwell.category.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Tag entity.
 * Provides custom queries for slug lookup, post count management, and trending tags.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    /**
     * Find a tag by its unique slug.
     *
     * @param slug The slug
     * @return Optional containing the tag if found
     */
    Optional<Tag> findBySlug(String slug);

    /**
     * Check if a tag with the given slug exists.
     *
     * @param slug The slug to check
     * @return true if exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Find the top N trending tags ordered by postCount (descending).
     * Used to show popular tags.
     *
     * @param limit The maximum number of tags to return
     * @return List of trending tags
     */
    @Query("SELECT t FROM Tag t ORDER BY t.postCount DESC LIMIT :limit")
    List<Tag> findTopTrendingTags(@Param("limit") int limit);

    /**
     * Atomically increment the post count for a tag.
     * Prevents race conditions during concurrent post tagging.
     *
     * @param tagId The tag ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Tag t SET t.postCount = t.postCount + 1 WHERE t.tagId = :tagId")
    void incrementPostCount(@Param("tagId") Integer tagId);

    /**
     * Atomically decrement the post count for a tag.
     * Prevents race conditions during concurrent post untagging.
     *
     * @param tagId The tag ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Tag t SET t.postCount = t.postCount - 1 WHERE t.tagId = :tagId AND t.postCount > 0")
    void decrementPostCount(@Param("tagId") Integer tagId);

    /**
     * Find tags by name pattern (case-insensitive).
     *
     * @param namePattern The name pattern to search
     * @return List of matching tags
     */
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :pattern, '%')) " +
           "ORDER BY t.name ASC")
    List<Tag> searchByName(@Param("pattern") String namePattern);
}

