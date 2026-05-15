package com.inkwell.auth.controller;

import com.inkwell.auth.dto.UserDTO;
import com.inkwell.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user-related endpoints.
 */
import com.inkwell.auth.dto.UpdateProfileRequest;
import com.inkwell.auth.dto.UpdatePasswordRequest;
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management APIs")
public class UserController {

    private final AuthService authService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "User dashboard")
    public String userDashboard() {
        return "Welcome User Dashboard";
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserDTO> updateProfile(@RequestHeader("X-User-Email") String email,
                                                 @RequestBody UpdateProfileRequest request) {
        UserDTO user = authService.updateProfileByEmail(email, request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/password")
    @Operation(summary = "Update user password")
    public ResponseEntity<Void> updatePassword(@RequestHeader("X-User-Email") String email,
                                               @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(email, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/follow/{followedId}")
    @Operation(summary = "Follow a user")
    public ResponseEntity<Void> followUser(@RequestParam Integer followerId, @PathVariable Integer followedId) {
        authService.followUser(followerId, followedId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unfollow/{followedId}")
    @Operation(summary = "Unfollow a user")
    public ResponseEntity<Void> unfollowUser(@RequestParam Integer followerId, @PathVariable Integer followedId) {
        authService.unfollowUser(followerId, followedId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/is-following/{followedId}")
    @Operation(summary = "Check if following a user")
    public ResponseEntity<Boolean> isFollowing(@RequestParam Integer followerId, @PathVariable Integer followedId) {
        return ResponseEntity.ok(authService.isFollowing(followerId, followedId));
    }

    @GetMapping("/{userId}/follower-count")
    @Operation(summary = "Get follower count")
    public ResponseEntity<Long> getFollowerCount(@PathVariable Integer userId) {
        return ResponseEntity.ok(authService.getFollowerCount(userId));
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "Get follower IDs")
    public ResponseEntity<java.util.List<Integer>> getFollowerIds(@PathVariable Integer userId) {
        return ResponseEntity.ok(authService.getFollowerIds(userId));
    }
}
