package com.utem.event_hub_navigation.service;

import java.time.LocalDateTime;
import java.util.List;

import com.utem.event_hub_navigation.model.Venue;
import com.utem.event_hub_navigation.model.VenueUtilizationData;

public interface VenueService {

    void addVenues(Venue venue);

    Venue getVenue(Integer venueId);

    List<Venue> getAllVenues();

    void deleteVenues(Integer venueId);

    List<Venue> getVenuesByCapacity(Integer capacity);

    List<VenueUtilizationData> getVenueUtilizationData(LocalDateTime startDate, LocalDateTime endDate);

}