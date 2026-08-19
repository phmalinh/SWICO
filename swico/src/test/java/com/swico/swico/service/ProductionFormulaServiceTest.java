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

    @Test
    void availabilityShouldUseShiftMinutesMinusDowntimeOverShiftMinutes() {
        ProductionCalculationResponse response = service.calculate(request(59, 52, 6, new BigDecimal("601"), 620, 40), 660);

        assertEquals(new BigDecimal("0.9394"), response.availabilityRate());
    }

    @Test
    void availabilityShouldBeOneForSameMachineChangeover() {
        ProductionCalculationResponse response = service.calculate(
                request(59, 52, 6, new BigDecimal("601"), 620, 40, "chuyển mã hàng gia công cùng máy"),
                660
        );

        assertEquals(new BigDecimal("1.0000"), response.availabilityRate());
    }

    private ProductionCalculationRequest request(int input, int good, int internalDefect, BigDecimal cycleTime, int operatingMinutes) {
        return request(input, good, internalDefect, cycleTime, operatingMinutes, 20);
    }

    private ProductionCalculationRequest request(int input, int good, int internalDefect, BigDecimal cycleTime, int operatingMinutes, int downtimeMinutes) {
        return request(input, good, internalDefect, cycleTime, operatingMinutes, downtimeMinutes, null);
    }

    private ProductionCalculationRequest request(int input, int good, int internalDefect, BigDecimal cycleTime, int operatingMinutes, int downtimeMinutes, String downtimeReason) {
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
                downtimeMinutes,
                input,
                good,
                input - good,
                internalDefect,
                externalDefect,
                "SWICO",
                null,
                downtimeReason,
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
