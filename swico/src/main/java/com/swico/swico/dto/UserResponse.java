package com.swico.swico.dto;

import com.swico.swico.entity.User;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String role,
        String lineCode,
        boolean active
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                user.getLineCode(),
                user.isActive()
        );
    }
}
