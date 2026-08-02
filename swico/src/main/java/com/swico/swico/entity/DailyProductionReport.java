package com.swico.swico.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_production_reports")
public class DailyProductionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private Line line;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @Column(name = "machine_code", length = 50)
    private String machineCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "total_operating_minutes", nullable = false)
    private Integer totalOperatingMinutes;

    @Column(name = "downtime_minutes")
    private Integer downtimeMinutes;

    @Column(name = "input_quantity")
    private Integer inputQuantity;

    @Column(name = "good_quantity")
    private Integer goodQuantity;

    @Column(name = "defect_quantity")
    private Integer defectQuantity;

    @Column(name = "company", length = 100)
    private String company;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "downtime_reason", length = 200)
    private String downtimeReason;

    @Column(name = "target_quantity", precision = 10, scale = 2)
    private BigDecimal targetQuantity;

    @Column(name = "production_efficiency", precision = 5, scale = 4)
    private BigDecimal productionEfficiency;

    @Column(name = "availability_rate", precision = 5, scale = 4)
    private BigDecimal availabilityRate;

    @Column(name = "performance_rate", precision = 5, scale = 4)
    private BigDecimal performanceRate;

    @Column(name = "quality_rate", precision = 5, scale = 4)
    private BigDecimal qualityRate;

    @Column(name = "oee", precision = 5, scale = 4)
    private BigDecimal oee;

    @Column(name = "evaluation_label", length = 100)
    private String evaluationLabel;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public String getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(String machineCode) {
        this.machineCode = machineCode;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getTotalOperatingMinutes() {
        return totalOperatingMinutes;
    }

    public void setTotalOperatingMinutes(Integer totalOperatingMinutes) {
        this.totalOperatingMinutes = totalOperatingMinutes;
    }

    public Integer getDowntimeMinutes() {
        return downtimeMinutes;
    }

    public void setDowntimeMinutes(Integer downtimeMinutes) {
        this.downtimeMinutes = downtimeMinutes;
    }

    public Integer getInputQuantity() {
        return inputQuantity;
    }

    public void setInputQuantity(Integer inputQuantity) {
        this.inputQuantity = inputQuantity;
    }

    public Integer getGoodQuantity() {
        return goodQuantity;
    }

    public void setGoodQuantity(Integer goodQuantity) {
        this.goodQuantity = goodQuantity;
    }

    public Integer getDefectQuantity() {
        return defectQuantity;
    }

    public void setDefectQuantity(Integer defectQuantity) {
        this.defectQuantity = defectQuantity;
    }

    public BigDecimal getTargetQuantity() {
        return targetQuantity;
    }

    public void setTargetQuantity(BigDecimal targetQuantity) {
        this.targetQuantity = targetQuantity;
    }

    public BigDecimal getProductionEfficiency() {
        return productionEfficiency;
    }

    public void setProductionEfficiency(BigDecimal productionEfficiency) {
        this.productionEfficiency = productionEfficiency;
    }

    public BigDecimal getAvailabilityRate() {
        return availabilityRate;
    }

    public void setAvailabilityRate(BigDecimal availabilityRate) {
        this.availabilityRate = availabilityRate;
    }

    public BigDecimal getPerformanceRate() {
        return performanceRate;
    }

    public void setPerformanceRate(BigDecimal performanceRate) {
        this.performanceRate = performanceRate;
    }

    public BigDecimal getQualityRate() {
        return qualityRate;
    }

    public void setQualityRate(BigDecimal qualityRate) {
        this.qualityRate = qualityRate;
    }

    public BigDecimal getOee() {
        return oee;
    }

    public void setOee(BigDecimal oee) {
        this.oee = oee;
    }

    public String getEvaluationLabel() {
        return evaluationLabel;
    }

    public void setEvaluationLabel(String evaluationLabel) {
        this.evaluationLabel = evaluationLabel;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDowntimeReason() {
        return downtimeReason;
    }

    public void setDowntimeReason(String downtimeReason) {
        this.downtimeReason = downtimeReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
