package com.swico.swico.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductionCalculationResponse(
        LocalDate reportDate,
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
        Integer shiftStandardTimeMinutes,
        BigDecimal dailyTargetQuantity,
        BigDecimal productionEfficiency,
        BigDecimal availabilityRate,
        BigDecimal performanceRate,
        BigDecimal qualityRate,
        BigDecimal oee,
        String company,
        String downtimeReason,
        String evaluationLabel
) {}
