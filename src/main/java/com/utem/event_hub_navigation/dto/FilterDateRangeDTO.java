package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterDateRangeDTO {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
