package com.swico.swico.controller;

import com.swico.swico.dto.DashboardSummaryResponse;
import com.swico.swico.dto.ProductionCalculationRequest;
import com.swico.swico.dto.ProductionCalculationResponse;
import com.swico.swico.dto.ProductionReportResponse;
import com.swico.swico.service.AuditLogService;
import com.swico.swico.service.ProductionExportService;
import com.swico.swico.service.ProductionFormulaService;
import com.swico.swico.service.ProductionReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/production-reports")
@CrossOrigin(origins = "*")
public class ProductionReportController {

    private static final Logger logger = LoggerFactory.getLogger(ProductionReportController.class);

    private final ProductionFormulaService formulaService;
    private final ProductionReportService reportService;
    private final ProductionExportService exportService;
    private final AuditLogService auditLogService;

    public ProductionReportController(ProductionFormulaService formulaService,
                                      ProductionReportService reportService,
                                      ProductionExportService exportService,
                                      AuditLogService auditLogService) {
        this.formulaService = formulaService;
        this.reportService = reportService;
        this.exportService = exportService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/calculate")
    public ProductionCalculationResponse calculate(@Valid @RequestBody ProductionCalculationRequest request) {
        Integer shiftMinutes = formulaService.resolveShiftMinutes(request.shiftName());
        return formulaService.calculate(request, shiftMinutes);
    }

    @PostMapping
    public ProductionReportResponse create(@Valid @RequestBody ProductionCalculationRequest request, Authentication authentication) {
        ProductionReportResponse response = reportService.createReport(request);
        auditLogService.record(
                "CREATE",
                "DailyProductionReport",
                response.id(),
                authentication.getName(),
                String.format("Tạo báo cáo ca %s - %s", response.lineCode(), response.partNumber())
        );
        return response;
    }

    @PutMapping("/{id}")
    public ProductionReportResponse update(@PathVariable Long id, @Valid @RequestBody ProductionCalculationRequest request, Authentication authentication) {
        ProductionReportResponse response = reportService.updateReport(id, request);
        auditLogService.record(
                "UPDATE",
                "DailyProductionReport",
                response.id(),
                authentication.getName(),
                String.format("Cập nhật báo cáo %s - %s", response.lineCode(), response.partNumber())
        );
        return response;
    }

    @GetMapping("/today")
    public List<ProductionReportResponse> today(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate,
            @RequestParam(required = false) String lineCode
    ) {
        return reportService.getTodayReports(reportDate, lineCode);
    }

    @GetMapping
    public List<ProductionReportResponse> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String lineCode,
            @RequestParam(required = false) String shiftName,
            @RequestParam(required = false) String partNumber
    ) {
        return reportService.getReports(from, to, lineCode, shiftName, partNumber);
    }

    @GetMapping("/dashboard")
    public DashboardSummaryResponse dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate
    ) {
        return reportService.getDashboard(reportDate);
    }

    @GetMapping("/export-v9")
    public ResponseEntity<byte[]> exportV9(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String lineCode,
            @RequestParam(required = false) String shiftName,
            @RequestParam(required = false) String partNumber
    ) {
        List<ProductionReportResponse> reports = reportService.getReports(from, to, lineCode, shiftName, partNumber);
        byte[] bytes = exportService.exportV9(reports);
        String filename = "OEE_V9_" + (from != null ? from : LocalDate.now()) + "_" + (to != null ? to : LocalDate.now()) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public List<ProductionReportResponse> importReports(@RequestPart("file") MultipartFile file, Authentication authentication) {
    //     List<ProductionReportResponse> reports = reportService.importReports(file);
    //     if (reports != null) {
    //         reports.forEach(report -> auditLogService.record(
    //                 "CREATE",
    //                 "DailyProductionReport",
    //                 report.id(),
    //                 authentication.getName(),
    //                 String.format("Nhập báo cáo từ Excel %s - %s", report.lineCode(), report.partNumber())
    //         ));
    //     }
    //     return reports;
    // }
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ProductionReportResponse> importReports(
            @RequestParam("file") MultipartFile file, // 👈 Đổi từ @RequestPart sang @RequestParam
            Authentication authentication
    ) {
        logger.info("Import endpoint reached, fileName={}, authPresent={}, username={}", file != null ? file.getOriginalFilename() : null, authentication != null, authentication != null ? authentication.getName() : null);
        List<ProductionReportResponse> reports = reportService.importReports(file);
        
        // Lấy tên người dùng an toàn (tránh NullPointerException nếu authentication null)
        String username = (authentication != null) ? authentication.getName() : "SYSTEM";

        if (reports != null) {
            reports.forEach(report -> auditLogService.record(
                    "CREATE",
                    "DailyProductionReport",
                    report.id(),
                    username,
                    String.format("Nhập báo cáo từ Excel %s - %s", report.lineCode(), report.partNumber())
            ));
        }
        return reports;
}

    @DeleteMapping
    public void deleteReports(@RequestBody java.util.List<Long> ids, Authentication authentication) {
        reportService.deleteReportsByIds(ids);
        if (ids != null) {
            ids.forEach(id -> auditLogService.record(
                    "DELETE",
                    "DailyProductionReport",
                    id,
                    authentication.getName(),
                    String.format("Xóa báo cáo OEE #%d", id)
            ));
        }
    }
}
