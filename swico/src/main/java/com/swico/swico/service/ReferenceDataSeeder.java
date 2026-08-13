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

    private static final String DAY_SHIFT_NAME = "Ca Ngay 06:00-14:00";

    private final LineRepository lineRepository;
    private final ShiftRepository shiftRepository;
    private final ProductRepository productRepository;
    private final MachineRepository machineRepository;
    private final DailyProductionReportRepository reportRepository;
    private final ProductionFormulaService formulaService;
    private final DowntimeReasonService downtimeReasonService;

    public ReferenceDataSeeder(
            LineRepository lineRepository,
            ShiftRepository shiftRepository,
            ProductRepository productRepository,
            MachineRepository machineRepository,
            DailyProductionReportRepository reportRepository,
            ProductionFormulaService formulaService,
            DowntimeReasonService downtimeReasonService
    ) {
        this.lineRepository = lineRepository;
        this.shiftRepository = shiftRepository;
        this.productRepository = productRepository;
        this.machineRepository = machineRepository;
        this.reportRepository = reportRepository;
        this.formulaService = formulaService;
        this.downtimeReasonService = downtimeReasonService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        Line a1 = ensureLine("A1", "Chuyen A1 - Lap rap");
        Line a2 = ensureLine("A2", "Chuyen A2 - Duc nhua");
        Line a4 = ensureLine("A4", "Chuyen A4 - Phun");
        Line b1 = ensureLine("B1", "Chuyen B1 - Dong goi");

        Shift day = ensureShift(DAY_SHIFT_NAME, 440);
        ensureShift("Ca Chieu 14:00-22:00", 440);
        ensureShift("Ca Dem 22:00-06:00", 425);
        ensureShift("Ca 1 06:00-18:00", 670);
        ensureShift("Ca 2 18:00-06:00", 635);
        seedDowntimeReasons();

        Product p1 = ensureProduct("PN-001", "Vo hop A1", new BigDecimal("12.5"));
        Product p2 = ensureProduct("PN-002", "Nap day B2", new BigDecimal("8"));
        Product p3 = ensureProduct("PN-003", "Khay nhua C3", new BigDecimal("15.2"));
        Product p4 = ensureProduct("PN-004", "Bo loc D4", new BigDecimal("22"));
        ensureProduct("PN-005", "Van xa E5", new BigDecimal("18.5"));

        if (machineRepository.count() == 0) {
            saveMachine("TC-31", "A1", "May ep TC-31");
            saveMachine("TC-32", "A1", "May ep TC-32");
            saveMachine("TC-41", "A2", "May phun TC-41");
            saveMachine("TC-42", "A4", "May phun TC-42");
            saveMachine("PK-01", "B1", "May dong goi PK-01");
        }

        if (reportRepository.count() == 0) {
            seedReport(LocalDate.now(), a1, day, "TC-31", p1, 420, 30, 1800, 1750, 50);
            seedReport(LocalDate.now(), a2, day, "TC-41", p2, 400, 45, 2500, 2400, 100);
            seedReport(LocalDate.now(), a4, day, "TC-42", p3, 450, 20, 1600, 1550, 50);
            seedReport(LocalDate.now(), b1, day, "PK-01", p4, 380, 60, 900, 850, 50);
        }
    }

    private Line ensureLine(String code, String description) {
        return lineRepository.findByLineCode(code).orElseGet(() -> saveLine(code, description));
    }

    private Shift ensureShift(String name, int minutes) {
        return shiftRepository.findByShiftName(name).orElseGet(() -> saveShift(name, minutes));
    }

    private Product ensureProduct(String partNumber, String partName, BigDecimal cycleTimeSeconds) {
        return productRepository.findByPartNumber(partNumber).orElseGet(() -> saveProduct(partNumber, partName, cycleTimeSeconds));
    }

    private void seedDowntimeReasons() {
        downtimeReasonService.ensure("A", "\u63db\u5200\uff08\u7c97\uff0f\u7cbe\u9762\u92d1\u5200\u3001\u5167\u5b54\u93dc\u5200\u3001\u947d\u982d\u7b49\uff09 / Thay dao (dao phay mat tho + tinh, dao moc lo, mui khoan, ...)", 1);
        downtimeReasonService.ensure("B", "\u7802\u8f2a\u7528\u76e1\u3001\u66f4\u63db\u7802\u8f2a\uff08\u91dd\u5c0d\u78e8\u5e8a\u7d44\uff09 / Het da, thay da (doi voi to Mai)", 2);
        downtimeReasonService.ensure("C", "\u505c\u6a5f\u7b49\u6599\uff08\u7b49\u5f85\u6bdb\u576f\uff09 / Ngung may cho phoi", 3);
        downtimeReasonService.ensure("D", "\u7b49\u5f85\u524d\u5de5\u5e8f\u4f86\u6599\uff08\u91dd\u5c0d\u524d\u5de5\u5e8f C/T \u9577\u65bc\u5f8c\u5de5\u5e8f\uff09 / Cho hang cong doan truoc (doi voi cong doan dau C/T lau hon cong doan sau)", 4);
        downtimeReasonService.ensure("E", "\u7b49\u5f85\u8abf\u6a5f\u4eba\u54e1\uff08\u6280\u8853\u54e1\uff09\u8abf\u6a5f / Cho can bo chinh may", 5);
        downtimeReasonService.ensure("F", "\u7b49\u5f85\u54c1\u6aa2\uff08QC\uff09\u9996\u4ef6\u78ba\u8a8d\uff0f\u8abf\u6a5f\u54c1\u78ba\u8a8d / Cho QC xac nhan hang chinh may", 6);
        downtimeReasonService.ensure("G", "\u64cd\u4f5c\u4eba\u54e1\u8acb\u5047\uff08\u7121\u66ff\u4ee3\u4eba\u54e1\u6642\uff09 / Nhan vien thao tac nghi phep (khi khong co nguoi thay the)", 7);
        downtimeReasonService.ensure("H", "\u5176\u4ed6 / Khac", 8);
    }

    private Line saveLine(String code, String description) {
        Line line = new Line();
        line.setLineCode(code);
        line.setDescription(description);
        return lineRepository.save(line);
    }

    private Shift saveShift(String name, int minutes) {
        Shift shift = new Shift();
        shift.setShiftName(name);
        shift.setStandardTimeMinutes(minutes);
        return shiftRepository.save(shift);
    }

    private Product saveProduct(String partNumber, String partName, BigDecimal cycleTimeSeconds) {
        Product product = new Product();
        product.setPartNumber(partNumber);
        product.setPartName(partName);
        product.setCycleTimeSeconds(cycleTimeSeconds);
        return productRepository.save(product);
    }

    private void saveMachine(String machineCode, String lineCode, String description) {
        Machine machine = new Machine();
        machine.setMachineCode(machineCode);
        machine.setLine(lineRepository.findByLineCode(lineCode).orElse(null));
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
                null,
                operatingMinutes,
                downtimeMinutes,
                inputQuantity,
                goodQuantity,
                defectQuantity,
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
                null,
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
