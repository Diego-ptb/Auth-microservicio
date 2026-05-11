package com.sanosysalvos.authservice.controller;

import com.sanosysalvos.authservice.dto.AuthResponse;
import com.sanosysalvos.authservice.dto.LoginRequest;
import com.sanosysalvos.authservice.dto.RefreshRequest;
import com.sanosysalvos.authservice.dto.RegisterRequest;
import com.sanosysalvos.authservice.dto.UserInfo;
import com.sanosysalvos.authservice.entity.Role;
import com.sanosysalvos.authservice.entity.User;
import com.sanosysalvos.authservice.security.CustomUserDetails;
import com.sanosysalvos.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void register_returnsResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setEmail("user@example.com");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse("token", "refresh", "user", Set.of(1L));
        when(authService.register(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = controller.register(request);

        assertSame(response, result.getBody());
    }

    @Test
    void login_returnsResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse("token", "refresh", "user", Set.of(1L));
        when(authService.login(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = controller.login(request);

        assertSame(response, result.getBody());
    }

    @Test
    void validate_stripsBearerPrefix() {
        when(authService.validateToken("abc")).thenReturn(true);

        ResponseEntity<Boolean> result = controller.validate("Bearer abc");

        verify(authService).validateToken("abc");
        assertTrue(result.getBody());
    }

    @Test
    void refresh_returnsResponse() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh");

        AuthResponse response = new AuthResponse("token", "refresh", "user", Set.of(1L));
        when(authService.refreshToken(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = controller.refresh(request);

        assertSame(response, result.getBody());
    }

    @Test
    void getMe_returnsUserInfo() {
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setRole(role);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        ResponseEntity<UserInfo> result = controller.getMe(userDetails);

        assertEquals("user", result.getBody().getUsername());
        assertEquals("user@example.com", result.getBody().getEmail());
        assertEquals(Set.of(1L), result.getBody().getRoles());
    }

    @Test
    void getMe_throwsWhenRoleMissing() {
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");

        CustomUserDetails userDetails = new CustomUserDetails(user);

        assertThrows(RuntimeException.class, () -> controller.getMe(userDetails));
    }
}
