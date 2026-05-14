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
import java.util.Map;

@RestController
@RequestMapping("/api/travelPlan")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @GetMapping("/favorites")
    public ApiResult<Page<PublicPlanCardDTO>> getFavoritePlans(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword) {
        Page<PublicPlanCardDTO> plans = travelPlanService.getFavoritePlans(principal.getUserId(), keyword, page, size);
        return ApiResult.success(plans);
    }

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
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer tag) {
        Long currentUserId = principal != null ? principal.getUserId() : null;
        Page<PublicPlanCardDTO> plans = travelPlanService.getPublicPlans(keyword, tag, page, size, currentUserId);
        return ApiResult.success(plans);
    }

    @GetMapping("/public/{id}")
    public ApiResult<PublicPlanDetailDTO> getPublicPlanDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Long currentUserId = principal != null ? principal.getUserId() : null;
        PublicPlanDetailDTO detail = travelPlanService.getPublicPlanDetail(id, currentUserId);
        return ApiResult.success(detail);
    }

    @GetMapping("/public/user/{userId}")
    public ApiResult<Page<PublicPlanCardDTO>> getPublicPlansByUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Long currentUserId = principal != null ? principal.getUserId() : null;
        Page<PublicPlanCardDTO> plans = travelPlanService.getPublicPlansByUser(userId, page, size, currentUserId);
        return ApiResult.success(plans);
    }

    @PutMapping("/{id}/visibility")
    public ApiResult<Void> toggleVisibility(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        travelPlanService.toggleVisibility(id, principal.getUserId());
        return ApiResult.success(null);
    }

    @PostMapping("/{id}/like")
    public ApiResult<Map<String, Object>> toggleLike(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Map<String, Object> result = travelPlanService.toggleLike(id, principal.getUserId());
        return ApiResult.success(result);
    }

    @PostMapping("/{id}/favorite")
    public ApiResult<Map<String, Object>> toggleFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Map<String, Object> result = travelPlanService.toggleFavorite(id, principal.getUserId());
        return ApiResult.success(result);
    }
}
