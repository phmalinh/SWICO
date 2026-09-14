package com.swico.swico.repository;

import com.swico.swico.entity.DailyProductionReportDowntime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyProductionReportDowntimeRepository extends JpaRepository<DailyProductionReportDowntime, Long> {
    List<DailyProductionReportDowntime> findByReportIdOrderByIdAsc(Long reportId);

    void deleteByReportId(Long reportId);
}
