package com.inkwell.media.util;

import com.inkwell.media.exception.FileStorageException;
import com.inkwell.media.exception.InvalidFileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Utility class for file storage operations.
 * Handles file validation, storage, and retrieval.
 * Enforces security constraints: file type restrictions and size limits.
 */
@Slf4j
public class FileStorageUtil {

    /**
     * Allowed MIME types for security.
     * Restricted to common image formats only.
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    );

    /**
     * Maximum file size in KB (10 MB = 10240 KB).
     */
    private static final long MAX_FILE_SIZE_KB = 10240L;

    /**
     * Default uploads directory.
     * Can be overridden via environment variable or configuration.
     */
    private static final String UPLOADS_DIRECTORY = "uploads";

    /**
     * Validate the uploaded file's MIME type and size.
     *
     * @param file the uploaded file
     * @throws InvalidFileTypeException if MIME type is not allowed
     * @throws InvalidFileTypeException if file size exceeds limit
     */
    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("File is empty or null");
        }

        String mimeType = file.getContentType();
        if (!isAllowedMimeType(mimeType)) {
            log.warn("File upload rejected: Invalid MIME type: {}", mimeType);
            throw InvalidFileTypeException.invalidFileType(mimeType);
        }

        long sizeKb = file.getSize() / 1024;
        if (sizeKb > MAX_FILE_SIZE_KB) {
            log.warn("File upload rejected: Size {} KB exceeds limit {} KB", sizeKb, MAX_FILE_SIZE_KB);
            throw InvalidFileTypeException.fileSizeExceeded(sizeKb);
        }

        log.debug("File validation passed: {} ({})", file.getOriginalFilename(), mimeType);
    }

    /**
     * Check if a MIME type is in the allowed list.
     *
     * @param mimeType the MIME type to check
     * @return true if allowed, false otherwise
     */
    public static boolean isAllowedMimeType(String mimeType) {
        return mimeType != null && ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase());
    }

    /**
     * Generate a unique filename to prevent overwrites.
     * Format: {uuid}-{original-filename}
     *
     * @param originalFilename the original filename from the upload
     * @return a unique filename with UUID prefix
     */
    public static String generateUniqueFilename(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String sanitizedName = sanitizeFilename(originalFilename);
        String uniqueFilename = uuid + "-" + sanitizedName;
        log.debug("Generated unique filename: {} from original: {}", uniqueFilename, originalFilename);
        return uniqueFilename;
    }

    /**
     * Sanitize filename by removing potentially dangerous characters.
     * Prevents path traversal and invalid characters.
     *
     * @param filename the filename to sanitize
     * @return sanitized filename
     */
    public static String sanitizeFilename(String filename) {
        // Remove any path separators and special characters
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Ensure the uploads directory exists, creating it if necessary.
     *
     * @throws FileStorageException if directory creation fails
     */
    public static void ensureUploadsDirectoryExists() {
        try {
            Path uploadsPath = Paths.get(UPLOADS_DIRECTORY);
            if (!Files.exists(uploadsPath)) {
                Files.createDirectories(uploadsPath);
                log.info("Created uploads directory: {}", uploadsPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create uploads directory", e);
            throw FileStorageException.directoryCreationFailed(UPLOADS_DIRECTORY);
        }
    }

    /**
     * Save a file to the uploads directory and return its path.
     *
     * @param file the file to save
     * @param uniqueFilename the unique filename to use
     * @return the relative path to the saved file
     * @throws FileStorageException if save operation fails
     */
    public static String saveFile(MultipartFile file, String uniqueFilename) {
        try {
            ensureUploadsDirectoryExists();
            Path targetPath = Paths.get(UPLOADS_DIRECTORY).resolve(uniqueFilename);
            Files.write(targetPath, file.getBytes());
            String relativePath = targetPath.toString();
            log.info("File saved successfully: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            log.error("Failed to save file: {}", uniqueFilename, e);
            throw FileStorageException.storageFailure(uniqueFilename);
        }
    }

    /**
     * Delete a file from the uploads directory.
     *
     * @param filename the filename to delete
     * @throws FileStorageException if delete operation fails
     */
    public static void deleteFile(String filename) {
        try {
            Path filePath = Paths.get(UPLOADS_DIRECTORY).resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", filename);
            } else {
                log.warn("File not found for deletion: {}", filename);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filename, e);
            // Note: We don't throw an exception here as soft delete already happened
            // in the database. Logging the error is sufficient.
        }
    }

    /**
     * Get the file size in kilobytes.
     *
     * @param file the multipart file
     * @return size in kilobytes
     */
    public static Long getFileSizeInKb(MultipartFile file) {
        return file.getSize() / 1024;
    }

    /**
     * Generate the public URL for accessing a media file.
     * Format: /api/v1/media/{mediaId}/download
     *
     * @param mediaId the ID of the media
     * @return the public URL
     */
    public static String generatePublicUrl(Integer mediaId) {
        return "/api/v1/media/" + mediaId + "/download";
    }

    /**
     * Get the file extension from the original filename.
     *
     * @param originalFilename the original filename
     * @return the file extension (without dot)
     */
    public static String getFileExtension(String originalFilename) {
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return originalFilename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}
