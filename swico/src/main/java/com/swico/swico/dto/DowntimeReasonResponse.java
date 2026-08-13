package com.swico.swico.dto;

public record DowntimeReasonResponse(
        Long id,
        String reasonCode,
        String reasonText,
        Integer sortOrder,
        Boolean active
) {}
