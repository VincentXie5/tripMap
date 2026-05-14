package com.travel.plan.repository;

import com.travel.plan.entity.PlanFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlanFavoriteRepository extends JpaRepository<PlanFavorite, Long> {
    List<PlanFavorite> findByUserIdAndPlanIdIn(Long userId, List<Long> planIds);
    Optional<PlanFavorite> findByUserIdAndPlanId(Long userId, Long planId);
    boolean existsByUserIdAndPlanId(Long userId, Long planId);
}
