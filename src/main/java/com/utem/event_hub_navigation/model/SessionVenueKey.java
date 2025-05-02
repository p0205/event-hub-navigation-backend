package com.utem.event_hub_navigation.model;

import java.io.Serializable;

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
public class SessionVenueKey implements Serializable {

    private Integer sessionId;
    private Integer venueId;
}
