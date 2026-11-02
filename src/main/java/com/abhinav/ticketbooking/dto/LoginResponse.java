package com.abhinav.ticketbooking.dto;

public class LoginResponse {

    private String token;
    private String tokenType;
    private String email;
    private String name;
    private String role;
    private Long userId;

    public LoginResponse() {
    }

    public LoginResponse(
            String token,
            String tokenType,
            String email
    ) {
        this.token = token;
        this.tokenType = tokenType;
        this.email = email;
    }

    public LoginResponse(
            String token,
            String tokenType,
            String email,
            String name,
            String role,
            Long userId
    ) {
        this.token = token;
        this.tokenType = tokenType;
        this.email = email;
        this.name = name;
        this.role = role;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
