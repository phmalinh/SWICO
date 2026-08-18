package com.swico.swico.service;

import com.swico.swico.dto.MachineResponse;
import com.swico.swico.dto.MachineUpsertRequest;
import com.swico.swico.entity.Line;
import com.swico.swico.entity.Machine;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.MachineRepository;
import com.swico.swico.repository.DailyProductionReportRepository;
import com.swico.swico.repository.ProductProcessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final LineRepository lineRepository;
    private final DailyProductionReportRepository reportRepository;
    private final ProductProcessRepository productProcessRepository;

    public MachineService(MachineRepository machineRepository, LineRepository lineRepository, DailyProductionReportRepository reportRepository, ProductProcessRepository productProcessRepository) {
        this.machineRepository = machineRepository;
        this.lineRepository = lineRepository;
        this.reportRepository = reportRepository;
        this.productProcessRepository = productProcessRepository;
    }

    public List<MachineResponse> getAll() {
        return machineRepository.findAll().stream().map(this::toResponse).toList();
    }

    public MachineResponse create(MachineUpsertRequest request) {
        Machine machine = new Machine();
        machine.setMachineCode(request.machineCode());
        machine.setDescription(request.description());
        machine.setLine(resolveLine(request.lineCode()));
        machine.setAssetCode(request.assetCode());
        machine.setPurchaseDate(request.purchaseDate());
        machine.setCustodyDepartment(request.custodyDepartment());
        Machine saved = machineRepository.save(machine);
        return toResponse(saved);
    }

    public MachineResponse update(Long id, MachineUpsertRequest request) {
        Machine machine = machineRepository.findById(id).orElseThrow();
        machine.setMachineCode(request.machineCode());
        machine.setDescription(request.description());
        machine.setLine(resolveLine(request.lineCode()));
        machine.setAssetCode(request.assetCode());
        machine.setPurchaseDate(request.purchaseDate());
        machine.setCustodyDepartment(request.custodyDepartment());
        return toResponse(machineRepository.save(machine));
    }

    public void delete(Long id) {
        Machine machine = machineRepository.findById(id).orElseThrow();
        java.util.List<String> blockers = new java.util.ArrayList<>();
        if (reportRepository.existsByMachineCode(machine.getMachineCode())) {
            blockers.add("báo cáo sản xuất");
        }
        if (productProcessRepository.existsByMachineCodeToken(machine.getMachineCode())) {
            blockers.add("công đoạn");
        }
        if (!blockers.isEmpty()) {
            throw new IllegalStateException("Không thể xóa thiết bị " + machine.getMachineCode() + " vì vẫn còn dữ liệu liên quan: " + String.join(", ", blockers) + ". Vui lòng xóa hoặc chuyển dữ liệu liên quan trước.");
        }
        machineRepository.deleteById(id);
    }

    @Transactional
    public void deleteAllMachines() {
        for (Machine machine : machineRepository.findAll()) {
            assertMachineCanBeDeleted(machine.getId());
        }
        machineRepository.deleteAll();
    }
            private void assertMachineCanBeDeleted(Long id) {
        Machine machine = machineRepository.findById(id).orElseThrow();
        java.util.List<String> blockers = new java.util.ArrayList<>();
        if (reportRepository.existsByMachineCode(machine.getMachineCode())) {
            blockers.add("báo cáo sản xuất");
        }
        if (productProcessRepository.existsByMachineCodeToken(machine.getMachineCode())) {
            blockers.add("công đoạn");
        }
        if (!blockers.isEmpty()) {
            throw new IllegalStateException("Không thể xóa thiết bị này" + machine.getMachineCode() + " vì vẫn còn dữ liệu liên quan: " + String.join(", ", blockers) + ". Vui lòng xóa hoặc chuyển dữ liệu liên quan trước.");
        }
    }

    private Line resolveLine(String lineCode) {
        if (lineCode == null || lineCode.isBlank()) {
            return null;
        }
        return lineRepository.findByLineCode(lineCode).orElse(null);
    }

    private MachineResponse toResponse(Machine machine) {
        return new MachineResponse(
                machine.getId(),
                machine.getMachineCode(),
                machine.getDescription(),
                machine.getLine() != null ? machine.getLine().getLineCode() : null,
                machine.getAssetCode(),
                machine.getPurchaseDate() != null ? machine.getPurchaseDate().toString() : null,
                machine.getCustodyDepartment()
        );
    }
}
