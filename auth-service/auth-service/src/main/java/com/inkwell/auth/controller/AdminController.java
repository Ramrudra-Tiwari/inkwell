package com.inkwell.auth.controller;

import com.inkwell.auth.dto.UserDTO;
import com.inkwell.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin User Management", description = "Endpoints for platform administrators to manage users")
public class AdminController {

    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Admin request: Fetching all users");
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Update user role")
    public ResponseEntity<Void> updateUserRole(@PathVariable Integer userId, @RequestBody Map<String, String> request) {
        String role = request.get("role");
        log.info("Admin request: Updating role for userId {} to {}", userId, role);
        authService.updateUserRole(userId, role);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/status")
    @Operation(summary = "Toggle user status (Active/Suspended)")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Integer userId, @RequestBody Map<String, Boolean> request) {
        boolean isActive = request.get("isActive");
        log.info("Admin request: Setting active status for userId {} to {}", userId, isActive);
        authService.toggleUserStatus(userId, isActive);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user permanently")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        log.info("Admin request: Deleting userId {}", userId);
        authService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/dashboard")
    @Operation(summary = "Admin dashboard heart-beat")
    public String adminDashboard() {
        return "InkWell System: Online and Healthy";
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get system audit logs")
    public ResponseEntity<List<com.inkwell.auth.entity.AuditLog>> getAuditLogs() {
        log.info("Admin request: Fetching audit logs");
        return ResponseEntity.ok(authService.getAuditLogs());
    }
}