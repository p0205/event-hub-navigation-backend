package com.utem.event_hub_navigation.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.model.Venue;
import com.utem.event_hub_navigation.model.VenueUtilizationData;
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
        return venueRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(Venue::getName)) // Sort by name
                .collect(Collectors.toList());
        
    }

    @Override
    public void deleteVenues(Integer venueId) {
        venueRepo.deleteById(venueId);
    }

    @Override
    public List<Venue> getVenuesByCapacity(Integer capacity) {
        return venueRepo.findByCapacityGreaterThanEqual(capacity);
    }

    @Override
    public List<VenueUtilizationData> getVenueUtilizationData(LocalDateTime startDate, LocalDateTime endDate) {
        return venueRepo.getVenueUtilizationData(startDate, endDate);
    }
}