package com.swico.swico.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        String reportDate,
        BigDecimal averageOee,
        BigDecimal averageAvailability,
        BigDecimal averagePerformance,
        BigDecimal averageQuality,
        Long activeLines,
        Long warningCount,
        List<LineDashboardResponse> lines
) {}
