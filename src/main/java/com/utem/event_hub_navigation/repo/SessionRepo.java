package com.utem.event_hub_navigation.repo;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.Session;

public interface SessionRepo extends JpaRepository<Session,Integer>{

    List<Session> findByEvent(Event event);

     @Query("SELECT DISTINCT v.name FROM Session s JOIN s.sessionVenues sv JOIN sv.venue v WHERE s.event.id = :eventId")
    Set<String> findDistinctVenueNamesByEventId(@Param("eventId") Integer eventId);

//     
}
