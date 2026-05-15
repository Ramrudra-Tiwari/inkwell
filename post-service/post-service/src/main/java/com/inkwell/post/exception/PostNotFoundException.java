package com.inkwell.post.exception;

/**
 * Exception thrown when a requested post is not found
 */
public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(String message) {
        super(message);
    }

    public PostNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

