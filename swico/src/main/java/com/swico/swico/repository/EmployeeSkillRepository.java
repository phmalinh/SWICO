package com.swico.swico.repository;

import com.swico.swico.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    void deleteByEmployeeCodeIn(Collection<String> employeeCodes);
    boolean existsByUserId(Long userId);
    boolean existsByProductId(Long productId);
    boolean existsByProductProcessId(Long productProcessId);
}
