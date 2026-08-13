package com.swico.swico.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProductionCalculationRequest(
        LocalDate reportDate,
        @NotBlank String lineCode,
        @NotBlank String shiftName,
        @NotBlank String machineCode,
        @NotBlank String partNumber,
        @NotBlank String partName,
        @NotNull @Min(1) BigDecimal cycleTimeSeconds,
        List<Long> processIds,
        @NotNull @Min(0) Integer totalOperatingMinutes,
        @NotNull @Min(0) Integer downtimeMinutes,
        @NotNull @Min(0) Integer inputQuantity,
        @NotNull @Min(0) Integer goodQuantity,
        @NotNull @Min(0) Integer defectQuantity,
        @Min(0) Integer internalDefectQuantity,
        @Min(0) Integer externalDefectQuantity,
        String company,
        String downtimeReason,
        String responsibility,
        BigDecimal deductionPercent,
        // Optional: overrides supplied by import file. If present they will be used instead of calculated values.
        Integer shiftStandardTimeMinutes,
        BigDecimal dailyTargetQuantity,
        BigDecimal productionEfficiency,
        BigDecimal availabilityRate,
        BigDecimal performanceRate,
        BigDecimal qualityRate,
        BigDecimal oee,
        String evaluationLabel
) {}
