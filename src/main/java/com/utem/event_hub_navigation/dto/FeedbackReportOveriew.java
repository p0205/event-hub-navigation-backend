package com.utem.event_hub_navigation.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackReportOveriew {

    private Double averageRating;
    private int feedbackCount;
    private Map<String, Long> ratings;
}
