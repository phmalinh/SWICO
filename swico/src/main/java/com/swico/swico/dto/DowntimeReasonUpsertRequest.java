package com.swico.swico.dto;

import jakarta.validation.constraints.NotBlank;

public record DowntimeReasonUpsertRequest(
        @NotBlank String reasonCode,
        @NotBlank String reasonText,
        String reasonCategoryCode,
        Integer sortOrder,
        Boolean active
) {}
