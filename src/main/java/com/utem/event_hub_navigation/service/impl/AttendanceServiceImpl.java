package com.utem.event_hub_navigation.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.utem.event_hub_navigation.dto.Attendee;
import com.utem.event_hub_navigation.dto.CheckInRequest;
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
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

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

//     @Override
//     public String checkIn(String payload, String email) throws Exception {
// System.out.println("Inside checkIn. Email is: " + email);
//         // QRPayload qrPayload = qrCodeGenerator.validateQRCode(payload);
//         QRPayload qrPayload = qrCodeGenerator.validateJSONQRCode(payload);

//         Event event = eventRepo.findById(qrPayload.getEventId())
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
//                         "Event not found with ID: " + qrPayload.getEventId()));

//         User participant = userRepo.findByEmail(email)
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
//                         "Participants Users not found with ID: " + email));

//         Registration registration = registrationRepo.findByEventAndParticipant(event, participant);
//         if (registration == null) {
//             throw new ResponseStatusException(HttpStatus.NOT_FOUND,
//                     "Registration not found");
//         }

//         Session session = sessionRepo.findById(qrPayload.getSessionId())
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
//                         "Session not found with ID: " + qrPayload.getSessionId()));

//         if (attendanceRepo.existsBysessionAndRegistration(session, registration))
//             return "Already Check-In";

//         AttendanceKey key = AttendanceKey.builder()
//                 .sessionId(qrPayload.getSessionId())
//                 .registrationId(registration.getId())
//                 .build();

//         Attendance attendance = Attendance.builder()
//                 .id(key)
//                 .session(session)
//                 .registration(registration)
//                 .checkinDateTime(LocalDateTime.now())
//                 .build();

//         attendanceRepo.save(attendance);
//         return "Check In Successfully";

//     }

    @Override
    public String checkInById(CheckInRequest request) throws Exception {
        System.out.println("Inside checkInById. Participant ID is: " + request.getParticipantId());

        QRPayload qrPayload = qrCodeGenerator.validateJSONQRCode(request.getQrCodePayload());

        Event event = eventRepo.findById(qrPayload.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Event not found with ID: " + qrPayload.getEventId()));

        User participant = userRepo.findById(request.getParticipantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participants Users not found with ID: " + request.getParticipantId()));

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
    public Page<Attendee> getCheckInParticipants(Integer sessionId, Pageable pageable) {
        Page<Attendee> attendees = attendanceRepo.findCheckInParticipantsBySession(sessionId, pageable);

        return attendees;
    }

   

    /**
     * Exports attendance data for a given event and session as an XLSX byte array.
     * In a real application, this would fetch data from a database.
     *
     * @param eventId The ID of the event.
     * @param sessionId The ID of the session.
     * @return A byte array containing the XLSX data.
     * @throws IOException If an I/O error occurs during XLSX generation.
     */
    public byte[] exportAttendanceXLSX(Integer sessionId) throws IOException {
        List<Attendee> attendanceRecords = attendanceRepo.findCheckInParticipantsBySession(sessionId);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Attendance");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"No", "Name", "Email", "Contact Number", "Faculty", "Course", "Year", "Check In Time"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Populate data rows
            int rowNum = 1;
            for (Attendee record : attendanceRecords) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum-1);
                row.createCell(1).setCellValue(record.getName());
                row.createCell(2).setCellValue(record.getEmail());
                row.createCell(3).setCellValue(record.getPhoneNo());
                row.createCell(4).setCellValue(record.getFaculty());
                row.createCell(5).setCellValue(record.getCourse());
                row.createCell(6).setCellValue(record.getYear());
                row.createCell(7).setCellValue(record.getCheckinDateTime().format(DATE_TIME_FORMATTER));
            }

            // Auto-size columns for better readability (optional, can be performance intensive for very large datasets)
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        }
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
