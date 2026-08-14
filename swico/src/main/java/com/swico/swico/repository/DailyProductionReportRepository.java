package com.swico.swico.repository;

import com.swico.swico.entity.DailyProductionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyProductionReportRepository extends JpaRepository<DailyProductionReport, Long> {
    boolean existsByLineId(Long lineId);
    boolean existsByShiftId(Long shiftId);
    boolean existsByProductId(Long productId);
    boolean existsByMachineCode(String machineCode);
    boolean existsByCreatedBy(String createdBy);
    boolean existsByDowntimeReasonContaining(String value);

    @Query("""
            select count(r) > 0
            from DailyProductionReport r
            where r.processIds = :processId
               or r.processIds like concat(:processId, ',%')
               or r.processIds like concat('%,', :processId)
               or r.processIds like concat('%,', :processId, ',%')
            """)
    boolean existsByProcessIdToken(@Param("processId") String processId);

    List<DailyProductionReport> findByReportDateOrderByCreatedAtDesc(LocalDate reportDate);

    List<DailyProductionReport> findByReportDateBetweenOrderByReportDateDescCreatedAtDesc(LocalDate from, LocalDate to);

    List<DailyProductionReport> findByReportDateAndLine_LineCodeOrderByCreatedAtDesc(LocalDate reportDate, String lineCode);

    List<DailyProductionReport> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    List<DailyProductionReport> findByCreatedByAndLine_LineCodeOrderByCreatedAtDesc(String createdBy, String lineCode);

    List<DailyProductionReport> findByCreatedByAndReportDateOrderByCreatedAtDesc(String createdBy, LocalDate reportDate);

    List<DailyProductionReport> findByCreatedByAndLine_LineCodeAndReportDateOrderByCreatedAtDesc(String createdBy, String lineCode, LocalDate reportDate);

    List<DailyProductionReport> findByCreatedByAndReportDateBetweenOrderByReportDateDescCreatedAtDesc(String createdBy, LocalDate from, LocalDate to);
}
