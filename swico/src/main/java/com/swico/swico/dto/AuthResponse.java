package com.swico.swico.dto;

public record AuthResponse(
        String token,
        String username,
        String fullName,
        String role,
        boolean mustChangePassword
) {}
