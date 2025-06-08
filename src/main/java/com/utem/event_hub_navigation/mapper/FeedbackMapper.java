package com.utem.event_hub_navigation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.utem.event_hub_navigation.dto.FeedbackDTO;
import com.utem.event_hub_navigation.model.Feedback;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(target="submittedAt",ignore = true)
    @Mapping(target="id",ignore = true)
    @Mapping(target="registration",ignore = true)
    Feedback fromDto(FeedbackDTO dto);
}
