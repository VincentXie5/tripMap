package com.travel.plan.service.impl;

import com.travel.plan.entity.DailyPlan;
import com.travel.plan.repository.DailyPlanRepository;
import com.travel.plan.service.DailyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return dailyPlanRepository.findAllByPlanIdOrderBySortOrder(planId);
    }

    @Override
    public DailyPlan updateDailyPlan(Long id, DailyPlan dailyPlan) {
        Optional<DailyPlan> existingPlan = dailyPlanRepository.findById(id);
        if (existingPlan.isPresent()) {
            DailyPlan plan = existingPlan.get();
            plan.setPlanDate(dailyPlan.getPlanDate());
            plan.setTime(dailyPlan.getTime());
            plan.setLocation(dailyPlan.getLocation());
            return dailyPlanRepository.save(plan);
        }
        throw new RuntimeException("DailyPlan not found with id: " + id);
    }

    @Override
    public void deleteDailyPlan(Long id) {
        dailyPlanRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<DailyPlan> updateSortOrder(Long planId, List<Map<String, Object>> sortOrderList) {
        for (Map<String, Object> item : sortOrderList) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer sortOrder = Integer.valueOf(item.get("sortOrder").toString());
            
            Optional<DailyPlan> optionalPlan = dailyPlanRepository.findById(id);
            if (optionalPlan.isPresent()) {
                DailyPlan plan = optionalPlan.get();
                plan.setSortOrder(sortOrder);
                dailyPlanRepository.save(plan);
            }
        }
        return dailyPlanRepository.findAllByPlanIdOrderBySortOrder(planId);
    }
}
