package com.swico.swico.dto;

public record LineMachineImportResponse(
        int linesImported,
        int machinesImported,
        int rowsSkipped
) {}
