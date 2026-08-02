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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(ProductionReportService.class);

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
        ProductionCalculationResponse effective = mergeCalculated(calculated, request);
        applyCalculatedFields(entity, effective);

        DailyProductionReport saved = reportRepository.save(entity);
        return toResponse(saved, effective);
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
        ProductionCalculationResponse effective = mergeCalculated(calculated, request);
        applyCalculatedFields(entity, effective);

        DailyProductionReport saved = reportRepository.save(entity);
        return toResponse(saved, effective);
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

            Row headerRow = findHeaderRow(sheet, 10);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file does not contain a recognizable header row.");
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow);
            if (headerIndex.isEmpty()) {
                throw new IllegalArgumentException("Excel header row could not be mapped to import fields.");
            }
            LOG.info("Built header index: {}", headerIndex);
            List<ProductionReportResponse> importedReports = new ArrayList<>();

            int firstDataRow = headerRow.getRowNum() + 1;
            for (int rowIndex = firstDataRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                // debug: show parsed values for key columns to detect swapped mapping
                BigDecimal dbgCycle = parseBigDecimal(getCell(row, headerIndex.getOrDefault("cycleTimeSeconds", -1)));
                Integer dbgTotalOp = parseInteger(getCell(row, headerIndex.getOrDefault("totalOperatingMinutes", -1)));
                BigDecimal dbgTarget = parseBigDecimal(getCell(row, headerIndex.getOrDefault("dailyTargetQuantity", -1)));
                BigDecimal dbgAvailability = parseBigDecimal(getCell(row, headerIndex.getOrDefault("availabilityRate", -1)));
                BigDecimal dbgEfficiency = parseBigDecimal(getCell(row, headerIndex.getOrDefault("productionEfficiency", -1)));
                Integer dbgInput = parseInteger(getCell(row, headerIndex.getOrDefault("inputQuantity", -1)));
                Integer dbgGood = parseInteger(getCell(row, headerIndex.getOrDefault("goodQuantity", -1)));
                LOG.info("ROW[{}] parsed: cycle={} , totalOp={} , target={} , availability={} , efficiency={} , input={} , good={}", rowIndex, dbgCycle, dbgTotalOp, dbgTarget, dbgAvailability, dbgEfficiency, dbgInput, dbgGood);

                ProductionCalculationRequest request = new ProductionCalculationRequest(
                    parseLocalDate(getCell(row, headerIndex.getOrDefault("reportDate", -1))),
                    parseString(getCell(row, headerIndex.getOrDefault("lineCode", -1))),
                    parseString(getCell(row, headerIndex.getOrDefault("shiftName", -1))),
                    parseString(getCell(row, headerIndex.getOrDefault("machineCode", -1))),
                    parseString(getCell(row, headerIndex.getOrDefault("partNumber", -1))),
                    parseString(getCell(row, headerIndex.getOrDefault("partName", -1))),
                    parseBigDecimal(getCell(row, headerIndex.getOrDefault("cycleTimeSeconds", -1))),
                    parseInteger(getCell(row, headerIndex.getOrDefault("totalOperatingMinutes", -1))),
                    parseInteger(getCell(row, headerIndex.getOrDefault("downtimeMinutes", -1))),
                    parseInteger(getCell(row, headerIndex.getOrDefault("inputQuantity", -1))),
                    parseInteger(getCell(row, headerIndex.getOrDefault("goodQuantity", -1))),
                    parseInteger(getCell(row, headerIndex.getOrDefault("defectQuantity", -1))),
                    parseString(getCell(row, headerIndex.getOrDefault("company", -1))),
                    parseString(getCell(row, headerIndex.getOrDefault("downtimeReason", -1))),
                    // optional calculated/override fields from import
                    parseInteger(getCell(row, headerIndex.getOrDefault("shiftStandardTimeMinutes", -1))),
                    parseBigDecimal(getCell(row, headerIndex.getOrDefault("dailyTargetQuantity", -1))),
                    parseBigDecimal(getCell(row, headerIndex.getOrDefault("productionEfficiency", -1))),
                    parseBigDecimal(getCell(row, headerIndex.getOrDefault("availabilityRate", -1))),
                    parseBigDecimal(getCell(row, headerIndex.getOrDefault("performanceRate", -1))),
                    parseBigDecimal(getCell(row, headerIndex.getOrDefault("qualityRate", -1))),
                    parseBigDecimal(getCell(row, headerIndex.getOrDefault("oee", -1))),
                    parseString(getCell(row, headerIndex.getOrDefault("evaluationLabel", -1)))
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
        LOG.debug("Import header mapping: starting");
        LOG.debug("Using header row index: {}", headerRow.getRowNum());
        for (Cell cell : headerRow) {
            String rawHeader = formatter.formatCellValue(cell).trim();
            String header = normalizeHeader(rawHeader);
            LOG.debug("Header found: '{}' -> '{}' @{}", rawHeader, header, cell.getColumnIndex());
            if (header.isEmpty()) {
                continue;
            }
            String fieldKey = mapHeaderKey(header);
            if (fieldKey != null) {
                indexMap.put(fieldKey, cell.getColumnIndex());
                LOG.debug("Mapped header column {} ('{}') to field '{}'", cell.getColumnIndex(), rawHeader, fieldKey);
            } else {
                LOG.debug("No mapping for header column {} ('{}')", cell.getColumnIndex(), rawHeader);
            }
        }
        // additionally log full header cell list for diagnosing merged/multi-line headers
        StringBuilder sb = new StringBuilder();
        for (Cell cell : headerRow) {
            sb.append("[col=").append(cell.getColumnIndex()).append(",val='").append(formatter.formatCellValue(cell).trim()).append("']");
        }
        LOG.debug("Header row cells: {}", sb.toString());
        return indexMap;
    }

    private Row findHeaderRow(Sheet sheet, int maxHeaderScanRows) {
        int lastRow = Math.min(maxHeaderScanRows, sheet.getLastRowNum());
        Row bestRow = null;
        int bestScore = 0;
        for (int rowIndex = 0; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            int score = countKnownHeaders(row);
            if (score > bestScore) {
                bestScore = score;
                bestRow = row;
            }
        }
        return bestScore >= 3 ? bestRow : null;
    }

    private int countKnownHeaders(Row headerRow) {
        DataFormatter formatter = new DataFormatter();
        int count = 0;
        for (Cell cell : headerRow) {
            String header = normalizeHeader(formatter.formatCellValue(cell));
            if (!header.isEmpty() && mapHeaderKey(header) != null) {
                count++;
            }
        }
        return count;
    }

    private String mapHeaderKey(String header) {
        if (header.contains("ngày") || header.contains("日期") || header.contains("report date") || header.contains("date")) {
            return "reportDate";
        }
        if (header.contains("chuyền") || header.contains("線別") || header.equals("line") || header.contains("line code") || header.contains("linecode") || header.contains("line no") || header.contains("line id")) {
            return "lineCode";
        }
        if (header.contains("班別") || header.equals("班別") || header.contains("班別") || header.contains("班別")) {
            return "shiftName";
        }
        if (header.contains("mã máy") || header.contains("機台") || header.contains("máy") || header.contains("machine code") || header.equals("machine") || header.contains("machine no") || header.contains("machine id")) {
            return "machineCode";
        }
        if (header.contains("công ty") || header.contains("公司") || header.equals("company")) {
            return "company";
        }
        if (header.contains("mã hàng") || header.contains("料號") || header.contains("part number") || header.contains("partnumber") || header.equals("part") || header.contains("pn")) {
            return "partNumber";
        }
        if (header.contains("tên hàng") || header.contains("品名") || header.contains("part name") || header.contains("product name") || header.contains("product") || header.equals("partname")) {
            return "partName";
        }
        if (header.contains("c t") || header.contains("cycle") || header.contains("c/t") || header.contains("ct") || header.contains("秒") || header.contains("cycle time")) {
            return "cycleTimeSeconds";
        }
        if (header.contains("tổng giờ chạy") || header.contains("總動時間") || header.contains("tổng tg") || header.contains("working") || header.contains("run time") || header.contains("operating") || header.contains("稼働時間") || header.contains("稼動時間") || header.contains("runtime") || header.contains("operating time")) {
            return "totalOperatingMinutes";
        }
        if (header.contains("tg dừng") || header.contains("停機(分)") || header.contains("downtime") || header.contains("stop time") || header.contains("stop") || header.contains("down time")) {
            return "downtimeMinutes";
        }
        if (header.contains("lý do") || header.contains("原因") || header.contains("reason") || header.contains("cause") || header.contains("note")) {
            return "downtimeReason";
        }
        if (header.contains("標準時間") || header.contains("標準") || header.contains("shift standard") || header.contains("standard time") || header.contains("standard minutes") || header.contains("shift standard time")) {
            return "shiftStandardTimeMinutes";
        }
        if (header.contains("投入數") || header.contains("sl nhập") || header.contains("input") || header.contains("input qty") || header.contains("input quantity")) {
            return "inputQuantity";
        }
        if (header.contains("良品數") || header.contains("sl đạt") || header.contains("good") || header.contains("good qty") || header.contains("good quantity")) {
            return "goodQuantity";
        }
        if (header.contains("不良數") || header.contains("sl lỗi") || header.contains("defect") || header.contains("reject") || header.contains("bad")) {
            return "defectQuantity";
        }
        if (header.contains("mục tiêu") || header.contains("每日目標") || header.contains("目標") || header.contains("daily target") || header.contains("target") || header.contains("target qty") || header.contains("target quantity") || header.contains("target amount") || header.contains("target value") || header.contains("muc tieu")) {
            return "dailyTargetQuantity";
        }
        if (header.contains("hiệu suất") || header.contains("生產效率") || header.contains("productionEfficiency") || header.contains("efficiency") || header.contains("eff") || header.contains("productivity") || header.contains("efficiency rate") || header.contains("production rate") || header.contains("hieu suat")) {
            return "productionEfficiency";
        }
        if (header.contains("稼動率") || header.contains("稼働率") || header.contains("availability") || header.contains("avail") || header.contains("availability rate") || header.contains("uptime") || header.contains("utilization") || header.contains("availability %") || header.contains("availability percent") || header.contains("tỉ lệ sẵn sàng") || header.contains("độ khả dụng") || header.contains("độ sẵn sàng") || header.contains("稼 動 率") || header.contains("稼動 率") || header.contains("稼働 率")) {
            return "availabilityRate";
        }
        if (header.contains("性能率") || header.contains("性 能 率")|| header.contains("性 能率")|| header.contains("性能 率")|| header.contains("性能率") || header.contains("performance") || header.contains("performance rate") || header.contains("performance %") || header.contains("throughput") || header.contains("tốc độ")) {
            return "performanceRate";
        }
        if (header.contains("良品率") || header.contains("quality") || header.contains("tỉ lệ chất") || header.contains("qr") || header.contains("quality rate") || header.contains("yield") || header.contains("good rate") || header.contains("good yield") || header.contains("tỉ lệ đạt") || header.contains("良品 率")) {
            return "qualityRate";
        }
        if (header.contains("oee")) {
            return "oee";
        }
        if (header.contains("đánh giá") || header.contains("評價") || header.contains("evaluation") || header.contains("status") || header.contains("remark") || header.contains("comment") || header.contains("rating") || header.contains("result") || header.contains("note") || header.contains("điểm") || header.contains("đánh gia")) {
            return "evaluationLabel";
        }
        return null;
    }

    private String normalizeHeader(String header) {
        if (header == null) return "";
        return header.trim()
                .toLowerCase()
                .replaceAll("[\u3000\uFEFF]", " ")
                .replaceAll("[\\p{Punct}&&[^%]]", " ")
                .replaceAll("\\s+", " ")
                .trim();
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
            return new BigDecimal(text.replaceAll("[, %]", ""));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Cell getCell(Row row, int index) {
        if (index < 0) return null;
        return row.getCell(index);
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
        entity.setProductionEfficiency(calculated.productionEfficiency());
        entity.setAvailabilityRate(calculated.availabilityRate());
        entity.setPerformanceRate(calculated.performanceRate());
        entity.setQualityRate(calculated.qualityRate());
        entity.setOee(calculated.oee());
        entity.setEvaluationLabel(calculated.evaluationLabel());
    }

    private ProductionCalculationResponse mergeCalculated(ProductionCalculationResponse calculated, com.swico.swico.dto.ProductionCalculationRequest request) {
        if (request == null) return calculated;
        boolean hasOverrides = request.shiftStandardTimeMinutes() != null
                || request.dailyTargetQuantity() != null
                || request.productionEfficiency() != null
                || request.availabilityRate() != null
                || request.performanceRate() != null
                || request.qualityRate() != null
                || request.oee() != null
                || request.evaluationLabel() != null;
        if (!hasOverrides) return calculated;

        return new ProductionCalculationResponse(
                request.reportDate() != null ? request.reportDate() : calculated.reportDate(),
                request.lineCode() != null ? request.lineCode() : calculated.lineCode(),
                request.shiftName() != null ? request.shiftName() : calculated.shiftName(),
                request.machineCode() != null ? request.machineCode() : calculated.machineCode(),
                request.partNumber() != null ? request.partNumber() : calculated.partNumber(),
                request.partName() != null ? request.partName() : calculated.partName(),
                request.cycleTimeSeconds() != null ? request.cycleTimeSeconds() : calculated.cycleTimeSeconds(),
                request.totalOperatingMinutes() != null ? request.totalOperatingMinutes() : calculated.totalOperatingMinutes(),
                request.downtimeMinutes() != null ? request.downtimeMinutes() : calculated.downtimeMinutes(),
                request.inputQuantity() != null ? request.inputQuantity() : calculated.inputQuantity(),
                request.goodQuantity() != null ? request.goodQuantity() : calculated.goodQuantity(),
                request.defectQuantity() != null ? request.defectQuantity() : calculated.defectQuantity(),
                request.shiftStandardTimeMinutes() != null ? request.shiftStandardTimeMinutes() : calculated.shiftStandardTimeMinutes(),
                request.dailyTargetQuantity() != null ? request.dailyTargetQuantity() : calculated.dailyTargetQuantity(),
                request.productionEfficiency() != null ? request.productionEfficiency() : calculated.productionEfficiency(),
                request.availabilityRate() != null ? request.availabilityRate() : calculated.availabilityRate(),
                request.performanceRate() != null ? request.performanceRate() : calculated.performanceRate(),
                request.qualityRate() != null ? request.qualityRate() : calculated.qualityRate(),
                request.oee() != null ? request.oee() : calculated.oee(),
                request.company() != null ? request.company() : calculated.company(),
                request.downtimeReason() != null ? request.downtimeReason() : calculated.downtimeReason(),
                request.evaluationLabel() != null ? request.evaluationLabel() : calculated.evaluationLabel()
        );
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
                calculated != null ? calculated.productionEfficiency() : report.getProductionEfficiency(),
                report.getAvailabilityRate(),
                report.getPerformanceRate(),
                report.getQualityRate(),
                report.getOee(),
                calculated != null ? calculated.evaluationLabel() : report.getEvaluationLabel(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getCreatedBy()
        );
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
