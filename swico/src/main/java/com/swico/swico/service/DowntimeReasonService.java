package com.swico.swico.service;

import com.swico.swico.dto.DowntimeReasonCategoryResponse;
import com.swico.swico.dto.DowntimeReasonCategoryUpsertRequest;
import com.swico.swico.dto.DowntimeReasonImportResponse;
import com.swico.swico.dto.DowntimeReasonResponse;
import com.swico.swico.dto.DowntimeReasonUpsertRequest;
import com.swico.swico.entity.DowntimeReason;
import com.swico.swico.entity.DowntimeReasonCategory;
import com.swico.swico.repository.DailyProductionReportRepository;
import com.swico.swico.repository.DowntimeReasonCategoryRepository;
import com.swico.swico.repository.DowntimeReasonRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DowntimeReasonService {
    private static final Pattern LEADING_NUMBER = Pattern.compile("^\\s*(\\d+)");

    private final DowntimeReasonRepository downtimeReasonRepository;
    private final DowntimeReasonCategoryRepository categoryRepository;
    private final DailyProductionReportRepository reportRepository;

    public DowntimeReasonService(DowntimeReasonRepository downtimeReasonRepository, DowntimeReasonCategoryRepository categoryRepository, DailyProductionReportRepository reportRepository) {
        this.downtimeReasonRepository = downtimeReasonRepository;
        this.categoryRepository = categoryRepository;
        this.reportRepository = reportRepository;
    }

    public List<DowntimeReasonResponse> getAll() {
        return downtimeReasonRepository.findAllByOrderByReasonCategoryCodeAscSortOrderAscReasonCodeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<DowntimeReasonCategoryResponse> getCategories() {
        return categoryRepository.findAllByOrderBySortOrderAscReasonCategoryCodeAsc().stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    public DowntimeReasonCategoryResponse createCategory(DowntimeReasonCategoryUpsertRequest request) {
        assertCategoryCodeAvailable(request.reasonCategoryCode(), null);
        DowntimeReasonCategory category = new DowntimeReasonCategory();
        applyCategoryRequest(category, request);
        return toCategoryResponse(categoryRepository.save(category));
    }

    public DowntimeReasonCategoryResponse updateCategory(Long id, DowntimeReasonCategoryUpsertRequest request) {
        DowntimeReasonCategory category = categoryRepository.findById(id).orElseThrow();
        String oldCode = category.getReasonCategoryCode();
        assertCategoryCodeAvailable(request.reasonCategoryCode(), id);
        applyCategoryRequest(category, request);
        DowntimeReasonCategory saved = categoryRepository.save(category);
        if (!oldCode.equals(saved.getReasonCategoryCode())) {
            downtimeReasonRepository.findAll().stream()
                    .filter(reason -> oldCode.equals(reason.getReasonCategoryCode()))
                    .forEach(reason -> {
                        reason.setReasonCategoryCode(saved.getReasonCategoryCode());
                        downtimeReasonRepository.save(reason);
                    });
        }
        return toCategoryResponse(saved);
    }

    public void deleteCategory(Long id) {
        DowntimeReasonCategory category = categoryRepository.findById(id).orElseThrow();
        if (downtimeReasonRepository.existsByReasonCategoryCode(category.getReasonCategoryCode())) {
            throw new IllegalStateException("Không thể xóa thể loại " + category.getReasonCategoryCode() + " vì vẫn còn nguyên nhân con. Vui lòng xóa/chuyển nguyên nhân con hoặc khóa thể loại này.");
        }
        categoryRepository.deleteById(id);
    }

    @Transactional
    public DowntimeReasonImportResponse importWorkbook(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file Excel.");
        }

        int categoriesImported = 0;
        int reasonsImported = 0;
        String currentCategoryCode = null;

        try (InputStream input = file.getInputStream(); Workbook workbook = WorkbookFactory.create(input)) {
            var sheet = workbook.getSheetAt(0);
            for (int rowIndex = 3; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String categoryText = cellText(row, 0);
                String reasonCode = cellText(row, 2);
                String reasonText = cellText(row, 3);

                if (!categoryText.isBlank()) {
                    currentCategoryCode = extractCategoryCode(categoryText, categoriesImported + 1);
                    ensureCategory(currentCategoryCode, categoryText, categoriesImported + 1);
                    categoriesImported++;
                }

                if (!reasonCode.isBlank() && !reasonText.isBlank()) {
                    ensure(reasonCode, reasonText, currentCategoryCode, reasonsImported + 1);
                    reasonsImported++;
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Không import được file Excel: " + ex.getMessage());
        }

        if (categoriesImported == 0 && reasonsImported == 0) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu thể loại/nguyên nhân trong file Excel.");
        }

        return new DowntimeReasonImportResponse(categoriesImported, reasonsImported);
    }

    public DowntimeReasonResponse create(DowntimeReasonUpsertRequest request) {
        assertReasonCodeAvailable(request.reasonCode(), null);
        DowntimeReason reason = new DowntimeReason();
        applyRequest(reason, request);
        return toResponse(downtimeReasonRepository.save(reason));
    }

    public DowntimeReasonResponse update(Long id, DowntimeReasonUpsertRequest request) {
        DowntimeReason reason = downtimeReasonRepository.findById(id).orElseThrow();
        assertReasonCodeAvailable(request.reasonCode(), id);
        applyRequest(reason, request);
        return toResponse(downtimeReasonRepository.save(reason));
    }

    public void delete(Long id) {
        DowntimeReason reason = downtimeReasonRepository.findById(id).orElseThrow();
        if (reportRepository.existsByDowntimeReasonContaining(reason.getReasonCode())
                || reportRepository.existsByDowntimeReasonContaining(reason.getReasonText())) {
            throw new IllegalStateException("Không thể xóa lý do dừng " + reason.getReasonCode() + " vì vẫn còn dữ liệu liên quan: báo cáo sản xuất. Vui lòng xóa hoặc chuyển dữ liệu liên quan trước.");
        }
        downtimeReasonRepository.deleteById(id);
    }

    public DowntimeReason ensure(String reasonCode, String reasonText, int sortOrder) {
        return ensure(reasonCode, reasonText, null, sortOrder);
    }

    public DowntimeReason ensure(String reasonCode, String reasonText, String reasonCategoryCode, int sortOrder) {
        return downtimeReasonRepository.findByReasonCode(reasonCode)
                .map(existing -> {
                    existing.setReasonText(reasonText);
                    existing.setReasonCategoryCode(reasonCategoryCode);
                    existing.setSortOrder(sortOrder);
                    if (existing.getActive() == null) {
                        existing.setActive(true);
                    }
                    return downtimeReasonRepository.save(existing);
                })
                .orElseGet(() -> {
                    DowntimeReason reason = new DowntimeReason();
                    reason.setReasonCode(reasonCode);
                    reason.setReasonText(reasonText);
                    reason.setReasonCategoryCode(reasonCategoryCode);
                    reason.setSortOrder(sortOrder);
                    reason.setActive(true);
                    return downtimeReasonRepository.save(reason);
                });
    }

    public DowntimeReasonCategory ensureCategory(String reasonCategoryCode, String reasonCategoryText, int sortOrder) {
        return categoryRepository.findByReasonCategoryCode(reasonCategoryCode)
                .map(existing -> {
                    existing.setReasonCategoryText(reasonCategoryText);
                    existing.setSortOrder(sortOrder);
                    if (existing.getActive() == null) {
                        existing.setActive(true);
                    }
                    return categoryRepository.save(existing);
                })
                .orElseGet(() -> {
                    DowntimeReasonCategory category = new DowntimeReasonCategory();
                    category.setReasonCategoryCode(reasonCategoryCode);
                    category.setReasonCategoryText(reasonCategoryText);
                    category.setSortOrder(sortOrder);
                    category.setActive(true);
                    return categoryRepository.save(category);
                });
    }

    private void applyRequest(DowntimeReason reason, DowntimeReasonUpsertRequest request) {
        reason.setReasonCode(request.reasonCode().trim());
        reason.setReasonText(request.reasonText().trim());
        reason.setReasonCategoryCode(normalizeBlank(request.reasonCategoryCode()));
        reason.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        reason.setActive(request.active() != null ? request.active() : true);
    }

    private String cellText(Row row, int cellIndex) {
        var cell = row.getCell(cellIndex);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double value = cell.getNumericCellValue();
                if (value == Math.rint(value)) {
                    yield String.valueOf((long) value);
                }
                yield String.valueOf(value);
            }
            case FORMULA -> cell.getCellFormula().trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private String extractCategoryCode(String categoryText, int fallbackOrder) {
        Matcher matcher = LEADING_NUMBER.matcher(categoryText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return String.valueOf(fallbackOrder);
    }

    private void assertCategoryCodeAvailable(String reasonCategoryCode, Long currentId) {
        categoryRepository.findByReasonCategoryCode(reasonCategoryCode.trim())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã thể loại " + reasonCategoryCode.trim() + " đã tồn tại.");
                });
    }

    private void assertReasonCodeAvailable(String reasonCode, Long currentId) {
        downtimeReasonRepository.findByReasonCode(reasonCode.trim())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã nguyên nhân " + reasonCode.trim() + " đã tồn tại.");
                });
    }

    private void applyCategoryRequest(DowntimeReasonCategory category, DowntimeReasonCategoryUpsertRequest request) {
        category.setReasonCategoryCode(request.reasonCategoryCode().trim());
        category.setReasonCategoryText(request.reasonCategoryText().trim());
        category.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        category.setActive(request.active() != null ? request.active() : true);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private DowntimeReasonResponse toResponse(DowntimeReason reason) {
        return new DowntimeReasonResponse(
                reason.getId(),
                reason.getReasonCode(),
                reason.getReasonText(),
                reason.getReasonCategoryCode(),
                reason.getSortOrder(),
                reason.getActive()
        );
    }

    private DowntimeReasonCategoryResponse toCategoryResponse(DowntimeReasonCategory category) {
        return new DowntimeReasonCategoryResponse(
                category.getId(),
                category.getReasonCategoryCode(),
                category.getReasonCategoryText(),
                category.getSortOrder(),
                category.getActive()
        );
    }
}
