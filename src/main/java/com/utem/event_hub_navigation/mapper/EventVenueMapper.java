package com.utem.event_hub_navigation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.utem.event_hub_navigation.dto.EventVenueDTO;
import com.utem.event_hub_navigation.model.EventVenue;

@Mapper(componentModel = "spring")
public interface EventVenueMapper {
    @Mapping(source = "venue.id", target = "venueId")
    EventVenueDTO toDto(EventVenue entity);

    @Mapping(source = "venueId", target = "venue.id")
    EventVenue toEntity(EventVenueDTO dto);
}
