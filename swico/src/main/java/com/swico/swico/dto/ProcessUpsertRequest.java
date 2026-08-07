package com.swico.swico.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record ProcessUpsertRequest(
        @NotBlank String process,
        Integer sequence,
        String lineCode,
        String machineCode,
        BigDecimal cycleTimeSeconds
) {}
