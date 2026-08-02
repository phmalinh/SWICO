package com.swico.swico.repository;

import com.swico.swico.entity.DailyProductionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyProductionReportRepository extends JpaRepository<DailyProductionReport, Long> {
    List<DailyProductionReport> findByReportDateOrderByCreatedAtDesc(LocalDate reportDate);

    List<DailyProductionReport> findByReportDateBetweenOrderByReportDateDescCreatedAtDesc(LocalDate from, LocalDate to);

    List<DailyProductionReport> findByReportDateAndLine_LineCodeOrderByCreatedAtDesc(LocalDate reportDate, String lineCode);

    List<DailyProductionReport> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    List<DailyProductionReport> findByCreatedByAndLine_LineCodeOrderByCreatedAtDesc(String createdBy, String lineCode);

    List<DailyProductionReport> findByCreatedByAndReportDateOrderByCreatedAtDesc(String createdBy, LocalDate reportDate);

    List<DailyProductionReport> findByCreatedByAndLine_LineCodeAndReportDateOrderByCreatedAtDesc(String createdBy, String lineCode, LocalDate reportDate);

    List<DailyProductionReport> findByCreatedByAndReportDateBetweenOrderByReportDateDescCreatedAtDesc(String createdBy, LocalDate from, LocalDate to);
}