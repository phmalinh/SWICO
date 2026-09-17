package com.swico.swico.dto;

public record ProductionReportDowntimeDto(
        Long id,
        String reasonCategoryCode,
        String reason,
        Integer minutes,
        String lotNo
) {}
