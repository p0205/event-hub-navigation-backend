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
    private String venueName;
    private int venueCapacity;
    private int totalHoursBooked;
    private double timeUtilizationRate;
    private int eventSessions;
    private int totalRegisteredAttendance;
    private double averageRegisteredSeatOccupancy;
    private double overallSpaceUtilizationRate;

}