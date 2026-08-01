package com.swico.swico.dto;

public record MachineResponse(
        Long id,
        String machineCode,
        String lineCode,
        String description
) {}
