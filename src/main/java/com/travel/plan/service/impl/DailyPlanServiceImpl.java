package com.travel.plan.service.impl;

import com.travel.plan.entity.DailyPlan;
import com.travel.plan.repository.DailyPlanRepository;
import com.travel.plan.service.DailyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyPlanServiceImpl implements DailyPlanService {

    @Autowired
    private DailyPlanRepository dailyPlanRepository;

    @Override
    public DailyPlan createDailyPlan(DailyPlan dailyPlan) {
        return dailyPlanRepository.save(dailyPlan);
    }

    @Override
    public List<DailyPlan> getAllDailyPlansByTravelPlanId(Long planId) {
        return dailyPlanRepository.findAllByPlanId(planId);
    }
}