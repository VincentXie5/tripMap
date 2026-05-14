package com.travel.plan.controller.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PublicPlanDetailDTO {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String creatorNickname;
    private String creatorAvatarUrl;
    private Long creatorUserId;
    private List<DailyPlanDTO> dailyPlans;
    private Integer likeCount;
    private Integer favoriteCount;
    private Boolean isLiked;
    private Boolean isFavorited;

    @Data
    public static class DailyPlanDTO {
        private Long id;
        private String planDate;
        private String time;
        private String location;
        private String remark;
        private Integer tag;
        private Integer sortOrder;
        private java.math.BigDecimal latitude;
        private java.math.BigDecimal longitude;
    }
}
