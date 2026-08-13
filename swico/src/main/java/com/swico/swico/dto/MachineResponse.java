package com.swico.swico.dto;

public record MachineResponse(
        Long id,
        String machineCode,
        String description,
        String lineCode,
        String assetCode,
        String purchaseDate,
        String custodyDepartment
) {}
