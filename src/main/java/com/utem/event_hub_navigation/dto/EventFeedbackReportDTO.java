package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFeedbackReportDTO {

    private String eventName;
    private String eventDescription;
    private String organizerName;
    private LocalDateTime eventStartDateTime;
    private LocalDateTime eventEndDateTime;
    private LocalDateTime reportGenerationDate;

    private int totolParticipants;

    private double feedbackSubmissionRate; // = totalFeedbackEntries/ totalParticipants

    private int totalFeedbackEntries;
    private double averageRating;
    private Map<String, Long> ratingsDistribution;

    private Map<Integer, List<String>> commentsForEachRating;


}
