package com.swico.swico.service;

import com.swico.swico.dto.MachineResponse;
import com.swico.swico.dto.MachineUpsertRequest;
import com.swico.swico.entity.Line;
import com.swico.swico.entity.Machine;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.MachineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final LineRepository lineRepository;

    public MachineService(MachineRepository machineRepository, LineRepository lineRepository) {
        this.machineRepository = machineRepository;
        this.lineRepository = lineRepository;
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
        machineRepository.deleteById(id);
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
