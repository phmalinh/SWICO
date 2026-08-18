package com.swico.swico.controller;

import com.swico.swico.dto.MasterDataResponse;
import com.swico.swico.dto.LineUpsertRequest;
import com.swico.swico.dto.ProductionInfoImportResponse;
import com.swico.swico.dto.ProductUpsertRequest;
import com.swico.swico.dto.ShiftUpsertRequest;
import com.swico.swico.dto.UserResponse;
import com.swico.swico.service.MasterDataService;
import com.swico.swico.service.ProductionInfoImportService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data")
@CrossOrigin(origins = "*")
public class MasterDataController {

    private final MasterDataService masterDataService;
    private final ProductionInfoImportService productionInfoImportService;

    public MasterDataController(MasterDataService masterDataService, ProductionInfoImportService productionInfoImportService) {
        this.masterDataService = masterDataService;
        this.productionInfoImportService = productionInfoImportService;
    }

    @GetMapping("/products")
    public List<MasterDataResponse> products() {
        return masterDataService.getProducts();
    }

    @GetMapping("/lines")
    public List<MasterDataResponse> lines() {
        return masterDataService.getLines();
    }

    @GetMapping("/shifts")
    public List<MasterDataResponse> shifts() {
        return masterDataService.getShifts();
    }

    @GetMapping("/leaders")
    public List<UserResponse> leaders() {
        return masterDataService.getLeaders();
    }

    @PostMapping("/products")
    public MasterDataResponse createProduct(@Valid @RequestBody ProductUpsertRequest request) {
        return masterDataService.saveProduct(request);
    }

    @PostMapping(value = "/products/import-production-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importProductionInfo(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(productionInfoImportService.importWorkbook(file));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/products/{id}")
    public MasterDataResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpsertRequest request) {
        return masterDataService.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            masterDataService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @PostMapping("/lines")
    public MasterDataResponse createLine(@Valid @RequestBody LineUpsertRequest request) {
        return masterDataService.saveLine(request);
    }

    @PutMapping("/lines/{id}")
    public MasterDataResponse updateLine(@PathVariable Long id, @Valid @RequestBody LineUpsertRequest request) {
        return masterDataService.updateLine(id, request);
    }

    @DeleteMapping("/lines/{id}")
    public ResponseEntity<?> deleteLine(@PathVariable Long id) {
        try {
            masterDataService.deleteLine(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @PostMapping("/shifts")
    public MasterDataResponse createShift(@Valid @RequestBody ShiftUpsertRequest request) {
        return masterDataService.saveShift(request);
    }

    @PutMapping("/shifts/{id}")
    public MasterDataResponse updateShift(@PathVariable Long id, @Valid @RequestBody ShiftUpsertRequest request) {
        return masterDataService.updateShift(id, request);
    }

    @DeleteMapping("/shifts/{id}")
    public ResponseEntity<?> deleteShift(@PathVariable Long id) {
        try {
            masterDataService.deleteShift(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @GetMapping("/products/{productId}/processes")
    public java.util.List<com.swico.swico.dto.ProcessDto> getProductProcesses(@PathVariable Long productId) {
        return masterDataService.getProcessesByProduct(productId);
    }

    @PostMapping("/products/{productId}/processes")
    public com.swico.swico.dto.ProcessDto addProductProcess(@PathVariable Long productId, @Valid @RequestBody com.swico.swico.dto.ProcessUpsertRequest req) {
        return masterDataService.addProcessToProduct(productId, req);
    }

    @PutMapping("/processes/{id}")
    public com.swico.swico.dto.ProcessDto updateProductProcess(@PathVariable Long id, @Valid @RequestBody com.swico.swico.dto.ProcessUpsertRequest req) {
        return masterDataService.updateProcess(id, req);
    }

    @DeleteMapping("/processes/{id}")
    public ResponseEntity<?> deleteProductProcess(@PathVariable Long id) {
        try {
            masterDataService.deleteProcess(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }
    @DeleteMapping("/processes")
    public ResponseEntity<?> deleteAllProcesses() {
        try {
            masterDataService.deleteAllProcesses();
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @DeleteMapping("/products")
    public ResponseEntity<?> deleteAllProducts() {
        try {
            masterDataService.deleteAllProducts();
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }
        @DeleteMapping("/lines")
    public ResponseEntity<?> deleteAllLines() {
        try {
            masterDataService.deleteAllLines();
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    // @DeleteMapping("/machines")
    // public ResponseEntity<?> deleteAllMachines() {
    //     try {
    //         masterDataService.deleteAllMachines();
    //         return ResponseEntity.noContent().build();
    //     } catch (IllegalStateException ex) {
    //         return ResponseEntity.status(409).body(ex.getMessage());
    //     }
    // }
}
