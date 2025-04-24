package com.utem.event_hub_navigation.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.model.Attendance;
import com.utem.event_hub_navigation.model.Registration;
import com.utem.event_hub_navigation.repo.AttendanceRepo;
import com.utem.event_hub_navigation.repo.RegistrationRepo;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepo attendanceRepo;

    @Autowired
    private RegistrationRepo registrationRepo;

    // public String markAttendance(Integer userId, String qrData) {
    //     // Parse QR code: "eventId:12345;timestamp:20250413T100000"
    //     String[] parts = qrData.split(";");
    //     Integer eventId = Integer.parseInt(parts[0].split(":")[1]);

    //     Registration registration = registrationRepo.findByEventIdAndParticipantId(eventId, userId);
    //     // Check if already marked
    //     if (attendanceRepo.existsByRegistrationId(registration.getId())) {
    //         return "Already marked";
    //     }

    //     // Save attendance
    //     Attendance attendance = new Attendance();
    //     attendance.setRegistration(registration);
    //     attendance.setCheckInDateTime(LocalDateTime.now());


    //     attendanceRepo.save(attendance);
    //     return "Attendance marked";
    // }
}
