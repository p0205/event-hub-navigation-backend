package com.utem.event_hub_navigation.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.utem.event_hub_navigation.dto.EventBudgetDTO;
import com.utem.event_hub_navigation.dto.EventDTO;
import com.utem.event_hub_navigation.dto.EventVenueDTO;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventBudget;
import com.utem.event_hub_navigation.model.EventVenue;

@Mapper(componentModel = "spring", uses = {EventMapperHelper.class})
public interface EventMapper {

    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUser")
    @Mapping(target = "eventVenues", source = "eventVenues")
    @Mapping(target = "eventBudgets", source = "eventBudgets")
    Event toEntity(EventDTO dto);

    @Mapping(source = "venueId", target = "venue", qualifiedByName = "mapVenue")
    @Mapping(target = "event", ignore = true) // Set manually if needed
    @Mapping(target = "id", ignore = true) // You’ll populate it manually
    EventVenue toEntity(EventVenueDTO dto);

    @Mapping(source = "budgetCategoryId", target = "budgetCategory", qualifiedByName = "mapCategory")
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "id", ignore = true) // You’ll populate it manually
    EventBudget toEntity(EventBudgetDTO dto);
}
