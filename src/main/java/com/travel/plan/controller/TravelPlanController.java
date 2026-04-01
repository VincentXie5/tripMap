package com.travel.plan.controller;

import com.travel.plan.entity.TravelPlan;
import com.travel.plan.service.TravelPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travelPlan")
public class TravelPlanController {

    @Autowired
    private TravelPlanService travelPlanService;

    @PostMapping
    public ResponseEntity<TravelPlan> createTravelPlan(@RequestBody TravelPlan travelPlan) {
        TravelPlan createdPlan = travelPlanService.createTravelPlan(travelPlan);
        return ResponseEntity.ok(createdPlan);
    }

    @GetMapping
    public ResponseEntity<List<TravelPlan>> getAllTravelPlans() {
        List<TravelPlan> travelPlans = travelPlanService.getAllTravelPlans();
        return ResponseEntity.ok(travelPlans);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelPlan> updateTravelPlan(@PathVariable Long id, @RequestBody TravelPlan travelPlan) {
        TravelPlan updatedPlan = travelPlanService.updateTravelPlan(id, travelPlan);
        return ResponseEntity.ok(updatedPlan);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTravelPlan(@PathVariable Long id) {
        travelPlanService.deleteTravelPlan(id);
        return ResponseEntity.ok().build();
    }
}
