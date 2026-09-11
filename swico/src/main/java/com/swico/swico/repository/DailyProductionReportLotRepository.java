package com.swico.swico.repository;

import com.swico.swico.entity.DailyProductionReportLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyProductionReportLotRepository extends JpaRepository<DailyProductionReportLot, Long> {
    List<DailyProductionReportLot> findByReportIdOrderByIdAsc(Long reportId);

    void deleteByReportId(Long reportId);
}
