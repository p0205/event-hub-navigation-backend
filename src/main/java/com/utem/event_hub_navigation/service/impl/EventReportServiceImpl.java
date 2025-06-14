package com.utem.event_hub_navigation.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.utem.event_hub_navigation.dto.AttendanceReportOverview;
import com.utem.event_hub_navigation.dto.BudgetReportOverview;
import com.utem.event_hub_navigation.dto.EventAttendanceReportDTO;
import com.utem.event_hub_navigation.dto.EventBudgetDTO;
import com.utem.event_hub_navigation.dto.EventBudgetReportDTO;
import com.utem.event_hub_navigation.dto.EventFeedbackReportDTO;
import com.utem.event_hub_navigation.dto.EventReportOverviewDTO;
import com.utem.event_hub_navigation.dto.FeedbackReportOveriew;
import com.utem.event_hub_navigation.dto.SessionAttendanceDTO;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventReport;
import com.utem.event_hub_navigation.model.ReportType;
import com.utem.event_hub_navigation.model.Session;
import com.utem.event_hub_navigation.repo.AttendanceRepo;
import com.utem.event_hub_navigation.repo.EventRepo;
import com.utem.event_hub_navigation.repo.EventReportRepo;
import com.utem.event_hub_navigation.repo.FeedbackRepo;
import com.utem.event_hub_navigation.repo.RegistrationRepo;
import com.utem.event_hub_navigation.repo.SessionRepo;
import com.utem.event_hub_navigation.service.EventBudgetService;
import com.utem.event_hub_navigation.service.EventReportService;
import com.utem.event_hub_navigation.utils.DateHelper;
import com.utem.event_hub_navigation.utils.ReportGeneratorUtils;
import com.utem.event_hub_navigation.utils.SupabaseStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventReportServiceImpl implements EventReportService {

        private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD);
        private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        private static final Font SUBHEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,
                        BaseColor.DARK_GRAY);
        private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
        private static final Font DESCRIPTION_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC,
                        BaseColor.GRAY);
        private static final Font COMMENT_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL,
                        BaseColor.DARK_GRAY);
        // ADD THIS NEW FONT
        // private static Font UNICODE_STAR_FONT= new Font(Font.FontFamily.COURIER, 10,
        // Font.NORMAL,
        // BaseColor.DARK_GRAY);

        private static final float HEADER_SPACING = 20f;

        private EventRepo eventRepo;

        private RegistrationRepo registrationRepo;

        private AttendanceRepo attendanceRepo;

        private SessionRepo sessionRepo;

        private SupabaseStorageService supabaseStorageService;

        private EventReportRepo eventReportRepo;

        private FeedbackRepo feedbackRepo;

        private EventBudgetService eventBudgetService;

        @Autowired
        public EventReportServiceImpl(EventRepo eventRepository, RegistrationRepo registrationRepository,
                        AttendanceRepo attendanceRepository, SessionRepo sessionRepository,
                        SupabaseStorageService supabaseStorageService, EventReportRepo eventReportRepo,
                        FeedbackRepo feedbackRepo,
                        EventBudgetService eventBudgetService) {
                this.eventRepo = eventRepository;
                this.registrationRepo = registrationRepository;
                this.sessionRepo = sessionRepository;
                this.attendanceRepo = attendanceRepository;
                this.supabaseStorageService = supabaseStorageService;
                this.eventReportRepo = eventReportRepo;
                this.feedbackRepo = feedbackRepo;
                this.eventBudgetService = eventBudgetService;
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
                byte[] reportBytes;
                String filename = "";
                if (reportType == ReportType.ATTENDANCE) {
                        reportBytes = generateEventAttendanceReport(eventId);
                        filename = UUID.randomUUID() + "_EventAttendanceReport_" + eventId + ".pdf";

                } else if (reportType == ReportType.BUDGET) {
                        reportBytes = generateEventBudgetReport(eventId);
                        filename = UUID.randomUUID() + "_EventBudgetReport_" + eventId + ".pdf";

                } else {
                        throw new IllegalArgumentException("Unsupported report type: " + reportType);
                }
                // Generate a unique, sanitized filename

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
                        throw new RuntimeException("Error generating PDF report :DocumentException" + e.toString());
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
                                "Report Generated On: " + DateHelper
                                                .formatHumanReadableDateTime(report.getReportGenerationDate()),
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
                Paragraph overviewHeader = new Paragraph("Event Overview", HEADER_FONT);
                overviewHeader.setSpacingAfter(HEADER_SPACING);
                document.add(overviewHeader);

                // overview content
                ReportGeneratorUtils.addKeyValueLine(document, "Event Duration",
                                DateHelper.formatDate(report.getEventStartDateTime())
                                                + " - " + DateHelper.formatDate(report.getEventEndDateTime()),
                                0);
                ReportGeneratorUtils.addKeyValueLine(document, "Organizer", report.getOrganizerName(), 0);
                ReportGeneratorUtils.addKeyValueLine(document, "Total Expected Participants",
                                String.valueOf(report.getTotalExpectedParticipants()),
                                0);
                ReportGeneratorUtils.addKeyValueLine(document, "Total Registered Participants",
                                String.valueOf(report.getTotalRegisteredParticipants()), 0);

                // FIX for the Attendance Report: Use correct String.format
                ReportGeneratorUtils.addKeyValueLine(document, "Registration Fill Rate",
                                String.format("%.2f%%", report.getRegistrationFillRate()), 0);

                document.add(Chunk.NEWLINE);
                document.add(new LineSeparator());

                // Session-Specific Attendance section
                Paragraph sessionHeader = new Paragraph("Session-Specific Attendance", HEADER_FONT);
                sessionHeader.setSpacingBefore(HEADER_SPACING);
                sessionHeader.setSpacingAfter(HEADER_SPACING);
                document.add(sessionHeader);

                // System.out.println(report.getSessionAttendances().toString());
                // Session Info
                for (SessionAttendanceDTO session : report.getSessionAttendances()) {
                        // Session Name
                        Paragraph sessionName = new Paragraph(session.getSessionName(), SUBHEADER_FONT);
                        sessionName.setSpacingAfter(5f);
                        document.add(sessionName);

                        // Session Details
                        ReportGeneratorUtils.addKeyValueLine(document, "Start Date and Time",
                                        DateHelper.formatHumanReadableDateTime(session.getSessionStartDate()), 20f);
                        ReportGeneratorUtils.addKeyValueLine(document, "End Date and Time",
                                        DateHelper.formatHumanReadableDateTime(session.getSessionEndDate()),
                                        20f);
                        ReportGeneratorUtils.addKeyValueLine(document, "Total Attendees",
                                        String.valueOf(session.getTotalAttendees()), 20f);

                        // This line also likely needs String.format if sessionAttendanceRate is a
                        // double
                        ReportGeneratorUtils.addKeyValueLine(document, "Session Attendance Rate",
                                        String.format("%.2f%%", session.getSessionAttendanceRate()),
                                        20f);

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

                                Image facultyChart = ReportGeneratorUtils.generatePieChartImage("Faculty Distribution",
                                                report.getDemographicData().get("Faculty"));
                                Image courseChart = ReportGeneratorUtils.generatePieChartImage("Course Distribution",
                                                report.getDemographicData().get("Course"));
                                Image yearChart = ReportGeneratorUtils.generatePieChartImage("Year Distribution",
                                                report.getDemographicData().get("Year"));
                                Image genderChart = ReportGeneratorUtils.generatePieChartImage("Gender Distribution",
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

        public byte[] generateEventBudgetReport(Integer eventId) {
                // You are currently calling getAttendanceReportData here.
                // It should be getBudgetReportData for a budget report.
                EventBudgetReportDTO report = getBudgetReportData(eventId);
                // Now, generate the report in PDF
                try {
                        return generateBudgetReportPDF(report, "EventBudgetReport_" + eventId + ".pdf");
                } catch (DocumentException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error generating PDF report " + e.toString());
                } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error generating PDF report " + e.toString());
                }

        }

        public byte[] saveEventFeedbackReport(Integer eventId, Integer commentsLimit) {
                // You are currently calling getAttendanceReportData here.
                // It should be getBudgetReportData for a budget report.
                EventFeedbackReportDTO reportData = getFeedbackReportData(eventId, commentsLimit);

                // Now, generate the report in PDF
                try {
                        String filename = UUID.randomUUID() + "EventFeedbackReport_" + eventId + ".pdf";

                        return generateFeedbackReportPDF(reportData,
                                        "EventFeedbackReport_" + eventId + ".pdf",
                                        commentsLimit);

                        // Generate a unique, sanitized filename

                        // Upload the file to Supabase
                        // String fileUrl = supabaseStorageService.uploadFile(reportBytes,
                        // "event-report", filename);

                        // // Fetch the event and ensure it exists
                        // Event event = eventRepo.findById(eventId)
                        // .orElseThrow(() -> new RuntimeException(
                        // "Event with ID " + eventId + " not found."));

                        // // Build and save the report
                        // EventReport report = EventReport.builder()
                        // .event(event)
                        // .type(ReportType.FEEDBACK)
                        // .fileUrl(fileUrl)
                        // .generatedAt(LocalDateTime.now())
                        // .build();

                        // eventReportRepo.save(report);
                } catch (DocumentException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error generating PDF report " + e.toString());
                } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error generating PDF report " + e.toString());
                }

        }

        public byte[] generateBudgetReportPDF(EventBudgetReportDTO report, String filename)
                        throws DocumentException {
                Document document = new Document(PageSize.A4, 50, 50, 50, 50);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, byteArrayOutputStream);

                document.open();

                // Report generation date header
                Paragraph generatedDateTime = new Paragraph(
                                "Report Generated On: " + DateHelper
                                                .formatHumanReadableDateTime(report.getReportGenerationDate()),
                                SMALL_FONT);
                generatedDateTime.setAlignment(Element.ALIGN_RIGHT);
                document.add(generatedDateTime);

                // Event Name
                Paragraph eventName = new Paragraph(report.getEventName(), TITLE_FONT);

                eventName.setAlignment(Element.ALIGN_CENTER);
                document.add(eventName);

                // Event Type
                Paragraph title = new Paragraph("Event Budget Report", TITLE_FONT);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(15f);
                document.add(title);

                // Overview section
                // Header
                Paragraph overviewHeader = new Paragraph("Event Overview", HEADER_FONT);
                overviewHeader.setSpacingAfter(HEADER_SPACING);
                document.add(overviewHeader);

                // Inside Event Overview section
                // OLD: addKeyValueLine(document, "Event Description",
                // report.getEventDescription(), 0);
                Paragraph descriptionParagraph = new Paragraph("Event Description:", NORMAL_FONT);
                descriptionParagraph.setSpacingAfter(5f);
                document.add(descriptionParagraph);
                Paragraph actualDescription = new Paragraph(report.getEventDescription(), DESCRIPTION_FONT);
                actualDescription.setIndentationLeft(20f); // Indent it slightly
                actualDescription.setSpacingAfter(10f);
                document.add(actualDescription);
                // overview content
                // addKeyValueLine(document, "Event Description", report.getEventDescription(),
                // 0);
                ReportGeneratorUtils.addKeyValueLine(document, "Event Duration",
                                DateHelper.formatDate(report.getEventStartDateTime())
                                                + " - " + DateHelper.formatDate(report.getEventEndDateTime()),
                                0);
                ReportGeneratorUtils.addKeyValueLine(document, "Organizer", report.getOrganizerName(), 0);
                ReportGeneratorUtils.addKeyValueLine(document, "Activity Scale",
                                String.valueOf(report.getTotalParticipants()) + " Participants", 0);

                document.add(Chunk.NEWLINE);
                document.add(new LineSeparator());

                // Overall budget summary section
                Paragraph overallSummaryHeader = new Paragraph("Overall Budget Summary", HEADER_FONT);
                overallSummaryHeader.setSpacingBefore(HEADER_SPACING);
                overallSummaryHeader.setSpacingAfter(HEADER_SPACING);
                document.add(overallSummaryHeader);

                ReportGeneratorUtils.addKeyValueLine(document, "Total Budget Allocated",
                                "RM " + String.format("%.2f", report.getTotalBudgetAllocated()), 0);
                ReportGeneratorUtils.addKeyValueLine(document, "Total Budget Spent",
                                "RM " + String.format("%.2f", report.getTotalBudgetSpent()), 0);

                double overallVariance = report.getTotalBudgetAllocated() - report.getTotalBudgetSpent();
                String overallVarianceString = "";
                if (overallVariance < 0) {
                        overallVarianceString = String.format("The event is over budget by RM%.2f",
                                        Math.abs(overallVariance));
                } else if (overallVariance == 0) {
                        overallVarianceString = "The event is on budget";
                } else {
                        overallVarianceString = String.format("The event is under budget by RM%.2f", overallVariance);
                }
                ReportGeneratorUtils.addKeyValueLine(document, "Variance (Overall)", overallVarianceString, 0); // Corrected
                                                                                                                // this
                                                                                                                // line

                document.add(Chunk.NEWLINE);
                document.add(new LineSeparator());

                // System-Generated Insights Section ---
                Paragraph insightsHeader = new Paragraph("System-Generated Insights", HEADER_FONT);
                insightsHeader.setSpacingBefore(HEADER_SPACING);
                insightsHeader.setSpacingAfter(10f);
                document.add(insightsHeader);

                // Combined Summary Paragraph (in full sentences)
                double budgetEfficiency = 0.0;
                if (report.getTotalBudgetAllocated() > 0) {
                        budgetEfficiency = (report.getTotalBudgetSpent() / report.getTotalBudgetAllocated()) * 100;
                }

                String overallPerformanceStatement;
                if (overallVariance < 0) {
                        overallPerformanceStatement = String.format("The event concluded over budget by RM%.2f.",
                                        Math.abs(overallVariance));
                } else if (overallVariance == 0) {
                        overallPerformanceStatement = "The event concluded precisely on budget.";
                } else {
                        overallPerformanceStatement = String.format("The event concluded under budget by RM%.2f.",
                                        overallVariance);
                }

                Paragraph summaryParagraph = new Paragraph(
                                String.format("%s This represents an overall budget efficiency of %.2f%%.",
                                                overallPerformanceStatement, budgetEfficiency),
                                NORMAL_FONT // Keep NORMAL_FONT for primary insights.
                );
                summaryParagraph.setSpacingAfter(10f); // Spacing after the paragraph
                document.add(summaryParagraph);

                // New: Budget Category Spending Summary (Highest to Lowest)
                Paragraph spendingSummaryHeader = new Paragraph("Budget Category Spending Summary (Highest to Lowest)",
                                SUBHEADER_FONT); // Use SUBHEADER_FONT for a sub-section
                spendingSummaryHeader.setSpacingAfter(5f);
                document.add(spendingSummaryHeader);

                List<EventBudgetDTO> allBudgetCategoriesSortedBySpent = report.getEventBudgets().stream()
                                .sorted((b1, b2) -> Double.compare(b2.getAmountSpent(), b1.getAmountSpent()))
                                .collect(Collectors.toList());

                for (EventBudgetDTO budget : allBudgetCategoriesSortedBySpent) {
                        // Format: Category Name - RM Actual_Spent_Amount
                        ReportGeneratorUtils.addKeyValueLine(document, budget.getBudgetCategoryName(),
                                        "RM " + String.format("%.2f", budget.getAmountSpent()), 20f); // Indent this
                                                                                                      // list
                }
                document.add(Chunk.NEWLINE); // Add a new line after the summary list
                document.add(new LineSeparator()); // Separator after the insights section
                // --- End System-Generated Insights Section ---

                // Detailed category breakdown section
                Paragraph categoryHeader = new Paragraph("Detailed category Breakdown", HEADER_FONT);
                categoryHeader.setSpacingBefore(HEADER_SPACING);
                categoryHeader.setSpacingAfter(HEADER_SPACING);
                document.add(categoryHeader);

                // Detailed Category Breakdown
                for (EventBudgetDTO budget : report.getEventBudgets()) {
                        // Session Name (should be Budget Category Name)
                        Paragraph budgetCategoryName = new Paragraph(budget.getBudgetCategoryName(), SUBHEADER_FONT);
                        budgetCategoryName.setSpacingAfter(5f);
                        document.add(budgetCategoryName);

                        ReportGeneratorUtils.addKeyValueLine(document, "Allocated Amount",
                                        "RM " + String.format("%.2f", budget.getAmountAllocated()), 20f);
                        ReportGeneratorUtils.addKeyValueLine(document, "Actual Spent Amount",
                                        "RM " + String.format("%.2f", budget.getAmountSpent()), 20f);

                        double categoryVariance = budget.getAmountAllocated() - budget.getAmountSpent();
                        String categoryVarianceString = "";
                        if (categoryVariance < 0) {
                                categoryVarianceString = String.format("Overspent by RM%.2f",
                                                Math.abs(categoryVariance));
                        } else if (categoryVariance == 0) {
                                categoryVarianceString = "On budget";
                        } else {
                                categoryVarianceString = String.format("Under budget by RM%.2f", categoryVariance);
                        }
                        ReportGeneratorUtils.addKeyValueLine(document, "Variance", categoryVarianceString, 20f);

                        // Handle potential division by zero for percentage of used
                        double percentageUsed = 0.0;
                        if (budget.getAmountAllocated() > 0) {
                                percentageUsed = (budget.getAmountSpent() / budget.getAmountAllocated()) * 100;
                        }
                        ReportGeneratorUtils.addKeyValueLine(document, "Percentage of used",
                                        String.format("%.2f%%", percentageUsed), 20f);

                }
                document.close();

                return byteArrayOutputStream.toByteArray();
        }

        public byte[] generateFeedbackReportPDF(EventFeedbackReportDTO report, String filename, Integer commentsLimit)
                        throws DocumentException, IOException {
                Document document = new Document(PageSize.A4, 50, 50, 50, 50);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, byteArrayOutputStream);

                document.open();

                // Report generation date header
                Paragraph generatedDateTime = new Paragraph(
                                "Report Generated On: " + DateHelper
                                                .formatHumanReadableDateTime(report.getReportGenerationDate()),
                                SMALL_FONT);
                generatedDateTime.setAlignment(Element.ALIGN_RIGHT);
                document.add(generatedDateTime);

                // Event Name
                Paragraph eventName = new Paragraph(report.getEventName(), TITLE_FONT);
                eventName.setAlignment(Element.ALIGN_CENTER);
                document.add(eventName);

                // Event Type
                Paragraph title = new Paragraph("Event Feedback Report", TITLE_FONT);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(15f);
                document.add(title);

                // --- Overview section ---
                // Header
                Paragraph overviewHeader = new Paragraph("Event Overview", HEADER_FONT);
                overviewHeader.setSpacingAfter(HEADER_SPACING);
                document.add(overviewHeader);

                // Inside Event Overview section
                Paragraph descriptionParagraph = new Paragraph("Event Description:", NORMAL_FONT);
                descriptionParagraph.setSpacingAfter(5f);
                document.add(descriptionParagraph);
                Paragraph actualDescription = new Paragraph(report.getEventDescription(), DESCRIPTION_FONT);
                actualDescription.setIndentationLeft(20f); // Indent it slightly
                actualDescription.setSpacingAfter(10f);
                document.add(actualDescription);

                // Helper method to add key-value lines
                ReportGeneratorUtils.addKeyValueLine(document, "Event Duration",
                                DateHelper.formatDate(report.getEventStartDateTime())
                                                + " - " + DateHelper.formatDate(report.getEventEndDateTime()),
                                0);
                ReportGeneratorUtils.addKeyValueLine(document, "Organizer", report.getOrganizerName(), 0);
                ReportGeneratorUtils.addKeyValueLine(document, "Activity Scale",
                                String.valueOf(report.getTotolParticipants()) + " Participants", 0);
                ReportGeneratorUtils.addKeyValueLine(document, "Number of Feedback Entries",
                                String.valueOf(report.getTotalFeedbackEntries()), 0);
                ReportGeneratorUtils.addKeyValueLine(document, "Feedback Submission Rate",
                                String.format("%.2f%%", report.getFeedbackSubmissionRate()), 0);

                document.add(Chunk.NEWLINE);
                document.add(new LineSeparator());

                // --- Overall Rating Summary section ---
                Paragraph overallSummaryHeader = new Paragraph("Overall Rating Summary", HEADER_FONT);
                overallSummaryHeader.setSpacingBefore(HEADER_SPACING);
                overallSummaryHeader.setSpacingAfter(HEADER_SPACING);
                document.add(overallSummaryHeader);

                ReportGeneratorUtils.addKeyValueLine(document, "Average Rating",
                                String.format("%.1f", report.getAverageRating()) + " / 5.0", 0);
                document.add(Chunk.NEWLINE);

                // Generate and add the chart to the cell
                Image ratingDistributionChart = ReportGeneratorUtils.generatePieChartImage("Ratings Distribution",
                                report.getRatingsDistribution());

                if (ratingDistributionChart != null) {
                        ratingDistributionChart.setAlignment(Element.ALIGN_CENTER);

                } else {
                        Paragraph errorMsg = new Paragraph("Could not generate ratings distribution chart.",
                                        SMALL_FONT);
                        errorMsg.setAlignment(Element.ALIGN_CENTER);

                }

                // Add the cell to the table and the table to the document

                document.add(ratingDistributionChart);

                document.add(Chunk.NEWLINE);
                document.add(new LineSeparator());

                Paragraph commentsHeader = new Paragraph("Feedback Comments", HEADER_FONT);
                commentsHeader.setSpacingBefore(HEADER_SPACING);
                commentsHeader.setSpacingAfter(10f);
                document.add(commentsHeader);

                // Check if there are any comments to display
                if (report.getCommentsForEachRating() != null && !report.getCommentsForEachRating().isEmpty()) {
                        // Iterate through ratings from 5 down to 1 to ensure consistent order in PDF
                        for (int rating = 5; rating >= 1; rating--) {
                                List<String> commentsList = report.getCommentsForEachRating().get(rating);

                                String ratingText = "Rating: " + rating + " stars";
                                Paragraph ratingTitle = new Paragraph(ratingText, SUBHEADER_FONT);
                                ratingTitle.setSpacingBefore(15f);
                                ratingTitle.setSpacingAfter(5f);
                                document.add(ratingTitle);

                                // Add comments or "No comments received" message
                                if (commentsList != null && !commentsList.isEmpty()) {
                                        // Iterate over each individual comment and add as a separate paragraph with
                                        // bullet
                                        for (String comment : commentsList) {
                                                Paragraph commentParagraph = new Paragraph("- \"" + comment + "\"",
                                                                COMMENT_FONT);
                                                commentParagraph.setIndentationLeft(20f);
                                                commentParagraph.setSpacingAfter(3f);
                                                document.add(commentParagraph);
                                        }
                                } else {
                                        // No comments for this rating
                                        Paragraph noCommentsParagraph = new Paragraph("No comments received.",
                                                        COMMENT_FONT);
                                        noCommentsParagraph.setIndentationLeft(20f);
                                        noCommentsParagraph.setSpacingAfter(3f);
                                        document.add(noCommentsParagraph);
                                }
                        }
                } else {
                        Paragraph noComments = new Paragraph("No feedback comments available for this event.",
                                        NORMAL_FONT);
                        noComments.setAlignment(Element.ALIGN_CENTER);
                        document.add(noComments);
                }

                document.close();
                return byteArrayOutputStream.toByteArray();
        }

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
                        double sessionAttendanceRate = (double) totalAttendeesForSession / totalRegisteredParticipants
                                        * 100;
                        report.addSessionAttendance(session.getSessionName(), session.getStartDateTime(),
                                        session.getEndDateTime(),
                                        totalAttendeesForSession, sessionAttendanceRate);
                }

                // Fetch Demographic Data (Gender and Faculty)
                Map<String, Map<String, Long>> demographicData = new HashMap<>();

                Map<String, Long> facultyData = convertGraphDataType(
                                registrationRepo.getDemographicDataGroupByFaculty(eventId));
                demographicData.put("Faculty", facultyData);

                Map<String, Long> courseData = convertGraphDataType(
                                registrationRepo.getDemographicDataGroupByCourse(eventId));
                demographicData.put("Course", courseData);

                Map<String, Long> rawYearData = convertGraphDataType(
                                registrationRepo.getDemographicDataGroupByYear(eventId));
                Map<String, Long> yearData = rawYearData.entrySet().stream()
                                .collect(Collectors.toMap(
                                                entry -> "Year " + entry.getKey(),
                                                Map.Entry::getValue));
                demographicData.put("Year", yearData);

                Map<String, Long> genderData = convertGraphDataType(
                                registrationRepo.getDemographicDataGroupByGender(eventId));
                demographicData.put("Gender", genderData);

                report.setDemographicData(demographicData);

                System.out.println(report.getDemographicData().toString());
                return report;
        }

        public EventBudgetReportDTO getBudgetReportData(Integer eventId) {
                EventBudgetReportDTO report = new EventBudgetReportDTO();

                // Fetch event details
                Event event = eventRepo.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
                report.setEventName(event.getName());
                report.setOrganizerName(event.getOrganizer().getName());
                report.setEventStartDateTime(event.getStartDateTime());
                report.setEventEndDateTime(event.getEndDateTime());
                report.setReportGenerationDate(LocalDateTime.now());
                report.setEventDescription(event.getDescription()); // Added event description

                // Fetch total participants (if applicable to budget report context)
                int totalRegisteredParticipants = registrationRepo.countByEventId(eventId);
                report.setTotalParticipants(totalRegisteredParticipants);

                // Calculate and set total allocated and spent budget
                // This part needs to be retrieved from your budget service or repository
                List<EventBudgetDTO> eventBudgets = eventBudgetService.getAllBudgetsByEventId(eventId);
                report.setEventBudgets(eventBudgets);

                double totalAllocated = eventBudgets.stream().mapToDouble(EventBudgetDTO::getAmountAllocated).sum();
                double totalSpent = eventBudgets.stream().mapToDouble(EventBudgetDTO::getAmountSpent).sum();

                report.setTotalBudgetAllocated(totalAllocated);
                report.setTotalBudgetSpent(totalSpent);

                return report;
        }

        public Map<String, Long> convertGraphDataType(List<Object[]> data) {
                return data
                                .stream()
                                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        }

        public List<SessionAttendanceDTO> getSessionAttendance(Event event) {
                List<SessionAttendanceDTO> sessionAttendance = new ArrayList<>();

                // Fetch registrations for event
                int totalRegisteredParticipants = registrationRepo.countByEventId(event.getId());
                // Calculate Session-Specific Attendance Rates
                List<Session> sessions = sessionRepo.findByEvent(event);
                for (Session session : sessions) {
                        int totalAttendeesForSession = attendanceRepo.countBySessionId(session.getId());
                        double sessionAttendanceRate = (double) totalAttendeesForSession / totalRegisteredParticipants
                                        * 100;
                        sessionAttendance.add(SessionAttendanceDTO.builder()
                                        .sessionName(session.getSessionName())
                                        .totalAttendees(totalAttendeesForSession)
                                        .sessionAttendanceRate(sessionAttendanceRate)
                                        .build());

                }
                return sessionAttendance;
        }

        @Override
        public EventReportOverviewDTO getEventReportOverviewDTO(Integer eventId) {
                Event event = eventRepo.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

                // Fetch all event reports for the given eventId
                List<EventReport> eventReports = getEventReport(eventId);

                // Initialize report variables to null
                EventReport budgetReport = null;
                EventReport attendanceReport = null;

                // Iterate through the fetched reports and assign them based on their type
                for (EventReport report : eventReports) {
                        // Safely convert the report type string to enum for comparison
                        // Make sure your ReportType enum has constant values like BUDGET and ATTENDANCE
                        if (ReportType.BUDGET.equals(report.getType())) {
                                budgetReport = report;
                        } else if (ReportType.ATTENDANCE.equals(report.getType())) {
                                attendanceReport = report;
                        }
                        // Add more conditions here if you have other report types
                }

                // --- Handle Attendance Report Overview ---
                List<SessionAttendanceDTO> attendanceDTOs = getSessionAttendance(event); // Assuming this class or an
                                                                                         // injected service handles
                                                                                         // this
                AttendanceReportOverview attendanceReportOverview = AttendanceReportOverview.builder()
                                .attendanceReport(attendanceReport) // Insert attendance report here
                                .sessionAttendances(attendanceDTOs)
                                .build();

                // --- Handle Budget Report Overview ---

                Map<String, Long> budgetExpenses = eventBudgetService.findTotalBudgetAndExpenseByEventId(eventId);
                BudgetReportOverview budgetReportOverview = BudgetReportOverview.builder()
                                .budgetReport(budgetReport)
                                .totalBudget(budgetExpenses.get("totalBudget"))
                                .totalExpenses(budgetExpenses.get("totalExpenses"))
                                // Calculate remaining budget, ensuring non-null values
                                .remaining(
                                                (budgetExpenses.get("totalBudget") != null
                                                                ? budgetExpenses.get("totalBudget")
                                                                : 0L) -
                                                                (budgetExpenses.get("totalExpenses") != null
                                                                                ? budgetExpenses.get("totalExpenses")
                                                                                : 0L))
                                .build();

                Map<String, Object> feedbackSummary = getFeedbackNoAndAverageRatings(eventId);

                Map<String, Long> ratingsDistribution = getRatingsDistribution(eventId);
                FeedbackReportOveriew feedbackReportOveriew = FeedbackReportOveriew.builder()
                                .averageRating((Double) feedbackSummary.get("averageRating"))
                                .feedbackCount((int) feedbackSummary.get("feedbackEntries"))
                                .ratings(ratingsDistribution)
                                .build();
                // --- Build Final Event Report Overview DTO ---
                EventReportOverviewDTO eventReportOverviewDTO = EventReportOverviewDTO.builder()
                                .attendance(attendanceReportOverview)
                                .budget(budgetReportOverview)
                                .feedback(feedbackReportOveriew)
                                .eventName(event.getName())
                                .build();

                /*
                 * 
                 * 
                 * private double averageRating;
                 * private int feedbackCount;
                 * private Map<Integer, Long> ratings;
                 */

                return eventReportOverviewDTO;
        }

        private EventFeedbackReportDTO getFeedbackReportData(Integer eventId, Integer commentsLimit) {
                EventFeedbackReportDTO report = new EventFeedbackReportDTO();

                // --- Common Event Details ---
                Event event = eventRepo.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
                report.setEventName(event.getName());
                // Assuming organizer is directly accessible, adjust if it's a separate entity
                report.setOrganizerName(event.getOrganizer() != null ? event.getOrganizer().getName() : "N/A");
                report.setEventStartDateTime(event.getStartDateTime());
                report.setEventEndDateTime(event.getEndDateTime());
                report.setReportGenerationDate(LocalDateTime.now());
                report.setEventDescription(event.getDescription());

                // --- Total Participants ---
                int totalRegisteredParticipants = registrationRepo.countByEventId(eventId);
                report.setTotolParticipants(totalRegisteredParticipants); // Corrected typo to 'totalParticipants' if
                                                                          // DTO allows

                // --- Total Feedback Entries & Average Rating ---
                List<Object[]> feedbackSummary = feedbackRepo.findTotalEntriesAndAverageRatingByEventId(eventId);
                int totalFeedbackEntries = 0;
                double averageRating = 0.0;

                if (feedbackSummary != null && !feedbackSummary.isEmpty()) {
                        Object[] data = feedbackSummary.get(0);
                        if (data[0] instanceof Long) {
                                totalFeedbackEntries = ((Long) data[0]).intValue();
                        }
                        if (data[1] instanceof Double) {
                                averageRating = (Double) data[1];
                        }
                }
                report.setTotalFeedbackEntries(totalFeedbackEntries);
                report.setAverageRating(averageRating);

                // --- Feedback Submission Rate ---
                if (totalRegisteredParticipants > 0) {
                        report.setFeedbackSubmissionRate(
                                        ((double) totalFeedbackEntries / totalRegisteredParticipants) * 100);
                } else {
                        report.setFeedbackSubmissionRate(0.0); // No participants, no submission rate
                }

                // --- Ratings Distribution ---
                Map<String, Long> ratingsDistribution = getRatingsDistribution(eventId);
                report.setRatingsDistribution(ratingsDistribution);

                // --- Comments for Each Rating ---
                List<Object[]> commentsData = feedbackRepo.findCommentsByEventId(eventId);
                Map<Integer, List<String>> commentsByRating = new LinkedHashMap<>(); // Use LinkedHashMap to keep rating
                                                                                     // order

                // Initialize the map with keys 1-5 first
                for (int i = 1; i <= 5; i++) {
                        commentsByRating.put(i, new ArrayList<>());
                }

                // Now populate with actual comments
                if (commentsData != null) {
                        Map<Integer, Integer> commentCounts = new HashMap<>(); // To track count per rating
                        boolean applyLimit = commentsLimit != null && commentsLimit > 0;

                        for (Object[] row : commentsData) {
                                if (row[0] instanceof Integer && row[1] instanceof String) {
                                        Integer rating = (Integer) row[0];
                                        String comment = (String) row[1];

                                        if (rating >= 1 && rating <= 5) {
                                                List<String> currentComments = commentsByRating.get(rating);
                                                int currentCount = commentCounts.getOrDefault(rating, 0);

                                                // Apply limit if enabled and current count is below limit
                                                if (!applyLimit || (commentsLimit != null
                                                                && currentCount < commentsLimit)) {
                                                        currentComments.add(comment);
                                                        commentCounts.put(rating, currentCount + 1);
                                                }
                                        }
                                }
                        }
                }

                report.setCommentsForEachRating(commentsByRating);
                return report;
        }

        private Map<String, Object> getFeedbackNoAndAverageRatings(Integer eventId) {
                List<Object[]> feedbackSummary = feedbackRepo.findTotalEntriesAndAverageRatingByEventId(eventId);
                int totalFeedbackEntries = 0;
                double averageRating = 0.0;

                if (feedbackSummary != null && !feedbackSummary.isEmpty()) {
                        Object[] data = feedbackSummary.get(0);
                        if (data[0] instanceof Long) {
                                totalFeedbackEntries = ((Long) data[0]).intValue();
                                System.out.println(totalFeedbackEntries);
                        }
                        if (data[1] instanceof Double) {
                                averageRating = (Double) data[1];
                        }
                }

                Map<String, Object> summary = new HashMap<>();
                summary.put("feedbackEntries", totalFeedbackEntries);
                summary.put("averageRating", averageRating);

                return summary;
        }

        private Map<String, Long> getRatingsDistribution(Integer eventId) {
                List<Object[]> ratingsDistributionData = feedbackRepo.findRatingsDistributionByEventId(eventId);
                Map<String, Long> ratingsDistribution = new HashMap<>();
                if (ratingsDistributionData != null) {
                        for (Object[] row : ratingsDistributionData) {
                                if (row[0] instanceof Integer && row[1] instanceof Long) {
                                        ratingsDistribution.put((String.valueOf(row[0])), ((Long) row[1]));
                                }
                        }
                }
                return ratingsDistribution;
        }

}
