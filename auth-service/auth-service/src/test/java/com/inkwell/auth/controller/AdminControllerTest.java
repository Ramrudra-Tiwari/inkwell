package com.inkwell.auth.controller;

import com.inkwell.auth.dto.UserDTO;
import com.inkwell.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminControllerTest {

    @Test
    void adminEndpointsDelegateToService() {
        AuthService authService = mock(AuthService.class);
        AdminController controller = new AdminController(authService);
        when(authService.getAllUsers()).thenReturn(List.of(UserDTO.builder().userId(1).build()));

        assertEquals(1, controller.getAllUsers().getBody().size());
        assertEquals(HttpStatus.OK, controller.updateUserRole(1, Map.of("role", "AUTHOR")).getStatusCode());
        assertEquals(HttpStatus.OK, controller.toggleUserStatus(1, Map.of("isActive", true)).getStatusCode());
        assertEquals(HttpStatus.OK, controller.deleteUser(1).getStatusCode());
        assertEquals("InkWell System: Online and Healthy", controller.adminDashboard());
        assertEquals(List.of(), controller.getAuditLogs().getBody());

        verify(authService).updateUserRole(1, "AUTHOR");
        verify(authService).toggleUserStatus(1, true);
        verify(authService).deleteUser(1);
    }
}
