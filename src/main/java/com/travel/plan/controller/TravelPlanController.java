package com.travel.plan.controller;

import com.travel.plan.common.ApiResult;
import com.travel.plan.config.UserPrincipal;
import com.travel.plan.entity.TravelPlan;
import com.travel.plan.service.TravelPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travelPlan")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @PostMapping
    public ApiResult<TravelPlan> createTravelPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody TravelPlan travelPlan) {
        TravelPlan createdPlan = travelPlanService.createTravelPlan(principal.getUserId(), travelPlan);
        return ApiResult.success(createdPlan);
    }

    @GetMapping
    public ApiResult<List<TravelPlan>> getAllTravelPlans(@AuthenticationPrincipal UserPrincipal principal) {
        List<TravelPlan> travelPlans = travelPlanService.getAllTravelPlansByUserId(principal.getUserId());
        return ApiResult.success(travelPlans);
    }

    @PutMapping("/{id}")
    public ApiResult<TravelPlan> updateTravelPlan(@PathVariable Long id, @RequestBody TravelPlan travelPlan) {
        TravelPlan updatedPlan = travelPlanService.updateTravelPlan(id, travelPlan);
        return ApiResult.success(updatedPlan);
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteTravelPlan(@PathVariable Long id) {
        travelPlanService.deleteTravelPlan(id);
        return ApiResult.success("删除成功", null);
    }
}
