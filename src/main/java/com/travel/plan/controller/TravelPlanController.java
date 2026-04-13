package com.travel.plan.controller;

import com.travel.plan.common.ApiResult;
import com.travel.plan.entity.TravelPlan;
import com.travel.plan.service.TravelPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travelPlan")
public class TravelPlanController {

    @Autowired
    private TravelPlanService travelPlanService;

    @PostMapping
    public ApiResult<TravelPlan> createTravelPlan(@RequestBody TravelPlan travelPlan) {
        TravelPlan createdPlan = travelPlanService.createTravelPlan(travelPlan);
        return ApiResult.success(createdPlan);
    }

    @GetMapping
    public ApiResult<List<TravelPlan>> getAllTravelPlans() {
        List<TravelPlan> travelPlans = travelPlanService.getAllTravelPlans();
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
