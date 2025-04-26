package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;

import com.utem.event_hub_navigation.model.EventStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventSimpleResponse {
    private Integer id;
    private String name;
    private LocalDateTime startDateTime;
    private EventStatus status;
}
