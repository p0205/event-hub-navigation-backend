package com.utem.event_hub_navigation.repo;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.model.User;

@Repository
public interface EventRepo extends JpaRepository<Event, Integer> {

    List<Event> findByOrganizerAndStatus(User organizer, EventStatus status);

    // @Query("SELECT e FROM Event e WHERE e.startDateTime >= :startOfDay AND e.startDateTime < :endOfDay")
    // List<Event> findByEventDate(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // You might also want to find events by just the organizer user
    List<Event> findByOrganizer(User organizer);


    List<Event> findByStatus(EventStatus status);

}
