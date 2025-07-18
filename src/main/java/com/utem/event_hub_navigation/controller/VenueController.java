package com.utem.event_hub_navigation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.model.Venue;
import com.utem.event_hub_navigation.service.VenueService;

@RestController
@RequestMapping("/api/venue")
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

    @GetMapping("/capacity")
    public ResponseEntity<?> getVenuesByCapacity(@RequestParam("capacity") Integer capacity){
        try {
            return new ResponseEntity<>(venueService.getVenuesByCapacity(capacity), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{venueId}")
    public ResponseEntity<Venue> getVenue(@PathVariable("venueId") Integer venueId) {
        Venue venue = venueService.getVenue(venueId);
        return new ResponseEntity<>(venue, HttpStatus.OK);
    }


    @DeleteMapping("/{venueId}")
    public ResponseEntity<Void> deleteVenues(@PathVariable("venueId") Integer venueId) {
        venueService.deleteVenues(venueId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
