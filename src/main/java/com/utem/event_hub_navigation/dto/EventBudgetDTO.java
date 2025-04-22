package com.utem.event_hub_navigation.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EventBudgetDTO {
 private BigDecimal amountAllocated;
    private BigDecimal amountSpent;
    private Long budgetCategoryId; // Just the ID
}
