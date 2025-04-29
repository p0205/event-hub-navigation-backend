package com.utem.event_hub_navigation.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.utem.event_hub_navigation.dto.QRPayload;
import com.utem.event_hub_navigation.model.Attendance;
import com.utem.event_hub_navigation.model.AttendanceKey;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventVenue;
import com.utem.event_hub_navigation.model.Registration;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.repo.AttendanceRepo;
import com.utem.event_hub_navigation.repo.EventRepo;
import com.utem.event_hub_navigation.repo.EventVenueRepo;
import com.utem.event_hub_navigation.repo.RegistrationRepo;
import com.utem.event_hub_navigation.repo.UserRepo;
import com.utem.event_hub_navigation.utils.QRCodeUtil;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AttendanceService {

    private EventVenueRepo eventVenueRepo;

    private QRCodeUtil qrCodeGenerator;

    private AttendanceRepo attendanceRepo;

    private RegistrationRepo registrationRepo;

    private EventRepo eventRepo;

    private UserRepo userRepo;

    @Autowired
    public AttendanceService(EventVenueRepo eventVenueRepo, QRCodeUtil qrCodeGenerator, AttendanceRepo attendanceRepo,
            RegistrationRepo registrationRepo, EventRepo eventRepo, UserRepo userRepo) {
        this.eventVenueRepo = eventVenueRepo;
        this.qrCodeGenerator = qrCodeGenerator;
        this.attendanceRepo = attendanceRepo;
        this.registrationRepo = registrationRepo;
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
    }

    public byte[] generateAndSaveQRCode(Integer eventId, Integer eventVenueId, LocalDateTime expiresAt)
            throws Exception {
        EventVenue eventVenue = eventVenueRepo.findById(eventVenueId)
                .orElseThrow(() -> new EntityNotFoundException("EventVenue not found with ID: " + eventVenueId));

        // If QR code already exists, reuse (optional)
        if (eventVenue.getQrCodeImage() != null) {
            return eventVenue.getQrCodeImage();
        }

        if (expiresAt == null) {
            expiresAt = eventVenue.getEndDateTime();
        }
        // Else generate new QR
        String qrContent = qrCodeGenerator.createPayload(eventId, eventVenueId, expiresAt); // customize as needed
        byte[] qrCode = qrCodeGenerator.generateQRCodeImage(qrContent, 300, 300);

        eventVenue.setQrCodeImage(qrCode);
        eventVenueRepo.save(eventVenue);

        return qrCode;
    }

    public byte[] downloadQRCode(Integer evenrVenueId) {
        Optional<EventVenue> eventVenueOp = eventVenueRepo.findById(evenrVenueId);
        if (eventVenueOp.isPresent()) {
            EventVenue eventVenue = eventVenueOp.get();
            return eventVenue.getQrCodeImage();
        }
        return null;
    }

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

        EventVenue eventVenue = eventVenueRepo.findById(qrPayload.getEventVenueId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "EventVenue not found with ID: " + qrPayload.getEventVenueId()));

        if (attendanceRepo.existsByEventVenueAndRegistration(eventVenue, registration))
            return "Already Check-In";
            
        AttendanceKey key = AttendanceKey.builder()
                .eventVenueId(qrPayload.getEventVenueId())
                .registrationId(registration.getId())
                .build();

        Attendance attendance = Attendance.builder()
                .id(key)
                .eventVenue(eventVenue)
                .registration(registration)
                .checkinDateTime(LocalDateTime.now())
                .build();

        attendanceRepo.save(attendance);
        return "Check In Successfully";

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
