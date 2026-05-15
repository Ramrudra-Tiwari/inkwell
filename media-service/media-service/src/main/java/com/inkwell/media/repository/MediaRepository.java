package com.inkwell.media.repository;

import com.inkwell.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Media entity.
 * Provides custom queries for file retrieval, soft deletion, and post associations.
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, Integer> {

    /**
     * Find a media file by its unique filename.
     *
     * @param filename the unique generated filename
     * @return Optional containing the media if found
     */
    Optional<Media> findByFilename(String filename);

    /**
     * Find all media files uploaded by a specific user.
     * Excludes soft-deleted files.
     *
     * @param uploaderId the ID of the uploader
     * @return list of media files
     */
    @Query("SELECT m FROM Media m WHERE m.uploaderId = :uploaderId AND m.isDeleted = false")
    List<Media> findByUploaderId(@Param("uploaderId") Integer uploaderId);

    /**
     * Find all media files linked to a specific post.
     * Excludes soft-deleted files.
     *
     * @param linkedPostId the ID of the post
     * @return list of media files linked to the post
     */
    @Query("SELECT m FROM Media m WHERE m.linkedPostId = :linkedPostId AND m.isDeleted = false")
    List<Media> findByLinkedPostId(@Param("linkedPostId") Integer linkedPostId);

    /**
     * Find a media file by ID, excluding soft-deleted files.
     *
     * @param mediaId the media ID
     * @return Optional containing the media if found and not deleted
     */
    @Query("SELECT m FROM Media m WHERE m.mediaId = :mediaId AND m.isDeleted = false")
    Optional<Media> findActiveMediaById(@Param("mediaId") Integer mediaId);

    /**
     * Soft delete a media file by setting isDeleted to true.
     * Preserves the database record for audit/referential purposes.
     *
     * @param mediaId the ID of the media to delete
     */
    @Modifying
    @Transactional
    @Query("UPDATE Media m SET m.isDeleted = true WHERE m.mediaId = :mediaId")
    void softDeleteById(@Param("mediaId") Integer mediaId);

    /**
     * Link a media file to a post by updating linkedPostId.
     * Atomic operation for thread safety.
     *
     * @param mediaId the ID of the media
     * @param postId the ID of the post to link
     */
    @Modifying
    @Transactional
    @Query("UPDATE Media m SET m.linkedPostId = :postId WHERE m.mediaId = :mediaId")
    void linkToPost(@Param("mediaId") Integer mediaId, @Param("postId") Integer postId);

    /**
     * Unlink a media file from any post by setting linkedPostId to null.
     *
     * @param mediaId the ID of the media
     */
    @Modifying
    @Transactional
    @Query("UPDATE Media m SET m.linkedPostId = null WHERE m.mediaId = :mediaId")
    void unlinkFromPost(@Param("mediaId") Integer mediaId);

    /**
     * Check if a filename already exists in the system.
     * Used to detect duplicate uploads.
     *
     * @param filename the filename to check
     * @return true if filename exists, false otherwise
     */
    boolean existsByFilename(String filename);

    /**
     * Count total media files uploaded by a user (including soft-deleted).
     *
     * @param uploaderId the ID of the uploader
     * @return count of media files
     */
    @Query("SELECT COUNT(m) FROM Media m WHERE m.uploaderId = :uploaderId")
    long countByUploaderId(@Param("uploaderId") Integer uploaderId);

    /**
     * Count active (non-deleted) media files linked to a post.
     *
     * @param linkedPostId the ID of the post
     * @return count of active media files
     */
    @Query("SELECT COUNT(m) FROM Media m WHERE m.linkedPostId = :linkedPostId AND m.isDeleted = false")
    long countActiveByLinkedPostId(@Param("linkedPostId") Integer linkedPostId);

    @Query("SELECT m FROM Media m WHERE m.isDeleted = false ORDER BY m.uploadedAt DESC")
    List<Media> findAllActiveMedia();
}

