package com.inkwell.messaging.controller;

import com.inkwell.messaging.dto.NotificationDTO;
import com.inkwell.messaging.dto.NotificationRequest;
import com.inkwell.messaging.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for notification operations.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Service", description = "APIs for managing user notifications")
public class NotificationResource {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create notification", description = "Create a new notification for a user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notification created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<NotificationDTO> createNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Creating notification for recipient: {}", request.getRecipientId());

        NotificationDTO notification = notificationService.createNotification(request);

        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a specific notification by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification found"),
        @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<NotificationDTO> getNotificationById(
            @Parameter(description = "Notification ID")
            @PathVariable Integer notificationId) {

        log.debug("Getting notification by ID: {}", notificationId);

        NotificationDTO notification = notificationService.getNotificationById(notificationId);

        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notifications", description = "Retrieve all notifications for a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    })
    public ResponseEntity<List<NotificationDTO>> getNotificationsForUser(
            @Parameter(description = "User ID")
            @PathVariable Integer userId) {

        log.debug("Getting notifications for user: {}", userId);

        List<NotificationDTO> notifications = notificationService.getNotificationsForUser(userId);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieve unread notifications for a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully")
    })
    public ResponseEntity<List<NotificationDTO>> getUnreadNotificationsForUser(
            @Parameter(description = "User ID")
            @PathVariable Integer userId) {

        log.debug("Getting unread notifications for user: {}", userId);

        List<NotificationDTO> notifications = notificationService.getUnreadNotificationsForUser(userId);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread/count")
    @Operation(summary = "Count unread notifications", description = "Get count of unread notifications for a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Long> countUnreadNotificationsForUser(
            @Parameter(description = "User ID")
            @PathVariable Integer userId) {

        log.debug("Counting unread notifications for user: {}", userId);

        long count = notificationService.countUnreadNotificationsForUser(userId);

        return ResponseEntity.ok(count);
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification marked as read"),
        @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Notification ID")
            @PathVariable Integer notificationId) {

        log.info("Marking notification as read: {}", notificationId);

        notificationService.markAsRead(notificationId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications as read for a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All notifications marked as read")
    })
    public ResponseEntity<Void> markAllAsReadForUser(
            @Parameter(description = "User ID")
            @PathVariable Integer userId) {

        log.info("Marking all notifications as read for user: {}", userId);

        notificationService.markAllAsReadForUser(userId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "Notification ID")
            @PathVariable Integer notificationId) {

        log.info("Deleting notification: {}", notificationId);

        notificationService.deleteNotification(notificationId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/broadcast")
    @Operation(summary = "Broadcast notification", description = "Send a notification to all users or filtered by role")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Broadcast initiated successfully")
    })
    public ResponseEntity<Void> broadcast(@RequestParam String title, @RequestParam String message, @RequestParam(required = false) String role) {
        log.info("Broadcast request: title={}, role={}", title, role);
        notificationService.broadcast(title, message, role);
        return ResponseEntity.ok().build();
    }
}
