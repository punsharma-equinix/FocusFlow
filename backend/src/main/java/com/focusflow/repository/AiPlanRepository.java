package com.focusflow.repository;

import com.focusflow.entity.AiPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiPlanRepository extends JpaRepository<AiPlan, Long> {
    List<AiPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
}
