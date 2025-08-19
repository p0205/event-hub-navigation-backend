package com.utem.event_hub_navigation.dto;



import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = ArticleManualInputsDto.ArticleManualInputsDtoBuilder.class)
public class ArticleManualInputsDto {
    private String organizingBody; // Official name of organizing body or club
    private String creditIndividuals; // List of individuals to credit (speakers, facilitators, etc.)
    private String eventObjectives; // Narrative description of event objectives
    private String activitiesConducted; // Content or activities conducted
    private String targetAudience; // Target audience or participant background
    private String perceivedImpact; // Perceived impact or outcomes
    private String acknowledgements; // Acknowledgements or messages of appreciation
    private String appreciationMessage; // Additional appreciation message if needed
    private String language;
} 