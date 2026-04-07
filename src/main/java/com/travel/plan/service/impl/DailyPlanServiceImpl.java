package com.travel.plan.service.impl;

import com.travel.plan.entity.DailyPlan;
import com.travel.plan.repository.DailyPlanRepository;
import com.travel.plan.service.DailyPlanService;
import com.travel.plan.service.GeocodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DailyPlanServiceImpl implements DailyPlanService {

    @Autowired
    private DailyPlanRepository dailyPlanRepository;

    @Autowired
    private GeocodeService geocodeService;

    @Override
    public DailyPlan createDailyPlan(DailyPlan dailyPlan) {
        // 自动地理编码
        autoGeocode(dailyPlan);
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
            
            // 地点发生变化时重新编码
            boolean locationChanged = !plan.getLocation().equals(dailyPlan.getLocation());
            plan.setLocation(dailyPlan.getLocation());
            plan.setRemark(dailyPlan.getRemark());
            plan.setTag(dailyPlan.getTag());

            if (locationChanged) {
                // 清空原有坐标，触发重新编码
                plan.setLatitude(null);
                plan.setLongitude(null);
                autoGeocode(plan);
            }
            
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

    /**
     * 自动地理编码
     * 当地点有值且坐标为空时，自动调用地理编码服务
     * @param dailyPlan 每日行程
     */
    private void autoGeocode(DailyPlan dailyPlan) {
        if (dailyPlan.getLocation() != null 
                && !dailyPlan.getLocation().isBlank()
                && dailyPlan.getLatitude() == null 
                && dailyPlan.getLongitude() == null) {
            
            BigDecimal[] coords = geocodeService.geocode(dailyPlan.getLocation());
            if (coords != null && coords.length == 2) {
                dailyPlan.setLatitude(coords[0]);
                dailyPlan.setLongitude(coords[1]);
            }
        }
    }
}
