package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EventVenueDTO {

    private String sessionName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer venueId; // Just the ID
}
