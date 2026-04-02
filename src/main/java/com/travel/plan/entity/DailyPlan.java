package com.travel.plan.entity;

import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
public class DailyPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private TravelPlan travelPlan;
    private LocalDate planDate;
    private LocalTime time;
    private String location;
    
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;
}
