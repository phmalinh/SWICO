package com.swico.swico.service;

import com.swico.swico.dto.EmployeeSkillImportResponse;
import com.swico.swico.dto.EmployeeSkillResponse;
import com.swico.swico.dto.EmployeeSkillUpsertRequest;
import com.swico.swico.dto.EmployeeSkillUserOption;
import com.swico.swico.entity.EmployeeSkill;
import com.swico.swico.entity.Product;
import com.swico.swico.entity.ProductProcess;
import com.swico.swico.entity.User;
import com.swico.swico.repository.EmployeeSkillRepository;
import com.swico.swico.repository.ProductProcessRepository;
import com.swico.swico.repository.ProductRepository;
import com.swico.swico.repository.UserRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class EmployeeSkillService {

    private final EmployeeSkillRepository repository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductProcessRepository productProcessRepository;

    public EmployeeSkillService(
            EmployeeSkillRepository repository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ProductProcessRepository productProcessRepository
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productProcessRepository = productProcessRepository;
    }

    public List<EmployeeSkillResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public List<EmployeeSkillUserOption> getUserOptions() {
        return userRepository.findAll().stream()
                .filter(User::isActive)
                .map(user -> new EmployeeSkillUserOption(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getRole() != null ? user.getRole().name() : null,
                        user.getLineCode(),
                        user.getJobTitle(),
                        user.getTeam(),
                        user.getHireDate() != null ? user.getHireDate().toString() : null
                ))
                .toList();
    }

    public EmployeeSkillResponse create(EmployeeSkillUpsertRequest request) {
        EmployeeSkill item = new EmployeeSkill();
        applyRequest(item, request);
        return toResponse(repository.save(item));
    }

    public EmployeeSkillResponse update(Long id, EmployeeSkillUpsertRequest request) {
        EmployeeSkill item = repository.findById(id).orElseThrow();
        applyRequest(item, request);
        return toResponse(repository.save(item));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    @Transactional
    public EmployeeSkillImportResponse importMatrix(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File Excel khong hop le.");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.US);
            Map<Integer, EmployeeHeader> employees = readEmployeeHeaders(sheet, formatter);
            List<EmployeeSkill> imported = new ArrayList<>();
            int productsScanned = 0;

            for (int rowIndex = 8; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String partName = cellText(row.getCell(0), formatter);
                String partNumber = cellText(row.getCell(1), formatter);
                if (isBlank(partName) && isBlank(partNumber)) continue;
                productsScanned++;

                for (Map.Entry<Integer, EmployeeHeader> entry : employees.entrySet()) {
                    String process = normalizeProcess(cellText(row.getCell(entry.getKey()), formatter));
                    if (isBlank(process) || "0".equals(process)) continue;
                    EmployeeHeader header = entry.getValue();
                    User user = userRepository.findByUsername(header.employeeCode()).orElse(null);
                    Product product = productRepository.findByPartNumber(partNumber).orElse(null);
                    ProductProcess productProcess = resolveProductProcess(product, process);
                    EmployeeSkill item = new EmployeeSkill();
                    item.setUser(user);
                    item.setEmployeeCode(header.employeeCode());
                    item.setEmployeeName(user != null ? user.getFullName() : header.employeeName());
                    item.setJobTitle(user != null ? user.getJobTitle() : header.jobTitle());
                    item.setTeam(user != null ? user.getTeam() : header.team());
                    item.setHireDate(user != null && user.getHireDate() != null ? user.getHireDate() : header.hireDate());
                    item.setProduct(product);
                    item.setPartName(product != null ? product.getPartName() : partName);
                    item.setPartNumber(product != null ? product.getPartNumber() : partNumber);
                    item.setProductProcess(productProcess);
                    item.setProcess(productProcess != null ? processLabel(productProcess) : process);
                    item.setSkill(process);
                    imported.add(item);
                }
            }

            List<String> employeeCodes = employees.values().stream()
                    .map(EmployeeHeader::employeeCode)
                    .filter(Objects::nonNull)
                    .filter(code -> !code.isBlank())
                    .distinct()
                    .toList();
            if (!employeeCodes.isEmpty()) {
                repository.deleteByEmployeeCodeIn(employeeCodes);
            }
            repository.saveAll(imported);
            return new EmployeeSkillImportResponse(employeeCodes.size(), productsScanned, imported.size());
        } catch (IOException e) {
            throw new IllegalArgumentException("Khong doc duoc file Excel: " + e.getMessage(), e);
        }
    }

    private void applyRequest(EmployeeSkill item, EmployeeSkillUpsertRequest request) {
        User user = resolveUser(request);
        Product product = resolveProduct(request);
        ProductProcess process = resolveProcess(request, product);

        item.setUser(user);
        item.setEmployeeCode(user != null ? user.getUsername() : request.employeeCode());
        item.setEmployeeName(user != null ? user.getFullName() : request.employeeName());
        item.setJobTitle(user != null ? user.getJobTitle() : request.jobTitle());
        item.setTeam(user != null ? user.getTeam() : request.team());
        item.setHireDate(user != null && user.getHireDate() != null ? user.getHireDate() : request.hireDate());
        item.setProduct(product);
        item.setPartName(product != null ? product.getPartName() : request.partName());
        item.setPartNumber(product != null ? product.getPartNumber() : request.partNumber());
        item.setProductProcess(process);
        item.setProcess(process != null ? processLabel(process) : request.process());
        item.setSkill(request.skill() != null && !request.skill().isBlank()
                ? request.skill()
                : (process != null ? processLabel(process) : request.process()));
    }

    private EmployeeSkillResponse toResponse(EmployeeSkill item) {
        return new EmployeeSkillResponse(
                item.getId(),
                item.getUser() != null ? item.getUser().getId() : null,
                item.getEmployeeCode(),
                item.getEmployeeName(),
                item.getJobTitle(),
                item.getTeam(),
                item.getSkill(),
                item.getHireDate() != null ? item.getHireDate().toString() : null,
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getPartName(),
                item.getPartNumber(),
                item.getProductProcess() != null ? item.getProductProcess().getId() : null,
                item.getProcess()
        );
    }

    private User resolveUser(EmployeeSkillUpsertRequest request) {
        if (request.userId() != null) {
            return userRepository.findById(request.userId()).orElse(null);
        }
        if (!isBlank(request.employeeCode())) {
            return userRepository.findByUsername(request.employeeCode()).orElse(null);
        }
        return null;
    }

    private Product resolveProduct(EmployeeSkillUpsertRequest request) {
        if (request.productId() != null) {
            return productRepository.findById(request.productId()).orElse(null);
        }
        if (!isBlank(request.partNumber())) {
            return productRepository.findByPartNumber(request.partNumber()).orElse(null);
        }
        return null;
    }

    private ProductProcess resolveProcess(EmployeeSkillUpsertRequest request, Product product) {
        if (request.processId() != null) {
            return productProcessRepository.findById(request.processId()).orElse(null);
        }
        return resolveProductProcess(product, request.process());
    }

    private ProductProcess resolveProductProcess(Product product, String processText) {
        if (product == null || isBlank(processText)) return null;
        String normalized = normalizeProcess(processText);
        if (normalized.contains("+") || normalized.contains("-")) return null;
        return productProcessRepository.findByProductIdOrderBySequence(product.getId()).stream()
                .filter(process -> normalized.equalsIgnoreCase(normalizeProcess(process.getProcessCode()))
                        || normalized.equalsIgnoreCase(normalizeProcess(process.getProcess())))
                .findFirst()
                .orElse(null);
    }

    private String processLabel(ProductProcess process) {
        if (!isBlank(process.getProcessCode()) && !isBlank(process.getProcess())) {
            return process.getProcessCode() + " - " + process.getProcess();
        }
        if (!isBlank(process.getProcessCode())) return process.getProcessCode();
        return process.getProcess();
    }

    private Map<Integer, EmployeeHeader> readEmployeeHeaders(Sheet sheet, DataFormatter formatter) {
        Map<Integer, EmployeeHeader> employees = new LinkedHashMap<>();
        Row nameRow = sheet.getRow(2);
        Row codeRow = sheet.getRow(3);
        Row titleRow = sheet.getRow(4);
        Row teamRow = sheet.getRow(5);
        Row hireDateRow = sheet.getRow(6);
        int lastCell = Math.max(nameRow != null ? nameRow.getLastCellNum() : 0, codeRow != null ? codeRow.getLastCellNum() : 0);

        for (int col = 2; col < lastCell; col++) {
            String employeeName = cellText(nameRow != null ? nameRow.getCell(col) : null, formatter);
            String employeeCode = cellText(codeRow != null ? codeRow.getCell(col) : null, formatter);
            if (isBlank(employeeCode) && isBlank(employeeName)) continue;
            if (isBlank(employeeCode)) employeeCode = employeeName;
            employees.put(col, new EmployeeHeader(
                    employeeCode,
                    employeeName,
                    cellText(titleRow != null ? titleRow.getCell(col) : null, formatter),
                    cellText(teamRow != null ? teamRow.getCell(col) : null, formatter),
                    cellDate(hireDateRow != null ? hireDateRow.getCell(col) : null, formatter)
            ));
        }
        return employees;
    }

    private String cellText(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        return formatter.formatCellValue(cell).replace('\u00a0', ' ').trim();
    }

    private LocalDate cellDate(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String text = cellText(cell, formatter);
        if (isBlank(text)) return null;
        List<DateTimeFormatter> patterns = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ofPattern("d-M-yyyy"),
                DateTimeFormatter.ofPattern("yyyy/M/d")
        );
        for (DateTimeFormatter pattern : patterns) {
            try {
                return LocalDate.parse(text, pattern);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private String normalizeProcess(String value) {
        if (value == null) return "";
        return value.replace("\r", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", "")
                .replaceAll("[,;]", "+")
                .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record EmployeeHeader(String employeeCode, String employeeName, String jobTitle, String team, LocalDate hireDate) {
    }
}
