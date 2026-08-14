package com.swico.swico.repository;

import com.swico.swico.entity.ProductProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductProcessRepository extends JpaRepository<ProductProcess, Long> {
    List<ProductProcess> findByProductIdOrderBySequence(Long productId);
    Optional<ProductProcess> findByProductIdAndProcessCode(Long productId, String processCode);
    boolean existsByProductId(Long productId);

    @Query("""
            select count(pp) > 0
            from ProductProcess pp
            where pp.lineCode = :lineCode
               or pp.lineCode like concat(:lineCode, ';%')
               or pp.lineCode like concat('%;', :lineCode)
               or pp.lineCode like concat('%;', :lineCode, ';%')
            """)
    boolean existsByLineCodeToken(@Param("lineCode") String lineCode);

    @Query("""
            select count(pp) > 0
            from ProductProcess pp
            where pp.machineCode = :machineCode
               or pp.machineCode like concat(:machineCode, ';%')
               or pp.machineCode like concat('%;', :machineCode)
               or pp.machineCode like concat('%;', :machineCode, ';%')
            """)
    boolean existsByMachineCodeToken(@Param("machineCode") String machineCode);
}
