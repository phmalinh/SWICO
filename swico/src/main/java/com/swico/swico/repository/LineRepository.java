package com.swico.swico.repository;

import com.swico.swico.entity.Line;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LineRepository extends JpaRepository<Line, Long> {
    Optional<Line> findByLineCode(String lineCode);
}