package com.utem.event_hub_navigation.dto;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String,Map<String,Long>> demographicData = new HashMap<>();

    // Getters and Setters
    
    public void addSessionAttendance(String sessionName, LocalDateTime sessionStartDate, LocalDateTime sessionEndDate, int totalAttendees, double attendanceRate) {
        sessionAttendances.add(new SessionAttendanceDTO(sessionName, sessionStartDate, sessionEndDate, totalAttendees, attendanceRate));
    }
    
    
}


