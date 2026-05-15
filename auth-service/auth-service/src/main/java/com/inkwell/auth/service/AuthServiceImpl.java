package com.inkwell.auth.service;

import com.inkwell.auth.dto.ForgotPasswordRequest;
import com.inkwell.auth.dto.LoginRequest;
import com.inkwell.auth.dto.LoginResponse;
import com.inkwell.auth.dto.RegisterRequest;
import com.inkwell.auth.dto.ResetPasswordRequest;
import com.inkwell.auth.dto.UpdatePasswordRequest;
import com.inkwell.auth.dto.UpdateProfileRequest;
import com.inkwell.auth.dto.UserDTO;
import com.inkwell.auth.dto.VerifyOtpRequest;
import com.inkwell.auth.entity.Follow;
import com.inkwell.auth.entity.PasswordResetToken;
import com.inkwell.auth.entity.Provider;
import com.inkwell.auth.entity.Role;
import com.inkwell.auth.entity.User;
import com.inkwell.auth.exception.UnauthorizedException;
import com.inkwell.auth.exception.UserNotFoundException;
import com.inkwell.auth.mapper.UserMapper;
import com.inkwell.auth.repository.AuditLogRepository;
import com.inkwell.auth.repository.FollowRepository;
import com.inkwell.auth.repository.PasswordResetTokenRepository;
import com.inkwell.auth.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String OTP_PURPOSE_PASSWORD_RESET = "PASSWORD_RESET";
    private static final String OTP_PURPOSE_REGISTRATION = "REGISTRATION";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository otpRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RestTemplate restTemplate;
    private final FollowRepository followRepository;
    private final AuditLogRepository auditLogRepository;

    @Value("${app.messaging-service-url}")
    private String messagingServiceUrl;

    @Value("${app.post-service-url:http://localhost:8082}")
    private String postServiceUrl;

    @Override
    public java.util.List<com.inkwell.auth.entity.AuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    @Override
    public void logAction(String action, String target, String performedBy, String details) {
        com.inkwell.auth.entity.AuditLog logEntry = com.inkwell.auth.entity.AuditLog.builder()
                .action(action)
                .target(target)
                .performedBy(performedBy)
                .details(details)
                .build();
        auditLogRepository.save(logEntry);
    }

    @Override
    public UserDTO register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (user.getRole() == null) {
            user.setRole(Role.READER);
        }

        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional
    public void initiateRegistration(RegisterRequest request) {
        User existingUserByEmail = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existingUserByEmail != null && Boolean.TRUE.equals(existingUserByEmail.getIsActive())) {
            throw new RuntimeException("Email already exists");
        }

        User existingUserByUsername = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (existingUserByUsername != null && Boolean.TRUE.equals(existingUserByUsername.getIsActive())) {
            throw new RuntimeException("Username already exists");
        }

        otpRepository.deleteByEmailAndPurpose(request.getEmail(), OTP_PURPOSE_REGISTRATION);

        String otp = generateOtp();
        PasswordResetToken token = PasswordResetToken.builder()
                .email(request.getEmail())
                .otp(otp)
                .purpose(OTP_PURPOSE_REGISTRATION)
                .username(request.getUsername())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .build();

        otpRepository.save(token);
        sendOtpEmail(request.getEmail(), otp);
    }

    @Override
    @Transactional
    public UserDTO verifyRegistrationOtp(VerifyOtpRequest request) {
        PasswordResetToken token = otpRepository.findByEmailAndOtpAndPurpose(
                        request.getEmail(),
                        request.getOtp(),
                        OTP_PURPOSE_REGISTRATION
                )
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.delete(token);
            throw new RuntimeException("OTP expired");
        }

        User activeUserByEmail = userRepository.findByEmail(request.getEmail())
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .orElse(null);
        if (activeUserByEmail != null) {
            otpRepository.delete(token);
            throw new RuntimeException("Email already exists");
        }

        User activeUserByUsername = userRepository.findByUsername(token.getUsername())
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .orElse(null);
        if (activeUserByUsername != null) {
            otpRepository.delete(token);
            throw new RuntimeException("Username already exists");
        }

        User existingUserByEmail = userRepository.findByEmail(request.getEmail()).orElse(null);
        User existingUserByUsername = userRepository.findByUsername(token.getUsername()).orElse(null);

        User user;
        if (existingUserByEmail != null || existingUserByUsername != null) {
            if (existingUserByEmail != null && existingUserByUsername != null
                    && !existingUserByEmail.getUserId().equals(existingUserByUsername.getUserId())) {
                otpRepository.delete(token);
                throw new RuntimeException("Email or username is already linked to another account");
            }

            user = existingUserByEmail != null ? existingUserByEmail : existingUserByUsername;
            user.setUsername(token.getUsername());
            user.setEmail(token.getEmail());
            user.setPasswordHash(token.getPasswordHash());
            user.setFullName(token.getFullName());
            user.setRole(Role.READER);
            user.setProvider(Provider.LOCAL);
            user.setIsActive(true);
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(LocalDateTime.now());
            }
        } else {
            user = User.builder()
                    .username(token.getUsername())
                    .email(token.getEmail())
                    .passwordHash(token.getPasswordHash())
                    .fullName(token.getFullName())
                    .role(Role.READER)
                    .provider(Provider.LOCAL)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        User savedUser = userRepository.save(user);
        otpRepository.delete(token);
        return userMapper.toDTO(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            log.info("Login attempt for: {}", request.getEmail());
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.warn("User not found: {}", request.getEmail());
                        return new UserNotFoundException("User not found");
                    });

            if (user.getPasswordHash() == null) {
                log.warn("Password hash missing for: {}", request.getEmail());
                throw new UnauthorizedException("Password not set");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                log.warn("Invalid password for: {}", request.getEmail());
                throw new UnauthorizedException("Invalid password");
            }

            if (!Boolean.TRUE.equals(user.getIsActive())) {
                log.warn("Account inactive: {}", request.getEmail());
                if (otpRepository.existsByEmailAndPurpose(request.getEmail(), OTP_PURPOSE_REGISTRATION)) {
                    throw new UnauthorizedException("Please verify your email before logging in.");
                }
                throw new UnauthorizedException("Account is suspended. Please contact support.");
            }

            String role = user.getRole() != null ? user.getRole().name() : Role.READER.name();
            String token = jwtUtils.generateToken(user.getEmail(), role);

            log.info("Login successful for: {}", request.getEmail());
            return LoginResponse.builder()
                    .token(token)
                    .user(userMapper.toDTO(user))
                    .build();
        } catch (UserNotFoundException | UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("CRITICAL LOGIN ERROR: ", e);
            throw new RuntimeException("Backend Error: " + e.getMessage());
        }
    }

    @Override
    public UserDTO updateProfileByEmail(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void updatePassword(String email, UpdatePasswordRequest request) {
        log.info("Password update request for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        logAction("PASSWORD_UPDATE", "User: " + user.getEmail(), user.getEmail(), "User updated their password successfully");
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        otpRepository.deleteByEmailAndPurpose(request.getEmail(), OTP_PURPOSE_PASSWORD_RESET);

        String otp = generateOtp();
        PasswordResetToken token = PasswordResetToken.builder()
                .email(request.getEmail())
                .otp(otp)
                .purpose(OTP_PURPOSE_PASSWORD_RESET)
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .build();

        otpRepository.save(token);
        sendOtpEmail(request.getEmail(), otp);
    }

    @Override
    public boolean verifyOtp(VerifyOtpRequest request) {
        PasswordResetToken token = otpRepository.findByEmailAndOtpAndPurpose(
                        request.getEmail(),
                        request.getOtp(),
                        OTP_PURPOSE_PASSWORD_RESET
                )
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.delete(token);
            throw new RuntimeException("OTP expired");
        }

        return true;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = otpRepository.findByEmailAndOtpAndPurpose(
                        request.getEmail(),
                        request.getOtp(),
                        OTP_PURPOSE_PASSWORD_RESET
                )
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.delete(token);
            throw new RuntimeException("OTP expired");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpRepository.delete(token);

        log.info("Password reset successful for user: {}", request.getEmail());
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return userMapper.toDTO(user);
    }

    @Override
    public UserDTO getUserById(Integer userId) {
        log.debug("Getting user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found ID: " + userId));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserDTO> getAllUsers() {
        log.info("Fetching all users from repository");
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void updateUserRole(Integer userId, String role) {
        log.info("Updating role for userId: {} to {}", userId, role);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        String oldRole = user.getRole().name();
        user.setRole(Role.valueOf(role.toUpperCase()));
        userRepository.save(user);
        logAction("ROLE_UPDATE", "User: " + user.getEmail(), "SYSTEM_ADMIN", "Changed role from " + oldRole + " to " + role);
    }

    @Override
    public void toggleUserStatus(Integer userId, boolean isActive) {
        log.info("Toggling status for userId: {} to {}", userId, isActive);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setIsActive(isActive);
        userRepository.save(user);
        logAction("STATUS_TOGGLE", "User: " + user.getEmail(), "SYSTEM_ADMIN", "Set isActive to " + isActive);
    }

    @Override
    public void deleteUser(Integer userId) {
        log.info("Deleting user with userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        deleteAuthorPosts(userId);
        userRepository.deleteById(userId);
        logAction("USER_DELETE", "User: " + user.getEmail(), "SYSTEM_ADMIN", "Permanently deleted user");
    }

    private void deleteAuthorPosts(Integer userId) {
        String baseUrl = (postServiceUrl == null || postServiceUrl.isBlank())
                ? "http://localhost:8082"
                : postServiceUrl;
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/posts/author/{authorId}")
                .buildAndExpand(userId)
                .toUriString();

        try {
            restTemplate.delete(url);
        } catch (RestClientException ex) {
            log.error("Failed to delete posts for expelled userId: {}", userId, ex);
            throw new RuntimeException("Unable to delete author posts before user deletion", ex);
        }
    }

    @Override
    @Transactional
    public void followUser(Integer followerId, Integer followedId) {
        if (followerId.equals(followedId)) {
            throw new RuntimeException("You cannot follow yourself");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("Follower not found"));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new UserNotFoundException("User to follow not found"));

        if (followRepository.existsByFollowerAndFollowed(follower, followed)) {
            return;
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .followed(followed)
                .build();

        followRepository.save(follow);
        log.info("User {} is now following {}", followerId, followedId);
    }

    @Override
    @Transactional
    public void unfollowUser(Integer followerId, Integer followedId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("Follower not found"));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new UserNotFoundException("Followed user not found"));

        followRepository.findByFollowerAndFollowed(follower, followed)
                .ifPresent(followRepository::delete);

        log.info("User {} unfollowed {}", followerId, followedId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Integer followerId, Integer followedId) {
        User follower = userRepository.findById(followerId).orElse(null);
        User followed = userRepository.findById(followedId).orElse(null);
        if (follower == null || followed == null) {
            return false;
        }
        return followRepository.existsByFollowerAndFollowed(follower, followed);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowerCount(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return 0;
        }
        return followRepository.countByFollowed(user);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Integer> getFollowerIds(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return java.util.List.of();
        }
        return followRepository.findByFollowed(user).stream()
                .map(follow -> follow.getFollower().getUserId())
                .collect(java.util.stream.Collectors.toList());
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void sendOtpEmail(String email, String otp) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(messagingServiceUrl + "/api/v1/messaging/email/send-otp")
                    .queryParam("email", email)
                    .queryParam("otp", otp)
                    .toUriString();

            restTemplate.postForEntity(url, null, String.class);
            log.info("OTP email request sent to messaging-service for: {}", email);
        } catch (Exception e) {
            log.error("Failed to trigger OTP email for {}: {}", email, e.getMessage());
            log.info("FALLBACK OTP: {}", otp);
        }
    }
}
