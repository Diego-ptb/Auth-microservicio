package com.sanosysalvos.authservice.service;

import com.sanosysalvos.authservice.dto.AuthResponse;
import com.sanosysalvos.authservice.dto.LoginRequest;
import com.sanosysalvos.authservice.dto.RefreshRequest;
import com.sanosysalvos.authservice.dto.RegisterRequest;
import com.sanosysalvos.authservice.entity.Role;
import com.sanosysalvos.authservice.entity.User;
import com.sanosysalvos.authservice.repository.RoleRepository;
import com.sanosysalvos.authservice.repository.UserRepository;
import com.sanosysalvos.authservice.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_throwsWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setEmail("user@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("user")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    void register_throwsWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setEmail("user@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    void register_successful() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setEmail("user@example.com");
        request.setPassword("password123");

        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(jwtService.generateToken(any(User.class))).thenReturn("token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(request);

        assertEquals("token", response.getToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals("user", response.getUsername());
        assertEquals(Set.of(1L), response.getRoles());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encoded", userCaptor.getValue().getPassword());
        assertSame(role, userCaptor.getValue().getRole());
    }

    @Test
    void login_assignsDefaultRoleWhenMissing() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("password123");

        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setRole(null);

        CustomUserDetails details = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(details);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(jwtService.generateToken(user)).thenReturn("token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh");

        AuthResponse response = authService.login(request);

        assertEquals("token", response.getToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals(Set.of(1L), response.getRoles());
        verify(userRepository).save(user);
    }

    @Test
    void refreshToken_successful() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh");

        UUID userId = UUID.randomUUID();
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User user = new User();
        user.setId(userId);
        user.setUsername("user");
        user.setRole(null);

        when(jwtService.extractSubject("refresh")).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(jwtService.isTokenValid("refresh", userId.toString())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-2");

        AuthResponse response = authService.refreshToken(request);

        assertEquals("token", response.getToken());
        assertEquals("refresh-2", response.getRefreshToken());
        assertEquals(Set.of(1L), response.getRoles());
        verify(userRepository).save(user);
    }

    @Test
    void refreshToken_throwsWhenInvalid() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh");

        UUID userId = UUID.randomUUID();
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User user = new User();
        user.setId(userId);
        user.setUsername("user");
        user.setRole(role);

        when(jwtService.extractSubject("refresh")).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("refresh", userId.toString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.refreshToken(request));
    }

    @Test
    void validateToken_returnsFalseWhenUserMissing() {
        UUID userId = UUID.randomUUID();

        when(jwtService.extractSubject("token")).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertFalse(authService.validateToken("token"));
        verify(jwtService, never()).isTokenValid(eq("token"), any());
    }

    @Test
    void validateToken_returnsFalseOnException() {
        when(jwtService.extractSubject("token")).thenThrow(new RuntimeException("boom"));

        assertFalse(authService.validateToken("token"));
    }
}
