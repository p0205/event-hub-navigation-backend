package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipantEventOverviewResponse {

    private Integer id;
    private String eventName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
