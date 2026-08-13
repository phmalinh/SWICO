package com.swico.swico.service;

import com.swico.swico.dto.ProductionInfoImportResponse;
import com.swico.swico.entity.Line;
import com.swico.swico.entity.Machine;
import com.swico.swico.entity.Product;
import com.swico.swico.entity.ProductProcess;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.MachineRepository;
import com.swico.swico.repository.ProductProcessRepository;
import com.swico.swico.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
public class ProductionInfoImportService {

    private final ProductRepository productRepository;
    private final ProductProcessRepository productProcessRepository;
    private final LineRepository lineRepository;
    private final MachineRepository machineRepository;

    public ProductionInfoImportService(
            ProductRepository productRepository,
            ProductProcessRepository productProcessRepository,
            LineRepository lineRepository,
            MachineRepository machineRepository
    ) {
        this.productRepository = productRepository;
        this.productProcessRepository = productProcessRepository;
        this.lineRepository = lineRepository;
        this.machineRepository = machineRepository;
    }

    @Transactional
    public ProductionInfoImportResponse importWorkbook(MultipartFile file) {
        int productsImported = 0;
        int processesImported = 0;
        int rowsSkipped = 0;
        Set<String> touchedProducts = new HashSet<>();
        Set<String> touchedProductCycleTimes = new HashSet<>();

        try (InputStream input = file.getInputStream(); Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            int headerRowIndex = findHeaderRow(sheet);
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    rowsSkipped++;
                    continue;
                }

                String customer = text(row, 0, formatter, evaluator);
                String partName = text(row, 1, formatter, evaluator);
                String partNumber = text(row, 2, formatter, evaluator);
                String processCode = text(row, 3, formatter, evaluator);
                String process = text(row, 4, formatter, evaluator);
                BigDecimal cycleTime = number(row, 5, formatter, evaluator);
                String lineCode = text(row, 6, formatter, evaluator);
                String machineCode = text(row, 7, formatter, evaluator);
                Integer sequence = integer(row, 8, formatter, evaluator);

                if (isBlank(partNumber) || isBlank(partName) || isBlank(process)) {
                    rowsSkipped++;
                    continue;
                }

                BigDecimal productCycleTime = null;
                if (cycleTime != null && cycleTime.compareTo(BigDecimal.ZERO) > 0 && !touchedProductCycleTimes.contains(partNumber)) {
                    productCycleTime = cycleTime;
                    touchedProductCycleTimes.add(partNumber);
                }

                Product product = upsertProduct(partNumber, partName, customer, productCycleTime);
                if (touchedProducts.add(partNumber)) {
                    productsImported++;
                }

                upsertProcess(product, processCode, process, sequence, lineCode, machineCode, cycleTime);
                ensureLines(lineCode);
                ensureMachines(lineCode, machineCode);
                processesImported++;
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot import production info workbook: " + ex.getMessage(), ex);
        }

        return new ProductionInfoImportResponse(productsImported, processesImported, rowsSkipped);
    }

    private int findHeaderRow(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();
        for (int i = 0; i <= Math.min(sheet.getLastRowNum(), 20); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String joined = "";
            for (int c = 0; c < Math.min(row.getLastCellNum(), 12); c++) {
                joined += " " + formatter.formatCellValue(row.getCell(c));
            }
            if (joined.contains("品號") || joined.toLowerCase().contains("mã hàng")) {
                return i;
            }
        }
        return 0;
    }

    private Product upsertProduct(String partNumber, String partName, String customer, BigDecimal cycleTime) {
        BigDecimal fallbackCycleTime = cycleTime != null && cycleTime.compareTo(BigDecimal.ZERO) > 0
                ? cycleTime
                : BigDecimal.ONE;
        return productRepository.findByPartNumber(partNumber)
                .map(existing -> {
                    existing.setPartName(partName);
                    existing.setCustomer(customer);
                    if (cycleTime != null && cycleTime.compareTo(BigDecimal.ZERO) > 0) {
                        existing.setCycleTimeSeconds(cycleTime);
                    }
                    return productRepository.save(existing);
                })
                .orElseGet(() -> {
                    Product product = new Product();
                    product.setPartNumber(partNumber);
                    product.setPartName(partName);
                    product.setCustomer(customer);
                    product.setCycleTimeSeconds(fallbackCycleTime);
                    return productRepository.save(product);
                });
    }

    private void upsertProcess(Product product, String processCode, String process, Integer sequence,
                               String lineCode, String machineCode, BigDecimal cycleTime) {
        ProductProcess productProcess = !isBlank(processCode)
                ? productProcessRepository.findByProductIdAndProcessCode(product.getId(), processCode).orElseGet(ProductProcess::new)
                : new ProductProcess();
        productProcess.setProduct(product);
        productProcess.setProcessCode(processCode);
        productProcess.setProcess(process);
        productProcess.setSequence(sequence);
        productProcess.setLineCode(lineCode);
        productProcess.setMachineCode(machineCode);
        productProcess.setCycleTimeSeconds(cycleTime);
        productProcessRepository.save(productProcess);
    }

    private void ensureLines(String value) {
        if (isBlank(value)) return;
        for (String raw : value.split(";")) {
            String code = raw.trim();
            if (code.isEmpty()) continue;
            ensureLine(code);
        }
    }

    private Line ensureLine(String code) {
        return lineRepository.findByLineCode(code).orElseGet(() -> {
            Line line = new Line();
            line.setLineCode(code);
            line.setDescription(code);
            return lineRepository.save(line);
        });
    }

    private void ensureMachines(String lineCodeValue, String machineCodeValue) {
        if (isBlank(machineCodeValue)) return;
        Line firstLine = null;
        if (!isBlank(lineCodeValue)) {
            String firstLineCode = lineCodeValue.split(";")[0].trim();
            if (!firstLineCode.isEmpty()) {
                firstLine = ensureLine(firstLineCode);
            }
        }

        for (String raw : machineCodeValue.split(";")) {
            String code = raw.trim();
            if (code.isEmpty()) continue;
            Line line = firstLine;
            machineRepository.findByMachineCode(code)
                    .map(existing -> {
                        if (existing.getLine() == null && line != null) {
                            existing.setLine(line);
                            return machineRepository.save(existing);
                        }
                        return existing;
                    })
                    .orElseGet(() -> {
                        Machine machine = new Machine();
                        machine.setMachineCode(code);
                        machine.setDescription(code);
                        machine.setLine(line);
                        return machineRepository.save(machine);
                    });
        }
    }

    private String text(Row row, int columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell, evaluator);
        return isBlank(value) ? null : value.trim();
    }

    private BigDecimal number(Row row, int columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        String value = text(row, columnIndex, formatter, evaluator);
        if (isBlank(value)) return null;
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer integer(Row row, int columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        BigDecimal value = number(row, columnIndex, formatter, evaluator);
        return value != null ? value.intValue() : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
