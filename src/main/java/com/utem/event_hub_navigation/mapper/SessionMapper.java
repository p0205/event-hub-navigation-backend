package com.utem.event_hub_navigation.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.utem.event_hub_navigation.dto.SessionDTO;
import com.utem.event_hub_navigation.model.Session;
import com.utem.event_hub_navigation.model.SessionVenue;
import com.utem.event_hub_navigation.model.Venue;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    SessionMapper INSTANCE = Mappers.getMapper(SessionMapper.class);


    @Mapping(target = "event", ignore = true)  //  set Event manually
    @Mapping(target = "sessionVenues", ignore = true)  //  handle venues separately
    Session toEntity(SessionDTO dto);

    @Mapping(source = "sessionVenues", target = "venues")
    SessionDTO toDto(Session session);

    @Mapping(source = "sessionVenues", target = "venues")
    List<SessionDTO> toDto(List<Session> session);

    default List<Venue> mapSessionVenuesToVenues(List<SessionVenue> sessionVenues) {
        if (sessionVenues == null || sessionVenues.isEmpty()) {
            // Return null or an empty list based on your API contract preference
            // return null;
            return Collections.emptyList();
        }
        return sessionVenues.stream()
                .map(SessionVenue::getVenue) // Get the Venue object from each SessionVenue join entity
                .filter(Objects::nonNull)    // Ensure the linked venue is not null
                .collect(Collectors.toList());
    }
}
