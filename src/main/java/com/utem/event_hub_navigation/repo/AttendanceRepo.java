package com.utem.event_hub_navigation.repo;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.dto.Attendee;
import com.utem.event_hub_navigation.model.Attendance;
import com.utem.event_hub_navigation.model.Session;
import com.utem.event_hub_navigation.model.Registration;

@Repository
public interface AttendanceRepo extends JpaRepository<Attendance, Integer> {
    boolean existsBysessionAndRegistration(Session session, Registration registration);

    // Spring Data JPA will automatically apply LIMIT and OFFSET based on Pageable
    @Query(value = """
                SELECT u.id AS userId,
                       u.name AS name,
                       u.email AS email,
                       u.phone_no AS phoneNo,
                       u.gender AS gender,
                       u.faculty AS faculty,
                       u.course AS course,
                       u.year AS year,
                       a.checkin_date_time AS checkinDateTime
                FROM attendance a
                JOIN registration r ON a.registration_id = r.id
                JOIN users u ON r.user_id = u.id
                WHERE a.session_id = :sessionId
            """,
            // countQuery is important for native queries to count total results across all
            // pages
            // without applying LIMIT/OFFSET
            countQuery = """
                         SELECT count(a.id)
                         FROM attendance a
                         JOIN registration r ON a.registration_id = r.id
                         JOIN users u ON r.user_id = u.id
                         WHERE a.session_id = :sessionId
                    """, nativeQuery = true)
    Page<Attendee> findCheckInParticipantsBySession(@Param("sessionId") Integer sessionId, Pageable pageable);

    @Query("SELECT count(a) FROM Attendance a JOIN a.session s WHERE s.id = :sessionId")
    int countBySessionId(Integer sessionId);

}
