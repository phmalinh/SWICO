package com.swico.swico.dto;

public record DowntimeReasonCategoryResponse(
        Long id,
        String reasonCategoryCode,
        String reasonCategoryText,
        Integer sortOrder,
        Boolean active
) {}
