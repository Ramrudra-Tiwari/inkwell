package com.inkwell.messaging.service.impl;

import com.inkwell.messaging.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService using JavaMailSender.
 * All links route through the API gateway (port 8080) so that
 * security filters and CORS policies are applied uniformly.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /** Gateway / frontend base URLs — override in production via env vars */
    @Value("${app.gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // -------------------------------------------------------------------------
    // Newsletter
    // -------------------------------------------------------------------------

    @Override
    public void sendConfirmationEmail(String toEmail, String confirmationToken) {
        String confirmLink = frontendUrl + "/confirm-subscription?token=" + confirmationToken;
        String subject = "✉️ Confirm Your InkWell Subscription";
        String body = "Hello,\n\n"
                + "Thanks for subscribing to the InkWell Newsletter!\n\n"
                + "Please confirm your email address by clicking the link below:\n"
                + confirmLink + "\n\n"
                + "If you did not subscribe, you can safely ignore this email.\n\n"
                + "— The InkWell Team";
        sendEmail(toEmail, subject, body);
    }

    @Override
    public void sendNewsletterEmail(String toEmail, String postTitle, String postSummary,
                                    Integer postId, String token) {
        String readLink    = frontendUrl + "/posts/" + postId;
        String unsubLink   = frontendUrl + "/unsubscribe?email=" + encode(toEmail) + "&token=" + token;

        String subject = "📖 New Story on InkWell: " + postTitle;
        String body = "Hello,\n\n"
                + postSummary + "\n\n"
                + "Read the full story: " + readLink + "\n\n"
                + "---\n"
                + "You are receiving this because you subscribed to the InkWell newsletter.\n"
                + "Unsubscribe: " + unsubLink;
        sendEmail(toEmail, subject, body);
    }

    @Override
    public void sendWelcomeEmail(String toEmail) {
        String subject = "🎉 Welcome to InkWell!";
        String body = "Hello,\n\n"
                + "You're now subscribed to the InkWell newsletter.\n"
                + "You'll be the first to know when our writers publish new stories.\n\n"
                + "Explore the latest posts: " + frontendUrl + "\n\n"
                + "— The InkWell Team";
        sendEmail(toEmail, subject, body);
    }

    @Override
    public void sendCampaignEmail(String toEmail, String subject, String body, String token) {
        String unsubLink = frontendUrl + "/unsubscribe?email=" + encode(toEmail) + "&token=" + token;
        String fullBody  = body + "\n\n---\n"
                + "You are receiving this because you subscribed to the InkWell newsletter.\n"
                + "Unsubscribe: " + unsubLink;
        sendEmail(toEmail, subject, fullBody);
    }

    // -------------------------------------------------------------------------
    // Account / Auth
    // -------------------------------------------------------------------------

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        log.info("🎯 Sending OTP to: {}", toEmail);
        String subject = "🔐 InkWell – Password Reset OTP";
        String body = "Hello,\n\n"
                + "Your one-time password (OTP) for resetting your InkWell password is:\n\n"
                + "        " + otp + "\n\n"
                + "This OTP is valid for 10 minutes.\n"
                + "If you did not request a password reset, please ignore this email.\n\n"
                + "— The InkWell Team";
        sendEmail(toEmail, subject, body);
    }

    // -------------------------------------------------------------------------
    // Social / Notifications
    // -------------------------------------------------------------------------

    @Override
    public void sendNotificationEmail(String toEmail, String subject, String message) {
        String fullBody = message + "\n\n"
                + "Visit InkWell: " + frontendUrl;
        sendEmail(toEmail, subject, fullBody);
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private void sendEmail(String to, String subject, String body) {
        log.info("📤 Sending email to {} | Subject: {}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("✅ Email delivered to SMTP relay for {}", to);
        } catch (Exception e) {
            log.error("❌ SMTP ERROR for {} — {}", to, e.getMessage());
        }
    }

    /** Simple URL encoding for email query params */
    private static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}
