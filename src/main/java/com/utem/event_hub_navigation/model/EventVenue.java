package com.utem.event_hub_navigation.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

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

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventVenue {

   
    @EmbeddedId
    private EventVenueKey id;

    private String sessionName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    // Add this: link back to Event
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId") // maps the eventId part of the embedded ID
    @JoinColumn(name = "event_id")
    @JsonBackReference
    private Event event;

    // Optionally, add venue relationship if applicable
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("venueId") // maps the venueId part of the embedded ID
    @JoinColumn(name = "venue_id")
    private Venue venue; // assuming you have a Venue entity


}
