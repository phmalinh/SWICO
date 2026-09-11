package com.swico.swico.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "daily_production_report_lots")
public class DailyProductionReportLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private DailyProductionReport report;

    @Column(name = "lot_no", length = 100)
    private String lotNo;

    @Column(name = "input_quantity")
    private Integer inputQuantity;

    @Column(name = "good_quantity")
    private Integer goodQuantity;

    @Column(name = "defect_quantity")
    private Integer defectQuantity;

    @Column(name = "internal_defect_quantity")
    private Integer internalDefectQuantity;

    @Column(name = "external_defect_quantity")
    private Integer externalDefectQuantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DailyProductionReport getReport() {
        return report;
    }

    public void setReport(DailyProductionReport report) {
        this.report = report;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
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

    public Integer getInternalDefectQuantity() {
        return internalDefectQuantity;
    }

    public void setInternalDefectQuantity(Integer internalDefectQuantity) {
        this.internalDefectQuantity = internalDefectQuantity;
    }

    public Integer getExternalDefectQuantity() {
        return externalDefectQuantity;
    }

    public void setExternalDefectQuantity(Integer externalDefectQuantity) {
        this.externalDefectQuantity = externalDefectQuantity;
    }
}
