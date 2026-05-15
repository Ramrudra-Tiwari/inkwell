package com.inkwell.messaging.service;

/**
 * Service for sending various types of emails.
 */
public interface EmailService {

    /**
     * Send confirmation email for newsletter subscription.
     */
    void sendConfirmationEmail(String toEmail, String confirmationToken);

    /**
     * Send newsletter email to subscriber.
     */
    void sendNewsletterEmail(String toEmail, String postTitle, String postSummary, Integer postId, String token);

    /**
     * Send welcome email after subscription confirmation.
     */
    void sendWelcomeEmail(String toEmail);

    /**
     * Send OTP email for password reset.
     */
    void sendOtpEmail(String toEmail, String otp);

    /**
     * Send general newsletter campaign.
     */
    void sendCampaignEmail(String toEmail, String subject, String body, String token);
    /**
     * Send general notification email.
     */
    void sendNotificationEmail(String toEmail, String subject, String message);
}
