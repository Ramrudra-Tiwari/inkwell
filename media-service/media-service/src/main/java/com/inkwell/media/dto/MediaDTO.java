package com.inkwell.media.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Media entity.
 * Used for API responses when retrieving media information.
 * Does not contain sensitive file system paths.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDTO {

    /**
     * Unique identifier for the media file.
     */
    private Integer mediaId;

    /**
     * ID of the user who uploaded this media.
     */
    private Integer uploaderId;

    /**
     * Generated unique filename (used for reference, not in URL).
     */
    private String filename;

    /**
     * Original filename as uploaded by the user.
     */
    private String originalName;

    /**
     * Public URL to access the media file.
     */
    private String url;

    /**
     * MIME type of the file (e.g., image/png).
     */
    private String mimeType;

    /**
     * File size in kilobytes.
     */
    private Long sizeKb;

    /**
     * Alternative text for accessibility.
     */
    private String altText;

    /**
     * ID of the post this media is linked to (if any).
     */
    private Integer linkedPostId;

    /**
     * Timestamp when the file was uploaded.
     */
    private LocalDateTime uploadedAt;

    /**
     * Indicates whether the media is soft-deleted.
     */
    private Boolean isDeleted;
}

