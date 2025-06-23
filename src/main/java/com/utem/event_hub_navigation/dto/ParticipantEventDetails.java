package com.utem.event_hub_navigation.dto;

import java.time.LocalDate;
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
public class ParticipantEventDetails {

    private Integer id; //Event Id
    private String eventName;

    private String description;
    private LocalDate registerDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String organizerName;
    private String picName;
    private String picContact;
    private String picEmail;
    @Builder.Default
    private List<ParticipantEventDetailsSessionDTO> sessions = new ArrayList<>();
    

}
