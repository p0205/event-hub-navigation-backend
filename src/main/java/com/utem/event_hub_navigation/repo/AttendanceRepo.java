package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.Attendance;

@Repository
public interface AttendanceRepo extends JpaRepository<Attendance, Integer> {
    boolean existsByRegistrationId(Integer registrationId);
}
