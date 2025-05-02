package com.utem.event_hub_navigation.service;

import java.util.List;

import com.utem.event_hub_navigation.model.Venue;

public interface VenueService {

    void addVenues(Venue venue);

    Venue getVenue(Integer venueId);

    List<Venue> getAllVenues();

    void deleteVenues(Integer venueId);

}