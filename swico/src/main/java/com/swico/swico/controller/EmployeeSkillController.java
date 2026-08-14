package com.swico.swico.controller;

import com.swico.swico.dto.EmployeeSkillImportResponse;
import com.swico.swico.dto.EmployeeSkillResponse;
import com.swico.swico.dto.EmployeeSkillUpsertRequest;
import com.swico.swico.dto.EmployeeSkillUserOption;
import com.swico.swico.service.EmployeeSkillService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data/employee-skills")
public class EmployeeSkillController {

    private final EmployeeSkillService service;

    public EmployeeSkillController(EmployeeSkillService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmployeeSkillResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/users")
    public List<EmployeeSkillUserOption> getUserOptions() {
        return service.getUserOptions();
    }

    @PostMapping
    public EmployeeSkillResponse create(@Valid @RequestBody EmployeeSkillUpsertRequest request) {
        return service.create(request);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmployeeSkillImportResponse importExcel(@RequestParam("file") MultipartFile file) {
        return service.importMatrix(file);
    }

    @PutMapping("/{id}")
    public EmployeeSkillResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeSkillUpsertRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @DeleteMapping
    public void deleteAll() {
        service.deleteAll();
    }
}
