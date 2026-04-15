package com.travel.plan.controller;

import com.travel.plan.common.ApiResult;
import com.travel.plan.service.GeocodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 地理编码接口控制器
 * 提供地点搜索联想功能
 */
@Slf4j
@RestController
@RequestMapping("/api/geocode")
public class GeocodeController {
    @Autowired
    private GeocodeService geocodeService;

    /**
     * 地点搜索联想接口
     * @param keyword 搜索关键词
     * @return 地点建议列表，最多5条
     */
    @GetMapping("/search")
    public ApiResult<List<Map<String, Object>>> searchLocations(@RequestParam String keyword) {
        log.info("地点搜索请求: {}", keyword);
        
        List<Map<String, Object>> results = geocodeService.searchLocations(keyword);
        
        return ApiResult.success(results);
    }
}
