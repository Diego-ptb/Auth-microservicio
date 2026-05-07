package com.sanosysalvos.authservice.dto;

import java.util.Set;

public class UserInfo {

    private String username;
    private String email;
    private Set<Long> roles;

    // Constructor
    public UserInfo(String username, String email, Set<Long> roles) {
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Set<Long> getRoles() {
        return roles;
    }
}