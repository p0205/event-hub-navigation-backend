package com.utem.event_hub_navigation.dto;

import java.util.ArrayList;
import java.util.List;

import com.utem.event_hub_navigation.model.Document;
import com.utem.event_hub_navigation.model.EventStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class EventDTO {

    private Integer id;
    private String name;
    private String description;

    private Integer organizerId;
    private String organizerName;


    @Enumerated(EnumType.STRING)
    private EventStatus status;


    private Document supportingDocument; // optional

    private Integer participantsNo;

    @Builder.Default
    private List<SessionDTO> sessions = new ArrayList<>();

    @Builder.Default
    private List<EventBudgetDTO> eventBudgets = new ArrayList<>();
}
