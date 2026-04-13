package com.travel.plan.controller;

import com.travel.plan.common.ApiResult;
import com.travel.plan.entity.DailyPlan;
import com.travel.plan.service.DailyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dailyPlan")
public class DailyPlanController {

    @Autowired
    private DailyPlanService dailyPlanService;

    @PostMapping
    public ApiResult<DailyPlan> addDailyPlan(@RequestBody DailyPlan dailyPlan) {
        DailyPlan createdPlan = dailyPlanService.createDailyPlan(dailyPlan);
        return ApiResult.success(createdPlan);
    }

    @GetMapping("/{planId}")
    public ApiResult<List<DailyPlan>> getDailyPlansByTravelPlanId(@PathVariable Long planId) {
        List<DailyPlan> dailyPlans = dailyPlanService.getAllDailyPlansByTravelPlanId(planId);
        return ApiResult.success(dailyPlans);
    }

    @PutMapping("/{id}")
    public ApiResult<DailyPlan> updateDailyPlan(@PathVariable Long id, @RequestBody DailyPlan dailyPlan) {
        DailyPlan updatedPlan = dailyPlanService.updateDailyPlan(id, dailyPlan);
        return ApiResult.success(updatedPlan);
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteDailyPlan(@PathVariable Long id) {
        dailyPlanService.deleteDailyPlan(id);
        return ApiResult.success("删除成功", null);
    }

    @PutMapping("/sort/{planId}")
    public ApiResult<List<DailyPlan>> updateSortOrder(@PathVariable Long planId, @RequestBody List<Map<String, Object>> sortOrderList) {
        List<DailyPlan> updatedPlans = dailyPlanService.updateSortOrder(planId, sortOrderList);
        return ApiResult.success(updatedPlans);
    }
}
