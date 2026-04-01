package com.travel.plan.service;

import com.travel.plan.entity.DailyPlan;
import java.util.List;

public interface DailyPlanService {
    DailyPlan createDailyPlan(DailyPlan dailyPlan);
    List<DailyPlan> getAllDailyPlansByTravelPlanId(Long planId);
}