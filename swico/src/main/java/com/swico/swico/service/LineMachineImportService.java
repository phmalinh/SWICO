package com.swico.swico.service;

import com.swico.swico.dto.LineMachineImportResponse;
import com.swico.swico.entity.Line;
import com.swico.swico.entity.Machine;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.MachineRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class LineMachineImportService {

    private final LineRepository lineRepository;
    private final MachineRepository machineRepository;

    public LineMachineImportService(LineRepository lineRepository, MachineRepository machineRepository) {
        this.lineRepository = lineRepository;
        this.machineRepository = machineRepository;
    }

    @Transactional
    public LineMachineImportResponse importWorkbook(MultipartFile file) {
        int rowsSkipped = 0;
        Set<String> touchedLines = new LinkedHashSet<>();
        Set<String> touchedMachines = new LinkedHashSet<>();

        try (InputStream input = file.getInputStream(); Workbook workbook = WorkbookFactory.create(input)) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = workbook.getSheetAt(0);
            int headerRowIndex = findHeaderRow(sheet, formatter);
            Row headerRow = sheet.getRow(headerRowIndex);
            int lineColumn = findColumn(headerRow, formatter, "line");
            int machineColumn = findColumn(headerRow, formatter, "machine");
            int assetColumn = findColumn(headerRow, formatter, "asset");
            int descriptionColumn = findColumn(headerRow, formatter, "description");
            int purchaseDateColumn = findColumn(headerRow, formatter, "purchaseDate");
            int custodyColumn = findColumn(headerRow, formatter, "custody");
            if (lineColumn < 0 && machineColumn < 0) {
                throw new IllegalArgumentException("Không tìm thấy cột chuyền hoặc thiết bị trong file Excel.");
            }

            Line currentLine = null;
            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    rowsSkipped++;
                    continue;
                }

                List<String> lineCodes = lineColumn >= 0 ? splitCodes(text(row, lineColumn, formatter, evaluator)) : List.of();
                List<String> machineCodes = machineColumn >= 0 ? splitCodes(text(row, machineColumn, formatter, evaluator)) : List.of();
                String assetCode = assetColumn >= 0 ? text(row, assetColumn, formatter, evaluator) : null;
                String description = descriptionColumn >= 0 ? text(row, descriptionColumn, formatter, evaluator) : null;
                LocalDate purchaseDate = purchaseDateColumn >= 0 ? date(row, purchaseDateColumn, formatter, evaluator) : null;
                String custodyDepartment = custodyColumn >= 0 ? text(row, custodyColumn, formatter, evaluator) : null;
                if (lineCodes.isEmpty() && machineCodes.isEmpty()) {
                    rowsSkipped++;
                    continue;
                }

                Line singleLine = lineCodes.size() == 1 ? ensureLine(lineCodes.get(0), touchedLines) : null;
                if (singleLine != null) {
                    currentLine = singleLine;
                }
                for (String lineCode : lineCodes) {
                    ensureLine(lineCode, touchedLines);
                }
                Line machineLine = singleLine != null ? singleLine : currentLine;
                for (String machineCode : machineCodes) {
                    ensureMachine(machineCode, machineLine, assetCode, description, purchaseDate, custodyDepartment, touchedMachines);
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Không import được chuyền và thiết bị: " + ex.getMessage(), ex);
        }

        return new LineMachineImportResponse(touchedLines.size(), touchedMachines.size(), rowsSkipped);
    }

    private int findHeaderRow(Sheet sheet, DataFormatter formatter) {
        for (int i = 0; i <= Math.min(sheet.getLastRowNum(), 30); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String joined = "";
            for (int c = 0; c < Math.max(0, row.getLastCellNum()); c++) {
                joined += " " + normalize(formatter.formatCellValue(row.getCell(c)));
            }
            if (joined.contains("line") || joined.contains("day chuyen") || joined.contains("線別") || joined.contains("機台")) {
                return i;
            }
        }
        return 0;
    }

    private int findColumn(Row headerRow, DataFormatter formatter, String type) {
        if (headerRow == null) return -1;
        for (int c = 0; c < Math.max(0, headerRow.getLastCellNum()); c++) {
            String header = normalize(formatter.formatCellValue(headerRow.getCell(c)));
            if (type.equals("line") && (header.contains("線別") || header.contains("line") || header.contains("day chuyen") || header.contains("chuyen"))) {
                return c;
            }
            if (type.equals("machine") && (header.contains("機台") || header.contains("設備編號") || header.contains("machine") || header.contains("ma so may") || header.contains("ma may") || header.contains("ma thiet bi"))) {
                return c;
            }
            if (type.equals("asset") && (header.contains("財產編號") || header.contains("ma tai san") || header.contains("asset"))) {
                return c;
            }
            if (type.equals("description") && (header.contains("設備名稱") || header.contains("ten tai san") || header.contains("ten thiet bi") || header.contains("machine name"))) {
                return c;
            }
            if (type.equals("purchaseDate") && (header.contains("購入日期") || header.contains("ngay nhap") || header.contains("purchase date"))) {
                return c;
            }
            if (type.equals("custody") && (header.contains("保管單位") || header.contains("bo phan bao quan") || header.contains("custody"))) {
                return c;
            }
        }
        return -1;
    }

    private Line ensureLine(String code, Set<String> touchedLines) {
        if (isBlank(code)) return null;
        touchedLines.add(code);
        return lineRepository.findByLineCode(code).orElseGet(() -> {
            Line line = new Line();
            line.setLineCode(code);
            line.setDescription(code);
            return lineRepository.save(line);
        });
    }

    private void ensureMachine(String code, Line line, String assetCode, String description, LocalDate purchaseDate,
                               String custodyDepartment, Set<String> touchedMachines) {
        if (isBlank(code)) return;
        touchedMachines.add(code);
        machineRepository.findByMachineCode(code)
                .map(existing -> {
                    applyMachineDetails(existing, line, assetCode, description, purchaseDate, custodyDepartment);
                    return machineRepository.save(existing);
                })
                .orElseGet(() -> {
                    Machine machine = new Machine();
                    machine.setMachineCode(code);
                    applyMachineDetails(machine, line, assetCode, description, purchaseDate, custodyDepartment);
                    return machineRepository.save(machine);
                });
    }

    private void applyMachineDetails(Machine machine, Line line, String assetCode, String description,
                                     LocalDate purchaseDate, String custodyDepartment) {
        if (line != null) {
            machine.setLine(line);
        }
        if (!isBlank(assetCode)) {
            machine.setAssetCode(assetCode.trim());
        }
        machine.setDescription(!isBlank(description) ? description.trim() : machine.getMachineCode());
        if (purchaseDate != null) {
            machine.setPurchaseDate(purchaseDate);
        }
        if (!isBlank(custodyDepartment)) {
            machine.setCustodyDepartment(custodyDepartment.trim());
        }
    }

    private List<String> splitCodes(String value) {
        if (isBlank(value)) return List.of();
        List<String> result = new ArrayList<>();
        for (String raw : value.split("[;；,，\\n\\r]+")) {
            String code = raw.trim();
            if (!code.isEmpty() && !result.contains(code)) {
                result.add(code);
            }
        }
        return result;
    }

    private String text(Row row, int columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell, evaluator);
        return isBlank(value) ? null : value.trim();
    }

    private LocalDate date(Row row, int columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String value = text(row, columnIndex, formatter, evaluator);
        if (isBlank(value)) return null;
        List<DateTimeFormatter> formats = Arrays.asList(
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        );
        for (DateTimeFormatter format : formats) {
            try {
                LocalDate parsed = LocalDate.parse(value.trim(), format);
                return parsed.getYear() >= 1900 ? parsed : null;
            } catch (DateTimeParseException ignored) {
                // Try the next common Excel text-date format.
            }
        }
        return null;
    }

    private String normalize(String value) {
        String ascii = Normalizer.normalize(String.valueOf(value == null ? "" : value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return ascii
                .toLowerCase(Locale.ROOT)
                .replace("đ", "d")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
