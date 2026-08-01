package com.swico.swico.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_name", nullable = false, length = 100)
    private String shiftName;

    @Column(name = "standard_time_minutes", nullable = false)
    private Integer standardTimeMinutes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public Integer getStandardTimeMinutes() {
        return standardTimeMinutes;
    }

    public void setStandardTimeMinutes(Integer standardTimeMinutes) {
        this.standardTimeMinutes = standardTimeMinutes;
    }
}
