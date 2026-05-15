package com.inkwell.media.exception;

/**
 * Exception thrown when a file storage operation fails.
 * This includes I/O errors, directory creation failures, and disk space issues.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method for storage failures.
     *
     * @param filename the filename that failed to store
     * @return a new FileStorageException
     */
    public static FileStorageException storageFailure(String filename) {
        return new FileStorageException("Failed to store file: " + filename);
    }

    /**
     * Factory method for directory creation failures.
     *
     * @param directoryPath the path that failed to create
     * @return a new FileStorageException
     */
    public static FileStorageException directoryCreationFailed(String directoryPath) {
        return new FileStorageException("Failed to create directory: " + directoryPath);
    }
}

