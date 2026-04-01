package com.travel.plan.repository;

import com.travel.plan.entity.DailyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {
    @Query("SELECT dp FROM DailyPlan dp WHERE dp.travelPlan.id = :planId")
    List<DailyPlan> findAllByPlanId(Long planId);
}