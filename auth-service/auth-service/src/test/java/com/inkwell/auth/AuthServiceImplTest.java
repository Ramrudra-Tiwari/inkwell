package com.inkwell.auth;

import com.inkwell.auth.dto.LoginRequest;
import com.inkwell.auth.dto.LoginResponse;
import com.inkwell.auth.dto.ForgotPasswordRequest;
import com.inkwell.auth.dto.RegisterRequest;
import com.inkwell.auth.dto.ResetPasswordRequest;
import com.inkwell.auth.dto.UpdateProfileRequest;
import com.inkwell.auth.dto.UpdatePasswordRequest;
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
import com.inkwell.auth.service.AuthServiceImpl;
import com.inkwell.auth.service.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordResetTokenRepository otpRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private User mappedUser;
    private User savedUser;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("inkwell-author")
                .email("author@inkwell.com")
                .password("secret123")
                .fullName("InkWell Author")
                .build();

        mappedUser = User.builder()
                .username("inkwell-author")
                .email("author@inkwell.com")
                .fullName("InkWell Author")
                .role(Role.READER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        savedUser = User.builder()
                .userId(10)
                .username("inkwell-author")
                .email("author@inkwell.com")
                .passwordHash("encoded-secret")
                .fullName("InkWell Author")
                .role(Role.AUTHOR)
                .provider(Provider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userDTO = UserDTO.builder()
                .userId(10)
                .username("inkwell-author")
                .email("author@inkwell.com")
                .fullName("InkWell Author")
                .role(Role.AUTHOR)
                .provider(Provider.LOCAL)
                .isActive(true)
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Test
    void register_successfullyCreatesReaderAccount() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userMapper.toEntity(registerRequest)).thenReturn(mappedUser);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(userDTO);

        UserDTO result = authService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User persistedUser = userCaptor.getValue();
        assertEquals("encoded-secret", persistedUser.getPasswordHash());
        assertEquals(Role.READER, persistedUser.getRole());
        assertNotNull(result);
        assertEquals(10, result.getUserId());
        assertEquals(Role.AUTHOR, result.getRole());
    }

    @Test
    void updateProfileByEmail_returnsUpdatedUserWhenEmailExists() {
        User existingUser = User.builder()
                .userId(10)
                .username("inkwell-author")
                .email("author@inkwell.com")
                .passwordHash("encoded-secret")
                .fullName("InkWell Author")
                .role(Role.AUTHOR)
                .provider(Provider.LOCAL)
                .isActive(true)
                .bio("Old bio")
                .avatarUrl("old.png")
                .createdAt(LocalDateTime.now())
                .build();

        UserDTO updatedUserDTO = UserDTO.builder()
                .userId(10)
                .bio("Updated bio")
                .avatarUrl("updated.png")
                .role(Role.AUTHOR)
                .build();

        UpdateProfileRequest request = new UpdateProfileRequest(null, "Updated bio", "updated.png");

        when(userRepository.findByEmail("author@inkwell.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toDTO(any(User.class))).thenReturn(updatedUserDTO);

        UserDTO result = authService.updateProfileByEmail("author@inkwell.com", request);

        assertNotNull(result);
        assertEquals("Updated bio", result.getBio());
        assertEquals("updated.png", result.getAvatarUrl());
    }

    @Test
    void updateProfileByEmail_throwsUserNotFoundExceptionWhenEmailDoesNotExist() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, "Bio", "avatar.png");

        when(userRepository.findByEmail("missing@inkwell.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.updateProfileByEmail("missing@inkwell.com", request)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void login_returnsTokenAndMappedUserForValidCredentials() {
        LoginRequest loginRequest = new LoginRequest("author@inkwell.com", "secret123");

        User existingUser = User.builder()
                .userId(10)
                .username("inkwell-author")
                .email("author@inkwell.com")
                .passwordHash("encoded-secret")
                .fullName("InkWell Author")
                .role(Role.AUTHOR)
                .provider(Provider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPasswordHash())).thenReturn(true);
        when(jwtUtils.generateToken(existingUser.getEmail(), Role.AUTHOR.name())).thenReturn("jwt-token");
        when(userMapper.toDTO(existingUser)).thenReturn(userDTO);

        LoginResponse result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals(userDTO, result.getUser());
    }

    @Test
    void login_throwsUnauthorizedExceptionWhenPasswordIsInvalid() {
        LoginRequest loginRequest = new LoginRequest("author@inkwell.com", "wrong-password");

        User existingUser = User.builder()
                .email("author@inkwell.com")
                .passwordHash("encoded-secret")
                .role(Role.AUTHOR)
                .isActive(true)
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPasswordHash())).thenReturn(false);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(savedUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(registerRequest));

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void login_throwsUnauthorizedExceptionWhenAccountInactive() {
        LoginRequest loginRequest = new LoginRequest("author@inkwell.com", "secret123");
        User inactiveUser = User.builder()
                .email("author@inkwell.com")
                .passwordHash("encoded-secret")
                .role(Role.AUTHOR)
                .isActive(false)
                .build();
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), inactiveUser.getPasswordHash())).thenReturn(true);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));

        assertEquals("Account is suspended. Please contact support.", exception.getMessage());
    }

    @Test
    void updatePassword_encodesAndSavesNewPassword() {
        User existingUser = User.builder()
                .email("author@inkwell.com")
                .passwordHash("old-hash")
                .provider(Provider.LOCAL)
                .build();
        UpdatePasswordRequest request = new UpdatePasswordRequest("old", "new-secret");
        when(userRepository.findByEmail("author@inkwell.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");

        authService.updatePassword("author@inkwell.com", request);

        assertEquals("new-hash", existingUser.getPasswordHash());
        verify(userRepository).save(existingUser);
        verify(auditLogRepository).save(any());
    }

    @Test
    void forgotPassword_storesOtpForExistingUser() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("author@inkwell.com");
        when(userRepository.findByEmail("author@inkwell.com")).thenReturn(Optional.of(savedUser));

        authService.forgotPassword(request);

        verify(otpRepository).deleteByEmailAndPurpose("author@inkwell.com", "PASSWORD_RESET");
        verify(otpRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void verifyOtp_returnsTrueForValidNonExpiredOtp() {
        PasswordResetToken token = PasswordResetToken.builder()
                .email("author@inkwell.com")
                .otp("123456")
                .purpose("PASSWORD_RESET")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        VerifyOtpRequest request = new VerifyOtpRequest("author@inkwell.com", "123456");
        when(otpRepository.findByEmailAndOtpAndPurpose("author@inkwell.com", "123456", "PASSWORD_RESET"))
                .thenReturn(Optional.of(token));

        assertEquals(true, authService.verifyOtp(request));
    }

    @Test
    void resetPassword_updatesPasswordAndDeletesOtp() {
        PasswordResetToken token = PasswordResetToken.builder()
                .email("author@inkwell.com")
                .otp("123456")
                .purpose("PASSWORD_RESET")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        ResetPasswordRequest request = new ResetPasswordRequest("author@inkwell.com", "123456", "new-secret");
        when(otpRepository.findByEmailAndOtpAndPurpose("author@inkwell.com", "123456", "PASSWORD_RESET"))
                .thenReturn(Optional.of(token));
        when(userRepository.findByEmail("author@inkwell.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");

        authService.resetPassword(request);

        assertEquals("new-hash", savedUser.getPasswordHash());
        verify(userRepository).save(savedUser);
        verify(otpRepository).delete(token);
    }

    @Test
    void initiateRegistration_storesRegistrationOtpAndEncodedPassword() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded-secret");

        authService.initiateRegistration(registerRequest);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(otpRepository).deleteByEmailAndPurpose(registerRequest.getEmail(), "REGISTRATION");
        verify(otpRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertEquals("REGISTRATION", savedToken.getPurpose());
        assertEquals(registerRequest.getEmail(), savedToken.getEmail());
        assertEquals(registerRequest.getUsername(), savedToken.getUsername());
        assertEquals(registerRequest.getFullName(), savedToken.getFullName());
        assertEquals("encoded-secret", savedToken.getPasswordHash());
        assertNotNull(savedToken.getOtp());
    }

    @Test
    void verifyRegistrationOtp_createsUserAndDeletesToken() {
        PasswordResetToken token = PasswordResetToken.builder()
                .email("author@inkwell.com")
                .otp("123456")
                .purpose("REGISTRATION")
                .username("inkwell-author")
                .fullName("InkWell Author")
                .passwordHash("encoded-secret")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        VerifyOtpRequest request = new VerifyOtpRequest("author@inkwell.com", "123456");

        when(otpRepository.findByEmailAndOtpAndPurpose("author@inkwell.com", "123456", "REGISTRATION"))
                .thenReturn(Optional.of(token));
        when(userRepository.findByEmail("author@inkwell.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("inkwell-author")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(userDTO);

        UserDTO result = authService.verifyRegistrationOtp(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(otpRepository).delete(token);

        User createdUser = userCaptor.getValue();
        assertEquals("inkwell-author", createdUser.getUsername());
        assertEquals("author@inkwell.com", createdUser.getEmail());
        assertEquals("encoded-secret", createdUser.getPasswordHash());
        assertEquals("InkWell Author", createdUser.getFullName());
        assertEquals(Role.READER, createdUser.getRole());
        assertEquals(Provider.LOCAL, createdUser.getProvider());
        assertEquals(true, createdUser.getIsActive());
        assertEquals(userDTO, result);
    }

    @Test
    void verifyRegistrationOtp_reusesInactiveExistingUser() {
        PasswordResetToken token = PasswordResetToken.builder()
                .email("author@inkwell.com")
                .otp("123456")
                .purpose("REGISTRATION")
                .username("inkwell-author")
                .fullName("InkWell Author")
                .passwordHash("encoded-secret")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        VerifyOtpRequest request = new VerifyOtpRequest("author@inkwell.com", "123456");
        User inactiveUser = User.builder()
                .userId(10)
                .username("old-user")
                .email("author@inkwell.com")
                .passwordHash("old-hash")
                .fullName("Old Name")
                .role(Role.READER)
                .provider(Provider.LOCAL)
                .isActive(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(otpRepository.findByEmailAndOtpAndPurpose("author@inkwell.com", "123456", "REGISTRATION"))
                .thenReturn(Optional.of(token));
        when(userRepository.findByEmail("author@inkwell.com"))
                .thenReturn(Optional.empty(), Optional.of(inactiveUser));
        when(userRepository.findByUsername("inkwell-author"))
                .thenReturn(Optional.empty(), Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(userDTO);

        UserDTO result = authService.verifyRegistrationOtp(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(otpRepository).delete(token);

        User updatedUser = userCaptor.getValue();
        assertEquals(10, updatedUser.getUserId());
        assertEquals("inkwell-author", updatedUser.getUsername());
        assertEquals("author@inkwell.com", updatedUser.getEmail());
        assertEquals("encoded-secret", updatedUser.getPasswordHash());
        assertEquals("InkWell Author", updatedUser.getFullName());
        assertEquals(true, updatedUser.getIsActive());
        assertEquals(userDTO, result);
    }

    @Test
    void getUserById_returnsMappedUser() {
        when(userRepository.findById(10)).thenReturn(Optional.of(savedUser));
        when(userMapper.toDTO(savedUser)).thenReturn(userDTO);

        assertEquals(userDTO, authService.getUserById(10));
    }

    @Test
    void getUserByUsername_returnsMappedUser() {
        when(userRepository.findByUsername("inkwell-author")).thenReturn(Optional.of(savedUser));
        when(userMapper.toDTO(savedUser)).thenReturn(userDTO);

        assertEquals(userDTO, authService.getUserByUsername("inkwell-author"));
    }

    @Test
    void getAllUsers_returnsMappedUsers() {
        when(userRepository.findAll()).thenReturn(List.of(savedUser));
        when(userMapper.toDTO(savedUser)).thenReturn(userDTO);

        List<UserDTO> users = authService.getAllUsers();

        assertEquals(1, users.size());
    }

    @Test
    void updateUserRole_changesRoleAndLogsAction() {
        when(userRepository.findById(10)).thenReturn(Optional.of(savedUser));

        authService.updateUserRole(10, "reader");

        assertEquals(Role.READER, savedUser.getRole());
        verify(userRepository).save(savedUser);
        verify(auditLogRepository).save(any());
    }

    @Test
    void toggleUserStatus_changesActiveFlagAndLogsAction() {
        when(userRepository.findById(10)).thenReturn(Optional.of(savedUser));

        authService.toggleUserStatus(10, false);

        assertEquals(false, savedUser.getIsActive());
        verify(userRepository).save(savedUser);
        verify(auditLogRepository).save(any());
    }

    @Test
    void deleteUser_removesExistingUserAndLogsAction() {
        when(userRepository.findById(10)).thenReturn(Optional.of(savedUser));

        authService.deleteUser(10);

        verify(restTemplate).delete("http://localhost:8082/api/v1/posts/author/10");
        verify(userRepository).deleteById(10);
        verify(auditLogRepository).save(any());
    }

    @Test
    void followUser_savesFollowWhenNotAlreadyFollowing() {
        User follower = User.builder().userId(1).build();
        User followed = User.builder().userId(2).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2)).thenReturn(Optional.of(followed));
        when(followRepository.existsByFollowerAndFollowed(follower, followed)).thenReturn(false);

        authService.followUser(1, 2);

        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void unfollowUser_deletesExistingFollow() {
        User follower = User.builder().userId(1).build();
        User followed = User.builder().userId(2).build();
        Follow follow = Follow.builder().follower(follower).followed(followed).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2)).thenReturn(Optional.of(followed));
        when(followRepository.findByFollowerAndFollowed(follower, followed)).thenReturn(Optional.of(follow));

        authService.unfollowUser(1, 2);

        verify(followRepository).delete(follow);
    }

    @Test
    void getFollowerIds_returnsFollowerUserIds() {
        User followed = User.builder().userId(2).build();
        User follower = User.builder().userId(1).build();
        Follow follow = Follow.builder().follower(follower).followed(followed).build();
        when(userRepository.findById(2)).thenReturn(Optional.of(followed));
        when(followRepository.findByFollowed(followed)).thenReturn(List.of(follow));

        assertEquals(List.of(1), authService.getFollowerIds(2));
    }

    @Test
    void isFollowing_returnsFalseWhenEitherUserIsMissing() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        when(userRepository.findById(2)).thenReturn(Optional.of(savedUser));

        assertEquals(false, authService.isFollowing(1, 2));
    }

    @Test
    void getFollowerCount_returnsRepositoryCount() {
        when(userRepository.findById(10)).thenReturn(Optional.of(savedUser));
        when(followRepository.countByFollowed(savedUser)).thenReturn(3L);

        assertEquals(3L, authService.getFollowerCount(10));
    }

    @Test
    void getAuditLogs_returnsRepositoryLogs() {
        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of());

        assertEquals(List.of(), authService.getAuditLogs());
    }
}
