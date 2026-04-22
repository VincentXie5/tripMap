package com.travel.plan.service;

import com.travel.plan.entity.TravelPlan;
import java.util.List;

public interface TravelPlanService {
    TravelPlan createTravelPlan(Long userId, TravelPlan travelPlan);
    List<TravelPlan> getAllTravelPlansByUserId(Long userId);
    TravelPlan updateTravelPlan(Long id, TravelPlan travelPlan);
    void deleteTravelPlan(Long id);
}