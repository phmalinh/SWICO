package com.swico.swico.dto;

import jakarta.validation.constraints.NotBlank;

public record DowntimeReasonCategoryUpsertRequest(
        @NotBlank String reasonCategoryCode,
        @NotBlank String reasonCategoryText,
        Integer sortOrder,
        Boolean active
) {}
