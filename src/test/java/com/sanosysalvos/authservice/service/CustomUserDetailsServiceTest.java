package com.sanosysalvos.authservice.service;

import com.sanosysalvos.authservice.entity.Role;
import com.sanosysalvos.authservice.entity.User;
import com.sanosysalvos.authservice.repository.RoleRepository;
import com.sanosysalvos.authservice.repository.UserRepository;
import com.sanosysalvos.authservice.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_assignsDefaultRoleWhenMissing() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setRole(null);

        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        CustomUserDetails details = (CustomUserDetails) service.loadUserByUsername("user");

        assertEquals("ROLE_USER", details.getAuthorities().iterator().next().getAuthority());
        verify(userRepository).save(user);
    }

    @Test
    void loadUserByUsername_throwsWhenMissing() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("user"));
    }

    @Test
    void loadUserById_returnsUserDetails() {
        UUID userId = UUID.randomUUID();
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User user = new User();
        user.setId(userId);
        user.setUsername("user");
        user.setRole(role);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        CustomUserDetails details = (CustomUserDetails) service.loadUserById(userId);

        assertEquals(userId.toString(), details.getUserId());
        verify(userRepository, never()).save(user);
    }
}
