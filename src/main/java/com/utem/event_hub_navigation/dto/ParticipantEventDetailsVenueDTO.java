package com.utem.event_hub_navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEventDetailsVenueDTO {
    private Integer id; // venue Id
    private String name;

}
