package com.travel.plan.service.impl;

import com.travel.plan.entity.TravelPlan;
import com.travel.plan.repository.TravelPlanRepository;
import com.travel.plan.service.TravelPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelPlanServiceImpl implements TravelPlanService {

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Override
    public TravelPlan createTravelPlan(TravelPlan travelPlan) {
        return travelPlanRepository.save(travelPlan);
    }

    @Override
    public List<TravelPlan> getAllTravelPlans() {
        return travelPlanRepository.findAll();
    }
}