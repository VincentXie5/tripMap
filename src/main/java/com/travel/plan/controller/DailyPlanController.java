package com.travel.plan.controller;

import com.travel.plan.entity.DailyPlan;
import com.travel.plan.service.DailyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dailyPlan")
public class DailyPlanController {

    @Autowired
    private DailyPlanService dailyPlanService;

    @PostMapping
    public ResponseEntity<DailyPlan> addDailyPlan(@RequestBody DailyPlan dailyPlan) {
        DailyPlan createdPlan = dailyPlanService.createDailyPlan(dailyPlan);
        return ResponseEntity.ok(createdPlan);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<List<DailyPlan>> getDailyPlansByTravelPlanId(@PathVariable Long planId) {
        List<DailyPlan> dailyPlans = dailyPlanService.getAllDailyPlansByTravelPlanId(planId);
        return ResponseEntity.ok(dailyPlans);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DailyPlan> updateDailyPlan(@PathVariable Long id, @RequestBody DailyPlan dailyPlan) {
        DailyPlan updatedPlan = dailyPlanService.updateDailyPlan(id, dailyPlan);
        return ResponseEntity.ok(updatedPlan);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDailyPlan(@PathVariable Long id) {
        dailyPlanService.deleteDailyPlan(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/sort/{planId}")
    public ResponseEntity<List<DailyPlan>> updateSortOrder(@PathVariable Long planId, @RequestBody List<Map<String, Object>> sortOrderList) {
        List<DailyPlan> updatedPlans = dailyPlanService.updateSortOrder(planId, sortOrderList);
        return ResponseEntity.ok(updatedPlans);
    }
}
