package com.swico.swico.dto;

import java.math.BigDecimal;

public record LineDashboardResponse(
        String lineCode,
        BigDecimal oee,
        BigDecimal availabilityRate,
        BigDecimal performanceRate,
        BigDecimal qualityRate,
        String status
) {}
