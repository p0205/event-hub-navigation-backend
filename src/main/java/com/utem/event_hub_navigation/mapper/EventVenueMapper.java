package com.utem.event_hub_navigation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.utem.event_hub_navigation.dto.EventVenueDTO;
import com.utem.event_hub_navigation.model.EventVenue;

@Mapper(componentModel = "spring", uses = {EventMapperHelper.class})
public interface EventVenueMapper {
    @Mapping(source = "venue.id", target = "venueId")
    EventVenueDTO toDto(EventVenue entity);
    
    @Mapping(source = "venueId", target = "venue", qualifiedByName = "mapVenue")
    EventVenue toEntity(EventVenueDTO dto);
}
