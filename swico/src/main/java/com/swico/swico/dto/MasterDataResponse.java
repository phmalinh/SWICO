package com.swico.swico.dto;

import java.math.BigDecimal;

public record MasterDataResponse(
        Long id,
        String code,
        String name,
        BigDecimal cycleTimeSeconds,
        Integer standardTimeMinutes
) {}
