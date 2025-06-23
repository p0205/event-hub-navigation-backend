package com.utem.event_hub_navigation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class VenueUtilizationData {
    private Integer venueId;
    private String venueName;
    private Integer venueCapacity;
    private Long totalHoursBooked;
    private double timeUtilizationRate;
    private Long eventSessions;
    private Long totalRegisteredAttendance;
    private double averageRegisteredSeatOccupancy;
    private double overallSpaceUtilizationRate;
}