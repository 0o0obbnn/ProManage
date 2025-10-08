package com.promanage.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProjectRequest {
    private String name;
    private String code;
    private String description;
    private Long ownerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String icon;
    private String color;
    private String type;
    private Integer priority;
}
