package com.swico.swico.service;

import com.swico.swico.dto.ProductionReportLotDto;
import com.swico.swico.dto.ProductionReportResponse;
import com.swico.swico.repository.ProductProcessRepository;
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

        byte[] bytes = service.exportV9(List.of(report()));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(1);

            assertEquals("45\n23\n30", row.getCell(16).getStringCellValue());
            assertEquals("44\n21\n28", row.getCell(17).getStringCellValue());
            assertEquals("1\n2\n2", row.getCell(18).getStringCellValue());
            assertEquals("1\n2\n0", row.getCell(19).getStringCellValue());
            assertEquals("0\n0\n2", row.getCell(20).getStringCellValue());
            assertEquals("A\nB\nC", row.getCell(21).getStringCellValue());
            assertEquals(66.0, row.getHeightInPoints());
        }
    }

    private ProductionReportResponse report() {
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
                null,
                new BigDecimal("0.0306"),
                new BigDecimal("0.0279"),
                List.of(1L, 1L),
                480,
                new BigDecimal("7620"),
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                new BigDecimal("0.0122"),
                "C",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "operator",
                List.of(
                        new ProductionReportLotDto(null, "A", 45, 44, 1, 1, 0),
                        new ProductionReportLotDto(null, "B", 23, 21, 2, 2, 0),
                        new ProductionReportLotDto(null, "C", 30, 28, 2, 0, 2)
                )
        );
    }
}
