package com.utem.event_hub_navigation.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.model.Venue;
import com.utem.event_hub_navigation.repo.VenueRepo;
import com.utem.event_hub_navigation.service.VenueService;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepo venueRepo;

    @Autowired
    public VenueServiceImpl(VenueRepo venueRepo) {
        this.venueRepo = venueRepo;
    }

    @Override
    public void addVenues(Venue venue) {
        venueRepo.save(venue);
    }

    @Override
    public Venue getVenue(Integer venueId) {
        return venueRepo.findById(venueId).orElse(null);
    }

    @Override
    public List<Venue> getAllVenues() {
        return venueRepo.findAll();
    }
    @Override
    public void deleteVenues(Integer venueId) {
        venueRepo.deleteById(venueId);
    }


}