package com.swico.swico.dto;

import jakarta.validation.constraints.NotBlank;

public record MachineUpsertRequest(
        @NotBlank String machineCode,
        String description
) {}
