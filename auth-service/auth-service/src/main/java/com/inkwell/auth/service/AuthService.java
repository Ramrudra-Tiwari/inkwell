package com.inkwell.auth.service;

import com.inkwell.auth.dto.*;

/**
 * Service interface for authentication and user management.
 */
public interface AuthService {

    /**
     * Registers a new user.
     * @param request the registration request
     * @return the registered user DTO
     */
    UserDTO register(RegisterRequest request);
    void initiateRegistration(RegisterRequest request);
    UserDTO verifyRegistrationOtp(VerifyOtpRequest request);

    /**
     * Authenticates a user and returns login response with token.
     * @param request the login request
     * @return the login response
     */
    LoginResponse login(LoginRequest request);

    /**
     * Updates user profile by email.
     * @param email the user email
     * @param request the profile update request
     * @return the updated user DTO
     */
    UserDTO updateProfileByEmail(String email, UpdateProfileRequest request);

    /**
     * Updates user password.
     * @param email the user email
     * @param request the password update request
     */
    void updatePassword(String email, UpdatePasswordRequest request);

    /**
     * Initiates the forgot password process by sending an OTP.
     * @param request the forgot password request
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Verifies the OTP sent to user's email.
     * @param request the verify otp request
     * @return true if valid, false otherwise
     */
    boolean verifyOtp(VerifyOtpRequest request);

    /**
     * Resets the user's password.
     * @param request the reset password request
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Gets user by username.
     * @param username the username to look up
     * @return the user DTO
     */
    UserDTO getUserByUsername(String username);
    UserDTO getUserById(Integer userId);
    java.util.List<UserDTO> getAllUsers();
    void updateUserRole(Integer userId, String role);
    void toggleUserStatus(Integer userId, boolean isActive);
    void deleteUser(Integer userId);

    void followUser(Integer followerId, Integer followedId);
    void unfollowUser(Integer followerId, Integer followedId);
    boolean isFollowing(Integer followerId, Integer followedId);
    long getFollowerCount(Integer userId);
    java.util.List<Integer> getFollowerIds(Integer userId);

    java.util.List<com.inkwell.auth.entity.AuditLog> getAuditLogs();
    void logAction(String action, String target, String performedBy, String details);
}
