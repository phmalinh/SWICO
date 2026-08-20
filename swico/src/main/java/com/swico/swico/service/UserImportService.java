package com.swico.swico.service;

import com.swico.swico.dto.UserImportResponse;
import com.swico.swico.entity.Role;
import com.swico.swico.entity.User;
import com.swico.swico.repository.UserRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class UserImportService {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("d/M/uuuu", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT),
            DateTimeFormatter.ofPattern("d-M-uuuu", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd-MM-uuuu", Locale.ROOT),
            DateTimeFormatter.ofPattern("uuuu-M-d", Locale.ROOT),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserImportService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserImportResponse importWorkbook(MultipartFile file) {
        int created = 0;
        int updated = 0;
        int skipped = 0;

        try (InputStream input = file.getInputStream(); Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            Map<String, Integer> columns = findColumns(sheet, formatter);
            Integer usernameCol = columns.get("username");
            Integer fullNameCol = columns.get("fullName");

            if (usernameCol == null || fullNameCol == null) {
                throw new IllegalArgumentException("File cần có cột MÃ SỐ và TÊN NHÂN VIÊN");
            }

            int headerRowIndex = findHeaderRowIndex(sheet, formatter);
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    skipped++;
                    continue;
                }

                String username = cleanCell(row, usernameCol, formatter);
                String fullName = cleanMultiline(cleanCell(row, fullNameCol, formatter));
                if (username.isBlank() || fullName.isBlank()) {
                    skipped++;
                    continue;
                }

                String jobTitle = cleanMultiline(cleanCell(row, columns.get("jobTitle"), formatter));
                String team = cleanCell(row, columns.get("team"), formatter);
                String password = cleanPassword(cleanCell(row, columns.get("password"), formatter));
                Role role = resolveRole(cleanCell(row, columns.get("role"), formatter), jobTitle);
                LocalDate hireDate = parseDate(row, columns.get("hireDate"), formatter);

                User user = userRepository.findByUsername(username).orElseGet(User::new);
                boolean isNew = user.getId() == null;
                user.setUsername(username);
                user.setFullName(fullName);
                user.setJobTitle(jobTitle);
                user.setTeam(team);
                user.setHireDate(hireDate);
                user.setRole(role);
                user.setActive(true);

                if (isNew || !password.isBlank()) {
                    user.setPassword(passwordEncoder.encode(password.isBlank() ? username : password));
                    user.setMustChangePassword(true);
                }

                userRepository.save(user);
                if (isNew) {
                    created++;
                } else {
                    updated++;
                }
            }
            return new UserImportResponse(created, updated, skipped);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Không import được danh sách tài khoản: " + ex.getMessage(), ex);
        }
    }

    private int findHeaderRowIndex(Sheet sheet, DataFormatter formatter) {
        for (int i = 0; i <= Math.min(sheet.getLastRowNum(), 20); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            for (Cell cell : row) {
                if (normalize(formatter.formatCellValue(cell)).contains("ma so")) {
                    return i;
                }
            }
        }
        return 0;
    }

    private Map<String, Integer> findColumns(Sheet sheet, DataFormatter formatter) {
        Row header = sheet.getRow(findHeaderRowIndex(sheet, formatter));
        Map<String, Integer> columns = new HashMap<>();
        if (header == null) return columns;

        for (Cell cell : header) {
            String label = normalize(formatter.formatCellValue(cell));
            int index = cell.getColumnIndex();
            if (label.contains("ma so") || label.contains("tai khoan") || label.contains("username")) {
                columns.put("username", index);
            } else if (label.contains("ten nhan vien") || label.contains("ho ten") || label.contains("full name")) {
                columns.put("fullName", index);
            } else if (label.contains("chuc vu") || label.contains("job title")) {
                columns.put("jobTitle", index);
            } else if (label.equals("to") || label.contains("team")) {
                columns.put("team", index);
            } else if (label.contains("ngay vao lam") || label.contains("hire date")) {
                columns.put("hireDate", index);
            } else if (label.contains("mat khau") || label.contains("password")) {
                columns.put("password", index);
            } else if (label.contains("vai tro") || label.contains("role") || label.contains("quyen")) {
                columns.put("role", index);
            }
        }
        return columns;
    }

    private String cleanCell(Row row, Integer columnIndex, DataFormatter formatter) {
        if (row == null || columnIndex == null) return "";
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private String cleanMultiline(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ").trim();
    }

    private String cleanPassword(String value) {
        return value == null ? "" : value.replace("\u00A0", " ").replace('\n', ' ').replace('\r', ' ').trim();
    }

    private LocalDate parseDate(Row row, Integer columnIndex, DataFormatter formatter) {
        if (row == null || columnIndex == null) return null;
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String raw = formatter.formatCellValue(cell).trim();
        if (raw.isBlank()) return null;
        for (DateTimeFormatter formatterCandidate : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(raw, formatterCandidate);
            } catch (DateTimeParseException ignored) {
                // Try the next supported date pattern.
            }
        }
        return null;
    }

    private Role resolveRole(String explicitRole, String jobTitle) {
        String role = normalize(explicitRole);
        if (role.contains("admin")) return Role.ROLE_ADMIN;
        if (role.contains("manager") || role.contains("quan ly")) return Role.ROLE_MANAGER;
        if (role.contains("leader") || role.contains("to truong")) return Role.ROLE_LEADER;

        String title = normalize(jobTitle);
        if (title.contains("admin")) return Role.ROLE_ADMIN;
        if (title.contains("quan ly") || title.contains("manager")) return Role.ROLE_MANAGER;
        if (title.contains("to truong")
                || title.contains("cb")
                || title.contains("can bo")
                || title.contains("pho phong")
                || title.contains("du bi")
                || title.contains("組長")
                || title.contains("幹部")
                || title.contains("主管")) {
            return Role.ROLE_LEADER;
        }
        return Role.ROLE_OPERATOR;
    }

    private String normalize(String value) {
        if (value == null) return "";
        String noAccent = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccent.toLowerCase(Locale.ROOT).replace('đ', 'd').trim();
    }
}
