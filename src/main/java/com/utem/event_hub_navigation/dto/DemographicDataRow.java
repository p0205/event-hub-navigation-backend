package com.utem.event_hub_navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DemographicDataRow{
    private String value;
    private Long count;
    private Float percentage;
    
}