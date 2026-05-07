package com.sanosysalvos.authservice.dto;

import java.util.Set;

public class AuthResponse {

    private String token;
    private String refreshToken;
    private String username;
    private Set<Long> roles;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(String token, String refreshToken, String username, Set<Long> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.roles = roles;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Long> getRoles() {
        return roles;
    }

    public void setRoles(Set<Long> roles) {
        this.roles = roles;
    }
}