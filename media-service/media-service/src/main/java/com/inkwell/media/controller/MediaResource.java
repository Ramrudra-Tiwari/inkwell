package com.inkwell.media.controller;

import com.inkwell.media.dto.MediaDTO;
import com.inkwell.media.dto.UploadResponse;
import com.inkwell.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for media file operations.
 * Exposes endpoints for file upload, retrieval, linking, and deletion.
 * All endpoints are protected by service-to-service authentication.
 */
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media Service", description = "APIs for managing media files (images, documents)")
public class MediaResource {

    private final MediaService mediaService;

    /**
     * Upload a new media file.
     * Accepts multipart form data with file and metadata.
     * Validates file type (JPEG, PNG, GIF, WebP) and size (max 10MB).
     *
     * @param file the file to upload
     * @param uploaderId the ID of the uploader (from auth context)
     * @param altText optional alternative text for accessibility
     * @return upload response with file metadata and public URL
     */
    @PostMapping("/upload")
    @Operation(
        summary = "Upload a media file",
        description = "Upload an image or document. Allowed types: JPEG, PNG, GIF, WebP. Max size: 10MB"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file type or size exceeds limit"),
        @ApiResponse(responseCode = "500", description = "Server error during file storage")
    })
    public ResponseEntity<UploadResponse> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploaderId") Integer uploaderId,
            @RequestParam(value = "altText", required = false) String altText) {

        log.info("File upload request: {} from uploader: {}", file.getOriginalFilename(), uploaderId);
        UploadResponse response = mediaService.uploadMedia(file, uploaderId, altText);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieve media metadata by ID.
     *
     * @param mediaId the ID of the media
     * @return media DTO with full metadata
     */
    @GetMapping("/{mediaId}")
    @Operation(
        summary = "Get media metadata",
        description = "Retrieve metadata for a specific media file"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Media metadata retrieved"),
        @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<MediaDTO> getMedia(
            @Parameter(description = "Media ID")
            @PathVariable Integer mediaId) {

        log.debug("Fetching media: {}", mediaId);
        MediaDTO mediaDTO = mediaService.getMediaById(mediaId);
        return ResponseEntity.ok(mediaDTO);
    }

    /**
     * Retrieve media by filename.
     *
     * @param filename the unique filename
     * @return media DTO
     */
    @GetMapping("/filename/{filename}")
    @Operation(
        summary = "Get media by filename",
        description = "Retrieve media metadata by unique filename"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Media found"),
        @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<MediaDTO> getMediaByFilename(
            @Parameter(description = "Unique filename")
            @PathVariable String filename) {

        log.debug("Fetching media by filename: {}", filename);
        MediaDTO mediaDTO = mediaService.getMediaByFilename(filename);
        return ResponseEntity.ok(mediaDTO);
    }

    /**
     * Get all media files uploaded by a user.
     *
     * @param uploaderId the ID of the uploader
     * @return list of media DTOs
     */
    @GetMapping("/uploader/{uploaderId}")
    @Operation(
        summary = "Get media by uploader",
        description = "Retrieve all media files uploaded by a specific user"
    )
    @ApiResponse(responseCode = "200", description = "List of media files")
    public ResponseEntity<List<MediaDTO>> getMediaByUploader(
            @Parameter(description = "Uploader ID")
            @PathVariable Integer uploaderId) {

        log.debug("Fetching media for uploader: {}", uploaderId);
        List<MediaDTO> mediaList = mediaService.getMediaByUploaderId(uploaderId);
        return ResponseEntity.ok(mediaList);
    }

    /**
     * Get all media files linked to a post.
     *
     * @param postId the ID of the post
     * @return list of media DTOs
     */
    @GetMapping("/post/{postId}")
    @Operation(
        summary = "Get media by post",
        description = "Retrieve all media files linked to a specific post"
    )
    @ApiResponse(responseCode = "200", description = "List of media files")
    public ResponseEntity<List<MediaDTO>> getMediaByPost(
            @Parameter(description = "Post ID")
            @PathVariable Integer postId) {

        log.debug("Fetching media for post: {}", postId);
        List<MediaDTO> mediaList = mediaService.getMediaByPostId(postId);
        return ResponseEntity.ok(mediaList);
    }

    /**
     * Link a media file to a post.
     *
     * @param mediaId the ID of the media
     * @param postId the ID of the post to link
     */
    @PutMapping("/{mediaId}/link/{postId}")
    @Operation(
        summary = "Link media to a post",
        description = "Associate a media file with a specific post"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Media linked successfully"),
        @ApiResponse(responseCode = "404", description = "Media or post not found")
    })
    public ResponseEntity<Void> linkToPost(
            @Parameter(description = "Media ID")
            @PathVariable Integer mediaId,
            @Parameter(description = "Post ID")
            @PathVariable Integer postId) {

        log.info("Linking media {} to post {}", mediaId, postId);
        mediaService.linkToPost(mediaId, postId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Unlink a media file from any post.
     *
     * @param mediaId the ID of the media
     */
    @PutMapping("/{mediaId}/unlink")
    @Operation(
        summary = "Unlink media from post",
        description = "Remove association between media and post"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Media unlinked successfully"),
        @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Void> unlinkFromPost(
            @Parameter(description = "Media ID")
            @PathVariable Integer mediaId) {

        log.info("Unlinking media {} from post", mediaId);
        mediaService.unlinkFromPost(mediaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Soft delete a media file.
     * The file is marked as deleted but preserved in the database.
     *
     * @param mediaId the ID of the media to delete
     */
    @DeleteMapping("/{mediaId}")
    @Operation(
        summary = "Delete media (soft delete)",
        description = "Mark a media file as deleted. The record is preserved for audit purposes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Media deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Void> deleteMedia(
            @Parameter(description = "Media ID")
            @PathVariable Integer mediaId) {

        log.info("Deleting media: {}", mediaId);
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Download a media file.
     * Returns the file content with appropriate Content-Type header.
     *
     * @param mediaId the ID of the media to download
     * @return file content as byte array with appropriate headers
     */
    @GetMapping("/{mediaId}/download")
    @Operation(
        summary = "Download media file",
        description = "Download the actual file content"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File content returned"),
        @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<byte[]> downloadMedia(
            @Parameter(description = "Media ID")
            @PathVariable Integer mediaId) {

        log.info("Downloading media: {}", mediaId);
        MediaDTO media = mediaService.getMediaById(mediaId);
        byte[] fileContent = mediaService.downloadMedia(mediaId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + media.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(media.getMimeType()))
                .body(fileContent);
    }

    /**
     * Get all media files in the system.
     *
     * @return list of all media DTOs
     */
    @GetMapping("/all")
    @Operation(
        summary = "Get all media files",
        description = "Retrieve metadata for all non-deleted media files in the platform"
    )
    @ApiResponse(responseCode = "200", description = "List of all media files")
    public ResponseEntity<List<MediaDTO>> getAllMedia() {
        log.debug("Fetching all platform media");
        List<MediaDTO> mediaList = mediaService.getAllMedia();
        return ResponseEntity.ok(mediaList);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Media Service is running 🚀");
    }
}
