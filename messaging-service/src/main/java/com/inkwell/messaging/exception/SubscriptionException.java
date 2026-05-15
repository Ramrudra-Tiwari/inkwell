package com.inkwell.messaging.exception;

/**
 * Exception thrown for subscription-related errors.
 */
public class SubscriptionException extends RuntimeException {

    public SubscriptionException(String message) {
        super(message);
    }

    public static SubscriptionException alreadySubscribed(String email) {
        return new SubscriptionException("Email already subscribed: " + email);
    }

    public static SubscriptionException invalidToken() {
        return new SubscriptionException("Invalid or expired subscription token");
    }

    public static SubscriptionException alreadyActivated() {
        return new SubscriptionException("Subscription is already activated");
    }
}
