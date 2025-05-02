package com.utem.event_hub_navigation.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.Session;

public interface SessionRepo extends JpaRepository<Session,Integer>{

    List<Session> findByEvent(Event event);

}
