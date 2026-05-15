package com.inkwell.auth.controller;

import com.inkwell.auth.dto.UpdateProfileRequest;
import com.inkwell.auth.dto.UpdatePasswordRequest;
import com.inkwell.auth.dto.UserDTO;
import com.inkwell.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    void updateProfile_delegatesToEmailBasedServiceMethod() {
        AuthService authService = mock(AuthService.class);
        UserController controller = new UserController(authService);
        UpdateProfileRequest request = new UpdateProfileRequest("Name", "Bio", "avatar.png");
        UserDTO user = UserDTO.builder().email("user@inkwell.com").bio("Bio").build();
        when(authService.updateProfileByEmail("user@inkwell.com", request)).thenReturn(user);

        assertEquals(user, controller.updateProfile("user@inkwell.com", request).getBody());
    }

    @Test
    void followAndFollowerEndpointsDelegateToService() {
        AuthService authService = mock(AuthService.class);
        UserController controller = new UserController(authService);
        when(authService.isFollowing(1, 2)).thenReturn(true);
        when(authService.getFollowerCount(2)).thenReturn(4L);
        when(authService.getFollowerIds(2)).thenReturn(List.of(1));

        assertEquals(HttpStatus.OK, controller.followUser(1, 2).getStatusCode());
        assertEquals(true, controller.isFollowing(1, 2).getBody());
        assertEquals(4L, controller.getFollowerCount(2).getBody());
        assertEquals(List.of(1), controller.getFollowerIds(2).getBody());
        verify(authService).followUser(1, 2);
    }

    @Test
    void dashboardPasswordAndUnfollowEndpointsDelegateToService() {
        AuthService authService = mock(AuthService.class);
        UserController controller = new UserController(authService);
        UpdatePasswordRequest request = new UpdatePasswordRequest("old", "new");

        assertEquals("Welcome User Dashboard", controller.userDashboard());
        assertEquals(HttpStatus.OK, controller.updatePassword("user@inkwell.com", request).getStatusCode());
        assertEquals(HttpStatus.OK, controller.unfollowUser(1, 2).getStatusCode());
        verify(authService).updatePassword("user@inkwell.com", request);
        verify(authService).unfollowUser(1, 2);
    }
}
