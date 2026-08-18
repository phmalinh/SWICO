package com.swico.swico.repository;

import com.swico.swico.entity.DowntimeReasonCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DowntimeReasonCategoryRepository extends JpaRepository<DowntimeReasonCategory, Long> {
    Optional<DowntimeReasonCategory> findByReasonCategoryCode(String reasonCategoryCode);
    List<DowntimeReasonCategory> findAllByOrderBySortOrderAscReasonCategoryCodeAsc();
}
