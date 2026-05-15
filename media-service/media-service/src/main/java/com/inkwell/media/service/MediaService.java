package com.inkwell.media.service;

import com.inkwell.media.dto.MediaDTO;
import com.inkwell.media.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for media operations.
 * Defines contracts for file upload, retrieval, linking, and soft deletion.
 */
public interface MediaService {

    /**
     * Upload a new media file.
     * Validates file type and size, saves to filesystem, and stores metadata in database.
     *
     * @param file the file to upload
     * @param uploaderId the ID of the uploader
     * @param altText optional alternative text for accessibility
     * @return upload response with file metadata
     */
    UploadResponse uploadMedia(MultipartFile file, Integer uploaderId, String altText);

    /**
     * Retrieve media metadata by ID.
     * Excludes soft-deleted media.
     *
     * @param mediaId the ID of the media
     * @return the media DTO
     */
    MediaDTO getMediaById(Integer mediaId);

    /**
     * Retrieve media by unique filename.
     *
     * @param filename the unique filename
     * @return the media DTO
     */
    MediaDTO getMediaByFilename(String filename);

    /**
     * Get all media files uploaded by a user.
     * Excludes soft-deleted files.
     *
     * @param uploaderId the ID of the uploader
     * @return list of media DTOs
     */
    List<MediaDTO> getMediaByUploaderId(Integer uploaderId);

    /**
     * Get all media files linked to a specific post.
     * Excludes soft-deleted files.
     *
     * @param postId the ID of the post
     * @return list of media DTOs
     */
    List<MediaDTO> getMediaByPostId(Integer postId);

    /**
     * Link a media file to a post.
     *
     * @param mediaId the ID of the media
     * @param postId the ID of the post to link
     */
    void linkToPost(Integer mediaId, Integer postId);

    /**
     * Unlink a media file from any post.
     *
     * @param mediaId the ID of the media
     */
    void unlinkFromPost(Integer mediaId);

    /**
     * Soft delete a media file by marking it as deleted.
     * Preserves the database record for audit purposes.
     * The physical file is also deleted from the filesystem.
     *
     * @param mediaId the ID of the media to delete
     */
    void deleteMedia(Integer mediaId);

    /**
     * Permanently delete a media file (hard delete).
     * Use with caution as this cannot be undone.
     *
     * @param mediaId the ID of the media to permanently delete
     */
    void permanentlyDeleteMedia(Integer mediaId);

    /**
     * Download a media file by its ID.
     * Returns the file content as a byte array.
     *
     * @param mediaId the ID of the media
     * @return the file content as bytes
     */
    byte[] downloadMedia(Integer mediaId);
    /**
     * Get all media files in the system.
     * Excludes soft-deleted files.
     *
     * @return list of all media DTOs
     */
    List<MediaDTO> getAllMedia();
}

