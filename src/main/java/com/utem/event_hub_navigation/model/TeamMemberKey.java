package com.utem.event_hub_navigation.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TeamMemberKey {

    private Integer eventId;
    private Integer userId;
    
}
