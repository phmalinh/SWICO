package com.swico.swico.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private Line line;

    @Column(name = "machine_code", nullable = false, unique = true, length = 50)
    private String machineCode;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "asset_code", length = 100)
    private String assetCode;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "custody_department", length = 255)
    private String custodyDepartment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public String getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(String machineCode) {
        this.machineCode = machineCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getCustodyDepartment() {
        return custodyDepartment;
    }

    public void setCustodyDepartment(String custodyDepartment) {
        this.custodyDepartment = custodyDepartment;
    }
}
