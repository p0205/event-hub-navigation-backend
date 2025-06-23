package com.utem.event_hub_navigation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ParticipantEventOverviewResponse {

    Integer getId();

    String getEventName();

    LocalDateTime getStartDateTime();

    LocalDateTime getEndDateTime();

    LocalDate getRegisterDate();
}
