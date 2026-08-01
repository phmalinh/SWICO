package com.swico.swico.service;

import com.swico.swico.dto.ProductionCalculationRequest;
import com.swico.swico.dto.ProductionCalculationResponse;
import com.swico.swico.entity.DailyProductionReport;
import com.swico.swico.entity.Line;
import com.swico.swico.entity.Machine;
import com.swico.swico.entity.Product;
import com.swico.swico.entity.Shift;
import com.swico.swico.repository.DailyProductionReportRepository;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.MachineRepository;
import com.swico.swico.repository.ProductRepository;
import com.swico.swico.repository.ShiftRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class ReferenceDataSeeder {

    private final LineRepository lineRepository;
    private final ShiftRepository shiftRepository;
    private final ProductRepository productRepository;
    private final MachineRepository machineRepository;
    private final DailyProductionReportRepository reportRepository;
    private final ProductionFormulaService formulaService;

    public ReferenceDataSeeder(
            LineRepository lineRepository,
            ShiftRepository shiftRepository,
            ProductRepository productRepository,
            MachineRepository machineRepository,
            DailyProductionReportRepository reportRepository,
            ProductionFormulaService formulaService
    ) {
        this.lineRepository = lineRepository;
        this.shiftRepository = shiftRepository;
        this.productRepository = productRepository;
        this.machineRepository = machineRepository;
        this.reportRepository = reportRepository;
        this.formulaService = formulaService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (lineRepository.count() == 0) {
            saveLine("A1", "Chuyen A1 - Lap rap");
            saveLine("A2", "Chuyen A2 - Duc nhua");
            saveLine("A4", "Chuyen A4 - Phun");
            saveLine("B1", "Chuyen B1 - Dong goi");
        }

        if (shiftRepository.count() == 0) {
            saveShift("白班 06:00-14:00 (Ca Ngay)", 440);
            saveShift("中班 14:00-22:00 (Ca Chieu)", 440);
            saveShift("夜班 22:00-06:00 (Ca Dem)", 425);
            saveShift("全天1 06:00-18:00", 670);
            saveShift("全天2 18:00-06:00", 635);
        }

        if (productRepository.count() == 0) {
            saveProduct("PN-001", "Vo hop A1", new BigDecimal("12.5"));
            saveProduct("PN-002", "Nap day B2", new BigDecimal("8"));
            saveProduct("PN-003", "Khay nhua C3", new BigDecimal("15.2"));
            saveProduct("PN-004", "Bo loc D4", new BigDecimal("22"));
            saveProduct("PN-005", "Van xa E5", new BigDecimal("18.5"));
        }

        if (machineRepository.count() == 0) {
            saveMachine("TC-31", "A1", "May ep TC-31");
            saveMachine("TC-32", "A1", "May ep TC-32");
            saveMachine("TC-41", "A2", "May phun TC-41");
            saveMachine("TC-42", "A4", "May phun TC-42");
            saveMachine("PK-01", "B1", "May dong goi PK-01");
        }

        if (reportRepository.count() == 0) {
            Line a1 = lineRepository.findByLineCode("A1").orElseThrow();
            Line a2 = lineRepository.findByLineCode("A2").orElseThrow();
            Line a4 = lineRepository.findByLineCode("A4").orElseThrow();
            Line b1 = lineRepository.findByLineCode("B1").orElseThrow();
            Shift day = shiftRepository.findByShiftName("白班 06:00-14:00 (Ca Ngay)").orElseThrow();
            Product p1 = productRepository.findByPartNumber("PN-001").orElseThrow();
            Product p2 = productRepository.findByPartNumber("PN-002").orElseThrow();
            Product p3 = productRepository.findByPartNumber("PN-003").orElseThrow();
            Product p4 = productRepository.findByPartNumber("PN-004").orElseThrow();

            seedReport(LocalDate.now(), a1, day, "TC-31", p1, 420, 30, 1800, 1750, 50);
            seedReport(LocalDate.now(), a2, day, "TC-41", p2, 400, 45, 2500, 2400, 100);
            seedReport(LocalDate.now(), a4, day, "TC-42", p3, 450, 20, 1600, 1550, 50);
            seedReport(LocalDate.now(), b1, day, "PK-01", p4, 380, 60, 900, 850, 50);
        }
    }

    private void saveLine(String code, String description) {
        Line line = new Line();
        line.setLineCode(code);
        line.setDescription(description);
        lineRepository.save(line);
    }

    private void saveShift(String name, int minutes) {
        Shift shift = new Shift();
        shift.setShiftName(name);
        shift.setStandardTimeMinutes(minutes);
        shiftRepository.save(shift);
    }

    private void saveProduct(String partNumber, String partName, BigDecimal cycleTimeSeconds) {
        Product product = new Product();
        product.setPartNumber(partNumber);
        product.setPartName(partName);
        product.setCycleTimeSeconds(cycleTimeSeconds);
        productRepository.save(product);
    }

    private void saveMachine(String machineCode, String lineCode, String description) {
        Machine machine = new Machine();
        machine.setMachineCode(machineCode);
        machine.setLine(lineRepository.findByLineCode(lineCode).orElseThrow());
        machine.setDescription(description);
        machineRepository.save(machine);
    }

    private void seedReport(LocalDate reportDate, Line line, Shift shift, String machineCode, Product product,
                            int operatingMinutes, int downtimeMinutes, int inputQuantity, int goodQuantity, int defectQuantity) {
        ProductionCalculationRequest request = new ProductionCalculationRequest(
                reportDate,
                line.getLineCode(),
                shift.getShiftName(),
                machineCode,
                product.getPartNumber(),
                product.getPartName(),
                product.getCycleTimeSeconds(),
                operatingMinutes,
                downtimeMinutes,
                inputQuantity,
                goodQuantity,
                defectQuantity,
                null,
                null
        );
        ProductionCalculationResponse calculated = formulaService.calculate(request, shift.getStandardTimeMinutes());

        DailyProductionReport report = new DailyProductionReport();
        report.setReportDate(reportDate);
        report.setLine(line);
        report.setShift(shift);
        report.setMachineCode(machineCode);
        report.setProduct(product);
        report.setTotalOperatingMinutes(operatingMinutes);
        report.setDowntimeMinutes(downtimeMinutes);
        report.setInputQuantity(inputQuantity);
        report.setGoodQuantity(goodQuantity);
        report.setDefectQuantity(defectQuantity);
        report.setTargetQuantity(calculated.dailyTargetQuantity());
        report.setAvailabilityRate(calculated.availabilityRate());
        report.setPerformanceRate(calculated.performanceRate());
        report.setQualityRate(calculated.qualityRate());
        report.setOee(calculated.oee());
                report.setCompany(calculated.company());
                report.setDowntimeReason(calculated.downtimeReason());
        reportRepository.save(report);
    }
}
