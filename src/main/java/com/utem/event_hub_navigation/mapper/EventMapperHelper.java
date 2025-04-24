package com.utem.event_hub_navigation.mapper;

import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.utem.event_hub_navigation.model.BudgetCategory;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.model.Venue;
import com.utem.event_hub_navigation.repo.BudgetCategoryRepo;
import com.utem.event_hub_navigation.repo.UserRepo;
import com.utem.event_hub_navigation.repo.VenueRepo;

@Component
public class EventMapperHelper {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private VenueRepo venueRepository;

    @Autowired
    private BudgetCategoryRepo budgetCategoryRepository;

    @Named("mapUser")
    public User mapUser(Integer id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Named("mapVenue")
    public Venue mapVenue(Integer id) {
        return venueRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venue not found"));
    }

    @Named("mapCategory")
    public BudgetCategory mapCategory(Integer id) {
        return budgetCategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Budget category not found"));
    }

    
}
