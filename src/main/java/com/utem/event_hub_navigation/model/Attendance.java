package com.utem.event_hub_navigation.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor



public class Attendance {

    @EmbeddedId
    private AttendanceKey id;

    // Add this: link back to Event
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)    
    @MapsId("eventVenueId") // maps the sessionId part of the embedded ID
    @JoinColumn(name = "event_venue_id")
    @JsonBackReference
    private EventVenue eventVenue;

    // Optionally, add venue relationship if applicable
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @MapsId("registrationId") // maps the registrationId part of the embedded ID
    @JoinColumn(name = "registration_id")
    @JsonBackReference
    private Registration registration; 


    private LocalDateTime checkinDateTime;

}
