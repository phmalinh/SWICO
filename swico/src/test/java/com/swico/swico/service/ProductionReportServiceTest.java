package com.swico.swico.service;

import com.swico.swico.repository.DailyProductionReportRepository;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.ProductRepository;
import com.swico.swico.repository.ShiftRepository;
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
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ProductionReportServiceTest {

    @Mock
    private DailyProductionReportRepository reportRepository;

    @Mock
    private LineRepository lineRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShiftRepository shiftRepository;

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
}
