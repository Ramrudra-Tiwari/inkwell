package com.inkwell.messaging.exception;

/**
 * Exception thrown when a subscriber is not found.
 */
public class SubscriberNotFoundException extends RuntimeException {

    public SubscriberNotFoundException(String message) {
        super(message);
    }

    public static SubscriberNotFoundException forId(Integer id) {
        return new SubscriberNotFoundException("Subscriber not found with ID: " + id);
    }

    public static SubscriberNotFoundException forEmail(String email) {
        return new SubscriberNotFoundException("Subscriber not found with email: " + email);
    }

    public static SubscriberNotFoundException forToken(String token) {
        return new SubscriberNotFoundException("Subscriber not found with token: " + token);
    }
}
