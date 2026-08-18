package com.swico.swico.service;

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
import java.util.List;
import java.util.Map;

@Service
public class ProductionExportService {

    private final ProductProcessRepository productProcessRepository;

    public ProductionExportService(ProductProcessRepository productProcessRepository) {
        this.productProcessRepository = productProcessRepository;
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
                    "\u65e5\u671f\nNgay", "\u7dda\u5225\nChuyen", "\u73ed\u5225\nCa (Dropdown)", "\u6a5f\u53f0\nMa May", "\u516c\u53f8\nCong Ty",
                    "\u4f5c\u54e1\nNhan Vien Thao Tac", "\u8ca0\u8cac\u5e79\u90e8\nCan Bo Phu Trach", "\u6599\u865f\nMa Hang",
                    "\u54c1\u540d\nTen Hang", "\u5de5\u5e8f\nCong doan", "C/T (\u79d2)", "\u7e3d\u52d5\u6642\u9593(\u5206)\nTong TG", "\u505c\u6a5f(\u5206)\nTG Dung",
                    "\u505c\u6a5f\u539f\u56e0\nLy Do Dung", "\u6a19\u6e96\u5de5\u6642(\u5206)\nTG Ca", "\u6bcf\u65e5\u76ee\u6a19\nMuc Tieu", "\u6295\u5165\u6578\nSL Nhap",
                    "\u826f\u54c1\u6578\nSL Dat", "\u4e0d\u826f\u6578\nSL Loi\n(\u5167\u88fd)", "\u4e0d\u826f\u6578\nSL Loi\n(\u5916\u88fd)",
                    "\u8cac\u4efb\nTrach nhiem", "\u6263\u9ede\u6578\n% tru", "\u751f\u7522\u6548\u7387\nHieu Suat", "\u7a3c\u52d5\u7387 A", "\u6027\u80fd\u7387 P", "\u826f\u54c1\u7387 Q", "OEE", "\u8a55\u50f9\nDanh Gia", "\u7c3d\u540d"
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
                int excelRow = row.getRowNum() + 1;
                setFormula(row.createCell(20), "S" + excelRow + "/Q" + excelRow, percentStyle);
                setFormula(row.createCell(21), "IF(U" + excelRow + ">0.27%,U" + excelRow + "-0.27%,0)", percentStyle);
                setFormula(row.createCell(22), "IF(U" + excelRow + ">0.27%,(Q" + excelRow + "*(K" + excelRow + "/60)/L" + excelRow + ")-V" + excelRow + ",(Q" + excelRow + "*(K" + excelRow + "/60)/L" + excelRow + "))", percentStyle);
                setValue(row.createCell(23), report.availabilityRate(), percentStyle);
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
                .collect(java.util.stream.Collectors.toMap(ProductProcess::getId, ProductProcess::getProcess));
        return processIds.stream()
                .map(processNameById::get)
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining("\uff1b "));
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

    private void setFormula(Cell cell, String formula, CellStyle style) {
        cell.setCellFormula(formula);
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
