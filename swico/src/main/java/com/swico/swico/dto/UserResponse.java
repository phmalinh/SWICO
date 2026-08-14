package com.swico.swico.dto;

import com.swico.swico.entity.User;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String role,
        String lineCode,
        String jobTitle,
        String team,
        String hireDate,
        boolean active,
        boolean mustChangePassword
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                user.getLineCode(),
                user.getJobTitle(),
                user.getTeam(),
                user.getHireDate() != null ? user.getHireDate().toString() : null,
                user.isActive(),
                user.isMustChangePassword()
        );
    }
}
