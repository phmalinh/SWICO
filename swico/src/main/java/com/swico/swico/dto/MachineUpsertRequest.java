package com.swico.swico.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record MachineUpsertRequest(
        @NotBlank String machineCode,
        String description,
        String lineCode,
        String assetCode,
        LocalDate purchaseDate,
        String custodyDepartment
) {}
