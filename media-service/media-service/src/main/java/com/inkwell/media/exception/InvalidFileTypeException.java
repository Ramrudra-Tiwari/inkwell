package com.inkwell.media.exception;

/**
 * Exception thrown when an uploaded file type is not allowed.
 * Only JPEG, PNG, GIF, and WebP are permitted for security and optimization.
 */
public class InvalidFileTypeException extends RuntimeException {

    public InvalidFileTypeException(String message) {
        super(message);
    }

    public InvalidFileTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method for creating invalid file type exceptions.
     *
     * @param mimeType the MIME type that was rejected
     * @return a new InvalidFileTypeException
     */
    public static InvalidFileTypeException invalidFileType(String mimeType) {
        return new InvalidFileTypeException(
            "File type not allowed: " + mimeType + ". Allowed types: image/jpeg, image/png, image/gif, image/webp"
        );
    }

    /**
     * Factory method for file size violations.
     *
     * @param sizeKb the size in kilobytes
     * @return a new InvalidFileTypeException
     */
    public static InvalidFileTypeException fileSizeExceeded(Long sizeKb) {
        return new InvalidFileTypeException(
            "File size (" + sizeKb + " KB) exceeds maximum allowed size of 10240 KB (10 MB)"
        );
    }
}

