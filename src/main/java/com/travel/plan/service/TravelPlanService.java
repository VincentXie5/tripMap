package com.travel.plan.service;

import com.travel.plan.controller.dto.PublicPlanCardDTO;
import com.travel.plan.controller.dto.PublicPlanDetailDTO;
import com.travel.plan.entity.TravelPlan;
import org.springframework.data.domain.Page;
import java.util.List;

public interface TravelPlanService {
    TravelPlan createTravelPlan(Long userId, TravelPlan travelPlan);
    List<TravelPlan> getAllTravelPlansByUserId(Long userId);
    TravelPlan updateTravelPlan(Long id, TravelPlan travelPlan);
    void deleteTravelPlan(Long id);
    Page<PublicPlanCardDTO> getPublicPlans(String keyword, Integer tag, int page, int size);
    PublicPlanDetailDTO getPublicPlanDetail(Long planId);
    Page<PublicPlanCardDTO> getPublicPlansByUser(Long userId, int page, int size);
    void toggleVisibility(Long planId, Long userId);
}