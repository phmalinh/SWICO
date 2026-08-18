package com.swico.swico.controller;

import com.swico.swico.dto.MachineResponse;
import com.swico.swico.dto.MachineUpsertRequest;
import com.swico.swico.service.MachineService;
import com.swico.swico.service.MasterDataService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data/machines")
@CrossOrigin(origins = "*")
public class MachineController {

    private final MachineService machineService;
    private final MasterDataService masterDataService;

    public MachineController(MasterDataService masterDataService, MachineService machineService) {
        this.masterDataService = masterDataService;
        this.machineService = machineService;
    }

    @GetMapping
    public List<MachineResponse> getAll() {
        return machineService.getAll();
    }

    @PostMapping
    public MachineResponse create(@Valid @RequestBody MachineUpsertRequest request) {
        return machineService.create(request);
    }

    @PutMapping("/{id}")
    public MachineResponse update(@PathVariable Long id, @Valid @RequestBody MachineUpsertRequest request) {
        return machineService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            machineService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }
    @DeleteMapping
    public ResponseEntity<?> deleteAllMachines() {
        try {
            machineService.deleteAllMachines();
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }
    
}
