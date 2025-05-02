package com.utem.event_hub_navigation.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionVenue {

    @EmbeddedId
    private SessionVenueKey id;

    @ManyToOne
    @MapsId("sessionId")
    @JoinColumn(name = "session_id")
    @JsonBackReference
    private Session session;

    @ManyToOne
    @MapsId("venueId") // maps venueId attribute of embedded id
    @JoinColumn(name = "venue_id")
    private Venue venue;

}
