package com.swico.swico.dto;

import java.math.BigDecimal;

public record ProcessDto(
        Long id,
        Long productId,
        String processCode,
        String process,
        Integer sequence,
        String lineCode,
        String machineCode,
        BigDecimal cycleTimeSeconds
) {}
