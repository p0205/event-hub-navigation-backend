package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private String qrCodePath; // optional

    private Document supportingDocument; // optional

    private Integer participantsNo;

    @Builder.Default
    private List<EventVenueDTO> eventVenues = new ArrayList<>();

    @Builder.Default
    private List<EventBudgetDTO> eventBudgets = new ArrayList<>();
}
