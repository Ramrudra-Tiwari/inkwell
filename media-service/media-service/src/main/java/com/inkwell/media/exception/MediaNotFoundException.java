package com.inkwell.media.exception;

/**
 * Exception thrown when a requested media file is not found.
 */
public class MediaNotFoundException extends RuntimeException {

    public MediaNotFoundException(String message) {
        super(message);
    }

    public MediaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method for creating not found exceptions.
     *
     * @param mediaId the ID of the media that was not found
     * @return a new MediaNotFoundException
     */
    public static MediaNotFoundException mediaNotFound(Integer mediaId) {
        return new MediaNotFoundException("Media not found with ID: " + mediaId);
    }

    /**
     * Factory method for creating not found exceptions by filename.
     *
     * @param filename the filename that was not found
     * @return a new MediaNotFoundException
     */
    public static MediaNotFoundException mediaNotFoundByFilename(String filename) {
        return new MediaNotFoundException("Media not found with filename: " + filename);
    }
}

