package com.travel.plan.controller;

import com.travel.plan.common.ApiResult;
import com.travel.plan.config.UserPrincipal;
import com.travel.plan.controller.dto.PublicPlanCardDTO;
import com.travel.plan.controller.dto.PublicPlanDetailDTO;
import com.travel.plan.entity.TravelPlan;
import com.travel.plan.service.TravelPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping("/public")
    public ApiResult<Page<PublicPlanCardDTO>> getPublicPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer tag) {
        Page<PublicPlanCardDTO> plans = travelPlanService.getPublicPlans(keyword, tag, page, size);
        return ApiResult.success(plans);
    }

    @GetMapping("/public/{id}")
    public ApiResult<PublicPlanDetailDTO> getPublicPlanDetail(@PathVariable Long id) {
        PublicPlanDetailDTO detail = travelPlanService.getPublicPlanDetail(id);
        return ApiResult.success(detail);
    }

    @GetMapping("/public/user/{userId}")
    public ApiResult<Page<PublicPlanCardDTO>> getPublicPlansByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<PublicPlanCardDTO> plans = travelPlanService.getPublicPlansByUser(userId, page, size);
        return ApiResult.success(plans);
    }

    @PutMapping("/{id}/visibility")
    public ApiResult<Void> toggleVisibility(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        travelPlanService.toggleVisibility(id, principal.getUserId());
        return ApiResult.success(null);
    }
}
