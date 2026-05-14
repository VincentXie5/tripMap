package com.travel.plan.repository;

import com.travel.plan.entity.TravelPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    List<TravelPlan> findByUserId(Long userId);

    Page<TravelPlan> findByIsPublicTrue(Pageable pageable);

    Page<TravelPlan> findByUserIdAndIsPublicTrue(Long userId, Pageable pageable);

    @Query("""
        SELECT DISTINCT t FROM TravelPlan t
        LEFT JOIN DailyPlan d ON d.travelPlan.id = t.id
        WHERE t.isPublic = true
        AND (:keyword IS NULL OR t.title LIKE %:keyword% OR d.location LIKE %:keyword% OR d.remark LIKE %:keyword%)
        """)
    Page<TravelPlan> searchPublic(@Param("keyword") String keyword, Pageable pageable);

    List<TravelPlan> findByIdInAndIsPublicTrue(List<Long> ids);
}
