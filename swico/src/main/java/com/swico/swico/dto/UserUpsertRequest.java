package com.swico.swico.dto;

public record UserUpsertRequest(
        String username,
        String fullName,
        String password,
        String role,
        String lineCode,
        Boolean active
) {
}
