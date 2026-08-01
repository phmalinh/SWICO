package com.swico.swico.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShiftUpsertRequest(
        @NotBlank String shiftName,
        @NotNull @Min(1) Integer standardTimeMinutes
) {}
