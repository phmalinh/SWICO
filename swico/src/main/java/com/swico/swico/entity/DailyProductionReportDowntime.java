package com.swico.swico.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "daily_production_report_downtimes")
public class DailyProductionReportDowntime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private DailyProductionReport report;

    @Column(name = "reason_category_code", length = 20)
    private String reasonCategoryCode;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "minutes")
    private Integer minutes;

    @Column(name = "lot_no", length = 100)
    private String lotNo;

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

    public String getReasonCategoryCode() {
        return reasonCategoryCode;
    }

    public void setReasonCategoryCode(String reasonCategoryCode) {
        this.reasonCategoryCode = reasonCategoryCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }
}
