package com.sanosysalvos.authservice.security;

import com.sanosysalvos.authservice.entity.Role;
import com.sanosysalvos.authservice.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomUserDetailsTest {

    @Test
    void exposesUserInformation() {
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setPassword("secret");
        user.setRole(role);

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals("user", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertEquals("ROLE_USER", details.getAuthorities().iterator().next().getAuthority());
        assertEquals(user.getId().toString(), details.getUserId());
    }
}
