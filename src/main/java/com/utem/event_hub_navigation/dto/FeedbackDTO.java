package com.utem.event_hub_navigation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackDTO {

    private Integer userId;
    private Integer rating;
    private String comment;
}
