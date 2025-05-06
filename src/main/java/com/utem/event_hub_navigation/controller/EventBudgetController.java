package com.utem.event_hub_navigation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.dto.EventBudgetDTO;
import com.utem.event_hub_navigation.service.EventBudgetService;

@RestController
@RequestMapping("/events/{eventId}/budget")

public class EventBudgetController {

    private final EventBudgetService eventBudgetService;

    @Autowired
    public EventBudgetController(EventBudgetService eventBudgetService) {
        this.eventBudgetService = eventBudgetService;
    }

    // Add budget
    @PostMapping
    public ResponseEntity<?> addBudget(@PathVariable("eventId") Integer eventId, @RequestBody EventBudgetDTO eventBudgetDTO) {
        try {
            eventBudgetService.addBudget(eventId, eventBudgetDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to add budget: " + e.getMessage());
        }
    }

    // Delete budget
    @DeleteMapping("/{budgetId}")
    public ResponseEntity<?> deleteBudget(@PathVariable("eventId") Integer eventId, @PathVariable("budgetId") Integer budgetId) {
        try {
            eventBudgetService.deleteEventBudget(eventId, budgetId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete budget: " + e.getMessage());
        }
    }

    // Get all budgets by event id
    @GetMapping
    public ResponseEntity<?> getAllBudgetsByEventId(@PathVariable("eventId") Integer eventId) {
        try {
            return ResponseEntity.ok(eventBudgetService.getAllBudgetsByEventId(eventId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get budgets: " + e.getMessage());
        }
    }

}
