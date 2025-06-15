package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class VenueUtilizationReportRequest {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private List<Integer> venueIds;
} 