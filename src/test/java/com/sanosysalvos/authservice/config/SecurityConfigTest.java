package com.sanosysalvos.authservice.config;

import com.sanosysalvos.authservice.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Test
    void passwordEncoder_isBCrypt() {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthenticationFilter.class));

        PasswordEncoder encoder = config.passwordEncoder();

        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void authenticationManager_returnsFromConfig() throws Exception {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthenticationFilter.class));

        AuthenticationManager manager = mock(AuthenticationManager.class);
        AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(manager);

        assertSame(manager, config.authenticationManager(authenticationConfiguration));
    }
}
