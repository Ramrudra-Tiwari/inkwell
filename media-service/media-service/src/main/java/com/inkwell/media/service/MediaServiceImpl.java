package com.inkwell.media.service;

import com.inkwell.media.dto.MediaDTO;
import com.inkwell.media.dto.UploadResponse;
import com.inkwell.media.entity.Media;
import com.inkwell.media.exception.FileStorageException;
import com.inkwell.media.exception.MediaNotFoundException;
import com.inkwell.media.mapper.MediaMapper;
import com.inkwell.media.repository.MediaRepository;
import com.inkwell.media.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of MediaService.
 * Handles file uploads, storage, metadata management, and soft deletion.
 * Enforces file type restrictions (JPEG, PNG, GIF, WebP) and 10MB size limit.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    private Path resolveStoredFilePath(String filename) {
        List<Path> candidates = new ArrayList<>();
        candidates.add(Paths.get("uploads", filename));
        candidates.add(Paths.get("..", "..", "uploads", filename).normalize());

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        return candidates.get(0);
    }

    @Override
    public UploadResponse uploadMedia(MultipartFile file, Integer uploaderId, String altText) {
        log.debug("Processing file upload for uploader: {}, filename: {}", uploaderId, file.getOriginalFilename());

        // Validate file type and size
        FileStorageUtil.validateFile(file);

        // Generate unique filename
        String uniqueFilename = FileStorageUtil.generateUniqueFilename(file.getOriginalFilename());

        // Save file to filesystem
        String filePath = FileStorageUtil.saveFile(file, uniqueFilename);

        // Get file size in KB
        Long sizeKb = FileStorageUtil.getFileSizeInKb(file);

        // Generate public URL
        String publicUrl = FileStorageUtil.generatePublicUrl(null); // Will be set after saving to DB

        // Create media entity
        Media media = Media.builder()
                .uploaderId(uploaderId)
                .filename(uniqueFilename)
                .originalName(file.getOriginalFilename())
                .url(publicUrl)
                .mimeType(file.getContentType())
                .sizeKb(sizeKb)
                .altText(altText != null ? altText : "")
                .isDeleted(false)
                .build();

        // Save to database
        media = mediaRepository.save(media);

        // Update URL with actual media ID
        String finalUrl = FileStorageUtil.generatePublicUrl(media.getMediaId());
        media.setUrl(finalUrl);
        mediaRepository.save(media);

        log.info("File uploaded successfully: {} (ID: {}, Size: {} KB)", 
                 uniqueFilename, media.getMediaId(), sizeKb);

        // Build and return upload response
        return UploadResponse.builder()
                .mediaId(media.getMediaId())
                .originalName(media.getOriginalName())
                .url(finalUrl)
                .mimeType(media.getMimeType())
                .sizeKb(sizeKb)
                .uploadedAt(media.getUploadedAt())
                .message("File uploaded successfully")
                .statusCode(201)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MediaDTO getMediaById(Integer mediaId) {
        log.debug("Fetching media by ID: {}", mediaId);

        Media media = mediaRepository.findActiveMediaById(mediaId)
                .orElseThrow(() -> MediaNotFoundException.mediaNotFound(mediaId));

        return mediaMapper.toDTO(media);
    }

    @Override
    @Transactional(readOnly = true)
    public MediaDTO getMediaByFilename(String filename) {
        log.debug("Fetching media by filename: {}", filename);

        Media media = mediaRepository.findByFilename(filename)
                .orElseThrow(() -> MediaNotFoundException.mediaNotFoundByFilename(filename));

        // Check if soft-deleted
        if (media.getIsDeleted()) {
            throw MediaNotFoundException.mediaNotFoundByFilename(filename);
        }

        return mediaMapper.toDTO(media);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaDTO> getMediaByUploaderId(Integer uploaderId) {
        log.debug("Fetching media for uploader: {}", uploaderId);

        return mediaRepository.findByUploaderId(uploaderId)
                .stream()
                .map(mediaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaDTO> getMediaByPostId(Integer postId) {
        log.debug("Fetching media linked to post: {}", postId);

        return mediaRepository.findByLinkedPostId(postId)
                .stream()
                .map(mediaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void linkToPost(Integer mediaId, Integer postId) {
        log.debug("Linking media {} to post {}", mediaId, postId);

        // Verify media exists and is not deleted
        Media media = mediaRepository.findActiveMediaById(mediaId)
                .orElseThrow(() -> MediaNotFoundException.mediaNotFound(mediaId));

        mediaRepository.linkToPost(mediaId, postId);
        log.info("Media {} linked to post {}", mediaId, postId);
    }

    @Override
    public void unlinkFromPost(Integer mediaId) {
        log.debug("Unlinking media {} from post", mediaId);

        // Verify media exists
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> MediaNotFoundException.mediaNotFound(mediaId));

        mediaRepository.unlinkFromPost(mediaId);
        log.info("Media {} unlinked from post", mediaId);
    }

    @Override
    public void deleteMedia(Integer mediaId) {
        log.debug("Soft deleting media: {}", mediaId);

        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> MediaNotFoundException.mediaNotFound(mediaId));

        // Perform soft delete
        mediaRepository.softDeleteById(mediaId);

        // Delete physical file from filesystem asynchronously to prevent blocking
        try {
            Path filePath = resolveStoredFilePath(media.getFilename());
            Files.deleteIfExists(filePath);
            log.info("Media soft deleted: {} (ID: {})", media.getFilename(), mediaId);
        } catch (Exception e) {
            log.error("Error deleting physical file for media {}: {}", mediaId, e.getMessage());
            // Continue execution - soft delete in database already completed
        }
    }

    @Override
    public void permanentlyDeleteMedia(Integer mediaId) {
        log.warn("Permanently deleting media: {}", mediaId);

        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> MediaNotFoundException.mediaNotFound(mediaId));

        // Delete physical file
        try {
            Files.deleteIfExists(resolveStoredFilePath(media.getFilename()));
        } catch (IOException e) {
            log.error("Failed to delete physical file for media {}: {}", mediaId, e.getMessage());
        }

        // Hard delete from database
        mediaRepository.deleteById(mediaId);
        log.info("Media permanently deleted: {} (ID: {})", media.getFilename(), mediaId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadMedia(Integer mediaId) {
        log.debug("Downloading media: {}", mediaId);

        Media media = mediaRepository.findActiveMediaById(mediaId)
                .orElseThrow(() -> MediaNotFoundException.mediaNotFound(mediaId));

        try {
            Path filePath = resolveStoredFilePath(media.getFilename());
            byte[] fileContent = Files.readAllBytes(filePath);
            log.info("Media downloaded: {} (Size: {} bytes)", mediaId, fileContent.length);
            return fileContent;
        } catch (IOException e) {
            log.error("Error downloading media {}: {}", mediaId, e.getMessage());
            throw FileStorageException.storageFailure(media.getFilename());
        }
    }
    @Override
    @Transactional(readOnly = true)
    public List<MediaDTO> getAllMedia() {
        log.debug("Fetching all media files");
        return mediaRepository.findAllActiveMedia().stream()
                .map(mediaMapper::toDTO)
                .collect(Collectors.toList());
    }
}

