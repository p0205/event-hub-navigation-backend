package com.utem.event_hub_navigation.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.dto.AddEventExpenseDTO;
import com.utem.event_hub_navigation.dto.EventBudgetDTO;
import com.utem.event_hub_navigation.model.BudgetCategory;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventBudget;
import com.utem.event_hub_navigation.model.EventBudgetKey;
import com.utem.event_hub_navigation.repo.EventBudgetRepo;
import com.utem.event_hub_navigation.service.BudgetCategoryService;
import com.utem.event_hub_navigation.service.EventBudgetService;
import com.utem.event_hub_navigation.service.EventService;

@Service
public class EventBudgetServiceImpl implements EventBudgetService {

    private final EventBudgetRepo eventBudgetRepo;

    private final EventService eventService;
    private final BudgetCategoryService budgetCategoryService;

    @Autowired
    public EventBudgetServiceImpl(EventBudgetRepo eventBudgetRepo, EventService eventService,
            BudgetCategoryService budgetCategoryService) {
        this.eventBudgetRepo = eventBudgetRepo;
        this.eventService = eventService;
        this.budgetCategoryService = budgetCategoryService;
    }

    // Add budget
    @Override
    public void addNewExpense(Integer eventId, AddEventExpenseDTO addEventExpenseDTO) throws Exception {

        EventBudgetKey eventBudgetKey = EventBudgetKey.builder()
                .eventId(eventId)
                .budgetId(addEventExpenseDTO.getBudgetCategoryId())
                .build();

        Optional<EventBudget> optExistingEventBudget = eventBudgetRepo.findById(eventBudgetKey);
        if (optExistingEventBudget.isPresent()) {
            EventBudget eventBudget = optExistingEventBudget.get();
            eventBudget.setAmountSpent(eventBudget.getAmountSpent() + addEventExpenseDTO.getAmount());
            eventBudgetRepo.save(eventBudget);

        } else {
            throw new Exception("Budget doesn't exists for this event");
        }
    }

    @Override
    public void addBudget(Integer eventId, EventBudgetDTO eventBudgetDTO) throws Exception {

        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new Exception("Event not found");
        }

        BudgetCategory budgetCategory = budgetCategoryService
                .getBudgetCategoryById(eventBudgetDTO.getBudgetCategoryId());
        if (budgetCategory == null) {
            throw new Exception("Budget category not found");
        }

        EventBudgetKey eventBudgetKey = EventBudgetKey.builder()
                .eventId(eventId)
                .budgetId(eventBudgetDTO.getBudgetCategoryId())
                .build();

        Optional<EventBudget> existingEventBudget = eventBudgetRepo.findById(eventBudgetKey);
        if (existingEventBudget.isPresent()) {
            throw new Exception("Budget already exists for this event");
        }

        EventBudget eventBudget = EventBudget.builder()
                .id(eventBudgetKey)
                .amountAllocated(eventBudgetDTO.getAmountAllocated())
                .amountSpent(eventBudgetDTO.getAmountSpent())
                .build();

        eventBudgetRepo.save(eventBudget);
    }

    // Delete event budget
    @Override
    public void deleteEventBudget(Integer eventId, Integer budgetId) {
        EventBudgetKey eventBudgetKey = EventBudgetKey.builder()
                .eventId(eventId)
                .budgetId(budgetId)
                .build();

        eventBudgetRepo.deleteById(eventBudgetKey);
    }

    // Get all budgets by event id
    @Override
    public List<EventBudgetDTO> getAllBudgetsByEventId(Integer eventId) {
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }
        List<EventBudget> eventBudgets = eventBudgetRepo.findByEvent(event);
        return eventBudgets.stream()
                .map(eventBudget -> EventBudgetDTO.builder()
                        .amountAllocated(eventBudget.getAmountAllocated())
                        .amountSpent(eventBudget.getAmountSpent())
                        .budgetCategoryId(eventBudget.getBudgetCategory().getId())
                        .budgetCategoryName(eventBudget.getBudgetCategory().getName())

                        .build())
                .collect(Collectors.toList());
    }
}
