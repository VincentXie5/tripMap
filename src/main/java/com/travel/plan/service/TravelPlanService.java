package com.travel.plan.service;

import com.travel.plan.entity.TravelPlan;
import java.util.List;

public interface TravelPlanService {
    TravelPlan createTravelPlan(TravelPlan travelPlan);
    List<TravelPlan> getAllTravelPlans();
    TravelPlan updateTravelPlan(Long id, TravelPlan travelPlan);
    void deleteTravelPlan(Long id);
}