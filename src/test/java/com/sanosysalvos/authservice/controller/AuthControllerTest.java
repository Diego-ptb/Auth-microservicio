package com.sanosysalvos.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanosysalvos.authservice.dto.AuthResponse;
import com.sanosysalvos.authservice.dto.LoginRequest;
import com.sanosysalvos.authservice.dto.RefreshRequest;
import com.sanosysalvos.authservice.dto.RegisterRequest;
import com.sanosysalvos.authservice.entity.Role;
import com.sanosysalvos.authservice.entity.User;
import com.sanosysalvos.authservice.repository.RoleRepository;
import com.sanosysalvos.authservice.repository.UserRepository;
import com.sanosysalvos.authservice.security.CustomUserDetails;
import com.sanosysalvos.authservice.security.JwtAuthenticationFilter;
import com.sanosysalvos.authservice.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(a -> a
                            .requestMatchers("/auth/register", "/auth/login",
                                    "/auth/validate", "/auth/refresh")
                            .permitAll()
                            .anyRequest().authenticated())
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // DataInitializer @Component in AuthServiceApplication requires these beans
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(
                any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    private AuthResponse mockAuthResponse() {
        return new AuthResponse("access-token", "refresh-token", "juan", Set.of(1L));
    }

    private RegisterRequest validRegisterRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setUsername("juanito");
        r.setEmail("juan@example.com");
        r.setPassword("secret123");
        r.setRut("12.345.678-9");
        return r;
    }

    // --- POST /auth/register ---

    @Test
    void register_validRequest_returnsOk() throws Exception {
        when(authService.register(any())).thenReturn(mockAuthResponse());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.username").value("juan"));
    }

    @Test
    void register_emptyUsername_returnsBadRequest() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setUsername(""); // viola @NotBlank: string vacía

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_usernameTooShort_returnsBadRequest() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setUsername("ab"); // viola @Size(min=3): 2 chars < min 3, pero no es blank

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmail_returnsBadRequest() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setEmail("no-es-email");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returnsBadRequest() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setPassword("123"); // viola @Size(min=6)

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidRut_returnsBadRequest() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setRut("rut-invalido"); // viola @Pattern

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingRut_returnsBadRequest() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setRut(null); // viola @NotBlank

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- POST /auth/login ---

    @Test
    void login_validRequest_returnsOk() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("juan");
        request.setPassword("secret123");

        when(authService.login(any())).thenReturn(mockAuthResponse());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"));
    }

    @Test
    void login_emptyUsername_returnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(""); // viola @NotBlank
        request.setPassword("secret123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_emptyPassword_returnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("juan");
        request.setPassword(""); // viola @NotBlank

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missingFields_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /auth/validate ---

    @Test
    void validate_bearerToken_stripsAndValidates() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void validate_invalidToken_returnsFalse() throws Exception {
        when(authService.validateToken("bad-token")).thenReturn(false);

        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // --- POST /auth/refresh ---

    @Test
    void refresh_validToken_returnsOk() throws Exception {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("valid-refresh-token");

        when(authService.refreshToken(any())).thenReturn(mockAuthResponse());

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"));
    }

    @Test
    void refresh_emptyRefreshToken_returnsBadRequest() throws Exception {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken(""); // viola @NotBlank

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_missingRefreshToken_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /auth/me ---

    @Test
    void getMe_authenticatedUser_returnsUserInfo() throws Exception {
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User user = new User();
        user.setUsername("juan");
        user.setEmail("juan@example.com");
        user.setRole(role);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        mockMvc.perform(get("/auth/me")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("juan"))
                .andExpect(jsonPath("$.email").value("juan@example.com"));
    }

    @Test
    void getMe_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isForbidden());
    }
}
