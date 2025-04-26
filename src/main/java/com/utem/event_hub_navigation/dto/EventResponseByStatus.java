package com.utem.event_hub_navigation.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventResponseByStatus {

    private List<EventSimpleResponse> pendingEvents;
    private List<EventSimpleResponse> activeEvents;
    private List<EventSimpleResponse> completedEvents;
}
