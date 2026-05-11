package com.sanosysalvos.authservice.security;

import com.sanosysalvos.authservice.entity.Role;
import com.sanosysalvos.authservice.entity.User;
import com.sanosysalvos.authservice.service.CustomUserDetailsService;
import com.sanosysalvos.authservice.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_skipsWhenNoHeader() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_setsAuthenticationWhenValidToken() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        UUID userId = UUID.randomUUID();
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User user = new User();
        user.setId(userId);
        user.setUsername("user");
        user.setRole(role);

        CustomUserDetails details = new CustomUserDetails(user);

        when(jwtService.extractSubject("token")).thenReturn(userId.toString());
        when(userDetailsService.loadUserById(userId)).thenReturn(details);
        when(jwtService.isTokenValid("token", userId.toString())).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertSame(details, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals("ROLE_USER", SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority());
    }
}
