package com.swico.swico.repository;

import com.swico.swico.entity.ProductProcess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductProcessRepository extends JpaRepository<ProductProcess, Long> {
    List<ProductProcess> findByProductIdOrderBySequence(Long productId);
}
