package com.swico.swico.dto;

import jakarta.validation.constraints.NotBlank;

public record LineUpsertRequest(
        @NotBlank String lineCode,
        @NotBlank String description
) {}
