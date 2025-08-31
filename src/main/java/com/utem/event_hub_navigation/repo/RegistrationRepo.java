package com.utem.event_hub_navigation.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.utem.event_hub_navigation.dto.CalendarEventDTO;
import com.utem.event_hub_navigation.dto.ParticipantEventOverviewResponse;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.Registration;
import com.utem.event_hub_navigation.model.User;

@Repository
public interface RegistrationRepo extends JpaRepository<Registration, Integer> {

    Registration findByEventAndParticipant(Event event, User user);

    Registration findByEvent_IdAndParticipant_Id(Integer eventId, Integer userId);

    Page<Registration> findByEvent(Event event, Pageable pageable);

    List<Registration> findByEvent_Id(Integer eventId);

    Long countByEvent(Event event);

    Boolean existsByEventAndParticipant(Event event, User user);

    @Query("SELECT count(r.id) FROM Registration r WHERE r.event.id = :eventId")
    int countByEventId(@Param("eventId") Integer eventId);

    @Query("""
            SELECT u.faculty, COUNT(u.id)
            FROM Registration r
            JOIN r.participant u
            WHERE r.event.id = :eventId AND u.faculty IS NOT NULL
            GROUP BY u.faculty
            """)
    List<Object[]> getDemographicDataGroupByFaculty(@Param("eventId") Integer eventId);

    @Query("""
            SELECT u.course, COUNT(u.id)
            FROM Registration r
            JOIN r.participant u
            WHERE r.event.id = :eventId AND u.course IS NOT NULL
            GROUP BY u.course
            """)
    List<Object[]> getDemographicDataGroupByCourse(@Param("eventId") Integer eventId);

    @Query("""
            SELECT u.year, COUNT(u.id)
            FROM Registration r
            JOIN r.participant u
            WHERE r.event.id = :eventId AND u.year IS NOT NULL
            GROUP BY u.year
            """)
    List<Object[]> getDemographicDataGroupByYear(@Param("eventId") Integer eventId);

    @Query("""
            SELECT
                CASE
                    WHEN u.gender = 'M' THEN 'Male'
                    WHEN u.gender = 'F' THEN 'Female'
                    ELSE 'Other'
                END,
                COUNT(u.id)
            FROM Registration r
            JOIN r.participant u
            WHERE r.event.id = :eventId AND u.gender IS NOT NULL
            GROUP BY u.gender
            """)
    List<Object[]> getDemographicDataGroupByGender(@Param("eventId") Integer eventId);

    @Query(value = """
                SELECT e.id AS id,
                       e.name AS eventName,
                       e.start_date_time AS startDateTime,
                       e.end_date_time AS endDateTime,
                       r.register_date AS registerDate
                FROM Registration r
                JOIN Event e ON e.id = r.event_id
                WHERE r.user_id = :userId && e.end_date_time > NOW()

            """, nativeQuery = true)
    List<ParticipantEventOverviewResponse> fetchParticipantUpcomingEvents(@Param("userId") Integer userId);

    @Query(value = """
                SELECT e.id AS id,
                       e.name AS eventName,
                       e.start_date_time AS startDateTime,
                       e.end_date_time AS endDateTime,
                       r.register_date AS registerDate
                FROM registration r
                JOIN event e ON e.id = r.event_id
                WHERE r.user_id = :userId && e.end_date_time <= NOW()

            """, nativeQuery = true)
    List<ParticipantEventOverviewResponse> fetchParticipantPastEvents(@Param("userId") Integer userId);

    // @Query(value = """
    //         SELECT e.id AS id,
    //                e.name AS eventName,
    //                e.start_date_time AS startDateTime,
    //                e.end_date_time AS endDateTime,
    //                r.register_date AS registerDate
    //         FROM Registration r
    //         JOIN Event e ON e.id = r.event_id
    //         WHERE r.user_id = :userId
    //         AND e.start_date_time >= :startDateTime
    //         AND e.start_date_time <= :endDateTime
    //         """, nativeQuery = true)
    // List<ParticipantEventOverviewResponse> fetchParticipantEventsByDateRange(
    //         @Param("userId") Integer userId,
    //         @Param("startDateTime") LocalDateTime startDateTime,
    //         @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = """
                SELECT
                    e.id AS eventId,
                    e.name AS eventName,
                    s.id AS sessionId,
                    s.session_name AS sessionName,
                    s.start_date_time AS startDateTime,
                    s.end_date_time AS endDateTime,
                    COALESCE(GROUP_CONCAT(DISTINCT v.name SEPARATOR ', '), '') AS venueNames
                FROM registration r
                JOIN event e ON r.event_id = e.id
                JOIN session s ON s.event_id = e.id
                LEFT JOIN session_venue sv ON sv.session_id = s.id
                LEFT JOIN venue v ON v.id = sv.venue_id

                WHERE r.user_id = :participantId
                    AND s.start_date_time >= :startDateTime
                    AND s.start_date_time <= :endDateTime
                GROUP BY s.id
            """, nativeQuery = true)
    List<CalendarEventDTO> fetchParticipantEventsByDateRange(
            @Param("participantId") Integer participantId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM registration
            WHERE event_id = :eventId AND user_id = :userId
            """, nativeQuery = true)
    void deleteByUserIdUserId(@Param("eventId") Integer eventId, @Param("userId") Integer participantId);

}
