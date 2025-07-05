package com.utem.event_hub_navigation.repo;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.dto.CalendarEventDTO;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.model.User;

@Repository
public interface EventRepo extends JpaRepository<Event, Integer> {

    List<Event> findByOrganizerAndStatusOrderByStartDateTimeAsc(User organizer, EventStatus status);

    // List<Event> findByOrganizerAndStatusOrderByStartDateTimeDesc(Users organizer,
    // EventStatus status);

    // @Query("SELECT e FROM Event e WHERE e.startDateTime >= :startOfDay AND
    // e.startDateTime < :endOfDay")
    // List<Event> findByEventDate(@Param("startOfDay") LocalDateTime startOfDay,
    // @Param("endOfDay") LocalDateTime endOfDay);

    // You might also want to find events by just the organizer user

    List<Event> findByOrganizerOrderByStartDateTimeDesc(User organizer);

    List<Event> findByStatus(EventStatus status);

    @Query("SELECT e.name FROM Event e WHERE e.id = :id")
    String findNameById(@Param("id") Integer id);

    @Query(value = """
                SELECT
                    e.id AS eventId,
                    e.name AS eventName,
                    s.id AS sessionId,
                    s.session_name AS sessionName,
                    s.start_date_time AS startDateTime,
                    s.end_date_time AS endDateTime,
                    COALESCE(GROUP_CONCAT(v.name SEPARATOR ', '), '') AS venueNames
                FROM event e
                JOIN session s ON s.event_id = e.id
                LEFT JOIN session_venue sv ON sv.session_id = s.id
                LEFT JOIN venue v ON v.id = sv.venue_id
                WHERE e.user_id = :organizerId AND e.status = 'ACTIVE' AND s.start_date_time >= NOW()
                GROUP BY s.id
            """, nativeQuery = true)
    List<CalendarEventDTO> findCalendarEntriesByOrganizerId(@Param("organizerId") Integer organizerId);

    List<Event> findByEndDateTimeBeforeAndStatus(LocalDateTime nowDateTime, EventStatus eventStatus);

    /**
     * Fetches a single Event by its ID, eagerly loading its organizer,
     * team members (and their associated users/roles),
     * sessions, and the venues for each session.
     *
     * This uses a single SQL query (typically with LEFT JOINs) to avoid the N+1
     * problem.
     */
    @Query(value = """
                        SELECT
                          e.id as event_id, e.name, e.description, r.register_date,
                          e.start_date_time, e.end_date_time,
                          organizer.name as organizer_name,
                          pic.name as pic_name, 
                          pic.phone_no as pic_contact,
                          pic.email as pic_email,
                          s.id as session_id, s.session_name,
                          s.start_date_time as session_start, s.end_date_time as session_end,
                          v.id as venue_id, v.name as venue_name
                        FROM event e

                        LEFT JOIN users organizer ON organizer.id = e.user_id
                        LEFT JOIN registration r ON r.event_id = e.id AND r.user_id = organizer.id

                        LEFT JOIN team_member t ON t.event_id = e.id
            LEFT JOIN role r2 ON r2.id = t.role_id AND r2.name = 'PIC'

                        LEFT JOIN users pic ON pic.id = t.user_id
                        LEFT JOIN session s ON s.event_id = e.id
                        LEFT JOIN session_venue vs ON vs.session_id = s.id
                        LEFT JOIN venue v ON v.id = vs.venue_id
                        WHERE e.id = :eventId

                                    """, nativeQuery = true)
    List<Object[]> findEventDetailsById(@Param("eventId") int eventId);

    @Query(value = """
                SELECT
                    e.id AS eventId,
                    e.name AS eventName,
                    s.id AS sessionId,
                    s.session_name AS sessionName,
                    e.description AS description,
                    e.type AS eventType,
                    s.start_date_time AS startDateTime,
                    s.end_date_time AS endDateTime,
                    COALESCE(GROUP_CONCAT(DISTINCT v.name SEPARATOR ', '), '') AS venueNames
            
                FROM event e
                JOIN session s ON s.event_id = e.id
                LEFT JOIN session_venue sv ON sv.session_id = s.id
                LEFT JOIN venue v ON v.id = sv.venue_id

                WHERE
                   s.start_date_time >= :startDateTime
                    AND s.end_date_time <= :endDateTime
                GROUP BY s.id
            """, nativeQuery = true)
    List<CalendarEventDTO> fetchAllCalendarEventByMonth(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = :status AND e.startDateTime >= :startDateTime AND e.endDateTime <= :endDateTime")
    long countByStatus(@Param("status") EventStatus status, 
                       @Param("startDateTime") LocalDateTime startDateTime,
                       @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = "SELECT DATE_FORMAT(e.start_date_time, '%Y-%m') as month, COUNT(e.id) as events " +
            "FROM event e " +
            "WHERE e.start_date_time >= :startDateTime " +
            "AND e.end_date_time <= :endDateTime " +
            "AND e.status = 'COMPLETED' " +
            "GROUP BY month " +
            "ORDER BY month ASC", nativeQuery = true)
    List<Object[]> countEventsByMonth(@Param("startDateTime") LocalDateTime startDateTime,
                                       @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = "SELECT DATE_FORMAT(s.startDateTime, '%H:00') as hour, COUNT(s.id) as count " + 
                    "FROM Session s "+ 
                    "JOIN s.event e " +
                    "WHERE s.startDateTime >= :startDateTime " +
                    "AND s.endDateTime <= :endDateTime " +
                    "AND e.startDateTime >= :startDateTime " +
                    "AND e.endDateTime <= :endDateTime " +
                    "AND e.status = 'COMPLETED' " +
                    "GROUP BY hour " +
                    "ORDER BY hour", nativeQuery = false)
    List<Object[]> countSessionsByHour(@Param("startDateTime") LocalDateTime startDateTime,
    @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = "SELECT e.type as name, COUNT(e.id) as value "+
                    "FROM Event e " + 
                    "WHERE e.startDateTime >= :startDateTime " +
                    "AND e.endDateTime <= :endDateTime " +
                    "AND e.status = 'COMPLETED' " +
                    "GROUP BY e.type " +
                    "ORDER BY value DESC LIMIT 5")
    List<Object[]> countEventsByType(@Param("startDateTime") LocalDateTime startDateTime,
    @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = "SELECT v.name as name, COUNT(e.id) as count "+ 
                    "FROM Event e " + 
                    "JOIN e.sessions s " +
                    "JOIN s.sessionVenues sv " +
                    "JOIN sv.venue v " + 
                    "WHERE e.startDateTime >= :startDateTime " +
                    "AND e.endDateTime <= :endDateTime " +
                    "AND e.status = 'COMPLETED' " +
                    "GROUP BY v.name " +
                    "ORDER BY count DESC LIMIT 5")
    List<Object[]> findTop5VenuesByEventCount(@Param("startDateTime") LocalDateTime startDateTime,
    @Param("endDateTime") LocalDateTime endDateTime);

    @Query(
        value = "WITH SessionAttendanceRates AS (" +
                "    SELECT " +
                "        e.id AS eventId, " +
                "        e.type AS eventType, " +
                "        s.id AS sessionId, " +
                "        COALESCE(" +
                "            CAST(COUNT(a.registration_id) AS DECIMAL(10, 2)) * 100.0 / " +
                "            NULLIF((SELECT COUNT(DISTINCT r.id) FROM registration r WHERE r.event_id = e.id), 0), " +
                "        0.00) AS sessionAttendanceRate " +
                "    FROM " +
                "        session s " +
                "    JOIN " +
                "        event e ON s.event_id = e.id " +
                "    LEFT JOIN " +
                "        attendance a ON a.session_id = s.id " +
                "    GROUP BY " +
                "        e.id, e.type, s.id, s.session_name " +
                ") " +
                "SELECT " +
                "  e.type as eventType, " +
                "  COUNT(e.id) as eventsHeld, " +
                "  COALESCE(" +
                "    SUM(" +
                "      (SELECT COUNT(r.id) " +
                "       FROM registration r " +
                "       WHERE r.event_id = e.id)" +
                "    ), 0) as totalRegistrations, " +
                "  COALESCE(" +
                "    AVG((SELECT COUNT(r2.id) " +
                "         FROM registration r2 " +
                "         WHERE r2.event_id = e.id)), 0) as avgRegPerEvent, " +
                "  COALESCE(" +
                "    AVG(CASE WHEN e.participants_no > 0 THEN " +
                "      (SELECT COUNT(r3.id) FROM registration r3 WHERE r3.event_id = e.id) * 100.0 / e.participants_no " +
                "      ELSE NULL END), 0) as avgFillRate, " +
                "  COALESCE(AVG(sar.sessionAttendanceRate), 0) as avgAttendanceRate " + 
                "FROM event e " +
                "LEFT JOIN SessionAttendanceRates sar ON e.id = sar.eventId " + 
                "WHERE e.status = 'COMPLETED' " +
                "  AND e.start_date_time >= :startDate " +
                "  AND e.end_date_time <= :endDate " +
                "GROUP BY e.type " +
                "ORDER BY eventsHeld DESC",
        nativeQuery = true
    )
    List<Object[]> fetchEventTypePerformanceData(@Param("startDate") String startDate, @Param("endDate") String endDate);
}