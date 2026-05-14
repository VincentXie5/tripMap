package com.travel.plan.controller.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PublicPlanCardDTO {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String creatorNickname;
    private String creatorAvatarUrl;
    private Long creatorUserId;
    private String routePreview;
    private Integer dominantTag;
    private Integer dayCount;
    private Integer locationCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Boolean isLiked;
    private Boolean isFavorited;
    private String favoritedAt;
}
