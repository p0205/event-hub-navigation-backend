package com.utem.event_hub_navigation.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.Attendance;
import com.utem.event_hub_navigation.model.Session;
import com.utem.event_hub_navigation.model.Registration;

@Repository
public interface AttendanceRepo extends JpaRepository<Attendance, Integer> {
    boolean existsBysessionAndRegistration(Session session, Registration registration);

    @Query(
        value = """
            SELECT u.id, u.name, u.email,u.phone_no,u.gender,u.faculty,u.course,u.year,u.role
            FROM attendance a
            JOIN registration r ON a.registration_id = r.id
            JOIN users u ON r.user_id = u.id
            WHERE a.event_venue_id = :sessionId
        """,
        nativeQuery = true
    )
    List<Object[]> findCheckInParticipantsBysession(@Param("sessionId") Integer sessionId);}
