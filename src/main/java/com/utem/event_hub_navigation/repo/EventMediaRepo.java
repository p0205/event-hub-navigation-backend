package com.utem.event_hub_navigation.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utem.event_hub_navigation.model.EventMedia;

public interface EventMediaRepo extends JpaRepository<EventMedia, Long> {
    // Custom query methods can be defined here if needed
    // For example, to find media by event ID:

    List<EventMedia> findByEventId(Integer eventId);

}
