package com.travel.plan.repository;

import com.travel.plan.entity.PlanLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlanLikeRepository extends JpaRepository<PlanLike, Long> {
    List<PlanLike> findByUserIdAndPlanIdIn(Long userId, List<Long> planIds);
    Optional<PlanLike> findByUserIdAndPlanId(Long userId, Long planId);
    boolean existsByUserIdAndPlanId(Long userId, Long planId);
}
