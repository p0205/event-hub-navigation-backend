package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;


public interface CalendarEventDTO {
    Integer getEventId();
    String getEventName();
    Integer getSessionId();
    String getSessionName();
    LocalDateTime getStartDateTime();
    LocalDateTime getEndDateTime();
    String getVenueNames(); // Comma-separated string
}