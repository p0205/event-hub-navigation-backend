package com.utem.event_hub_navigation.service;

import java.time.LocalDateTime;
import java.util.List;

import com.utem.event_hub_navigation.dto.UserDTO;

public interface AttendanceService {

    byte[] generateAndSaveQRCode(Integer eventId, Integer sessionId, LocalDateTime expiresAt)
            throws Exception;

    byte[] downloadQRCode(Integer evenrVenueId);

    String checkIn(String payload, Integer participantId) throws Exception;

    List<UserDTO> getCheckInParticipants(Integer sessionId);

}