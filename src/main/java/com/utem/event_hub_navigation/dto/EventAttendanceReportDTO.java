package com.utem.event_hub_navigation.dto;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendanceReportDTO {
    
    private String eventName;
    private String organizerName;
    private LocalDateTime eventStartDateTime;
    private LocalDateTime eventEndDateTime;
    private LocalDateTime reportGenerationDate;
    private int totalExpectedParticipants;
    private int totalRegisteredParticipants;
    private double registrationFillRate;

    @Builder.Default
    private List<SessionAttendanceDTO> sessionAttendances = new ArrayList<>();
    @Builder.Default
    private List<DemographicDataDTO> demographicData = new ArrayList<>();

    // Getters and Setters
    
    public void addSessionAttendance(String sessionName, LocalDateTime sessionStartDate, LocalDateTime sessionEndDate, int totalAttendees, double attendanceRate) {
        sessionAttendances.add(new SessionAttendanceDTO(sessionName, sessionStartDate, sessionEndDate, totalAttendees, attendanceRate));
    }
    
    // Add other getters and setters here
    
}


