package com.sanosysalvos.authservice.controller;

import com.sanosysalvos.authservice.dto.AuthResponse;
import com.sanosysalvos.authservice.dto.LoginRequest;
import com.sanosysalvos.authservice.dto.RefreshRequest;
import com.sanosysalvos.authservice.dto.RegisterRequest;
import com.sanosysalvos.authservice.dto.UserInfo;
import com.sanosysalvos.authservice.security.CustomUserDetails;
import com.sanosysalvos.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.sanosysalvos.authservice.entity.User;
import com.sanosysalvos.authservice.entity.Role;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Boolean> validate(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user information")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserInfo> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        // Ensure user has a role
        if (user.getRole() == null) {
            // This shouldn't happen after login, but just in case
            throw new RuntimeException("User has no role assigned");
        }
        Set<Long> roles = Set.of(user.getRole().getId());
        UserInfo userInfo = new UserInfo(user.getUsername(), user.getEmail(), roles);
        return ResponseEntity.ok(userInfo);
    }
}