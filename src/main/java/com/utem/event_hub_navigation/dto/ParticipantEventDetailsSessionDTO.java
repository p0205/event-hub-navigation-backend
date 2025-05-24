package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEventDetailsSessionDTO {

    private Integer id; //Session id
    private String sessionName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    @Builder.Default
    private List<ParticipantEventDetailsVenueDTO> venues = new ArrayList<>();

}
