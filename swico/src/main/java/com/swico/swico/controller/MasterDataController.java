package com.swico.swico.controller;

import com.swico.swico.dto.MasterDataResponse;
import com.swico.swico.dto.LineUpsertRequest;
import com.swico.swico.dto.ProductUpsertRequest;
import com.swico.swico.dto.ShiftUpsertRequest;
import com.swico.swico.service.MasterDataService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data")
@CrossOrigin(origins = "*")
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
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

    @PostMapping("/products")
    public MasterDataResponse createProduct(@Valid @RequestBody ProductUpsertRequest request) {
        return masterDataService.saveProduct(request);
    }

    @PutMapping("/products/{id}")
    public MasterDataResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpsertRequest request) {
        return masterDataService.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable Long id) {
        masterDataService.deleteProduct(id);
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
    public void deleteLine(@PathVariable Long id) {
        masterDataService.deleteLine(id);
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
    public void deleteShift(@PathVariable Long id) {
        masterDataService.deleteShift(id);
    }
}
