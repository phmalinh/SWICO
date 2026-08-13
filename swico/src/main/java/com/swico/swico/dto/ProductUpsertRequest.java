package com.swico.swico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductUpsertRequest(
        @NotBlank String partNumber,
        @NotBlank String partName,
        String customer,
        @NotNull BigDecimal cycleTimeSeconds
) {}
