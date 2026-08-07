package com.swico.swico.dto;

import com.swico.swico.entity.User;

public record UserProfileResponse(
        String username,
        String fullName,
        String role,
        String lineCode,
        boolean active,
        boolean mustChangePassword
) {
    public static UserProfileResponse fromEntity(User user) {
        return new UserProfileResponse(
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                user.getLineCode(),
                user.isActive(),
                user.isMustChangePassword()
        );
    }
}
