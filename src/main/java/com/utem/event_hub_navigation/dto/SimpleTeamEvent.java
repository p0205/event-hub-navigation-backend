package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;


import com.utem.event_hub_navigation.model.EventStatus;


public interface SimpleTeamEvent {


    Integer getId();

    String getName();

    LocalDateTime getStartDateTime();

    EventStatus getStatus();

    String getRoles();
}
