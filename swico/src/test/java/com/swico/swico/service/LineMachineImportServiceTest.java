package com.swico.swico.service;

import com.swico.swico.entity.Machine;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.MachineRepository;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LineMachineImportServiceTest {
    @Test
    void importShouldReadYesAndNoAndDefaultMissingValueToPowered() throws Exception {
        MachineRepository machines = mock(MachineRepository.class);
        List<Machine> saved = new ArrayList<>();
        when(machines.save(any(Machine.class))).thenAnswer(call -> {
            Machine machine = call.getArgument(0);
            saved.add(machine);
            return machine;
        });
        LineMachineImportService service = new LineMachineImportService(mock(LineRepository.class), machines);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet();
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã Máy");
            header.createCell(1).setCellValue("Dùng điện / Không dùng điện");
            String[] values = {"yes", "no", ""};
            for (int i = 0; i < values.length; i++) {
                var row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue("MC-" + i);
                row.createCell(1).setCellValue(values[i]);
            }
            workbook.write(out);
            service.importWorkbook(new MockMultipartFile("file", "machines.xlsx", "application/octet-stream", out.toByteArray()));
        }
        assertEquals(List.of(true, false, true), saved.stream().map(Machine::getUsesElectricity).toList());
    }

    @Test
    void importingLegacyFileShouldKeepExistingNonElectricSetting() throws Exception {
        MachineRepository machines = mock(MachineRepository.class);
        Machine machine = new Machine();
        machine.setMachineCode("MP-03");
        machine.setUsesElectricity(false);
        when(machines.findByMachineCode("MP-03")).thenReturn(Optional.of(machine));
        LineMachineImportService service = new LineMachineImportService(mock(LineRepository.class), machines);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("Mã Máy");
            sheet.createRow(1).createCell(0).setCellValue("MP-03");
            workbook.write(out);
            service.importWorkbook(new MockMultipartFile("file", "legacy.xlsx", "application/octet-stream", out.toByteArray()));
        }
        assertEquals(false, machine.getUsesElectricity());
    }
}
