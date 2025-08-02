package com.utem.event_hub_navigation.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.utem.event_hub_navigation.dto.Attendee;

public interface AttendanceService {

    byte[] generateAndSaveQRCode(Integer eventId, Integer sessionId, LocalDateTime expiresAt)
            throws Exception;

    byte[] downloadQRCode(Integer evenrVenueId);

    String checkIn(String payload, String email) throws Exception;

    Page<Attendee> getCheckInParticipants(Integer sessionId, Pageable pageable);

    public byte[] exportAttendanceXLSX(Integer sessionId) throws IOException ;
}