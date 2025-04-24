package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.Registration;
import com.utem.event_hub_navigation.model.User;

@Repository
public interface RegistrationRepo extends JpaRepository<Registration, Integer> {

    Registration findByEventAndParticipant(Event event, User user);
    Boolean existsByEventAndParticipant(Event event, User user);
}
