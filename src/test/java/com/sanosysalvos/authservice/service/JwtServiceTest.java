package com.sanosysalvos.authservice.service;

import com.sanosysalvos.authservice.entity.Role;
import com.sanosysalvos.authservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generateToken_extractsClaims() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1000L);

        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setRole(role);

        String token = jwtService.generateToken(user);

        assertEquals(user.getId().toString(), jwtService.extractSubject(token));
        assertEquals("user", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user.getId().toString()));
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);

        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setRole(role);

        String token = jwtService.generateToken(user);

        assertFalse(jwtService.isTokenValid(token, user.getId().toString()));
    }
}
