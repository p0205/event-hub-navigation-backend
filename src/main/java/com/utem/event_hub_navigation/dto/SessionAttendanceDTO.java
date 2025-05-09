package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionAttendanceDTO {
    private String sessionName;
    private LocalDateTime sessionStartDate;
    private LocalDateTime sessionEndDate;
    private int totalAttendees;
    private double sessionAttendanceRate;

    // Constructor, Getters, Setters
}
