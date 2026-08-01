package com.swico.swico.service;

import com.swico.swico.dto.*;
import com.swico.swico.entity.DailyProductionReport;
import com.swico.swico.entity.Line;
import com.swico.swico.entity.Product;
import com.swico.swico.entity.Shift;
import com.swico.swico.repository.DailyProductionReportRepository;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.ProductRepository;
import com.swico.swico.repository.ShiftRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductionReportService {

    private final DailyProductionReportRepository reportRepository;
    private final LineRepository lineRepository;
    private final ProductRepository productRepository;
    private final ShiftRepository shiftRepository;
    private final MasterDataService masterDataService;
    private final ProductionFormulaService formulaService;

    public ProductionReportService(
            DailyProductionReportRepository reportRepository,
            LineRepository lineRepository,
            ProductRepository productRepository,
            ShiftRepository shiftRepository,
            MasterDataService masterDataService,
            ProductionFormulaService formulaService
    ) {
        this.reportRepository = reportRepository;
        this.lineRepository = lineRepository;
        this.productRepository = productRepository;
        this.shiftRepository = shiftRepository;
        this.masterDataService = masterDataService;
        this.formulaService = formulaService;
    }

    @Transactional
    public ProductionReportResponse createReport(ProductionCalculationRequest request, String createdBy) {
        String normalizedShiftName = request.shiftName() != null ? request.shiftName().trim() : null;
        Integer shiftMinutes = formulaService.resolveShiftMinutes(normalizedShiftName);
        if (shiftMinutes == null) {
            Shift shift = shiftRepository.findByShiftName(normalizedShiftName)
                    .orElseGet(() -> masterDataService.upsertShift(normalizedShiftName, request.totalOperatingMinutes()));
            shiftMinutes = shift.getStandardTimeMinutes();
        }
        final Integer effectiveShiftMinutes = shiftMinutes;

        Line line = lineRepository.findByLineCode(request.lineCode())
                .orElseGet(() -> masterDataService.upsertLine(request.lineCode(), request.lineCode()));
        Product product = productRepository.findByPartNumber(request.partNumber())
                .orElseGet(() -> masterDataService.upsertProduct(request.partNumber(), request.partName(), request.cycleTimeSeconds()));
        Shift shift = shiftRepository.findByShiftName(normalizedShiftName)
            .orElseGet(() -> masterDataService.upsertShift(normalizedShiftName, effectiveShiftMinutes));

        DailyProductionReport entity = new DailyProductionReport();
        entity.setReportDate(request.reportDate() != null ? request.reportDate() : LocalDate.now());
        entity.setLine(line);
        entity.setShift(shift);
        entity.setMachineCode(request.machineCode());
        entity.setProduct(product);
        entity.setTotalOperatingMinutes(request.totalOperatingMinutes());
        entity.setDowntimeMinutes(request.downtimeMinutes());
        entity.setInputQuantity(request.inputQuantity());
        entity.setGoodQuantity(request.goodQuantity());
        entity.setDefectQuantity(request.defectQuantity());
        entity.setCompany(request.company());
        entity.setCreatedBy(createdBy);
        entity.setDowntimeReason(request.downtimeReason());

        ProductionCalculationResponse calculated = formulaService.calculate(request, effectiveShiftMinutes);
        applyCalculatedFields(entity, calculated);

        DailyProductionReport saved = reportRepository.save(entity);
        return toResponse(saved, calculated);
    }

    @Transactional
    public ProductionReportResponse updateReport(Long id, ProductionCalculationRequest request) {
        DailyProductionReport entity = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));

        String normalizedShiftName = request.shiftName() != null ? request.shiftName().trim() : null;
        Integer shiftMinutes = formulaService.resolveShiftMinutes(normalizedShiftName);
        if (shiftMinutes == null) {
            Shift shift = shiftRepository.findByShiftName(normalizedShiftName)
                .orElseGet(() -> masterDataService.upsertShift(normalizedShiftName, request.totalOperatingMinutes()));
            shiftMinutes = shift.getStandardTimeMinutes();
        }
        final Integer effectiveShiftMinutes = shiftMinutes;

        Line line = lineRepository.findByLineCode(request.lineCode())
                .orElseGet(() -> masterDataService.upsertLine(request.lineCode(), request.lineCode()));
        Product product = productRepository.findByPartNumber(request.partNumber())
                .orElseGet(() -> masterDataService.upsertProduct(request.partNumber(), request.partName(), request.cycleTimeSeconds()));
        Shift shift = shiftRepository.findByShiftName(normalizedShiftName)
            .orElseGet(() -> masterDataService.upsertShift(normalizedShiftName, effectiveShiftMinutes));

        entity.setReportDate(request.reportDate() != null ? request.reportDate() : LocalDate.now());
        entity.setLine(line);
        entity.setShift(shift);
        entity.setMachineCode(request.machineCode());
        entity.setProduct(product);
        entity.setTotalOperatingMinutes(request.totalOperatingMinutes());
        entity.setDowntimeMinutes(request.downtimeMinutes());
        entity.setInputQuantity(request.inputQuantity());
        entity.setGoodQuantity(request.goodQuantity());
        entity.setDefectQuantity(request.defectQuantity());
        entity.setCompany(request.company());
        entity.setDowntimeReason(request.downtimeReason());

        ProductionCalculationResponse calculated = formulaService.calculate(request, effectiveShiftMinutes);
        applyCalculatedFields(entity, calculated);

        DailyProductionReport saved = reportRepository.save(entity);
        return toResponse(saved, calculated);
    }

    @Transactional
    public void deleteReportsByIds(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        reportRepository.deleteAllById(ids);
    }

    @Transactional
    public List<ProductionReportResponse> importReports(MultipartFile file, String createdBy) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel file does not contain any sheet.");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file does not contain a header row.");
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow);
            List<ProductionReportResponse> importedReports = new ArrayList<>();

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                ProductionCalculationRequest request = new ProductionCalculationRequest(
                        parseLocalDate(row.getCell(headerIndex.getOrDefault("reportDate", -1))),
                        parseString(row.getCell(headerIndex.getOrDefault("lineCode", -1))),
                        parseString(row.getCell(headerIndex.getOrDefault("shiftName", -1))),
                        parseString(row.getCell(headerIndex.getOrDefault("machineCode", -1))),
                        parseString(row.getCell(headerIndex.getOrDefault("partNumber", -1))),
                        parseString(row.getCell(headerIndex.getOrDefault("partName", -1))),
                        parseBigDecimal(row.getCell(headerIndex.getOrDefault("cycleTimeSeconds", -1))),
                        parseInteger(row.getCell(headerIndex.getOrDefault("totalOperatingMinutes", -1))),
                        parseInteger(row.getCell(headerIndex.getOrDefault("downtimeMinutes", -1))),
                        parseInteger(row.getCell(headerIndex.getOrDefault("inputQuantity", -1))),
                        parseInteger(row.getCell(headerIndex.getOrDefault("goodQuantity", -1))),
                        parseInteger(row.getCell(headerIndex.getOrDefault("defectQuantity", -1))),
                        parseString(row.getCell(headerIndex.getOrDefault("company", -1))),
                        parseString(row.getCell(headerIndex.getOrDefault("downtimeReason", -1)))
                );

                ProductionReportResponse imported = createReport(request, createdBy);
                importedReports.add(imported);
            }

            return importedReports;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to import Excel file", e);
        }
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow) {
        DataFormatter formatter = new DataFormatter();
        Map<String, Integer> indexMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = formatter.formatCellValue(cell).trim().toLowerCase();
            if (header.isEmpty()) {
                continue;
            }
            if (header.contains("ngày") || header.contains("日期")) {
                indexMap.put("reportDate", cell.getColumnIndex());
            } else if (header.contains("chuyền") || header.contains("線別") || header.contains("line")) {
                indexMap.put("lineCode", cell.getColumnIndex());
            } else if (header.contains("班別") || header.contains("班別") || header.contains("shift") || header.contains("shift_name") || header.contains("shift name") || header.contains("shiftname")) {
                indexMap.put("shiftName", cell.getColumnIndex());
            } else if (header.contains("mã máy") || header.contains("機台") || header.contains("máy")) {
                indexMap.put("machineCode", cell.getColumnIndex());
            } else if (header.contains("công ty") || header.contains("公司")) {
                indexMap.put("company", cell.getColumnIndex());
            } else if (header.contains("mã hàng") || header.contains("料號") || header.contains("partnumber") || header.contains("mã hàng")) {
                indexMap.put("partNumber", cell.getColumnIndex());
            } else if (header.contains("tên hàng") || header.contains("品名")) {
                indexMap.put("partName", cell.getColumnIndex());
            } else if (header.contains("c/t") || header.contains("ct") || header.contains("(秒)") || header.contains("cycle")) {
                indexMap.put("cycleTimeSeconds", cell.getColumnIndex());
            } else if (header.contains("tổng giờ chạy") || header.contains("總動時間") || header.contains("tổng tg") || header.contains("working")) {
                indexMap.put("totalOperatingMinutes", cell.getColumnIndex());
            } else if (header.contains("dừng") || header.contains("停機") || header.contains("downtime")) {
                if (header.contains("lý do") || header.contains("原因")) {
                    indexMap.put("downtimeReason", cell.getColumnIndex());
                } else {
                    indexMap.put("downtimeMinutes", cell.getColumnIndex());
                }
            } else if (header.contains("標準工時") || header.contains("giờ chuẩn") || header.contains("TG Ca")) {
                indexMap.put("shiftStandardTimeMinutes", cell.getColumnIndex());
            } else if (header.contains("投入數") || header.contains("sl nhập") || header.contains("input")) {
                indexMap.put("inputQuantity", cell.getColumnIndex());
            } else if (header.contains("良品數") || header.contains("sl đạt") || header.contains("good")) {
                indexMap.put("goodQuantity", cell.getColumnIndex());
            } else if (header.contains("不良數") || header.contains("sl lỗi") || header.contains("defect")) {
                indexMap.put("defectQuantity", cell.getColumnIndex());
            }
        }
        return indexMap;
    }

    private boolean isRowEmpty(Row row) {
        DataFormatter formatter = new DataFormatter();
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String parseString(Cell cell) {
        if (cell == null) return null;
        String value = new DataFormatter().formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private LocalDate parseLocalDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String text = parseString(cell);
        if (text == null) return null;

        try {
            return LocalDate.parse(text);
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("M/d/yyyy"));
        } catch (Exception ignored) {
        }
        return null;
    }

    private BigDecimal parseBigDecimal(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String text = parseString(cell);
        if (text == null) return null;
        try {
            return new BigDecimal(text.replaceAll("[, ]", ""));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer parseInteger(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        String text = parseString(cell);
        if (text == null) return null;
        try {
            return Integer.parseInt(text.replaceAll("[, ]", ""));
        } catch (Exception ignored) {
            try {
                return (int) Double.parseDouble(text.replaceAll("[, ]", ""));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ProductionReportResponse> getReports(LocalDate from, LocalDate to, String lineCode, String shiftName, String partNumber) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now();
        LocalDate effectiveTo = to != null ? to : effectiveFrom;
        List<DailyProductionReport> reports = reportRepository.findByReportDateBetweenOrderByReportDateDescCreatedAtDesc(effectiveFrom, effectiveTo);
        return reports.stream()
                .filter(r -> lineCode == null || lineCode.isBlank() || (r.getLine() != null && lineCode.equals(r.getLine().getLineCode())))
                .filter(r -> shiftName == null || shiftName.isBlank() || (r.getShift() != null && shiftName.equals(r.getShift().getShiftName())))
                .filter(r -> partNumber == null || partNumber.isBlank() || (r.getProduct() != null && r.getProduct().getPartNumber().contains(partNumber)))
                .sorted(Comparator.comparing(DailyProductionReport::getCreatedAt).reversed())
                .map(r -> toResponse(r, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductionReportResponse> getMyReports(String createdBy, LocalDate reportDate, String lineCode, String shiftName, String partNumber) {
        List<DailyProductionReport> reports;
        boolean hasLineFilter = lineCode != null && !lineCode.isBlank();

        if (reportDate == null) {
            if (hasLineFilter) {
                reports = reportRepository.findByCreatedByAndLine_LineCodeOrderByCreatedAtDesc(createdBy, lineCode);
            } else {
                reports = reportRepository.findByCreatedByOrderByCreatedAtDesc(createdBy);
            }
        } else {
            if (hasLineFilter) {
                reports = reportRepository.findByCreatedByAndLine_LineCodeAndReportDateOrderByCreatedAtDesc(createdBy, lineCode, reportDate);
            } else {
                reports = reportRepository.findByCreatedByAndReportDateOrderByCreatedAtDesc(createdBy, reportDate);
            }
        }

        return reports.stream()
                .filter(r -> shiftName == null || shiftName.isBlank() || (r.getShift() != null && shiftName.equals(r.getShift().getShiftName())))
                .filter(r -> partNumber == null || partNumber.isBlank() || (r.getProduct() != null && r.getProduct().getPartNumber().contains(partNumber)))
                .map(r -> toResponse(r, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductionReportResponse> getTodayReports(LocalDate reportDate, String lineCode) {
        LocalDate date = reportDate != null ? reportDate : LocalDate.now();
        List<DailyProductionReport> reports = (lineCode == null || lineCode.isBlank())
                ? reportRepository.findByReportDateOrderByCreatedAtDesc(date)
                : reportRepository.findByReportDateAndLine_LineCodeOrderByCreatedAtDesc(date, lineCode);
        return reports.stream().map(r -> toResponse(r, null)).toList();
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboard(LocalDate reportDate) {
        LocalDate date = reportDate != null ? reportDate : LocalDate.now();
        List<DailyProductionReport> reports = reportRepository.findByReportDateOrderByCreatedAtDesc(date);

        BigDecimal avgOee = average(reports.stream().map(DailyProductionReport::getOee).toList());
        BigDecimal avgA = average(reports.stream().map(DailyProductionReport::getAvailabilityRate).toList());
        BigDecimal avgP = average(reports.stream().map(DailyProductionReport::getPerformanceRate).toList());
        BigDecimal avgQ = average(reports.stream().map(DailyProductionReport::getQualityRate).toList());

        List<LineDashboardResponse> lineCards = reports.stream()
                .collect(java.util.stream.Collectors.groupingBy(r -> r.getLine() != null ? r.getLine().getLineCode() : "N/A"))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> {
                    List<DailyProductionReport> lineReports = entry.getValue();
                    BigDecimal oee = average(lineReports.stream().map(DailyProductionReport::getOee).toList());
                    BigDecimal av = average(lineReports.stream().map(DailyProductionReport::getAvailabilityRate).toList());
                    BigDecimal pr = average(lineReports.stream().map(DailyProductionReport::getPerformanceRate).toList());
                    BigDecimal qr = average(lineReports.stream().map(DailyProductionReport::getQualityRate).toList());
                    return new LineDashboardResponse(entry.getKey(), oee, av, pr, qr, status(oee));
                })
                .toList();

        long warningCount = reports.stream()
                .map(DailyProductionReport::getOee)
                .filter(o -> o != null && o.compareTo(new BigDecimal("0.65")) < 0)
                .count();

        return new DashboardSummaryResponse(
                date.toString(),
                avgOee,
                avgA,
                avgP,
                avgQ,
                (long) lineCards.size(),
                warningCount,
                lineCards
        );
    }

    private void applyCalculatedFields(DailyProductionReport entity, ProductionCalculationResponse calculated) {
        entity.setTargetQuantity(calculated.dailyTargetQuantity());
        entity.setAvailabilityRate(calculated.availabilityRate());
        entity.setPerformanceRate(calculated.performanceRate());
        entity.setQualityRate(calculated.qualityRate());
        entity.setOee(calculated.oee());
    }

    private ProductionReportResponse toResponse(DailyProductionReport report, ProductionCalculationResponse calculated) {
        BigDecimal cycleTime = report.getProduct() != null ? report.getProduct().getCycleTimeSeconds() : null;
        Integer shiftMinutes = report.getShift() != null ? report.getShift().getStandardTimeMinutes() : null;
        return new ProductionReportResponse(
                report.getId(),
                report.getReportDate() != null ? report.getReportDate().toString() : null,
                report.getLine() != null ? report.getLine().getLineCode() : null,
                report.getShift() != null ? report.getShift().getShiftName() : null,
                report.getMachineCode(),
                report.getProduct() != null ? report.getProduct().getPartNumber() : null,
                report.getProduct() != null ? report.getProduct().getPartName() : null,
                cycleTime,
                report.getTotalOperatingMinutes(),
                report.getDowntimeMinutes(),
                report.getInputQuantity(),
                report.getGoodQuantity(),
                report.getDefectQuantity(),
                calculated != null ? calculated.company() : report.getCompany(),
                calculated != null ? calculated.downtimeReason() : report.getDowntimeReason(),
                shiftMinutes,
                calculated != null ? calculated.dailyTargetQuantity() : report.getTargetQuantity(),
                calculated != null ? calculated.productionEfficiency() : productionEfficiency(report),
                report.getAvailabilityRate(),
                report.getPerformanceRate(),
                report.getQualityRate(),
                report.getOee(),
                calculated != null ? calculated.evaluationLabel() : formulaService.evaluationLabel(report.getOee()),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getCreatedBy()
        );
    }

    private BigDecimal productionEfficiency(DailyProductionReport report) {
        if (report.getTargetQuantity() == null || report.getTargetQuantity().compareTo(BigDecimal.ZERO) <= 0 || report.getInputQuantity() == null) {
            return null;
        }
        return BigDecimal.valueOf(report.getInputQuantity()).divide(report.getTargetQuantity(), 4, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal average(List<BigDecimal> values) {
        List<BigDecimal> filtered = values.stream().filter(v -> v != null).toList();
        if (filtered.isEmpty()) {
            return null;
        }
        BigDecimal sum = filtered.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(filtered.size()), 4, java.math.RoundingMode.HALF_UP);
    }

    private String status(BigDecimal oee) {
        if (oee == null) {
            return "N/A";
        }
        if (oee.compareTo(new BigDecimal("0.85")) >= 0) {
            return "Ổn định";
        }
        if (oee.compareTo(new BigDecimal("0.65")) >= 0) {
            return "Theo dõi";
        }
        return "Cảnh báo";
    }
}
