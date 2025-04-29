package com.utem.event_hub_navigation.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.model.Attendance;
import com.utem.event_hub_navigation.model.EventVenue;
import com.utem.event_hub_navigation.model.Registration;
import com.utem.event_hub_navigation.repo.AttendanceRepo;
import com.utem.event_hub_navigation.repo.EventVenueRepo;
import com.utem.event_hub_navigation.repo.RegistrationRepo;
import com.utem.event_hub_navigation.utils.QRCodeGenerator;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AttendanceService {

    private EventVenueRepo eventVenueRepo;

    private QRCodeGenerator qrCodeGenerator;

    @Autowired
    public AttendanceService(EventVenueRepo eventVenueRepo, QRCodeGenerator qrCodeGenerator) {
        this.eventVenueRepo = eventVenueRepo;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    public byte[] generateAndSaveQRCode(Integer eventId, Integer eventVenueId, LocalDateTime expiresAt) throws Exception {
        EventVenue eventVenue = eventVenueRepo.findById(eventVenueId)
                .orElseThrow(() -> new EntityNotFoundException("EventVenue not found with ID: " + eventVenueId));


        // If QR code already exists, reuse (optional)
        if (eventVenue.getQrCodeImage() != null) {
            return eventVenue.getQrCodeImage();
        }

        if(expiresAt == null){
            expiresAt = eventVenue.getEndDateTime();
        }
        // Else generate new QR
        String qrContent = qrCodeGenerator.createPayload(eventId, eventVenueId,expiresAt); // customize as needed
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
}
