package com.utem.event_hub_navigation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.utem.event_hub_navigation.model.Venue;
import com.utem.event_hub_navigation.service.VenueService;

@Repository
public class VenueController {

    private final VenueService venueService;

    @Autowired
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    public ResponseEntity<Venue> addVenues(Venue venue) {
        venueService.addVenues(venue);
        return new ResponseEntity<>(venue, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Venue>> getAllVenues() {
        List<Venue> venues = venueService.getAllVenues();
        return new ResponseEntity<>(venues, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteVenues(Integer venueId) {
        venueService.deleteVenues(venueId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
