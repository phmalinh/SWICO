package com.swico.swico.service;

import com.swico.swico.dto.ProductionCalculationRequest;
import com.swico.swico.dto.ProductionCalculationResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductionFormulaServiceTest {

    private final ProductionFormulaService service = new ProductionFormulaService();

    @Test
    void productionEfficiencyShouldUseInputCycleTimeOperatingMinutesAndDeduction() {
        ProductionCalculationResponse withoutDeduction = service.calculate(request(40, 40, 0, new BigDecimal("46.67"), 395), 415);
        ProductionCalculationResponse withDeduction = service.calculate(request(59, 52, 6, new BigDecimal("601"), 620), 660);

        assertEquals(new BigDecimal("0.0788"), withoutDeduction.productionEfficiency());
        assertEquals(new BigDecimal("0.8542"), withDeduction.productionEfficiency());
    }

    private ProductionCalculationRequest request(int input, int good, int internalDefect, BigDecimal cycleTime, int operatingMinutes) {
        int externalDefect = Math.max(input - good - internalDefect, 0);
        return new ProductionCalculationRequest(
                LocalDate.of(2026, 8, 18),
                "A1",
                "白班 06:00-14:00 (Ca Ngày)",
                "MC-01",
                "PN-001",
                "Part",
                cycleTime,
                List.of(),
                operatingMinutes,
                20,
                input,
                good,
                input - good,
                internalDefect,
                externalDefect,
                "SWICO",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
