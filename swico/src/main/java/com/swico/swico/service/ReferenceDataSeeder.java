package com.swico.swico.service;

import com.swico.swico.config.AppClock;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "app.seed.reference-data", havingValue = "true")
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
            LocalDate today = AppClock.today();
            seedReport(today, a1, day, "TC-31", p1, 420, 30, 1800, 1750, 50);
            seedReport(today, a2, day, "TC-41", p2, 400, 45, 2500, 2400, 100);
            seedReport(today, a4, day, "TC-42", p3, 450, 20, 1600, 1550, 50);
            seedReport(today, b1, day, "PK-01", p4, 380, 60, 900, 850, 50);
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
        downtimeReasonService.ensureCategory("1", "Thay dao, thay khuon, chinh may", 1);
        downtimeReasonService.ensureCategory("2", "Cho dung cu, cho vat lieu, cho cong doan truoc", 2);
        downtimeReasonService.ensureCategory("3", "Bat thuong thiet bi va nghiep vu", 3);
        downtimeReasonService.ensureCategory("4", "Bat thuong chat luong", 4);
        downtimeReasonService.ensureCategory("5", "Yeu to quan ly, nhan su", 5);
        downtimeReasonService.ensureCategory("6", "Ngung may ke hoach, ve sinh, bao duong", 6);

        downtimeReasonService.ensure("1-1", "Thay dao (dao phay mat tho + tinh, dao moc lo, mui khoan, ...)", "1", 1);
        downtimeReasonService.ensure("1-2", "Het da, thay da (doi voi to Mai)", "1", 2);
        downtimeReasonService.ensure("1-3", "Cho can bo chinh may", "1", 3);
        downtimeReasonService.ensure("1-4", "Don ve sinh", "1", 4);
        downtimeReasonService.ensure("2-1", "Cho dao/dung cu kiem", "2", 5);
        downtimeReasonService.ensure("2-2", "Ngung may cho phoi", "2", 6);
        downtimeReasonService.ensure("2-3", "Cho hang cong doan truoc", "2", 7);
        downtimeReasonService.ensure("3-1", "May hu", "3", 8);
        downtimeReasonService.ensure("3-2", "Xon hang di tham nhot", "3", 9);
        downtimeReasonService.ensure("3-3", "Cup dien/cup nuoc", "3", 10);
        downtimeReasonService.ensure("4-1", "Cho QC xac nhan hang chinh may", "4", 11);
        downtimeReasonService.ensure("4-2", "Xu ly hang toan kiem", "4", 12);
        downtimeReasonService.ensure("5-1", "Nhan vien thao tac nghi phep", "5", 13);
        downtimeReasonService.ensure("5-2", "Hop", "5", 14);
        downtimeReasonService.ensure("5-3", "Dao tao nhan vien", "5", 15);
        downtimeReasonService.ensure("6-1", "Ve sinh may cuoi ca/cuoi tuan", "6", 16);
        downtimeReasonService.ensure("6-2", "Bao duong may dinh ky", "6", 17);
        downtimeReasonService.ensure("6-3", "Kiem ke dinh ky", "6", 18);
        downtimeReasonService.ensure("6-4", "Khong co lenh san xuat", "6", 19);
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
