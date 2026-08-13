package com.swico.swico.service;

import com.swico.swico.dto.ProductionCalculationRequest;
import com.swico.swico.dto.ProductionCalculationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

@Service
public class ProductionFormulaService {

    private static final Map<String, Integer> SHIFT_MINUTES = Map.of(
            "白班 06:00-14:00 (Ca Ngày)", 440,
            "中班 14:00-22:00 (Ca Chiều)", 440,
            "夜班 22:00-06:00 (Ca Đêm)", 425,
            "全天1 06:00-18:00", 670,
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
        if (operatingMinutes != null && cycleTimeSeconds != null && cycleTimeSeconds.compareTo(BigDecimal.ZERO) > 0) {
            if (operatingMinutes > 0) {
                dailyTarget = BigDecimal.valueOf(operatingMinutes)
                        .multiply(BigDecimal.valueOf(60))
                        .divide(cycleTimeSeconds, 4, RoundingMode.HALF_UP);
            }
        }

        BigDecimal productionEfficiency = null;
        if (dailyTarget != null && dailyTarget.compareTo(BigDecimal.ZERO) > 0) {
            productionEfficiency = BigDecimal.valueOf(inputQuantity)
                    .divide(dailyTarget, 4, RoundingMode.HALF_UP);
        }

        BigDecimal availabilityRate = null;
        BigDecimal performanceRate = null;
        BigDecimal qualityRate = null;
        BigDecimal oee = null;

        if (downtimeMinutes != null && shiftMinutes != null && shiftMinutes > 0) {
            availabilityRate = BigDecimal.valueOf(Math.max(shiftMinutes - downtimeMinutes, 0))
                    .divide(BigDecimal.valueOf(shiftMinutes), 4, RoundingMode.HALF_UP);
        } else if (operatingMinutes != null && operatingMinutes > 0 && downtimeMinutes != null) {
            availabilityRate = BigDecimal.valueOf(Math.max(operatingMinutes - downtimeMinutes, 0))
                    .divide(BigDecimal.valueOf(operatingMinutes), 4, RoundingMode.HALF_UP);
        }

        if (dailyTarget != null && dailyTarget.compareTo(BigDecimal.ZERO) > 0) {
            performanceRate = BigDecimal.valueOf(inputQuantity)
                    .divide(dailyTarget, 4, RoundingMode.HALF_UP);
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

        return new ProductionCalculationResponse(
                request.reportDate() != null ? request.reportDate() : LocalDate.now(),
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
                productionEfficiency,
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
