package com.utem.event_hub_navigation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.utem.event_hub_navigation.dto.UpdateVenueRequest;
import com.utem.event_hub_navigation.model.Venue;
import com.utem.event_hub_navigation.model.VenueUtilizationData;

public interface VenueService {

    void addVenues(Venue venue);

    Venue getVenue(Integer venueId);

    List<Venue> getAllVenues();

    Page<Venue> getAllVenuesByFloor(Pageable pageable, Integer floorLevel);

    void deleteVenues(Integer venueId);

    List<Venue> getVenuesByCapacity(Integer capacity);

    List<VenueUtilizationData> getVenueUtilizationData(LocalDateTime startDate, LocalDateTime endDate);

    Venue updateVenue(UpdateVenueRequest request);

}