package com.utem.event_hub_navigation.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.TabSettings;
import com.itextpdf.text.TabStop;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.utem.event_hub_navigation.dto.EventAttendanceReportDTO;
import com.utem.event_hub_navigation.dto.SessionAttendanceDTO;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventReport;
import com.utem.event_hub_navigation.model.ReportType;
import com.utem.event_hub_navigation.model.Session;
import com.utem.event_hub_navigation.repo.AttendanceRepo;
import com.utem.event_hub_navigation.repo.EventRepo;
import com.utem.event_hub_navigation.repo.EventReportRepo;
import com.utem.event_hub_navigation.repo.RegistrationRepo;
import com.utem.event_hub_navigation.repo.SessionRepo;
import com.utem.event_hub_navigation.service.EventReportService;
import com.utem.event_hub_navigation.utils.DateHelper;
import com.utem.event_hub_navigation.utils.PieChartGenerator;
import com.utem.event_hub_navigation.utils.SupabaseStorageService;

@Service
public class EventReportServiceImpl implements EventReportService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font SUBHEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
    private static final float HEADER_SPACING = 20f;

    private EventRepo eventRepo;

    private RegistrationRepo registrationRepo;

    private AttendanceRepo attendanceRepo;

    private SessionRepo sessionRepo;

    private SupabaseStorageService supabaseStorageService;

    private EventReportRepo eventReportRepo;

    @Autowired
    public EventReportServiceImpl(EventRepo eventRepository, RegistrationRepo registrationRepository,
            AttendanceRepo attendanceRepository, SessionRepo sessionRepository,
            SupabaseStorageService supabaseStorageService, EventReportRepo eventReportRepo) {
        this.eventRepo = eventRepository;
        this.registrationRepo = registrationRepository;
        this.sessionRepo = sessionRepository;
        this.attendanceRepo = attendanceRepository;
        this.supabaseStorageService = supabaseStorageService;
        this.eventReportRepo = eventReportRepo;
    }

    @Override
    public List<EventReport> getEventReport(Integer eventId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event with ID " + eventId + " not found."));

        return eventReportRepo.findByEvent(event);

    }

    @Override
    public void storeReport(Integer eventId, ReportType reportType) throws IOException {
        // Generate the report as a byte array
        byte[] reportBytes = generateEventAttendanceReport(eventId);

        // Generate a unique, sanitized filename
        String filename = UUID.randomUUID() + "_EventAttendanceReport_" + eventId + ".pdf";

        // Upload the file to Supabase
        String fileUrl = supabaseStorageService.uploadFile(reportBytes, "event-report", filename);

        // Fetch the event and ensure it exists
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event with ID " + eventId + " not found."));

        // Build and save the report
        EventReport report = EventReport.builder()
                .event(event)
                .type(reportType)
                .fileUrl(fileUrl)
                .generatedAt(LocalDateTime.now())
                .build();

        eventReportRepo.save(report);
        System.out.println("Report stored successfully: " + fileUrl);
    }

    public byte[] generateEventAttendanceReport(Integer eventId) {
        // The existing logic to fetch data and populate EventAttendanceReportDTO
        EventAttendanceReportDTO report = getAttendanceReportData(eventId);
        // Now, generate the report in PDF
        try {
            return generateAttendanceReportPDF(report, "EventAttendanceReport_" + eventId + ".pdf");
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF report " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF report " + e.toString());
        }

    }

    public byte[] generateAttendanceReportPDF(EventAttendanceReportDTO report, String filename)
            throws DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, byteArrayOutputStream);

        document.open();

        // Report generation date header
        Paragraph generatedDateTime = new Paragraph(
                "Report Generated On: " + DateHelper.formatHumanReadableDateTime(report.getReportGenerationDate()),
                SMALL_FONT);
        generatedDateTime.setAlignment(Element.ALIGN_RIGHT);
        document.add(generatedDateTime);

        // Event Name
        Paragraph eventName = new Paragraph(report.getEventName(), TITLE_FONT);

        eventName.setAlignment(Element.ALIGN_CENTER);
        document.add(eventName);

        // Event Type
        Paragraph title = new Paragraph("Event Attendance Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15f);
        document.add(title);

        // Overview section
        // Header
        Paragraph overviewHeader = new Paragraph("Overview", HEADER_FONT);
        overviewHeader.setSpacingAfter(HEADER_SPACING);
        document.add(overviewHeader);

        // overview content
        addKeyValueLine(document, "Event Duration", DateHelper.formatDate(report.getEventStartDateTime()) + " - " + DateHelper.formatDate(report.getEventEndDateTime()), 0);
        addKeyValueLine(document, "Organizer", report.getEventName(), 0);
        addKeyValueLine(document, "Total Expected Participants", String.valueOf(report.getTotalExpectedParticipants()),
                0);
        addKeyValueLine(document, "Total Registered Participants",
                String.valueOf(report.getTotalRegisteredParticipants()), 0);
        addKeyValueLine(document, "Registration Fill Rate", String.format("%.2f", report.getRegistrationFillRate()+"%"), 0);
        document.add(Chunk.NEWLINE);
        document.add(new LineSeparator());

        // Session-Specific Attendance section
        Paragraph sessionHeader = new Paragraph("Session-Specific Attendance", HEADER_FONT);
        sessionHeader.setSpacingBefore(HEADER_SPACING);
        sessionHeader.setSpacingAfter(HEADER_SPACING);
        document.add(sessionHeader);

        System.out.println(report.getSessionAttendances().toString());
        // Session Info
        for (SessionAttendanceDTO session : report.getSessionAttendances()) {
            // Session Name
            Paragraph sessionName = new Paragraph(session.getSessionName(), SUBHEADER_FONT);
            sessionName.setSpacingAfter(5f);
            document.add(sessionName);

            // Session Details
            addKeyValueLine(document, "Start Date",
                    DateHelper.formatHumanReadableDateTime(session.getSessionStartDate()), 20f);
            addKeyValueLine(document, "End Date", DateHelper.formatHumanReadableDateTime(session.getSessionEndDate()),
                    20f);
            addKeyValueLine(document, "Total Attendees", String.valueOf(session.getTotalAttendees()), 20f);
            addKeyValueLine(document, "Session Attendance Rate", session.getSessionAttendanceRate() + "%", 20f);

        }
        document.add(Chunk.NEWLINE);
        document.add(new LineSeparator());

        // Demographic Section
        // Header
        Paragraph demographicHeader = new Paragraph("Participants Demographics", HEADER_FONT);
        demographicHeader.setSpacingBefore(HEADER_SPACING);
        demographicHeader.setSpacingAfter(HEADER_SPACING);
        document.add(demographicHeader);

        // Graph
        // Demographics Charts
        if (report.getDemographicData() != null) {
            try {

                Image facultyChart = PieChartGenerator.generatePieChartImage("Faculty Distribution",
                        report.getDemographicData().get("Faculty"));
                Image courseChart = PieChartGenerator.generatePieChartImage("Course Distribution",
                        report.getDemographicData().get("Course"));
                Image yearChart = PieChartGenerator.generatePieChartImage("Year Distribution",
                        report.getDemographicData().get("Year"));
                Image genderChart = PieChartGenerator.generatePieChartImage("Gender Distribution",
                        report.getDemographicData().get("Gender"));
                // Faculty Chart
                document.add(facultyChart);
                document.add(Chunk.NEWLINE);

                // Course Chart
                document.add(courseChart);
                document.add(Chunk.NEWLINE);

                // Year Chart
                document.add(yearChart);
                document.add(Chunk.NEWLINE);

                // Gender Chart
                document.add(genderChart);
                document.add(Chunk.NEWLINE);

            } catch (Exception e) {
                e.printStackTrace();
                throw new DocumentException("Error generating Gender Distribution chart");
            }

        }

        document.close();

        return byteArrayOutputStream.toByteArray();
    }

    // private Image generatePieChartImage(String title, DemographicDataDTO
    // demographicDataDTO) throws Exception {
    // DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
    // for (DemographicDataRow row : demographicDataDTO.getData()) {
    // dataset.setValue(row.getValue(), row.getCount());
    // }

    // // 2. Create the chart
    // JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true,
    // false);

    // // 3. Convert the chart to a PNG image
    // ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
    // EncoderUtil.writeBufferedImage(chart.createBufferedImage(500, 300),
    // ImageFormat.PNG, chartOutputStream);

    // // 4. Convert to iText Image
    // Image chartImage = Image.getInstance(chartOutputStream.toByteArray());
    // chartImage.setAlignment(Element.ALIGN_CENTER);
    // chartImage.setSpacingBefore(10f);
    // chartImage.setSpacingAfter(10f);

    // return chartImage;
    // }

    // Add Information Key-value pair in pdf
    private void addKeyValueLine(Document document, String key, String value, float leftIndentation)
            throws DocumentException {
        float tabPosition = 200f;
        TabStop tabStop = new TabStop(tabPosition, TabStop.Alignment.LEFT);
        TabSettings tabSettings = new TabSettings(Arrays.asList(tabStop), 50f);

        Paragraph paragraph = new Paragraph();
        paragraph.setIndentationLeft(leftIndentation);
        paragraph.setTabSettings(tabSettings); // Apply the tab settings
        paragraph.add(new Chunk(key, NORMAL_FONT)); // Add the key
        paragraph.add(Chunk.TABBING); // Insert a tab to move to the defined tab stop
        paragraph.add(new Chunk(" : " + value, NORMAL_FONT)); // Add the value
        paragraph.setSpacingAfter(5f); // Add some spacing after the line
        document.add(paragraph);
    }

    // private void addTableHeader(PdfPTable table, String headerTitle) {
    // PdfPCell header = new PdfPCell();
    // header.setBackgroundColor(BaseColor.LIGHT_GRAY);
    // header.setBorderWidth(2);
    // header.setPhrase(new Phrase(headerTitle, SUBHEADER_FONT));
    // table.addCell(header);
    // }

    public EventAttendanceReportDTO getAttendanceReportData(Integer eventId) {
        EventAttendanceReportDTO report = new EventAttendanceReportDTO();

        // Fetch event details
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        report.setEventName(event.getName());
        report.setOrganizerName(event.getOrganizer().getName());
        report.setEventStartDateTime(event.getStartDateTime());
        report.setEventEndDateTime(event.getEndDateTime());
        report.setReportGenerationDate(LocalDateTime.now());

        // Fetch registrations for event
        int totalRegisteredParticipants = registrationRepo.countByEventId(eventId);
        int expectedParticipants = event.getParticipantsNo();
        report.setTotalRegisteredParticipants(totalRegisteredParticipants);
        report.setTotalExpectedParticipants(expectedParticipants);

        // Calculate Registration Fill Rate
        double registrationFillRate = (double) totalRegisteredParticipants / expectedParticipants * 100;
        report.setRegistrationFillRate(registrationFillRate);

        // Calculate Session-Specific Attendance Rates
        List<Session> sessions = sessionRepo.findByEvent(event);
        for (Session session : sessions) {
            int totalAttendeesForSession = attendanceRepo.countBySessionId(session.getId());
            double sessionAttendanceRate = (double) totalAttendeesForSession / totalRegisteredParticipants * 100;
            report.addSessionAttendance(session.getSessionName(), session.getStartDateTime(), session.getEndDateTime(),
                    totalAttendeesForSession, sessionAttendanceRate);
        }

        // Fetch Demographic Data (Gender and Faculty)

        Map<String, Map<String, Long>> demographicData = new HashMap<>();

        Map<String, Long> facultyData = convertGraphDataType(
                registrationRepo.getDemographicDataGroupByFaculty(eventId));
        demographicData.put("Faculty", facultyData);

        Map<String, Long> courseData = convertGraphDataType(registrationRepo.getDemographicDataGroupByCourse(eventId));
        demographicData.put("Course", courseData);

        Map<String, Long> rawYearData = convertGraphDataType(registrationRepo.getDemographicDataGroupByYear(eventId));
        Map<String, Long> yearData = rawYearData.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> "Year " + entry.getKey(),
                        Map.Entry::getValue));
        demographicData.put("Year", yearData);

        Map<String, Long> genderData = convertGraphDataType(registrationRepo.getDemographicDataGroupByGender(eventId));
        demographicData.put("Gender", genderData);

        report.setDemographicData(demographicData);

        System.out.println(report.getDemographicData().toString());
        return report;
    }

    public Map<String, Long> convertGraphDataType(List<Object[]> data) {
        return data
                .stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
    }
}
