package com.inkwell.media.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for file upload operations.
 * Contains essential information about the uploaded file.
 * Used specifically in POST /media/upload endpoint responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    /**
     * Unique identifier assigned to the uploaded media.
     */
    private Integer mediaId;

    /**
     * Original filename as provided by the uploader.
     */
    private String originalName;

    /**
     * Public URL where the file can be accessed.
     * Clients should use this URL to reference the media.
     */
    private String url;

    /**
     * MIME type of the uploaded file (e.g., image/png, image/jpeg).
     * Validated against allowed types during upload.
     */
    private String mimeType;

    /**
     * Size of the file in kilobytes.
     * Useful for quota tracking and bandwidth management.
     */
    private Long sizeKb;

    /**
     * Timestamp when the file was successfully uploaded.
     */
    private LocalDateTime uploadedAt;

    /**
     * Informational message about the upload process.
     */
    private String message;

    /**
     * HTTP status code (201 Created, 400 Bad Request, etc.).
     */
    private Integer statusCode;
}

