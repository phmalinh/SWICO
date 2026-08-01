package com.swico.swico.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_number", nullable = false, unique = true, length = 100)
    private String partNumber;

    @Column(name = "part_name", nullable = false, length = 255)
    private String partName;

    @Column(name = "cycle_time_seconds", nullable = false, precision = 10, scale = 2)
    private BigDecimal cycleTimeSeconds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public BigDecimal getCycleTimeSeconds() {
        return cycleTimeSeconds;
    }

    public void setCycleTimeSeconds(BigDecimal cycleTimeSeconds) {
        this.cycleTimeSeconds = cycleTimeSeconds;
    }
}
