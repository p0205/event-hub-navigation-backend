package com.utem.event_hub_navigation.repo;

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

}
