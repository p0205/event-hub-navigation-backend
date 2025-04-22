package com.utem.event_hub_navigation.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor



public class TeamMember {

    @EmbeddedId
    private TeamMemberKey id;

    private String role; //ENUM
}

