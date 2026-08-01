package com.swico.swico.service;

import com.swico.swico.dto.ProductionReportResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProductionExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public byte[] exportV9(List<ProductionReportResponse> reports) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("每日填寫 V9");
            sheet.setDefaultColumnWidth(16);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);
            CellStyle decimalStyle = createDecimalStyle(workbook);
            CellStyle intStyle = createIntStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            String[] headers = {
                    "日期\nNgày", "線別\nChuyền", "班別\nCa (Dropdown)", "機台\nMã Máy", "公司\nCông Ty", "料號\nMã Hàng",
                    "品名\nTên Hàng", "C/T (秒)", "總動時間(分)\nTổng TG\n手動輸入", "停機(分)\nTG Dừng",
                    "停機原因\nLý Do Dừng", "標準工時(分)\nTG Ca\n(自動)", "每日目標\nMục Tiêu\n=標準*60/C/T", "投入數\nSL Nhập",
                    "良品數\nSL Đạt", "不良數\nSL Lỗi", "生產效率\nHiệu Suất\n=投入/目標",
                    "稼動率 A\n=(總動-停機)/總動", "性能率 P\n=投入*C/T/((總動-停機)*60)",
                    "良品率 Q\n=良品/投入", "OEE\n=A*P*Q", "評價\nĐánh Giá", "簽名"
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
                setValue(row.createCell(5), report.partNumber(), textStyle);
                setValue(row.createCell(6), report.partName(), textStyle);
                setValue(row.createCell(7), report.cycleTimeSeconds(), decimalStyle);
                setValue(row.createCell(8), report.totalOperatingMinutes(), intStyle);
                setValue(row.createCell(9), report.downtimeMinutes(), intStyle);
                setValue(row.createCell(10), report.downtimeReason(), textStyle);
                setValue(row.createCell(11), report.shiftStandardTimeMinutes(), intStyle);
                setValue(row.createCell(12), report.dailyTargetQuantity(), decimalStyle);
                setValue(row.createCell(13), report.inputQuantity(), intStyle);
                setValue(row.createCell(14), report.goodQuantity(), intStyle);
                setValue(row.createCell(15), report.defectQuantity(), intStyle);
                setValue(row.createCell(16), report.productionEfficiency(), decimalStyle);
                setValue(row.createCell(17), report.availabilityRate(), decimalStyle);
                setValue(row.createCell(18), report.performanceRate(), decimalStyle);
                setValue(row.createCell(19), report.qualityRate(), decimalStyle);
                setValue(row.createCell(20), report.oee(), decimalStyle);
                setValue(row.createCell(21), report.evaluationLabel(), textStyle);
                row.createCell(22).setCellValue("");
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

    private void setValue(Cell cell, String value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    private void setValue(Cell cell, Integer value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    private void setValue(Cell cell, BigDecimal value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
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

    private CellStyle createDecimalStyle(Workbook workbook) {
        CellStyle style = createTextStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00"));
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
