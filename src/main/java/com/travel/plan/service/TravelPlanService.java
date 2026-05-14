package com.travel.plan.service;

import com.travel.plan.controller.dto.PublicPlanCardDTO;
import com.travel.plan.controller.dto.PublicPlanDetailDTO;
import com.travel.plan.entity.TravelPlan;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;

public interface TravelPlanService {
    TravelPlan createTravelPlan(Long userId, TravelPlan travelPlan);
    List<TravelPlan> getAllTravelPlansByUserId(Long userId);
    TravelPlan updateTravelPlan(Long id, TravelPlan travelPlan);
    void deleteTravelPlan(Long id);
    Page<PublicPlanCardDTO> getPublicPlans(String keyword, Integer tag, int page, int size, Long currentUserId);
    PublicPlanDetailDTO getPublicPlanDetail(Long planId, Long currentUserId);
    Page<PublicPlanCardDTO> getPublicPlansByUser(Long userId, int page, int size, Long currentUserId);
    void toggleVisibility(Long planId, Long userId);
    Map<String, Object> toggleLike(Long planId, Long userId);
    Map<String, Object> toggleFavorite(Long planId, Long userId);
}