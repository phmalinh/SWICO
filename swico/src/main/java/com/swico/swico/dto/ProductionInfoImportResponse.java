package com.swico.swico.dto;

public record ProductionInfoImportResponse(
        int productsImported,
        int processesImported,
        int rowsSkipped
) {}
