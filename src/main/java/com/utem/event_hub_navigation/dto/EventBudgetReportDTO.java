package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventBudgetReportDTO {
    private String eventName;
    private String eventDescription;
    private String organizerName;
    private LocalDateTime eventStartDateTime;
    private LocalDateTime eventEndDateTime;
    private LocalDateTime reportGenerationDate;
    private int totalParticipants; // Give context of spending

    private double totalBudgetAllocated;
    private double totalBudgetSpent;



    @Builder.Default
    private List<EventBudgetDTO> eventBudgets = new ArrayList<>();
    
}
