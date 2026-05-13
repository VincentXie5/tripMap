package com.travel.plan.service.impl;

import com.travel.plan.common.BusinessException;
import com.travel.plan.common.code.TravelPlanCode;
import com.travel.plan.controller.dto.PublicPlanCardDTO;
import com.travel.plan.controller.dto.PublicPlanDetailDTO;
import com.travel.plan.entity.DailyPlan;
import com.travel.plan.entity.TravelPlan;
import com.travel.plan.entity.User;
import com.travel.plan.repository.DailyPlanRepository;
import com.travel.plan.repository.TravelPlanRepository;
import com.travel.plan.repository.UserRepository;
import com.travel.plan.service.TravelPlanService;
import com.travel.plan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TravelPlanServiceImpl implements TravelPlanService {

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private DailyPlanRepository dailyPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public TravelPlan createTravelPlan(Long userId, TravelPlan travelPlan) {
        validateDateRange(travelPlan.getStartDate(), travelPlan.getEndDate());
        travelPlan.setUserId(userId);
        return travelPlanRepository.save(travelPlan);
    }

    @Override
    public List<TravelPlan> getAllTravelPlansByUserId(Long userId) {
        return travelPlanRepository.findByUserId(userId);
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
        throw new BusinessException(TravelPlanCode.NOT_FOUND, id);
    }

    @Override
    @Transactional
    public void deleteTravelPlan(Long id) {
        List<DailyPlan> dailyPlans = dailyPlanRepository.findAllByPlanId(id);
        if (!dailyPlans.isEmpty()) {
            dailyPlanRepository.deleteAll(dailyPlans);
        }
        travelPlanRepository.deleteById(id);
    }

    @Override
    public Page<PublicPlanCardDTO> getPublicPlans(String keyword, Integer tag, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<TravelPlan> plans;
        if (keyword != null && !keyword.trim().isEmpty()) {
            plans = travelPlanRepository.searchPublic(keyword.trim(), pageable);
        } else {
            plans = travelPlanRepository.findByIsPublicTrue(pageable);
        }
        return plans.map(plan -> toCardDTO(plan, tag));
    }

    @Override
    public PublicPlanDetailDTO getPublicPlanDetail(Long planId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(TravelPlanCode.NOT_FOUND, planId));
        if (!Boolean.TRUE.equals(plan.getIsPublic())) {
            throw new BusinessException(TravelPlanCode.NOT_PUBLIC);
        }
        return toDetailDTO(plan);
    }

    @Override
    public Page<PublicPlanCardDTO> getPublicPlansByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return travelPlanRepository.findByUserIdAndIsPublicTrue(userId, pageable)
                .map(plan -> toCardDTO(plan, null));
    }

    @Override
    @Transactional
    public void toggleVisibility(Long planId, Long userId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(TravelPlanCode.NOT_FOUND, planId));
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException(TravelPlanCode.NOT_OWNER);
        }
        plan.setIsPublic(!Boolean.TRUE.equals(plan.getIsPublic()));
        travelPlanRepository.save(plan);
    }

    private PublicPlanCardDTO toCardDTO(TravelPlan plan, Integer tagFilter) {
        PublicPlanCardDTO dto = new PublicPlanCardDTO();
        dto.setId(plan.getId());
        dto.setTitle(plan.getTitle());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());

        User creator = userRepository.findById(plan.getUserId()).orElse(null);
        if (creator != null) {
            dto.setCreatorNickname(creator.getNickname());
            dto.setCreatorAvatarUrl(userService.generateAvatarUrl(creator));
            dto.setCreatorUserId(creator.getId());
        }

        List<DailyPlan> dailyPlans = dailyPlanRepository.findAllByPlanIdOrderBySortOrder(plan.getId());
        dto.setRoutePreview(generateRoutePreview(dailyPlans));
        dto.setDominantTag(calculateDominantTag(dailyPlans, tagFilter));
        dto.setDayCount(calculateDayCount(plan));
        dto.setLocationCount(dailyPlans.size());

        return dto;
    }

    private PublicPlanDetailDTO toDetailDTO(TravelPlan plan) {
        PublicPlanDetailDTO dto = new PublicPlanDetailDTO();
        dto.setId(plan.getId());
        dto.setTitle(plan.getTitle());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());

        User creator = userRepository.findById(plan.getUserId()).orElse(null);
        if (creator != null) {
            dto.setCreatorNickname(creator.getNickname());
            dto.setCreatorAvatarUrl(userService.generateAvatarUrl(creator));
            dto.setCreatorUserId(creator.getId());
        }

        List<DailyPlan> dailyPlans = dailyPlanRepository.findAllByPlanIdOrderBySortOrder(plan.getId());
        dto.setDailyPlans(dailyPlans.stream().map(dp -> {
            PublicPlanDetailDTO.DailyPlanDTO dpDto = new PublicPlanDetailDTO.DailyPlanDTO();
            dpDto.setId(dp.getId());
            dpDto.setPlanDate(dp.getPlanDate() != null ? dp.getPlanDate().toString() : null);
            dpDto.setTime(dp.getTime() != null ? dp.getTime().toString() : null);
            dpDto.setLocation(dp.getLocation());
            dpDto.setRemark(dp.getRemark());
            dpDto.setTag(dp.getTag());
            dpDto.setSortOrder(dp.getSortOrder());
            dpDto.setLatitude(dp.getLatitude());
            dpDto.setLongitude(dp.getLongitude());
            return dpDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    String generateRoutePreview(List<DailyPlan> dailyPlans) {
        if (dailyPlans.isEmpty()) {
            return null;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (DailyPlan dp : dailyPlans) {
            if (dp.getLocation() != null && !dp.getLocation().trim().isEmpty()) {
                String shortName = extractShortName(dp.getLocation().trim());
                seen.add(shortName);
            }
            if (seen.size() >= 4) break;
        }
        return seen.isEmpty() ? null : String.join(" → ", seen);
    }

    private String extractShortName(String location) {
        // 取逗号、空格、 "-" 、 "·"  之前的部分作为短名称
        for (char sep : new char[]{'，', ',', ' ', '-', '·', '（', '('}) {
            int idx = location.indexOf(sep);
            if (idx > 0) {
                return location.substring(0, idx);
            }
        }
        return location.length() > 8 ? location.substring(0, 8) + "..." : location;
    }

    Integer calculateDominantTag(List<DailyPlan> dailyPlans, Integer tagFilter) {
        if (dailyPlans.isEmpty()) {
            return 0;
        }
        Map<Integer, Long> tagCounts = dailyPlans.stream()
                .map(dp -> dp.getTag() != null ? dp.getTag() : 0)
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        if (tagFilter != null) {
            // 当有标签筛选时，返回该标签的计数用于排序
            return tagCounts.getOrDefault(tagFilter, 0L).intValue();
        }
        return tagCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
    }

    private Integer calculateDayCount(TravelPlan plan) {
        if (plan.getStartDate() != null && plan.getEndDate() != null) {
            return (int) ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1;
        }
        return 0;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(TravelPlanCode.END_DATE_LARGER_THAN_START_DATE);
        }
    }
}
