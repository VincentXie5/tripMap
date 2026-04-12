package com.travel.plan.service.impl;

import com.travel.plan.entity.DailyPlan;
import com.travel.plan.entity.TravelPlan;
import com.travel.plan.repository.DailyPlanRepository;
import com.travel.plan.repository.TravelPlanRepository;
import com.travel.plan.service.TravelPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TravelPlanServiceImpl implements TravelPlanService {

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private DailyPlanRepository dailyPlanRepository;

    @Override
    public TravelPlan createTravelPlan(TravelPlan travelPlan) {
        validateDateRange(travelPlan.getStartDate(), travelPlan.getEndDate());
        return travelPlanRepository.save(travelPlan);
    }

    @Override
    public List<TravelPlan> getAllTravelPlans() {
        return travelPlanRepository.findAll();
    }

    @Override
    public TravelPlan updateTravelPlan(Long id, TravelPlan travelPlan) {
        Optional<TravelPlan> existingPlan = travelPlanRepository.findById(id);
        if (existingPlan.isPresent()) {
            validateDateRange(travelPlan.getStartDate(), travelPlan.getEndDate());
            TravelPlan plan = existingPlan.get();
            plan.setTitle(travelPlan.getTitle());
            plan.setStartDate(travelPlan.getStartDate());
            plan.setEndDate(travelPlan.getEndDate());
            return travelPlanRepository.save(plan);
        }
        throw new RuntimeException("TravelPlan not found with id: " + id);
    }

    @Override
    @Transactional
    public void deleteTravelPlan(Long id) {
        // 先删除关联的所有每日行程
        List<DailyPlan> dailyPlans = dailyPlanRepository.findAllByPlanId(id);
        if (!dailyPlans.isEmpty()) {
            dailyPlanRepository.deleteAll(dailyPlans);
        }
        // 再删除旅行计划
        travelPlanRepository.deleteById(id);
    }

    /**
     * 校验日期范围：结束日期必须大于等于开始日期
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期必须大于等于开始日期");
        }
    }
}
