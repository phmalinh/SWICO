package com.swico.swico.repository;

import com.swico.swico.entity.DowntimeReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DowntimeReasonRepository extends JpaRepository<DowntimeReason, Long> {
    Optional<DowntimeReason> findByReasonCode(String reasonCode);
    List<DowntimeReason> findAllByOrderByReasonCategoryCodeAscSortOrderAscReasonCodeAsc();
    boolean existsByReasonCategoryCode(String reasonCategoryCode);
}
