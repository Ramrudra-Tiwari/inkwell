package com.inkwell.messaging.exception;

/**
 * Exception thrown when a notification is not found.
 */
public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(String message) {
        super(message);
    }

    public static NotificationNotFoundException forId(Integer id) {
        return new NotificationNotFoundException("Notification not found with ID: " + id);
    }
}
