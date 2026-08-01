package com.swico.swico.repository;

import com.swico.swico.entity.DailyProductionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyProductionReportRepository extends JpaRepository<DailyProductionReport, Long> {
    List<DailyProductionReport> findByReportDateOrderByCreatedAtDesc(LocalDate reportDate);

    List<DailyProductionReport> findByReportDateBetweenOrderByReportDateDescCreatedAtDesc(LocalDate from, LocalDate to);

    List<DailyProductionReport> findByReportDateAndLine_LineCodeOrderByCreatedAtDesc(LocalDate reportDate, String lineCode);
}