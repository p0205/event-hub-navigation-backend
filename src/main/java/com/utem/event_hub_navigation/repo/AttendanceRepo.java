package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.Attendance;
import com.utem.event_hub_navigation.model.EventVenue;
import com.utem.event_hub_navigation.model.Registration;

@Repository
public interface AttendanceRepo extends JpaRepository<Attendance, Integer> {
    boolean existsByEventVenueAndRegistration(EventVenue eventVenue, Registration registration);
}
