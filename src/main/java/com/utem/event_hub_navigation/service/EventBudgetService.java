package com.utem.event_hub_navigation.service;

import java.util.List;
import java.util.Map;

import com.utem.event_hub_navigation.dto.AddEventExpenseDTO;
import com.utem.event_hub_navigation.dto.EventBudgetDTO;

public interface EventBudgetService {

    // Add budget
    void addBudget(Integer eventId, EventBudgetDTO eventBudgetDTO) throws Exception;

     void addNewExpense(Integer eventId, AddEventExpenseDTO eventBudgetDTO) throws Exception ;

    // Delete event budget
    void deleteEventBudget(Integer eventId, Integer budgetId);

    // Get all budgets by event id
    List<EventBudgetDTO> getAllBudgetsByEventId(Integer eventId);

    Map<String, Long> findTotalBudgetAndExpenseByEventId(Integer eventId);

}