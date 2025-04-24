package com.utem.event_hub_navigation.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.utem.event_hub_navigation.dto.EventDTO;
import com.utem.event_hub_navigation.model.Event;

@Mapper(
    componentModel = "spring",
    uses = {EventMapperHelper.class, EventVenueMapper.class, EventBudgetMapper.class}
)
public interface EventMapper {

    @Mapping(source = "organizer.id", target = "organizerId")
    EventDTO tDto(Event event);

    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUser")
    Event toEntity(EventDTO dto);

    List<EventDTO> toEventDTOs(List<Event> events);
}
