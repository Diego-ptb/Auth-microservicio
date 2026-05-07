package com.sanosysalvos.authservice.service;

import com.sanosysalvos.authservice.dto.AuthResponse;
import com.sanosysalvos.authservice.dto.LoginRequest;
import com.sanosysalvos.authservice.dto.RegisterRequest;
import com.sanosysalvos.authservice.dto.RefreshRequest;
import com.sanosysalvos.authservice.security.CustomUserDetails;
import com.sanosysalvos.authservice.entity.Role;
import com.sanosysalvos.authservice.entity.User;
import com.sanosysalvos.authservice.repository.RoleRepository;
import com.sanosysalvos.authservice.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        Set<Long> roles = Set.of(user.getRole().getId());

        return new AuthResponse(token, refreshToken, user.getUsername(), roles);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // Ensure user has a role (for backward compatibility)
        if (user.getRole() == null) {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Default role USER not found"));
            user.setRole(defaultRole);
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        Set<Long> roles = Set.of(user.getRole().getId());

        return new AuthResponse(token, refreshToken, user.getUsername(), roles);
    }

    public AuthResponse refreshToken(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String subject = jwtService.extractSubject(refreshToken);
        UUID userId = UUID.fromString(subject);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Ensure user has a role (for backward compatibility)
        if (user.getRole() == null) {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Default role USER not found"));
            user.setRole(defaultRole);
            userRepository.save(user);
        }

        if (jwtService.isTokenValid(refreshToken, user.getId().toString())) {
            String newToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            Set<Long> roles = Set.of(user.getRole().getId());

            return new AuthResponse(newToken, newRefreshToken, user.getUsername(), roles);
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    public boolean validateToken(String token) {
        try {
            String subject = jwtService.extractSubject(token);
            UUID userId = UUID.fromString(subject);
            User user = userRepository.findById(userId).orElse(null);
            return user != null && jwtService.isTokenValid(token, user.getId().toString());
        } catch (Exception e) {
            return false;
        }
    }
}