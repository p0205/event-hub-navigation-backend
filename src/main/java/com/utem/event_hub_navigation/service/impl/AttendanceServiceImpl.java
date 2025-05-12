package com.utem.event_hub_navigation.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.utem.event_hub_navigation.dto.Attendee;
import com.utem.event_hub_navigation.dto.QRPayload;
import com.utem.event_hub_navigation.model.Attendance;
import com.utem.event_hub_navigation.model.AttendanceKey;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.Session;
import com.utem.event_hub_navigation.model.Registration;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.repo.AttendanceRepo;
import com.utem.event_hub_navigation.repo.EventRepo;
import com.utem.event_hub_navigation.repo.SessionRepo;
import com.utem.event_hub_navigation.repo.RegistrationRepo;
import com.utem.event_hub_navigation.repo.UserRepo;
import com.utem.event_hub_navigation.service.AttendanceService;
import com.utem.event_hub_navigation.utils.QRCodeUtil;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private SessionRepo sessionRepo;

    private QRCodeUtil qrCodeGenerator;

    private AttendanceRepo attendanceRepo;

    private RegistrationRepo registrationRepo;

    private EventRepo eventRepo;

    private UserRepo userRepo;


    @Autowired
    public AttendanceServiceImpl(SessionRepo sessionRepo, QRCodeUtil qrCodeGenerator, AttendanceRepo attendanceRepo,
            RegistrationRepo registrationRepo, EventRepo eventRepo, UserRepo userRepo) {
        this.sessionRepo = sessionRepo;
        this.qrCodeGenerator = qrCodeGenerator;
        this.attendanceRepo = attendanceRepo;
        this.registrationRepo = registrationRepo;
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
    }

    @Override
    public byte[] generateAndSaveQRCode(Integer eventId, Integer sessionId, LocalDateTime expiresAt)
            throws Exception {
        Session session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with ID: " + sessionId));

        // If QR code already exists, reuse 
        if (session.getQrCodeImage() != null) {
            return session.getQrCodeImage();
        }

        if (expiresAt == null) {
            expiresAt = session.getEndDateTime();
        }
        // Else generate new QR
        String qrContent = qrCodeGenerator.createPayload(eventId, sessionId, expiresAt); // customize as needed
        byte[] qrCode = qrCodeGenerator.generateQRCodeImage(qrContent, 300, 300);

        session.setQrCodeImage(qrCode);
        sessionRepo.save(session);

        return qrCode;
    }

    @Override
    public byte[] downloadQRCode(Integer sessionId) {
        Optional<Session> sessionOp = sessionRepo.findById(sessionId);
        if (sessionOp.isPresent()) {
            Session session = sessionOp.get();
            return session.getQrCodeImage();
        }
        return null;
    }

    @Override
    public String checkIn(String payload, Integer participantId) throws Exception {

        QRPayload qrPayload = qrCodeGenerator.validateQRCode(payload);

        Event event = eventRepo.findById(qrPayload.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Event not found with ID: " + qrPayload.getEventId()));

        User participant = userRepo.findById(participantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participants User not found with ID: " + participantId));

        Registration registration = registrationRepo.findByEventAndParticipant(event, participant);
        if (registration == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Registration not found");
        }

        Session session = sessionRepo.findById(qrPayload.getSessionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Session not found with ID: " + qrPayload.getSessionId()));

        if (attendanceRepo.existsBysessionAndRegistration(session, registration))
            return "Already Check-In";

        AttendanceKey key = AttendanceKey.builder()
                .sessionId(qrPayload.getSessionId())
                .registrationId(registration.getId())
                .build();

        Attendance attendance = Attendance.builder()
                .id(key)
                .session(session)
                .registration(registration)
                .checkinDateTime(LocalDateTime.now())
                .build();

        attendanceRepo.save(attendance);
        return "Check In Successfully";

    }

    @Override
    public Page<Attendee> getCheckInParticipants(Integer sessionId,Pageable pageable) {
        Page<Attendee> attendees = attendanceRepo.findCheckInParticipantsBySession(sessionId,pageable);
        
        return attendees;
    }
}

// public String markAttendance(Integer userId, String qrData) {
// // Parse QR code: "eventId:12345;timestamp:20250413T100000"
// String[] parts = qrData.split(";");
// Integer eventId = Integer.parseInt(parts[0].split(":")[1]);

// Registration registration =
// registrationRepo.findByEventIdAndParticipantId(eventId, userId);
// // Check if already marked
// if (attendanceRepo.existsByRegistrationId(registration.getId())) {
// return "Already marked";
// }

// // Save attendance
// Attendance attendance = new Attendance();
// attendance.setRegistration(registration);
// attendance.setCheckInDateTime(LocalDateTime.now());

// attendanceRepo.save(attendance);
// return "Attendance marked";
// }
