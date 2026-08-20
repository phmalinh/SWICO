package com.swico.swico.dto;

public record UserImportResponse(
        int created,
        int updated,
        int skipped
) {
}
