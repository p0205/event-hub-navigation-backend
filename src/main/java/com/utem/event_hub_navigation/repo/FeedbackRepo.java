package com.utem.event_hub_navigation.repo;

import java.util.List;
import java.util.OptionalDouble;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utem.event_hub_navigation.model.Feedback;

public interface FeedbackRepo extends JpaRepository<Feedback, Integer> {

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM feedback f
                JOIN registration r ON f.registration_id = r.id
                WHERE r.event_id = :eventId AND r.user_id = :userId
            )
            """, nativeQuery = true)
    Integer existsByEventIdAndUserId(@Param("eventId") Integer eventId, @Param("userId") Integer userId);

    /**
     * Retrieves the total number of feedback entries and the average rating for a
     * given event.
     *
     * @param eventId The ID of the event.
     * @return A List containing an Object array, where Object[0] is the total count
     *         (Long)
     *         and Object[1] is the average rating (Double). Returns an empty list
     *         if no feedback.
     */
    @Query("""
            SELECT COUNT(f.id), AVG(f.rating)
            FROM Feedback f
            JOIN f.registration r
            WHERE r.event.id = :eventId
            """)
    List<Object[]> findTotalEntriesAndAverageRatingByEventId(@Param("eventId") Integer eventId);

    /**
     * Retrieves the distribution of ratings for a given event.
     *
     * @param eventId The ID of the event.
     * @return A List of Object arrays, where each array contains:
     *         Object[0]: The rating (Integer).
     *         Object[1]: The count of feedback entries for that rating (Long).
     */
    @Query("""
            SELECT f.rating, COUNT(f.id)
            FROM Feedback f
            JOIN f.registration r
            WHERE r.event.id = :eventId
            GROUP BY f.rating
            ORDER BY f.rating ASC
            """)
    List<Object[]> findRatingsDistributionByEventId(@Param("eventId") Integer eventId);

    /**
     * Retrieves comments for each rating for a given event, ordered by generation
     * date (latest first).
     * This query fetches all relevant comments, and the service layer will then
     * be responsible for grouping them by rating and applying any limits (e.g.,
     * "latest few comments").
     *
     * @param eventId The ID of the event.
     * @return A List of Object arrays, where each array contains:
     *         Object[0]: The rating (Integer).
     *         Object[1]: The comment text (String).
     */
    @Query("""
            SELECT f.rating, f.comment
            FROM Feedback f
            JOIN f.registration r
            WHERE r.event.id = :eventId
            ORDER BY f.submittedAt DESC, f.id DESC
            """)
    List<Object[]> findCommentsByEventId(@Param("eventId") Integer eventId);

     @Query("SELECT AVG(f.rating) FROM Feedback f JOIN f.registration r WHERE r.event.id = :eventId")
    Double findAverageRatingByEventId(@Param("eventId") Integer eventId);

    @Query("SELECT COUNT(f) FROM Feedback f JOIN f.registration r WHERE r.event.id = :eventId")
    long countByEventId(@Param("eventId") Integer eventId);
}
