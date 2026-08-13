package com.swico.swico.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "downtime_reasons")
public class DowntimeReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reason_code", nullable = false, unique = true, length = 20)
    private String reasonCode;

    @Column(name = "reason_text", nullable = false, length = 1000)
    private String reasonText;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private Boolean active = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
