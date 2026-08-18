package com.swico.swico.dto;

public record DowntimeReasonImportResponse(
        int categoriesImported,
        int reasonsImported
) {}
