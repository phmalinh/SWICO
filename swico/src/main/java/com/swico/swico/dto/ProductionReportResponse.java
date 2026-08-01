package com.swico.swico.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductionReportResponse(
        Long id,
        String reportDate,
        String lineCode,
        String shiftName,
        String machineCode,
        String partNumber,
        String partName,
        BigDecimal cycleTimeSeconds,
        Integer totalOperatingMinutes,
        Integer downtimeMinutes,
        Integer inputQuantity,
        Integer goodQuantity,
        Integer defectQuantity,
        String company,
        String downtimeReason,
        Integer shiftStandardTimeMinutes,
        BigDecimal dailyTargetQuantity,
        BigDecimal productionEfficiency,
        BigDecimal availabilityRate,
        BigDecimal performanceRate,
        BigDecimal qualityRate,
        BigDecimal oee,
        String evaluationLabel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy
) {}
