package com.utem.event_hub_navigation.dto;

// Import necessary types, e.g., for checkinDateTime
import java.time.LocalDateTime;
// Or java.util.Date, java.sql.Timestamp depending on your DB and mapping

public interface Attendee {

    // Getter methods must match the aliases in the native query SELECT clause
    Integer getUserId();
    String getName();
    String getEmail();
    String getPhoneNo();
    String getGender();
    String getFaculty();
    String getCourse();
    Integer getYear();
    LocalDateTime getCheckinDateTime(); // Make sure the type matches what JPA/Hibernate retrieves

    // You can add default methods or other interface features if needed
}