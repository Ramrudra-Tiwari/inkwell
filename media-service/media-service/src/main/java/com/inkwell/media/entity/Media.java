package com.inkwell.media.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Media Entity representing uploaded files.
 * Stores metadata about images, documents, and other media files.
 * Uses soft delete (isDeleted flag) to preserve relationships.
 */
@Entity
@Table(name = "media", indexes = {
    @Index(name = "idx_uploader_id", columnList = "uploader_id"),
    @Index(name = "idx_linked_post_id", columnList = "linked_post_id"),
    @Index(name = "idx_uploaded_at", columnList = "uploaded_at"),
    @Index(name = "idx_is_deleted", columnList = "is_deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer mediaId;

    /**
     * ID of the user who uploaded this media.
     * Maintained as Integer (microservice isolation).
     */
    @Column(nullable = false)
    private Integer uploaderId;

    /**
     * Generated unique filename (e.g., "uuid-original-filename.ext")
     * Prevents overwrites by using UUID prefix.
     */
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Filename cannot be blank")
    private String filename;

    /**
     * Original filename as uploaded by the user.
     */
    @Column(nullable = false)
    @NotBlank(message = "Original name cannot be blank")
    private String originalName;

    /**
     * Public URL to access the media file.
     * Format: /media/{mediaId}/download or external CDN URL
     */
    @Column(nullable = false)
    @NotBlank(message = "URL cannot be blank")
    private String url;

    /**
     * MIME type of the file (e.g., image/png, image/jpeg, image/gif, image/webp).
     * Validated during upload to ensure only allowed types.
     */
    @Column(nullable = false)
    @NotBlank(message = "MIME type cannot be blank")
    private String mimeType;

    /**
     * File size in kilobytes. Captured during upload.
     * Used for quota management and display purposes.
     */
    @Column(nullable = false)
    private Long sizeKb;

    /**
     * Alternative text for accessibility (WCAG compliance).
     * Useful for SEO and screen readers.
     */
    private String altText;

    /**
     * Reference to the post this media is linked to.
     * Nullable - media can exist without being linked to a post.
     * Maintained as Integer (microservice isolation).
     */
    @Column(name = "linked_post_id")
    private Integer linkedPostId;

    /**
     * Timestamp when the file was uploaded.
     * Set automatically via @PrePersist.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Soft delete flag. When true, treat as deleted but preserve record.
     * Prevents data loss and maintains referential integrity.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * JPA lifecycle callback to set uploadedAt timestamp on creation.
     */
    @PrePersist
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();
    }
}

