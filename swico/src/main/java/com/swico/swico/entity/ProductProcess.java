package com.swico.swico.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_process")
public class ProductProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "process", length = 150, nullable = false)
    private String process;

    @Column(name = "sequence_order")
    private Integer sequence;

    @Column(name = "line_code", length = 50)
    private String lineCode;

    @Column(name = "machine_code", length = 50)
    private String machineCode;

    @Column(name = "cycle_time_seconds", precision = 10, scale = 2)
    private BigDecimal cycleTimeSeconds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    public String getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(String machineCode) {
        this.machineCode = machineCode;
    }

    public BigDecimal getCycleTimeSeconds() {
        return cycleTimeSeconds;
    }

    public void setCycleTimeSeconds(BigDecimal cycleTimeSeconds) {
        this.cycleTimeSeconds = cycleTimeSeconds;
    }
}
