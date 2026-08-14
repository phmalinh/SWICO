package com.swico.swico.controller;

import com.swico.swico.dto.DowntimeReasonResponse;
import com.swico.swico.dto.DowntimeReasonUpsertRequest;
import com.swico.swico.service.DowntimeReasonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
