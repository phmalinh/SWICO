package com.swico.swico.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record PivotExportRequest(
        @NotNull @Valid PivotSheet quantity,
        @NotNull @Valid PivotSheet rates
) {
    public record PivotSheet(
            @NotBlank @Size(max = 31) String name,
            @NotNull @Size(min = 5, max = 6) List<@NotBlank String> headers,
            @NotNull List<@Valid PivotRow> rows
    ) {}

    public record PivotRow(
            @NotNull String label,
            @Min(0) @Max(2) int level,
            boolean total,
            @NotNull @Size(min = 4, max = 5) List<BigDecimal> values
    ) {}
}
