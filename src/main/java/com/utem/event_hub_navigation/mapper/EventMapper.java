package com.utem.event_hub_navigation.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.utem.event_hub_navigation.dto.EventDTO;
import com.utem.event_hub_navigation.model.Event;

@Mapper(componentModel = "spring", uses = { EventMapperHelper.class, EventBudgetMapper.class, SessionMapper.class })
public interface EventMapper {

    @Mapping(source = "organizer.id", target = "organizerId")
    @Mapping(source = "organizer.name", target = "organizerName")
    EventDTO toDto(Event event);

    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUser")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "endDateTime", ignore = true)
    @Mapping(target = "startDateTime", ignore = true)
    Event toEntity(EventDTO dto);

    List<EventDTO> toEventDTOs(List<Event> events);
}
