package com.swico.swico.service;

import com.swico.swico.dto.ProductionCalculationRequest;
import com.swico.swico.dto.ProductionCalculationResponse;
import com.swico.swico.dto.ProductionReportResponse;
import com.swico.swico.entity.ProductProcess;
import com.swico.swico.repository.ProductProcessRepository;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
public class ProductionExportService {

    private final ProductProcessRepository productProcessRepository;
    private final ProductionFormulaService formulaService;

    public ProductionExportService(ProductProcessRepository productProcessRepository, ProductionFormulaService formulaService) {
        this.productProcessRepository = productProcessRepository;
        this.formulaService = formulaService;
    }

    public byte[] exportV9(List<ProductionReportResponse> reports) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("\u6bcf\u65e5\u586b\u5beb V9");
            sheet.setDefaultColumnWidth(16);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);
            CellStyle wrappedTextStyle = createWrappedTextStyle(workbook);
            CellStyle decimalStyle = createDecimalStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            CellStyle intStyle = createIntStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            String[] headers = {
                    "\u65e5\u671f\nNgày", "\u7dda\u5225\nChuyền", "\u73ed\u5225\nCa (Dropdown)", "\u6a5f\u53f0\nMã Máy", "\u516c\u53f8\nCông Ty",
                    "\u4f5c\u54e1\nNhân Viên Thao Tác", "\u8ca0\u8cac\u5e79\u90e8\nCán Bộ Phụ Trách", "\u6599\u865f\nMã Hàng",
                    "\u54c1\u540d\nTên Hàng", "\u5de5\u5e8f\nCông Đoạn", "C/T (\u79d2)", "\u7e3d\u52d5\u6642\u9593(\u5206)\nTổng TG", "\u505c\u6a5f(\u5206)\nTG Dừng",
                    "\u505c\u6a5f\u539f\u56e0\nLý Do Dừng", "\u6a19\u6e96\u5de5\u6642(\u5206)\nTG Ca", "\u6bcf\u65e5\u76ee\u6a19\nMục Tiêu", "\u6295\u5165\u6578\nSL Nhập",
                    "\u826f\u54c1\u6578\nSL Đạt", "\u4e0d\u826f\u6578\nSL Lỗi\n(\u5167\u88fd)", "\u4e0d\u826f\u6578\nSL Lỗi\n(\u5916\u88fd)",
                    "\u8cac\u4efb\nTrách Nhiệm", "\u6263\u9ede\u6578\n% Trừ", "\u751f\u7522\u6548\u7387\nHiệu Suất", "\u7a3c\u52d5\u7387 A", "\u6027\u80fd\u7387 P", "\u826f\u54c1\u7387 Q", "OEE", "\u8a55\u50f9\nĐánh Giá", "\u7c3d\u540d"
            };

            Row header = sheet.createRow(0);
            header.setHeightInPoints(42);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (ProductionReportResponse report : reports) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                setValue(row.createCell(0), report.reportDate(), dateStyle);
                setValue(row.createCell(1), report.lineCode(), textStyle);
                setValue(row.createCell(2), report.shiftName(), textStyle);
                setValue(row.createCell(3), report.machineCode(), textStyle);
                setValue(row.createCell(4), report.company(), textStyle);
                setValue(row.createCell(5), report.operatorName(), textStyle);
                setValue(row.createCell(6), report.responsibleLeader(), textStyle);
                setValue(row.createCell(7), report.partNumber(), textStyle);
                setValue(row.createCell(8), report.partName(), textStyle);
                setValue(row.createCell(9), formatProcesses(report.processIds()), wrappedTextStyle);
                setValue(row.createCell(10), report.cycleTimeSeconds(), decimalStyle);
                setValue(row.createCell(11), report.totalOperatingMinutes(), intStyle);
                setValue(row.createCell(12), report.downtimeMinutes(), intStyle);
                setValue(row.createCell(13), report.downtimeReason(), wrappedTextStyle);
                setValue(row.createCell(14), report.shiftStandardTimeMinutes(), intStyle);
                setValue(row.createCell(15), report.dailyTargetQuantity(), decimalStyle);
                setValue(row.createCell(16), report.inputQuantity(), intStyle);
                setValue(row.createCell(17), report.goodQuantity(), intStyle);
                setValue(row.createCell(18), report.internalDefectQuantity(), intStyle);
                setValue(row.createCell(19), report.externalDefectQuantity(), intStyle);
                ProductionCalculationResponse fallback = null;
                if (report.responsibility() == null
                        || report.deductionPercent() == null
                        || report.productionEfficiency() == null
                        || report.availabilityRate() == null) {
                    fallback = calculateFallback(report);
                }
                setValue(row.createCell(20), firstNonNull(report.responsibility(), fallback != null ? fallback.responsibility() : null), percentStyle);
                setValue(row.createCell(21), firstNonNull(report.deductionPercent(), fallback != null ? fallback.deductionPercent() : null), percentStyle);
                setValue(row.createCell(22), firstNonNull(report.productionEfficiency(), fallback != null ? fallback.productionEfficiency() : null), percentStyle);
                setValue(row.createCell(23), firstNonNull(report.availabilityRate(), fallback != null ? fallback.availabilityRate() : null), percentStyle);
                setValue(row.createCell(24), report.performanceRate(), percentStyle);
                setValue(row.createCell(25), report.qualityRate(), percentStyle);
                setValue(row.createCell(26), report.oee(), percentStyle);
                setValue(row.createCell(27), report.evaluationLabel(), textStyle);
                row.createCell(28).setCellValue("");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate V9 Excel export", e);
        }
    }

    private String formatProcesses(List<Long> processIds) {
        if (processIds == null || processIds.isEmpty()) {
            return null;
        }
        List<ProductProcess> processes = productProcessRepository.findAllById(processIds);
        Map<Long, String> processNameById = processes.stream()
                .collect(java.util.stream.Collectors.toMap(ProductProcess::getId, this::exportProcessCode));
        return processIds.stream()
                .map(processNameById::get)
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining("\uff1b "));
    }

    private String exportProcessCode(ProductProcess process) {
        if (process.getProcessCode() != null && !process.getProcessCode().isBlank()) {
            return process.getProcessCode();
        }
        return process.getProcess();
    }

    private ProductionCalculationResponse calculateFallback(ProductionReportResponse report) {
        return formulaService.calculate(new ProductionCalculationRequest(
                parseDate(report.reportDate()),
                report.lineCode(),
                report.shiftName(),
                report.machineCode(),
                report.partNumber(),
                report.partName(),
                report.cycleTimeSeconds(),
                report.processIds(),
                report.totalOperatingMinutes(),
                report.downtimeMinutes(),
                report.inputQuantity(),
                report.goodQuantity(),
                report.defectQuantity(),
                report.internalDefectQuantity(),
                report.externalDefectQuantity(),
                report.company(),
                report.responsibleLeader(),
                report.downtimeReason(),
                null,
                report.deductionPercent(),
                report.shiftStandardTimeMinutes(),
                report.dailyTargetQuantity(),
                report.productionEfficiency(),
                report.availabilityRate(),
                report.performanceRate(),
                report.qualityRate(),
                report.oee(),
                report.evaluationLabel()
        ), report.shiftStandardTimeMinutes());
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private BigDecimal firstNonNull(BigDecimal value, BigDecimal fallback) {
        return value != null ? value : fallback;
    }

    private void setValue(Cell cell, String value, CellStyle style) {
        if (value != null) cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void setValue(Cell cell, Integer value, CellStyle style) {
        if (value != null) cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void setValue(Cell cell, BigDecimal value, CellStyle style) {
        if (value != null) cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 10);

        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setThinBorders(style);
        return style;
    }

    private CellStyle createTextStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setThinBorders(style);
        return style;
    }

    private CellStyle createWrappedTextStyle(Workbook workbook) {
        CellStyle style = createTextStyle(workbook);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createDecimalStyle(Workbook workbook) {
        CellStyle style = createTextStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00"));
        return style;
    }

    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = createTextStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }

    private CellStyle createIntStyle(Workbook workbook) {
        CellStyle style = createTextStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0"));
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createTextStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("yyyy/mm/dd"));
        return style;
    }

    private void setThinBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
