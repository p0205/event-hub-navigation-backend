package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.Venue;

@Repository
public interface VenueRepo extends JpaRepository<Venue, Integer> {
    // Custom query methods can be defined here if needed
    // For example, find by name or location
    // List<Venue> findByName(String name);
    // List<Venue> findByLocation(String location);

}
