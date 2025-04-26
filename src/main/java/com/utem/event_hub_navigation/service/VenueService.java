package com.utem.event_hub_navigation.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.model.Venue;
import com.utem.event_hub_navigation.repo.VenueRepo;

@Service
public class VenueService {

    private final VenueRepo venueRepo;

    @Autowired
    public VenueService(VenueRepo venueRepo) {
        this.venueRepo = venueRepo;
    }

    public void addVenues(Venue venue) {
        venueRepo.save(venue);
    }

    public Venue getVenue(Integer venueId) {
        return venueRepo.findById(venueId).orElse(null);
    }

    public List<Venue> getAllVenues() {
        return venueRepo.findAll();
    }
    public void deleteVenues(Integer venueId) {
        venueRepo.deleteById(venueId);
    }


}