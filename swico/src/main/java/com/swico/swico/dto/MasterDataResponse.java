package com.swico.swico.dto;

import java.math.BigDecimal;

public record MasterDataResponse(
        Long id,
        String code,
        String name,
        String customer,
        BigDecimal cycleTimeSeconds,
        Integer standardTimeMinutes
) {}
