package com.swico.swico.service;

import com.swico.swico.dto.ProductionCalculationRequest;
import com.swico.swico.dto.ProductionCalculationResponse;
import com.swico.swico.dto.ProductionReportDowntimeDto;
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
    void availabilityShouldIgnoreSameMachineChangeoverDowntime() {
        ProductionCalculationResponse response = service.calculate(
                request(59, 52, 6, new BigDecimal("601"), 620, 40, "chuyển mã hàng gia công cùng máy"),
                660
        );

        assertEquals(new BigDecimal("1.0000"), response.availabilityRate());
    }

    @Test
    void availabilityShouldOnlyCountNonChangeoverDowntimeRows() {
        ProductionCalculationResponse response = service.calculate(
                request(
                        59,
                        52,
                        6,
                        new BigDecimal("601"),
                        620,
                        360,
                        "chuyển mã - 340； hỏng máy - 20",
                        List.of(
                                new ProductionReportDowntimeDto(null, null, "chuyển mã", 340),
                                new ProductionReportDowntimeDto(null, null, "hỏng máy", 20)
                        )
                ),
                660
        );

        assertEquals(new BigDecimal("0.9697"), response.availabilityRate());
    }

    @Test
    void availabilityShouldUseSixHundredSixtyMinutesForFullDayShiftOne() {
        ProductionCalculationResponse response = service.calculate(
                request(
                        "全天1 06:00-18:00",
                        0,
                        0,
                        0,
                        new BigDecimal("22"),
                        60,
                        600,
                        "chuyển mã - 260； ve sinh may cuoi ca/cuoi tuan - 220； xon hang di tham nhot - 120",
                        List.of(
                                new ProductionReportDowntimeDto(null, null, "Chuyển mã hàng gia công cùng máy", 260),
                                new ProductionReportDowntimeDto(null, null, "Ve sinh may cuoi ca/cuoi tuan", 220),
                                new ProductionReportDowntimeDto(null, null, "Xon hang di tham nhot", 120)
                        )
                ),
                null
        );

        assertEquals(660, response.shiftStandardTimeMinutes());
        assertEquals(new BigDecimal("0.4848"), response.availabilityRate());
    }

    private ProductionCalculationRequest request(int input, int good, int internalDefect, BigDecimal cycleTime, int operatingMinutes) {
        return request(input, good, internalDefect, cycleTime, operatingMinutes, 20);
    }

    private ProductionCalculationRequest request(int input, int good, int internalDefect, BigDecimal cycleTime, int operatingMinutes, int downtimeMinutes) {
        return request(input, good, internalDefect, cycleTime, operatingMinutes, downtimeMinutes, null);
    }

    private ProductionCalculationRequest request(int input, int good, int internalDefect, BigDecimal cycleTime, int operatingMinutes, int downtimeMinutes, String downtimeReason) {
        return request(input, good, internalDefect, cycleTime, operatingMinutes, downtimeMinutes, downtimeReason, List.of());
    }

    private ProductionCalculationRequest request(int input, int good, int internalDefect, BigDecimal cycleTime, int operatingMinutes, int downtimeMinutes, String downtimeReason, List<ProductionReportDowntimeDto> downtimeRows) {
        return request("白班 06:00-14:00 (Ca Ngày)", input, good, internalDefect, cycleTime, operatingMinutes, downtimeMinutes, downtimeReason, downtimeRows);
    }

    private ProductionCalculationRequest request(String shiftName, int input, int good, int internalDefect, BigDecimal cycleTime, int operatingMinutes, int downtimeMinutes, String downtimeReason, List<ProductionReportDowntimeDto> downtimeRows) {
        int externalDefect = Math.max(input - good - internalDefect, 0);
        return new ProductionCalculationRequest(
                LocalDate.of(2026, 8, 18),
                "A1",
                shiftName,
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
                null,
                null,
                downtimeRows
        );
    }
}
