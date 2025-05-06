package com.utem.event_hub_navigation.service;

import java.util.List;

import com.utem.event_hub_navigation.dto.EventBudgetDTO;

public interface EventBudgetService {

    // Add budget 
    void addBudget(Integer eventId, EventBudgetDTO eventBudgetDTO) throws Exception;

    // Delete event budget
    void deleteEventBudget(Integer eventId, Integer budgetId);

    // Get all budgets by event id
    List<EventBudgetDTO> getAllBudgetsByEventId(Integer eventId);

}