package com.swico.swico.service;

import com.swico.swico.dto.MasterDataResponse;
import com.swico.swico.dto.LineUpsertRequest;
import com.swico.swico.dto.ProductUpsertRequest;
import com.swico.swico.dto.ShiftUpsertRequest;
import com.swico.swico.entity.Line;
import com.swico.swico.entity.Product;
import com.swico.swico.entity.Shift;
import com.swico.swico.entity.ProductProcess;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.ProductRepository;
import com.swico.swico.repository.ShiftRepository;
import com.swico.swico.repository.ProductProcessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MasterDataService {

    private final ProductRepository productRepository;
    private final LineRepository lineRepository;
    private final ShiftRepository shiftRepository;
    private final ProductProcessRepository productProcessRepository;

    public MasterDataService(ProductRepository productRepository, LineRepository lineRepository, ShiftRepository shiftRepository, ProductProcessRepository productProcessRepository) {
        this.productRepository = productRepository;
        this.lineRepository = lineRepository;
        this.shiftRepository = shiftRepository;
        this.productProcessRepository = productProcessRepository;
    }

    public List<MasterDataResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(p -> new MasterDataResponse(
                        p.getId(),
                        p.getPartNumber(),
                        p.getPartName(),
                        p.getCustomer(),
                        p.getCycleTimeSeconds(),
                        null
                ))
                .toList();
    }

    public List<MasterDataResponse> getLines() {
        return lineRepository.findAll().stream()
                .map(l -> new MasterDataResponse(l.getId(), l.getLineCode(), l.getDescription(), null, null, null))
                .toList();
    }

    public List<MasterDataResponse> getShifts() {
        return shiftRepository.findAll().stream()
                .map(s -> new MasterDataResponse(s.getId(), null, s.getShiftName(), null, null, s.getStandardTimeMinutes()))
                .toList();
    }

    public MasterDataResponse saveProduct(ProductUpsertRequest request) {
        Product saved = upsertProduct(
                request.partNumber(),
                request.partName(),
                request.customer(),
                request.cycleTimeSeconds()
        );
        return new MasterDataResponse(
                saved.getId(),
                saved.getPartNumber(),
                saved.getPartName(),
                saved.getCustomer(),
                saved.getCycleTimeSeconds(),
                null
        );
    }

    public MasterDataResponse saveLine(LineUpsertRequest request) {
        Line saved = upsertLine(request.lineCode(), request.description());
        return new MasterDataResponse(saved.getId(), saved.getLineCode(), saved.getDescription(), null, null, null);
    }

    public MasterDataResponse saveShift(ShiftUpsertRequest request) {
        Shift saved = upsertShift(request.shiftName(), request.standardTimeMinutes());
        return new MasterDataResponse(saved.getId(), null, saved.getShiftName(), null, null, saved.getStandardTimeMinutes());
    }

    public MasterDataResponse updateProduct(Long id, ProductUpsertRequest request) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setPartNumber(request.partNumber());
        product.setPartName(request.partName());
        product.setCustomer(request.customer());
        product.setCycleTimeSeconds(request.cycleTimeSeconds());
        Product saved = productRepository.save(product);
        return new MasterDataResponse(
                saved.getId(),
                saved.getPartNumber(),
                saved.getPartName(),
                saved.getCustomer(),
                saved.getCycleTimeSeconds(),
                null
        );
    }

    public java.util.List<com.swico.swico.dto.ProcessDto> getProcessesByProduct(Long productId) {
        return productProcessRepository.findByProductIdOrderBySequence(productId).stream()
                .map(pp -> new com.swico.swico.dto.ProcessDto(pp.getId(), pp.getProduct().getId(), pp.getProcessCode(), pp.getProcess(), pp.getSequence(), pp.getLineCode(), pp.getMachineCode(), pp.getCycleTimeSeconds()))
                .toList();
    }

    public com.swico.swico.dto.ProcessDto addProcessToProduct(Long productId, com.swico.swico.dto.ProcessUpsertRequest req) {
        Product product = productRepository.findById(productId).orElseThrow();
        ProductProcess pp = new ProductProcess();
        pp.setProduct(product);
        pp.setProcessCode(req.processCode());
        pp.setProcess(req.process());
        pp.setSequence(req.sequence());
        pp.setLineCode(req.lineCode());
        pp.setMachineCode(req.machineCode());
        pp.setCycleTimeSeconds(req.cycleTimeSeconds());
        ProductProcess saved = productProcessRepository.save(pp);
        return new com.swico.swico.dto.ProcessDto(saved.getId(), product.getId(), saved.getProcessCode(), saved.getProcess(), saved.getSequence(), saved.getLineCode(), saved.getMachineCode(), saved.getCycleTimeSeconds());
    }

    public com.swico.swico.dto.ProcessDto updateProcess(Long id, com.swico.swico.dto.ProcessUpsertRequest req) {
        ProductProcess pp = productProcessRepository.findById(id).orElseThrow();
        pp.setProcessCode(req.processCode());
        pp.setProcess(req.process());
        pp.setSequence(req.sequence());
        pp.setLineCode(req.lineCode());
        pp.setMachineCode(req.machineCode());
        pp.setCycleTimeSeconds(req.cycleTimeSeconds());
        ProductProcess saved = productProcessRepository.save(pp);
        return new com.swico.swico.dto.ProcessDto(saved.getId(), saved.getProduct().getId(), saved.getProcessCode(), saved.getProcess(), saved.getSequence(), saved.getLineCode(), saved.getMachineCode(), saved.getCycleTimeSeconds());
    }

    public void deleteProcess(Long id) {
        productProcessRepository.deleteById(id);
    }

    public MasterDataResponse updateLine(Long id, LineUpsertRequest request) {
        Line line = lineRepository.findById(id).orElseThrow();
        line.setLineCode(request.lineCode());
        line.setDescription(request.description());
        Line saved = lineRepository.save(line);
        return new MasterDataResponse(saved.getId(), saved.getLineCode(), saved.getDescription(), null, null, null);
    }

    public MasterDataResponse updateShift(Long id, ShiftUpsertRequest request) {
        Shift shift = shiftRepository.findById(id).orElseThrow();
        shift.setShiftName(request.shiftName());
        shift.setStandardTimeMinutes(request.standardTimeMinutes());
        Shift saved = shiftRepository.save(shift);
        return new MasterDataResponse(saved.getId(), null, saved.getShiftName(), null, null, saved.getStandardTimeMinutes());
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (productProcessRepository.existsByProductId(id)) {
            throw new IllegalStateException("Không thể xóa mã hàng này vì vẫn còn dữ liệu công đoạn. Vui lòng xóa công đoạn liên quan trước.");
        }
        productRepository.deleteById(id);
    }

    public void deleteLine(Long id) {
        lineRepository.deleteById(id);
    }

    public void deleteShift(Long id) {
        shiftRepository.deleteById(id);
    }

    public Product upsertProduct(String partNumber, String partName, String customer, BigDecimal cycleTimeSeconds) {
        return productRepository.findByPartNumber(partNumber)
                .map(existing -> {
                    existing.setPartName(partName);
                    existing.setCustomer(customer);
                    existing.setCycleTimeSeconds(cycleTimeSeconds);
                    return productRepository.save(existing);
                })
                .orElseGet(() -> {
                    Product product = new Product();
                    product.setPartNumber(partNumber);
                    product.setPartName(partName);
                    product.setCustomer(customer);
                    product.setCycleTimeSeconds(cycleTimeSeconds);
                    return productRepository.save(product);
                });
    }

    public Line upsertLine(String lineCode, String description) {
        return lineRepository.findByLineCode(lineCode)
                .map(existing -> {
                    existing.setDescription(description);
                    return lineRepository.save(existing);
                })
                .orElseGet(() -> {
                    Line line = new Line();
                    line.setLineCode(lineCode);
                    line.setDescription(description);
                    return lineRepository.save(line);
                });
    }

    public Shift upsertShift(String shiftName, Integer standardTimeMinutes) {
        String normalized = shiftName != null ? shiftName.trim() : null;
        return shiftRepository.findByShiftName(normalized)
                .map(existing -> {
                    existing.setStandardTimeMinutes(standardTimeMinutes);
                    return shiftRepository.save(existing);
                })
                .orElseGet(() -> {
                    Shift shift = new Shift();
                    shift.setShiftName(normalized);
                    shift.setStandardTimeMinutes(standardTimeMinutes);
                    return shiftRepository.save(shift);
                });
    }
}
