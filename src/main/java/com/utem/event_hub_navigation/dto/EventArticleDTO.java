package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for post-event article generation, combining database and manual input fields.
 */
@Data
@Builder
public class EventArticleDTO {
    // Database-derived fields
    /** Event name (from event table) */
    private String eventName;
    /** Event type (from event table) */
    private String eventType;
    /** Event start date/time (from event table) */
    private LocalDateTime startDateTime;
    /** Event end date/time (from event table) */
    private LocalDateTime endDateTime;
    /** Total number of participants (from registration table) */
    private Long participantsNo;
    /** Session and venue information (from session/session_venue/venue tables) */
    private List<String> sessionVenueSummaries;
    /** Media file URLs (from event_media table) */
    private List<String> mediaUrls;
    /** Names of organizing committee/contributors (from team_member/users/role tables) */
    private List<String> committeeNames;

    private List<SessionDTO> sessions;

    private ArticleManualInputsDto manualInputs; // Manual inputs for article generation
}