package com.swico.swico.service;

import com.swico.swico.dto.MachineResponse;
import com.swico.swico.dto.MachineUpsertRequest;
import com.swico.swico.entity.Machine;
import com.swico.swico.repository.MachineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineService {

    private final MachineRepository machineRepository;

    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    public List<MachineResponse> getAll() {
        return machineRepository.findAll().stream().map(this::toResponse).toList();
    }

    public MachineResponse create(MachineUpsertRequest request) {
        Machine machine = new Machine();
        machine.setMachineCode(request.machineCode());
        machine.setDescription(request.description());
        Machine saved = machineRepository.save(machine);
        return toResponse(saved);
    }

    public MachineResponse update(Long id, MachineUpsertRequest request) {
        Machine machine = machineRepository.findById(id).orElseThrow();
        machine.setMachineCode(request.machineCode());
        machine.setDescription(request.description());
        return toResponse(machineRepository.save(machine));
    }

    public void delete(Long id) {
        machineRepository.deleteById(id);
    }

    private MachineResponse toResponse(Machine machine) {
        return new MachineResponse(
                machine.getId(),
                machine.getMachineCode(),
                machine.getDescription()
        );
    }
}
