package com.travel.plan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 地理编码服务
 * 调用Nominatim OpenStreetMap API将地点名称转换为经纬度
 * 遵循Nominatim使用规范：每秒最多1次请求
 */
@Slf4j
@Service
public class GeocodeService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    
    /**
     * 最后一次请求时间，用于限流
     */
    private static long lastRequestTime = 0;
    
    /**
     * 限流间隔：1200毫秒 (略大于官方要求1秒，防止边界冲突)
     */
    private static final long RATE_LIMIT_MS = 1200;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeocodeService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 地点搜索：返回多条匹配的地点建议
     * @param keyword 搜索关键词
     * @return 地点列表，包含名称、地址、经纬度
     */
    public List<Map<String, Object>> searchLocations(String keyword) {
        if (keyword == null || keyword.isBlank() || keyword.length() < 1) {
            return List.of();
        }

        try {
            // 限流控制：保证至少间隔1.2秒
            rateLimit();

            String url = String.format("%s?format=json&q=%s&limit=5&accept-language=zh-CN,zh&addressdetails=1",
                    NOMINATIM_URL,
                    java.net.URLEncoder.encode(keyword.trim(), "UTF-8"));

            log.info("地点搜索请求: {}", keyword);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .header("Accept", "application/json")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Cache-Control", "no-cache")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (response.statusCode() != 200) {
                log.error("地点搜索HTTP错误: 状态码={}, 响应={}", response.statusCode(), responseBody);
                return List.of();
            }

            JsonNode root = objectMapper.readTree(responseBody);

            List<Map<String, Object>> results = new java.util.ArrayList<>();

            if (root.isArray() && root.size() > 0) {
                for (int i = 0; i < root.size(); i++) {
                    JsonNode item = root.get(i);
                    Map<String, Object> location = new java.util.HashMap<>();

                    location.put("name", item.get("display_name").asText());
                    location.put("lat", new BigDecimal(item.get("lat").asText()));
                    location.put("lon", new BigDecimal(item.get("lon").asText()));

                    // 提取行政区划信息
                    JsonNode address = item.get("address");
                    if (address != null) {
                        StringBuilder addressBuilder = new StringBuilder();
                        if (address.has("city")) addressBuilder.append(address.get("city").asText());
                        if (address.has("state")) addressBuilder.append(" ").append(address.get("state").asText());
                        if (address.has("country")) addressBuilder.append(" ").append(address.get("country").asText());
                        location.put("address", addressBuilder.toString());
                    }

                    results.add(location);
                }
            }

            log.info("地点搜索返回 {} 条结果: {}", results.size(), keyword);
            return results;

        } catch (Exception e) {
            log.error("地点搜索失败: {}, 错误: {}", keyword, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 地理编码：将地点名称转换为经纬度
     * @param location 地点名称
     * @return 经纬度数组 [纬度, 经度]，失败返回null
     */
    public BigDecimal[] geocode(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }

        try {
            // 限流控制：保证至少间隔1.2秒
            rateLimit();

            String url = String.format("%s?format=json&q=%s&limit=1&accept-language=zh-CN,zh",
                    NOMINATIM_URL,
                    java.net.URLEncoder.encode(location.trim(), "UTF-8"));

            log.info("地理编码请求: {}", location);
            
            // ✅ 原生HttpClient，100% 可控请求头
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .header("Accept", "application/json")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Cache-Control", "no-cache")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (response.statusCode() != 200) {
                log.error("地理编码HTTP错误: 状态码={}, 响应={}", response.statusCode(), responseBody);
                return null;
            }

            JsonNode root = objectMapper.readTree(responseBody);

                if (root.isArray() && root.size() > 0) {
                JsonNode result = root.get(0);
                BigDecimal lat = new BigDecimal(result.get("lat").asText());
                BigDecimal lon = new BigDecimal(result.get("lon").asText());
                
                log.info("地理编码成功: {} -> 纬度:{}, 经度:{}", location, lat, lon);
                return new BigDecimal[]{lat, lon};
            }

            log.warn("地理编码未找到结果: {}", location);
            return null;

        } catch (Exception e) {
            log.error("地理编码失败: {}, 错误: {}", location, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 限流控制
     * 保证两次请求之间至少间隔1.2秒
     */
    private synchronized void rateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        
        if (elapsed < RATE_LIMIT_MS) {
            long sleepTime = RATE_LIMIT_MS - elapsed;
            log.debug("地理编码限流，等待 {}ms", sleepTime);
            Thread.sleep(sleepTime);
        }
        
        lastRequestTime = System.currentTimeMillis();
    }
}