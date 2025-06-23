package com.utem.event_hub_navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventTypePerformanceData {
    private String eventType;
    private int eventsHeld;
    private int totalRegistrations;
    private double avgRegPerEvent;
    private double avgFillRate;
    private Double avgAttendanceRate; // Nullable for N/A
}
