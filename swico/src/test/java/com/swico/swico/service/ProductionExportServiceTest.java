package com.swico.swico.service;

import com.swico.swico.dto.ProductionReportLotDto;
import com.swico.swico.dto.ProductionReportDowntimeDto;
import com.swico.swico.dto.ProductionReportResponse;
import com.swico.swico.repository.ProductProcessRepository;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionExportServiceTest {

    @Mock
    private ProductProcessRepository productProcessRepository;

    @Mock
    private ProductionFormulaService formulaService;

    @Test
    void exportV9ShouldWriteLotRowsAsMultilineCells() throws Exception {
        when(productProcessRepository.findAllById(List.of(1L, 1L))).thenReturn(List.of());
        ProductionExportService service = new ProductionExportService(productProcessRepository, formulaService);

        byte[] bytes = service.exportV9(List.of(report(), report()));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(1);

            assertEquals("\u8f38\u5165\u6642\u9593\nGiờ nhập", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("\u505c\u6a5f\u6642\u9593\nThời Gian", sheet.getRow(0).getCell(16).getStringCellValue());
            assertEquals("\u6bcf\u65e5\u76ee\u6a19\nMục tiêu ngày", sheet.getRow(0).getCell(18).getStringCellValue());
            assertEquals("\u5be6\u969b\u76ee\u6a19\nMục Tiêu", sheet.getRow(0).getCell(19).getStringCellValue());
            assertEquals("\u6bcf\u65e5\u76ee\u6a19\u6548\u7387\nHiệu suất mục tiêu ngày", sheet.getRow(0).getCell(29).getStringCellValue());
            assertEquals("\u5de5\u4f5c\u72c0\u614b\nTrạng thái làm việc", sheet.getRow(0).getCell(7).getStringCellValue());
            assertEquals("Làm bình thường", row.getCell(7).getStringCellValue());
            assertEquals("6-1. Chuyển mã hàng gia công cùng máy", sheet.getRow(1).getCell(15).getStringCellValue());
            assertEquals(260.0, sheet.getRow(1).getCell(16).getNumericCellValue());
            assertEquals("1-2. Hết đá, thay đá", sheet.getRow(2).getCell(15).getStringCellValue());
            assertEquals(20.0, sheet.getRow(2).getCell(16).getNumericCellValue());
            assertEquals("3-4. Không có lệnh sản xuất", sheet.getRow(3).getCell(15).getStringCellValue());
            assertEquals(60.0, sheet.getRow(3).getCell(16).getNumericCellValue());
            assertEquals("ROUNDDOWN(R2*60/M2,0)", row.getCell(18).getCellFormula());
            assertEquals("SUM(U2:U4)/S2", row.getCell(29).getCellFormula());
            assertEquals("SUM(U5:U7)/S5", sheet.getRow(4).getCell(29).getCellFormula());
            assertEquals(98.0 / 28800, workbook.getCreationHelper().createFormulaEvaluator()
                    .evaluate(row.getCell(29)).getNumberValue(), 0.00000001);
            assertEquals(98.0 / 28800, workbook.getCreationHelper().createFormulaEvaluator()
                    .evaluate(sheet.getRow(4).getCell(29)).getNumberValue(), 0.00000001);
            assertEquals(45.0, sheet.getRow(1).getCell(20).getNumericCellValue());
            assertEquals(44.0, sheet.getRow(1).getCell(21).getNumericCellValue());
            assertEquals(1.0, sheet.getRow(1).getCell(22).getNumericCellValue());
            assertEquals(1.0, sheet.getRow(1).getCell(23).getNumericCellValue());
            assertEquals(0.0, sheet.getRow(1).getCell(24).getNumericCellValue());
            assertEquals("A", sheet.getRow(1).getCell(25).getStringCellValue());
            assertEquals(23.0, sheet.getRow(2).getCell(20).getNumericCellValue());
            assertEquals(21.0, sheet.getRow(2).getCell(21).getNumericCellValue());
            assertEquals(2.0, sheet.getRow(2).getCell(22).getNumericCellValue());
            assertEquals(2.0, sheet.getRow(2).getCell(23).getNumericCellValue());
            assertEquals(0.0, sheet.getRow(2).getCell(24).getNumericCellValue());
            assertEquals("B", sheet.getRow(2).getCell(25).getStringCellValue());
            assertEquals(30.0, sheet.getRow(3).getCell(20).getNumericCellValue());
            assertEquals(28.0, sheet.getRow(3).getCell(21).getNumericCellValue());
            assertEquals(2.0, sheet.getRow(3).getCell(22).getNumericCellValue());
            assertEquals(0.0, sheet.getRow(3).getCell(23).getNumericCellValue());
            assertEquals(2.0, sheet.getRow(3).getCell(24).getNumericCellValue());
            assertEquals("C", sheet.getRow(3).getCell(25).getStringCellValue());
            assertEquals(22.0, row.getHeightInPoints());
            assertEquals(28L, sheet.getMergedRegions().stream()
                    .filter(region -> region.getFirstRow() == 1 && region.getLastRow() == 3)
                    .filter(region -> region.getFirstColumn() == region.getLastColumn())
                    .filter(region -> region.getFirstColumn() != 15 && region.getFirstColumn() != 16)
                    .filter(region -> region.getFirstColumn() < 20 || region.getFirstColumn() > 25)
                    .count());
            assertMergedRegionBlackBorders(sheet, 0);
            assertMergedRegionBlackBorders(sheet, 35);
        }
    }

    @Test
    void exportV9ShouldMergeLotColumnsForTheSameLotNoDowntimeGroup() throws Exception {
        when(productProcessRepository.findAllById(List.of(1L, 1L))).thenReturn(List.of());
        ProductionExportService service = new ProductionExportService(productProcessRepository, formulaService);

        byte[] bytes = service.exportV9(List.of(report(
                List.of(
                        new ProductionReportLotDto(null, "A", 56, 56, 0, 0, 0),
                        new ProductionReportLotDto(null, "B", 90, 86, 4, 4, 0)
                ),
                List.of(
                        new ProductionReportDowntimeDto(null, null, "2-2. Ngung may cho phoi", 56, "A"),
                        new ProductionReportDowntimeDto(null, null, "6-1. Ve sinh may cuoi ca/cuoi tuan", 47, "A"),
                        new ProductionReportDowntimeDto(null, null, "1-3. Cho can bo chinh may", 15, "B")
                )
        )));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);

            assertEquals(56.0, sheet.getRow(1).getCell(20).getNumericCellValue());
            assertEquals("A", sheet.getRow(1).getCell(25).getStringCellValue());
            assertEquals(90.0, sheet.getRow(3).getCell(20).getNumericCellValue());
            assertEquals("B", sheet.getRow(3).getCell(25).getStringCellValue());
            assertEquals("SUM(U2:U4)/S2", sheet.getRow(1).getCell(29).getCellFormula());
            assertEquals(146.0 / 28800, workbook.getCreationHelper().createFormulaEvaluator()
                    .evaluate(sheet.getRow(1).getCell(29)).getNumberValue(), 0.00000001);
            for (int column = 20; column <= 25; column++) {
                assertTrue(hasMergedRegion(sheet, 1, 2, column));
            }
        }
    }

    private void assertMergedRegionBlackBorders(Sheet sheet, int column) {
        CellStyle top = sheet.getRow(1).getCell(column).getCellStyle();
        CellStyle middle = sheet.getRow(2).getCell(column).getCellStyle();
        CellStyle bottom = sheet.getRow(3).getCell(column).getCellStyle();
        assertEquals(BorderStyle.THIN, top.getBorderTop());
        assertEquals(BorderStyle.THIN, middle.getBorderLeft());
        assertEquals(BorderStyle.THIN, middle.getBorderRight());
        assertEquals(BorderStyle.THIN, bottom.getBorderBottom());
        assertEquals(IndexedColors.BLACK.getIndex(), top.getTopBorderColor());
        assertEquals(IndexedColors.BLACK.getIndex(), middle.getLeftBorderColor());
        assertEquals(IndexedColors.BLACK.getIndex(), middle.getRightBorderColor());
        assertEquals(IndexedColors.BLACK.getIndex(), bottom.getBottomBorderColor());
    }

    private boolean hasMergedRegion(Sheet sheet, int firstRow, int lastRow, int column) {
        return sheet.getMergedRegions().stream()
                .anyMatch(region -> region.getFirstRow() == firstRow
                        && region.getLastRow() == lastRow
                        && region.getFirstColumn() == column
                        && region.getLastColumn() == column);
    }

    private ProductionReportResponse report() {
        return report(
                List.of(
                        new ProductionReportLotDto(null, "A", 45, 44, 1, 1, 0),
                        new ProductionReportLotDto(null, "B", 23, 21, 2, 2, 0),
                        new ProductionReportLotDto(null, "C", 30, 28, 2, 0, 2)
                ),
                List.of(
                        new ProductionReportDowntimeDto(null, null, "6-1. Chuyển mã hàng gia công cùng máy", 260, null),
                        new ProductionReportDowntimeDto(null, null, "1-2. Hết đá, thay đá", 20, null),
                        new ProductionReportDowntimeDto(null, null, "3-4. Không có lệnh sản xuất", 60, null)
                )
        );
    }

    @Test
    void exportV9ShouldWriteTraineeEmploymentStatus() throws Exception {
        when(productProcessRepository.findAllById(List.of(1L, 1L))).thenReturn(List.of());
        ProductionExportService service = new ProductionExportService(productProcessRepository, formulaService);

        byte[] bytes = service.exportV9(List.of(report(List.of(), List.of(), "TRAINEE")));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertEquals("Học việc", workbook.getSheetAt(0).getRow(1).getCell(7).getStringCellValue());
            assertEquals("SUM(U2:U2)/S2", workbook.getSheetAt(0).getRow(1).getCell(29).getCellFormula());
            assertEquals(98.0 / 28800, workbook.getCreationHelper().createFormulaEvaluator()
                    .evaluate(workbook.getSheetAt(0).getRow(1).getCell(29)).getNumberValue(), 0.00000001);
        }
    }

    private ProductionReportResponse report(List<ProductionReportLotDto> lots, List<ProductionReportDowntimeDto> downtimes) {
        return report(lots, downtimes, "NORMAL");
    }

    private ProductionReportResponse report(List<ProductionReportLotDto> lots, List<ProductionReportDowntimeDto> downtimes, String employmentStatus) {
        return new ProductionReportResponse(
                1L,
                "2026-09-10",
                "A1",
                "Ca ngày",
                "TC-28",
                "PN-001",
                "Part",
                BigDecimal.ONE,
                480,
                0,
                98,
                93,
                5,
                3,
                2,
                "SWICO",
                "A； B； C",
                "Operator",
                "Leader",
                employmentStatus,
                null,
                new BigDecimal("0.0306"),
                new BigDecimal("0.0279"),
                List.of(1L, 1L),
                480,
                new BigDecimal("7620"),
                new BigDecimal("28800.00"),
                BigDecimal.ONE,
                new BigDecimal("0.0034"),
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                new BigDecimal("0.0122"),
                "C",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "operator",
                lots,
                downtimes
        );
    }
}
