package com.swico.swico.service;

import com.swico.swico.config.AppClock;
import com.swico.swico.dto.ProductionCalculationRequest;
import com.swico.swico.dto.ProductionCalculationResponse;
import com.swico.swico.dto.ProductionReportDowntimeDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ProductionFormulaService {

    private static final String SAME_MACHINE_CHANGEOVER_KEYWORD = "chuyen ma hang gia cong cung may";

    private static final Map<String, Integer> SHIFT_MINUTES = Map.of(
            "白班 06:00-14:00 (Ca Ngày)", 440,
            "中班 14:00-22:00 (Ca Chiều)", 440,
            "夜班 22:00-06:00 (Ca Đêm)", 425,
            "全天1 06:00-18:00", 660,
            "全天2 18:00-06:00", 635
    );

    public ProductionCalculationResponse calculate(ProductionCalculationRequest request, Integer resolvedShiftMinutes) {
        BigDecimal cycleTimeSeconds = request.cycleTimeSeconds();
        Integer operatingMinutes = request.totalOperatingMinutes();
        Integer downtimeMinutes = request.downtimeMinutes();
        Integer inputQuantity = request.inputQuantity();
        Integer goodQuantity = request.goodQuantity();
        Integer defectQuantity = request.defectQuantity();
        Integer internalDefectQuantity = request.internalDefectQuantity() != null ? request.internalDefectQuantity() : 0;

        Integer shiftMinutes = resolvedShiftMinutes != null ? resolvedShiftMinutes : resolveShiftMinutes(request.shiftName());

        BigDecimal dailyTarget = null;
        BigDecimal dailyTargetDay = null;
        if (operatingMinutes != null && cycleTimeSeconds != null && cycleTimeSeconds.compareTo(BigDecimal.ZERO) > 0) {
            if (operatingMinutes > 0) {
                dailyTarget = BigDecimal.valueOf(operatingMinutes)
                        .multiply(BigDecimal.valueOf(60))
                        .divide(cycleTimeSeconds, 4, RoundingMode.HALF_UP);
            }
        }
        if (shiftMinutes != null && shiftMinutes > 0 && cycleTimeSeconds != null && cycleTimeSeconds.compareTo(BigDecimal.ZERO) > 0) {
            dailyTargetDay = BigDecimal.valueOf(shiftMinutes)
                    .multiply(BigDecimal.valueOf(60))
                    .divide(cycleTimeSeconds, 4, RoundingMode.HALF_UP);
        }

        BigDecimal productionEfficiency = null;
        BigDecimal dailyTargetEfficiency = null;
        BigDecimal availabilityRate = null;
        BigDecimal performanceRate = null;
        BigDecimal qualityRate = null;
        BigDecimal oee = null;

        Integer availabilityDowntimeMinutes = availabilityDowntimeMinutes(request);
        if (availabilityDowntimeMinutes != null && shiftMinutes != null && shiftMinutes > 0) {
            availabilityRate = BigDecimal.valueOf(Math.max(shiftMinutes - availabilityDowntimeMinutes, 0))
                    .divide(BigDecimal.valueOf(shiftMinutes), 4, RoundingMode.HALF_UP);
        } else if (operatingMinutes != null && operatingMinutes > 0 && availabilityDowntimeMinutes != null) {
            availabilityRate = BigDecimal.valueOf(Math.max(operatingMinutes - availabilityDowntimeMinutes, 0))
                    .divide(BigDecimal.valueOf(operatingMinutes), 4, RoundingMode.HALF_UP);
        }

        if (dailyTarget != null && dailyTarget.compareTo(BigDecimal.ZERO) > 0) {
            performanceRate = BigDecimal.valueOf(inputQuantity)
                    .divide(dailyTarget, 4, RoundingMode.HALF_UP);
        }

        if (dailyTargetDay != null && dailyTargetDay.compareTo(BigDecimal.ZERO) > 0 && inputQuantity != null) {
            dailyTargetEfficiency = BigDecimal.valueOf(inputQuantity)
                    .divide(dailyTargetDay, 4, RoundingMode.HALF_UP);
        }

        if (inputQuantity != null && inputQuantity > 0) {
            qualityRate = BigDecimal.valueOf(goodQuantity)
                    .divide(BigDecimal.valueOf(inputQuantity), 4, RoundingMode.HALF_UP);
        }

        if (availabilityRate != null && performanceRate != null && qualityRate != null) {
            oee = availabilityRate.multiply(performanceRate).multiply(qualityRate).setScale(4, RoundingMode.HALF_UP);
        }

        BigDecimal responsibility = BigDecimal.ZERO;
        if (inputQuantity != null && inputQuantity > 0) {
            responsibility = BigDecimal.valueOf(internalDefectQuantity)
                    .divide(BigDecimal.valueOf(inputQuantity), 4, RoundingMode.HALF_UP);
        }

        BigDecimal deductionPercent = responsibility.subtract(new BigDecimal("0.0027"));
        if (deductionPercent.compareTo(BigDecimal.ZERO) < 0) {
            deductionPercent = BigDecimal.ZERO;
        }
        deductionPercent = deductionPercent.setScale(4, RoundingMode.HALF_UP);

        if (operatingMinutes != null && operatingMinutes > 0 && cycleTimeSeconds != null && cycleTimeSeconds.compareTo(BigDecimal.ZERO) > 0 && inputQuantity != null) {
            BigDecimal baseEfficiency = BigDecimal.valueOf(inputQuantity)
                    .multiply(cycleTimeSeconds.divide(BigDecimal.valueOf(60), 8, RoundingMode.HALF_UP))
                    .divide(BigDecimal.valueOf(operatingMinutes), 4, RoundingMode.HALF_UP);
            productionEfficiency = responsibility.compareTo(new BigDecimal("0.0027")) > 0
                    ? baseEfficiency.subtract(deductionPercent).setScale(4, RoundingMode.HALF_UP)
                    : baseEfficiency;
        }

        return new ProductionCalculationResponse(
                request.reportDate() != null ? request.reportDate() : AppClock.today(),
                request.lineCode(),
                request.shiftName(),
                request.machineCode(),
                request.partNumber(),
                request.partName(),
                cycleTimeSeconds,
                operatingMinutes,
                downtimeMinutes,
                inputQuantity,
                goodQuantity,
                defectQuantity,
                shiftMinutes,
                dailyTarget,
                dailyTargetDay,
                productionEfficiency,
                dailyTargetEfficiency,
                availabilityRate,
                performanceRate,
                qualityRate,
            oee,
            request.company(),
            request.downtimeReason(),
            responsibility,
            deductionPercent,
            evaluationLabel(oee)
        );
    }

    public Integer resolveShiftMinutes(String shiftName) {
        return SHIFT_MINUTES.get(shiftName);
    }

    private Integer availabilityDowntimeMinutes(ProductionCalculationRequest request) {
        List<ProductionReportDowntimeDto> downtimeRows = request.downtimeRows();
        if (downtimeRows != null && !downtimeRows.isEmpty()) {
            return downtimeRows.stream()
                    .filter(row -> row != null && !isSameMachineChangeover(row.reason()))
                    .map(ProductionReportDowntimeDto::minutes)
                    .mapToInt(this::number)
                    .sum();
        }

        Integer downtimeMinutes = request.downtimeMinutes();
        if (downtimeMinutes == null) {
            return null;
        }
        if (isSameMachineChangeover(request.downtimeReason())) {
            return 0;
        }
        return downtimeMinutes;
    }

    private int number(Integer value) {
        return value != null ? Math.max(value, 0) : 0;
    }

    private boolean isSameMachineChangeover(String downtimeReason) {
        return normalizeText(downtimeReason).contains(SAME_MACHINE_CHANGEOVER_KEYWORD);
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    public String evaluationLabel(BigDecimal oee) {
        if (oee == null) {
            return null;
        }
        if (oee.compareTo(new BigDecimal("0.85")) >= 0) {
            return "優秀 Xuất Sắc - 世界級";
        }
        if (oee.compareTo(new BigDecimal("0.60")) >= 0) {
            return "合格 Đạt - 平均";
        }
        if (oee.compareTo(new BigDecimal("0.40")) >= 0) {
            return "需改善 Cần cải tiến";
        }
        return "差 Kém - 需檢討";
    }
}
