package com.swico.swico.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "downtime_reasons_category")
public class DowntimeReasonCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reason_category_code", nullable = false, unique = true, length = 20)
    private String reasonCategoryCode;

    @Column(name = "reason_category_text", nullable = false, length = 1000)
    private String reasonCategoryText;

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

    public String getReasonCategoryCode() {
        return reasonCategoryCode;
    }

    public void setReasonCategoryCode(String reasonCategoryCode) {
        this.reasonCategoryCode = reasonCategoryCode;
    }

    public String getReasonCategoryText() {
        return reasonCategoryText;
    }

    public void setReasonCategoryText(String reasonCategoryText) {
        this.reasonCategoryText = reasonCategoryText;
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