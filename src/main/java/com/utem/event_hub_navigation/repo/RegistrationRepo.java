package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.Registration;

@Repository
public interface RegistrationRepo extends JpaRepository<Registration, Integer> {

    Registration findByEventIdAndParticipantId(Integer eventId, Integer participantId);
}
