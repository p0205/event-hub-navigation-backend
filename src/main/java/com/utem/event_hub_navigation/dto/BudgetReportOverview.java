package com.utem.event_hub_navigation.dto;

import com.utem.event_hub_navigation.model.EventReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetReportOverview {
    EventReport budgetReport;
    private double totalBudget;
    private double totalExpenses;
    private double remaining;
}
