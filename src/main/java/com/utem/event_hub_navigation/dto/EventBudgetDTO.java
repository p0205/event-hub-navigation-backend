package com.utem.event_hub_navigation.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventBudgetDTO {
    private Double amountAllocated;
    private Double amountSpent;
    private Integer budgetCategoryId; // Just the ID
    private String budgetCategoryName; // Just the name
}
