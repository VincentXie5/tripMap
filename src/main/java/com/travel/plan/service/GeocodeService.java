package com.travel.plan.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GeocodeService {
    List<Map<String, Object>> searchLocations(String keyword);

    BigDecimal[] geocode(String location);
}
