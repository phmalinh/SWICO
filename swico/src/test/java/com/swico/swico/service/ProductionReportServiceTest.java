package com.swico.swico.service;

import com.swico.swico.repository.DailyProductionReportRepository;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.ProductRepository;
import com.swico.swico.repository.ProductProcessRepository;
import com.swico.swico.repository.ShiftRepository;
import com.swico.swico.repository.UserRepository;
import com.swico.swico.entity.Role;
import com.swico.swico.entity.User;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionReportServiceTest {

    @Mock
    private DailyProductionReportRepository reportRepository;

    @Mock
    private LineRepository lineRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductProcessRepository productProcessRepository;

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MasterDataService masterDataService;

    @Mock
    private ProductionFormulaService formulaService;

    @InjectMocks
    private ProductionReportService service;

    @Test
    void parseLocalDateShouldHandleExcelDateCells() throws Exception {
        Workbook workbook = WorkbookFactory.create(true);
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("yyyy/mm/dd"));
        cell.setCellStyle(style);
        cell.setCellValue(new GregorianCalendar(2024, Calendar.FEBRUARY, 20).getTime());

        Method method = ProductionReportService.class.getDeclaredMethod("parseLocalDate", Cell.class);
        method.setAccessible(true);

        Object result = method.invoke(service, cell);

        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 2, 20), result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildHeaderIndexShouldMatchCurrentExportLayout() throws Exception {
        Workbook workbook = WorkbookFactory.create(true);
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        String[] headers = {
                "\u65e5\u671f\nNgày", "\u7dda\u5225\nChuyền", "\u73ed\u5225\nCa (Dropdown)", "\u6a5f\u53f0\nMã Máy", "\u5ba2\u6236\nKhách hàng",
                "\u4f5c\u54e1\nNhân Viên Thao Tác", "\u8ca0\u8cac\u5e79\u90e8\nCán Bộ Phụ Trách", "\u6599\u865f\nMã Hàng",
                "\u54c1\u540d\nTên Hàng", "\u5de5\u5e8f\nCông Đoạn", "C/T (\u79d2)", "\u7e3d\u52d5\u6642\u9593(\u5206)\nTổng TG", "\u505c\u6a5f(\u5206)\nTG Dừng",
                "\u505c\u6a5f\u539f\u56e0\nLý Do Dừng", "\u6a19\u6e96\u5de5\u6642(\u5206)\nTG Ca", "\u6bcf\u65e5\u76ee\u6a19\nMục Tiêu", "\u6295\u5165\u6578\nSL Nhập",
                "\u826f\u54c1\u6578\nSL Đạt", "\u4e0d\u826f\u6578\nSL Lỗi\n(\u5167\u88fd)", "\u4e0d\u826f\u6578\nSL Lỗi\n(\u5916\u88fd)",
                "\u8cac\u4efb\nTrách Nhiệm", "\u6263\u9ede\u6578\n% Trừ", "\u751f\u7522\u6548\u7387\nHiệu Suất", "\u7a3c\u52d5\u7387 A", "\u6027\u80fd\u7387 P", "\u826f\u54c1\u7387 Q", "OEE", "\u8a55\u50f9\nĐánh Giá", "\u7c3d\u540d"
        };
        for (int i = 0; i < headers.length; i++) {
            row.createCell(i).setCellValue(headers[i]);
        }

        Method method = ProductionReportService.class.getDeclaredMethod("buildHeaderIndex", Row.class);
        method.setAccessible(true);

        Map<String, Integer> result = (Map<String, Integer>) method.invoke(service, row);

        assertEquals(4, result.get("company"));
        assertEquals(18, result.get("internalDefectQuantity"));
        assertEquals(19, result.get("externalDefectQuantity"));
        assertEquals(20, result.get("responsibility"));
        assertEquals(21, result.get("deductionPercent"));
        assertEquals(22, result.get("productionEfficiency"));
        assertEquals(23, result.get("availabilityRate"));
        assertEquals(24, result.get("performanceRate"));
        assertEquals(25, result.get("qualityRate"));
        assertEquals(26, result.get("oee"));
        assertEquals(27, result.get("evaluationLabel"));
    }

    @Test
    void resolveImportedCreatedByShouldMatchOperatorFullName() throws Exception {
        User operator = new User("260306", "encoded", "TẠO PHI LONG 曹飛龍", Role.ROLE_OPERATOR, "A1", true);
        when(userRepository.findByUsername("TẠO PHI LONG")).thenReturn(java.util.Optional.empty());
        when(userRepository.findByActiveTrueOrderByFullNameAscUsernameAsc()).thenReturn(List.of(operator));

        Method method = ProductionReportService.class.getDeclaredMethod("resolveImportedCreatedBy", String.class, String.class);
        method.setAccessible(true);

        Object result = method.invoke(service, "TẠO PHI LONG", "admin");

        assertEquals("260306", result);
    }

    @Test
    void resolveImportedCreatedByShouldKeepExcelNameWhenNoAccountMatches() throws Exception {
        when(userRepository.findByUsername("Người ngoài Excel")).thenReturn(java.util.Optional.empty());
        when(userRepository.findByActiveTrueOrderByFullNameAscUsernameAsc()).thenReturn(List.of());

        Method method = ProductionReportService.class.getDeclaredMethod("resolveImportedCreatedBy", String.class, String.class);
        method.setAccessible(true);

        Object result = method.invoke(service, "Người ngoài Excel", "admin");

        assertEquals("Người ngoài Excel", result);
    }
}
