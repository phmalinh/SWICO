package com.swico.swico.controller;

import com.swico.swico.dto.DowntimeReasonCategoryResponse;
import com.swico.swico.dto.DowntimeReasonCategoryUpsertRequest;
import com.swico.swico.dto.DowntimeReasonImportResponse;
import com.swico.swico.dto.DowntimeReasonResponse;
import com.swico.swico.dto.DowntimeReasonUpsertRequest;
import com.swico.swico.service.DowntimeReasonService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data/downtime-reasons")
@CrossOrigin(origins = "*")
public class DowntimeReasonController {

    private final DowntimeReasonService downtimeReasonService;

    public DowntimeReasonController(DowntimeReasonService downtimeReasonService) {
        this.downtimeReasonService = downtimeReasonService;
    }

    @GetMapping
    public List<DowntimeReasonResponse> getAll() {
        return downtimeReasonService.getAll();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DowntimeReasonImportResponse importWorkbook(@RequestParam("file") MultipartFile file) {
        return downtimeReasonService.importWorkbook(file);
    }

    @GetMapping("/categories")
    public List<DowntimeReasonCategoryResponse> getCategories() {
        return downtimeReasonService.getCategories();
    }

    @PostMapping("/categories")
    public DowntimeReasonCategoryResponse createCategory(@Valid @RequestBody DowntimeReasonCategoryUpsertRequest request) {
        return downtimeReasonService.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    public DowntimeReasonCategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody DowntimeReasonCategoryUpsertRequest request) {
        return downtimeReasonService.updateCategory(id, request);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            downtimeReasonService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @PostMapping
    public DowntimeReasonResponse create(@Valid @RequestBody DowntimeReasonUpsertRequest request) {
        return downtimeReasonService.create(request);
    }

    @PutMapping("/{id}")
    public DowntimeReasonResponse update(@PathVariable Long id, @Valid @RequestBody DowntimeReasonUpsertRequest request) {
        return downtimeReasonService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            downtimeReasonService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }
}
