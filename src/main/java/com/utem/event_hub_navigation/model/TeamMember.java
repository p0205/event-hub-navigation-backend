package com.utem.event_hub_navigation.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TeamMember {

    @EmbeddedId
    private TeamMemberKey id;

    // Add this: link back to Event
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId("eventId") // maps the eventId part of the embedded ID
    @JoinColumn(name = "event_id")
    @JsonBackReference
    @ToString.Exclude
    private Event event;

    // Optionally, add venue relationship if applicable
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId("userId") // maps the venueId part of the embedded ID
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;
}
