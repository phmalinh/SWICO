package com.swico.swico.repository;

import com.swico.swico.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    Optional<Shift> findByShiftName(String shiftName);
}