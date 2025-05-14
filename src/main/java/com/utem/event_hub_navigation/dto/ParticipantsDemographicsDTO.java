package com.utem.event_hub_navigation.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantsDemographicsDTO {

    private int totalNumber;
    private Map<String, Long> byFaculty;
    private Map<String, Long> byCourse;
    private Map<String, Long> byYear;
    private Map<String, Long> byGender;

}
