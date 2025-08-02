package com.utem.event_hub_navigation.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.utem.event_hub_navigation.dto.EventTypePerformanceData;
import com.utem.event_hub_navigation.model.VenueUtilizationData;
import com.utem.event_hub_navigation.repo.VenueRepo;
import com.utem.event_hub_navigation.service.AdminReportService;
import com.utem.event_hub_navigation.repo.EventRepo;
import com.utem.event_hub_navigation.utils.ReportGeneratorUtils;
import java.util.LinkedHashMap;
import org.jfree.chart.plot.PlotOrientation;

@Service
public class AdminReportServiceImpl  implements AdminReportService {
        // --- Static Font Definitions ---
        // --- Static Font Definitions (iText 5 compatible - as provided by you) ---
        private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD);
        private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        private static final Font SUBHEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,
                        BaseColor.DARK_GRAY);
        private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
        private static final Font DESCRIPTION_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC,
                        BaseColor.GRAY);

        private static final Font ASSUMPTION_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC,
                        BaseColor.GRAY);
        private static final Font FORMULA_FONT = new Font(Font.FontFamily.COURIER, 9, Font.NORMAL, BaseColor.GRAY);
        private static final Font FORMULA_LABEL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL,
                        BaseColor.GRAY);
        // You mentioned adding this, but it was commented out in your example.
        // If you enable it, ensure Font.FontFamily.COURIER is suitable for your unicode
        // characters.
        // private static Font UNICODE_STAR_FONT= new Font(Font.FontFamily.COURIER, 10,
        // Font.NORMAL,
        // BaseColor.DARK_GRAY);

        private static final float HEADER_SPACING = 20f;

        @Autowired
        private VenueRepo venueRepo;
        @Autowired
        private EventRepo eventRepo;


        // --- Mock Data Class (for demonstration purposes) ---

        /**
         * Generates a mock Venue Utilization Report PDF using iText 5 syntax.
         * This function orchestrates the creation of different report sections.
         *
         * @return A byte array containing the generated PDF.
         * @throws DocumentException If there's an error creating or adding elements to
         *                           the PDF.
         * @throws IOException       If there's an error reading image data (for mock
         *                           charts).
         */
        @Override
        public byte[] generateVenueUtilizationReport(LocalDateTime startDateTime, LocalDateTime endDateTime,
                        List<Integer> venueIds) throws DocumentException, IOException {
                Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, byteArrayOutputStream);

                document.open();

                List<VenueUtilizationData> utilizationData = venueRepo.getVenueUtilizationData(startDateTime,
                                endDateTime);

                // Filter venues if venueIds is provided
                if (venueIds != null && !venueIds.isEmpty()) {

                        utilizationData = utilizationData.stream()
                                        .filter(data -> venueIds.contains(data.getVenueId()))
                                        .toList();
                }

                addReportHeader(document, "Venue Utilization Report");
                addFilterSummary(document, startDateTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + " - " +
                                endDateTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), utilizationData,
                                utilizationData.size());
                addSummaryStatistics(document, utilizationData);

                // Add page break before summary visualizations
                document.newPage();
                addSummaryVisualizations(document, utilizationData);

                // Add page break before detailed statistics
                document.newPage();
                addDetailedTable(document, utilizationData);
                addVenueAssumptionsAndFormulas(document);

                document.close();

                // // Upload to storage
                // supabaseStorageService.uploadFile(byteArrayOutputStream.toByteArray(),
                // "event-report", "venue-utilization.pdf");

                // Return the PDF bytes
                return byteArrayOutputStream.toByteArray();
        }

        /**
         * Generates the Event Type Performance Overview Report PDF.
         * This is the new report type requested.
         *
         * @return A byte array containing the generated PDF.
         * @throws DocumentException If there's an error creating or adding elements to
         *                           the PDF.
         * @throws IOException       If there's an error reading image data (for mock
         *                           charts).
         */
        public byte[] generateEventTypesPerformanceReport(LocalDateTime startDate, LocalDateTime endDate)
                        throws DocumentException, IOException {
                Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, byteArrayOutputStream);

                document.open();

                // --- Real Data for Event Type Performance ---
                List<EventTypePerformanceData> allEventTypesData = fetchEventTypePerformanceData(startDate, endDate);

                // Sort data for "Top 10" visualization
                // Sort by Total Registrations for the Bar Chart
                List<EventTypePerformanceData> top10ByRegistrations = allEventTypesData.stream()
                                .sorted(Comparator.comparingInt(EventTypePerformanceData::getTotalRegistrations)
                                                .reversed())
                                .limit(10)
                                .collect(Collectors.toList());

                // Sort by Avg. Attendance Rate for the Line Chart (handling N/A)
                List<EventTypePerformanceData> top10ByAttendance = allEventTypesData.stream()
                                .filter(d -> d.getAvgAttendanceRate() != null) // Filter out N/A for sorting
                                .sorted(Comparator.comparingDouble(EventTypePerformanceData::getAvgAttendanceRate)
                                                .reversed())
                                .limit(10)
                                .collect(Collectors.toList());

                // --- Report Generation Flow ---
                addReportHeader(document, "Event Type Performance Overview");
                addFilterSummaryEventType(document,
                                startDate.toLocalDate().toString() + " - " + endDate.toLocalDate().toString(), "All");
                addSummaryStatisticsEventType(document, allEventTypesData);
                document.newPage();
                addEventPerformanceMetricsTable(document, allEventTypesData);
                document.newPage(); // Start visualizations on a new page for clarity
                addVisualizationsEventType(document, top10ByRegistrations, top10ByAttendance);
                document.newPage();
                addEventTypesAssumptionsAndFormulas(document);

                document.close();
                return byteArrayOutputStream.toByteArray();
        }

        private List<EventTypePerformanceData> fetchEventTypePerformanceData(LocalDateTime startDate,
                        LocalDateTime endDate) {
                String start = startDate.toString();
                String end = endDate.toString();
                List<Object[]> rows = eventRepo.fetchEventTypePerformanceData(start, end);
                List<EventTypePerformanceData> result = new ArrayList<>();
                for (Object[] row : rows) {
                        String eventType = ReportGeneratorUtils
                                        .formatStringRemoveUnderscore(row[0] != null ? row[0].toString() : "N/A");
                        int eventsHeld = row[1] != null ? ((Number) row[1]).intValue() : 0;
                        int totalRegistrations = row[2] != null ? ((Number) row[2]).intValue() : 0;
                        double avgRegPerEvent = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
                        double avgFillRate = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
                        Double avgAttendanceRate = row[5] != null ? ((Number) row[5]).doubleValue() : null;
                        result.add(new EventTypePerformanceData(eventType, eventsHeld, totalRegistrations,
                                        avgRegPerEvent, avgFillRate, avgAttendanceRate));
                }
                return result;
        }

        private void addReportHeader(Document document, String reportTitle) throws DocumentException {
                // Create a table with 2 columns for the header
                PdfPTable headerTable = new PdfPTable(2);
                headerTable.setWidthPercentage(100);
                headerTable.setWidths(new float[] { 1f, 1f });

                // Faculty Information (left side)
                Paragraph uniInfo = new Paragraph("Universiti Teknikal Malaysia Melaka (UTeM)", SMALL_FONT);
                PdfPCell uniCell = new PdfPCell(uniInfo);
                uniCell.setBorder(0);
                uniCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                headerTable.addCell(uniCell);

                // Generated DateTime (right side)
                Paragraph generatedDateTime = new Paragraph(
                                "Report Generated On: "
                                                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) +
                                                " at "
                                                + java.time.LocalTime.now()
                                                                .format(DateTimeFormatter.ofPattern("HH:mm")),
                                SMALL_FONT);
                PdfPCell dateCell = new PdfPCell(generatedDateTime);
                dateCell.setBorder(0);
                dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                headerTable.addCell(dateCell);

                document.add(headerTable);
                document.add(Chunk.NEWLINE);

                // Report Title
                Paragraph title = new Paragraph(reportTitle, TITLE_FONT);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                // Faculty Information
                Paragraph facultyInfo = new Paragraph("Faculty of Information & Communication Technology", NORMAL_FONT);
                facultyInfo.setAlignment(Element.ALIGN_CENTER);
                facultyInfo.setSpacingAfter(HEADER_SPACING); // Use HEADER_SPACING for main section separation
                document.add(facultyInfo);

        }

        private void addFilterSummary(Document document, String dateRange, List<VenueUtilizationData> selectedVenueData,
                        int totalAvailableVenues) throws DocumentException {
                Paragraph filterHeader = new Paragraph("Report Filters:", SUBHEADER_FONT);
                filterHeader.setSpacingAfter(5f);
                document.add(filterHeader);

                ReportGeneratorUtils.addKeyValueLine(document, "Date Range", dateRange, 10);
                ReportGeneratorUtils.addKeyValueLine(document, "Number of Venues Selected",
                                String.valueOf(selectedVenueData.size()), 10);

                document.add(Chunk.NEWLINE);
                document.add(new LineSeparator());
                document.add(Chunk.NEWLINE);
        }

        private void addSummaryStatistics(Document document, List<VenueUtilizationData> selectedVenueData)
                        throws DocumentException {
                Paragraph summaryHeader = new Paragraph("Summary Statistics:", HEADER_FONT);
                summaryHeader.setSpacingAfter(10f);
                document.add(summaryHeader);

                long totalEventSessions = selectedVenueData.stream().mapToLong(d -> d.getEventSessions()).sum();
                double avgTimeUtil = selectedVenueData.stream().mapToDouble(d -> d.getTimeUtilizationRate()).average()
                                .orElse(0.0);
                double avgSeatOccupancy = selectedVenueData.stream()
                                .mapToDouble(d -> d.getAverageRegisteredSeatOccupancy())
                                .average().orElse(0.0);
                double avgOverallSpaceUtil = selectedVenueData.stream()
                                .mapToDouble(d -> d.getOverallSpaceUtilizationRate())
                                .average().orElse(0.0);

                VenueUtilizationData mostUtilized = selectedVenueData.stream()
                                .max((v1, v2) -> Double.compare(v1.getOverallSpaceUtilizationRate(),
                                                v2.getOverallSpaceUtilizationRate()))
                                .orElse(null);
                VenueUtilizationData leastUtilized = selectedVenueData.stream()
                                .min((v1, v2) -> Double.compare(v1.getOverallSpaceUtilizationRate(),
                                                v2.getOverallSpaceUtilizationRate()))
                                .orElse(null);

                ReportGeneratorUtils.addKeyValueLine(document, "Total Event Sessions",
                                String.valueOf(totalEventSessions), 10);
                ReportGeneratorUtils.addKeyValueLine(document, "Average Time Utilization Rate",
                                String.format("%.1f%%", avgTimeUtil), 10);
                ReportGeneratorUtils.addKeyValueLine(document, "Average Registered Seat Occupancy",
                                String.format("%.1f%%", avgSeatOccupancy), 10);
                ReportGeneratorUtils.addKeyValueLine(document, "Average Space Utilization Rate",
                                String.format("%.1f%%", avgOverallSpaceUtil), 10);

                if (mostUtilized != null) {
                        ReportGeneratorUtils.addKeyValueLine(document, "Most Utilized Venue",
                                        mostUtilized.getVenueName() +
                                                        " (Space Utilization: "
                                                        + String.format("%.1f%%",
                                                                        mostUtilized.getOverallSpaceUtilizationRate())
                                                        + ")",
                                        10);

                }
                if (leastUtilized != null) {
                        ReportGeneratorUtils.addKeyValueLine(document, "Least Utilized Venue",
                                        leastUtilized.getVenueName() +
                                                        " (Space Utilization: "
                                                        + String.format("%.1f%%",
                                                                        leastUtilized.getOverallSpaceUtilizationRate())
                                                        + ")",
                                        10);
                }

                document.add(Chunk.NEWLINE);
                document.add(new LineSeparator());
                document.add(Chunk.NEWLINE);
        }

        private void addSummaryVisualizations(Document document, List<VenueUtilizationData> selectedVenueData)
                        throws DocumentException, IOException {
                Paragraph vizHeader = new Paragraph("Summary Visualizations:", HEADER_FONT);
                vizHeader.setSpacingAfter(10f);
                document.add(vizHeader);

                // --- Histogram ---
                Paragraph histogramTitle = new Paragraph("\nHistogram: Distribution of Overall Space Utilization Rates",
                                SUBHEADER_FONT);

                document.add(histogramTitle);

                Paragraph histogramDesc = new Paragraph(
                                "This histogram shows the distribution of venues across different utilization rate ranges, helping identify whether most venues are efficiently utilized, underutilized, or over-utilized.",
                                DESCRIPTION_FONT);
                histogramDesc.setAlignment(Element.ALIGN_JUSTIFIED);
                document.add(histogramDesc);

                // Generate histogram data
                Map<String, Long> histogramData = createHistogramData(selectedVenueData);

                // Generate and add the histogram chart using bar chart
                Image histogramChart = ReportGeneratorUtils.generateBarChartImage("Venue Utilization Distribution",
                                histogramData, "Venues", PlotOrientation.VERTICAL, "Utilization Rate Range",
                                "Number of Venues");
                histogramChart.scaleToFit(500f, 300f);
                document.add(histogramChart);
                document.add(Chunk.NEWLINE);

                document.newPage();
                // --- Scatter Plot ---
                Paragraph scatterPlotTitle = new Paragraph(
                                "\nScatter Plot: Overall Space Utilization vs. Venue Capacity",
                                SUBHEADER_FONT);
                document.add(scatterPlotTitle);

                Paragraph scatterPlotDesc = new Paragraph(
                                "This scatter plot displays each selected venue as a point, with its capacity on the X-axis and its overall space utilization on the Y-axis. This visualization helps identify the relationship between venue size and utilization efficiency, showing if specific capacity ranges are consistently under or over-performing.",
                                DESCRIPTION_FONT);
                scatterPlotDesc.setAlignment(Element.ALIGN_JUSTIFIED);
                document.add(scatterPlotDesc);

                // Generate scatter plot data
                Map<String, Double> xValues = new HashMap<>();
                Map<String, Double> yValues = new HashMap<>();
                for (VenueUtilizationData venue : selectedVenueData) {
                        xValues.put(venue.getVenueName(), (double) venue.getVenueCapacity());
                        yValues.put(venue.getVenueName(), venue.getOverallSpaceUtilizationRate());
                }

                // Generate and add the scatter plot
                Image scatterPlotChart = ReportGeneratorUtils.generateScatterPlotImage("Venue Capacity vs. Utilization",
                                xValues, yValues);
                scatterPlotChart.scaleToFit(500f, 300f);
                document.add(scatterPlotChart);

        }

        private Map<String, Long> createHistogramData(List<VenueUtilizationData> venueData) {
                // Define utilization rate ranges
                String[] ranges = {
                                "0-20%",
                                "21-40%",
                                "41-60%",
                                "61-80%",
                                "81-100%"
                };
                // Use LinkedHashMap to preserve insertion order
                Map<String, Long> histogramData = new LinkedHashMap<>();
                for (String range : ranges) {
                        histogramData.put(range, 0L); // Initialize counts to 0 in the desired order
                }

                for (VenueUtilizationData venue : venueData) {
                        double rate = venue.getOverallSpaceUtilizationRate();
                        if (rate <= 20) {
                                histogramData.put("0-20%", histogramData.get("0-20%") + 1);
                        } else if (rate <= 40) {
                                histogramData.put("21-40%", histogramData.get("21-40%") + 1);
                        } else if (rate <= 60) {
                                histogramData.put("41-60%", histogramData.get("41-60%") + 1);
                        } else if (rate <= 80) {
                                histogramData.put("61-80%", histogramData.get("61-80%") + 1);
                        } else { // rate > 80
                                histogramData.put("81-100%", histogramData.get("81-100%") + 1);
                        }
                }

                return histogramData;
        }

        private void addDetailedTable(Document document, List<VenueUtilizationData> selectedVenueData)
                        throws DocumentException {
                Paragraph tableHeader = new Paragraph("Detailed Statistics:", HEADER_FONT);
                tableHeader.setSpacingAfter(10f);
                document.add(tableHeader);

                // Define column widths for 9 columns (adjust as needed for content)
                // In iText 5, column widths are floats representing relative widths.
                float[] columnWidths = { 0.5f, 1.8f, 0.8f, 1f, 1f, 0.8f, 1.2f, 1.2f, 1.2f };
                PdfPTable table = new PdfPTable(columnWidths); // Use PdfPTable for iText 5
                table.setWidthPercentage(100); // Set table width to 100% of page width
                table.setSpacingAfter(HEADER_SPACING);

                String[] headers = { "No", "Venue Name", "Capacity", "Hours Booked", "Time Util. (%)", "Sessions",
                                "Total Registered Participants", "Avg. Registered Seat Occupancy (%)",
                                "Overall Space Util. (%)" };
                for (String header : headers) {
                        PdfPCell headerCell = new PdfPCell(new Phrase(header, NORMAL_FONT)); // Use Phrase to apply font
                        headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        headerCell.setPadding(5); // Add some padding
                        table.addCell(headerCell);
                }

                int i = 0;
                // Populate table with data
                for (VenueUtilizationData data : selectedVenueData) {
                        i++;
                        // Venue Name - align left
                        PdfPCell noCell = new PdfPCell(new Phrase(String.valueOf(i), SMALL_FONT));
                        noCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        noCell.setPadding(5);
                        table.addCell(noCell);

                        PdfPCell venueNameCell = new PdfPCell(new Phrase(data.getVenueName(), SMALL_FONT));
                        venueNameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        venueNameCell.setPadding(5);
                        table.addCell(venueNameCell);

                        // All other columns - align center
                        PdfPCell capacityCell = new PdfPCell(
                                        new Phrase(String.valueOf(data.getVenueCapacity()), SMALL_FONT));
                        capacityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        capacityCell.setPadding(5);
                        table.addCell(capacityCell);

                        PdfPCell hoursCell = new PdfPCell(
                                        new Phrase(String.valueOf(data.getTotalHoursBooked()), SMALL_FONT));
                        hoursCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        hoursCell.setPadding(5);
                        table.addCell(hoursCell);

                        PdfPCell timeUtilCell = new PdfPCell(
                                        new Phrase(String.format("%.1f", data.getTimeUtilizationRate()), SMALL_FONT));
                        timeUtilCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        timeUtilCell.setPadding(5);
                        table.addCell(timeUtilCell);

                        PdfPCell sessionsCell = new PdfPCell(
                                        new Phrase(String.valueOf(data.getEventSessions()), SMALL_FONT));
                        sessionsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        sessionsCell.setPadding(5);
                        table.addCell(sessionsCell);

                        PdfPCell participantsCell = new PdfPCell(
                                        new Phrase(String.valueOf(data.getTotalRegisteredAttendance()), SMALL_FONT));
                        participantsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        participantsCell.setPadding(5);
                        table.addCell(participantsCell);

                        PdfPCell avgOccupancyCell = new PdfPCell(
                                        new Phrase(String.format("%.1f", data.getAverageRegisteredSeatOccupancy()),
                                                        SMALL_FONT));
                        avgOccupancyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        avgOccupancyCell.setPadding(5);
                        table.addCell(avgOccupancyCell);

                        PdfPCell overallUtilCell = new PdfPCell(
                                        new Phrase(String.format("%.1f", data.getOverallSpaceUtilizationRate()),
                                                        SMALL_FONT));
                        overallUtilCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        overallUtilCell.setPadding(5);
                        table.addCell(overallUtilCell);
                }
                document.add(table);

        }

        private void addVenueAssumptionsAndFormulas(Document document) throws DocumentException {
                Paragraph assumptionsTitle = new Paragraph("Assumptions:",
                                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY));
                assumptionsTitle.setSpacingAfter(5f);
                document.add(assumptionsTitle);

                Paragraph assumptionsList = new Paragraph();
                Font assumptionFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
                assumptionsList.add(new Chunk(
                                "\u2022 Availability Definition: Total 'Available Hours' are calculated based on the university's defined operating hours for venues (Monday-Thursday, 8:00 AM - 6:00 PM, Friday 8:00 AM - 12:15 PM, 2:45 PM - 5:00 PM).\n",
                                assumptionFont));

                assumptionsList.add(new Chunk(
                                "\u2022 Inclusion Criteria: Only venues that have recorded actual utilization data are included in the utilization reports. Venues with no booked hours during the period will be excluded.\n",
                                assumptionFont));

                assumptionsList.add(new Chunk(
                                "\u2022 Participants Basis: Participation metrics are derived from the number of registered participants for each event. It is important to note that these figures may overestimate actual physical presence in the venue.\n",
                                assumptionFont));
                assumptionsList
                                .add(new Chunk("\u2022 Event Status: Only 'Active' events are included in utilization calculations.\n",
                                                assumptionFont));
                document.add(assumptionsList);
                document.add(Chunk.NEWLINE);

                Paragraph formulasTitle = new Paragraph("Formulas:",
                                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY));
                formulasTitle.setSpacingAfter(5f);
                document.add(formulasTitle);

                Paragraph formulasList = new Paragraph();
                Font formulaFont = new Font(Font.FontFamily.COURIER, 9, Font.NORMAL, BaseColor.GRAY);
                Font formulaLabelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
                formulasList.add(new Chunk("\u2022 Time Utilization Rate (%): ", formulaLabelFont));
                formulasList.add(new Chunk(
                                "(Sum of (Event End Time - Event Start Time)) / Total Defined Available Hours for Period * 100\n",
                                formulaFont));
                formulasList.add(new Chunk("\u2022 Average Registered Seat Occupancy (%): ", formulaLabelFont));
                formulasList.add(new Chunk(
                                "(Total Registered Participants for All Events in Venue) / (Venue Capacity * Number of Event Sessions) * 100\n",
                                formulaFont));
                formulasList.add(new Chunk("\u2022 Overall Space Utilization Rate (%): ", formulaLabelFont));
                formulasList.add(
                                new Chunk("(Time Utilization Rate (%) * Average Registered Seat Occupancy (%)) / 100\n",
                                                formulaFont));
                document.add(formulasList);
        }

        private void addFilterSummaryEventType(Document document, String dateRange, String departments)
                        throws DocumentException {
                Paragraph filterHeader = new Paragraph("Report Filters:", SUBHEADER_FONT);
                filterHeader.setSpacingAfter(5f);
                document.add(filterHeader);

                ReportGeneratorUtils.addKeyValueLine(document, "Date Range", dateRange, 10);

                document.add(Chunk.NEWLINE);
                document.add(new LineSeparator());
                document.add(Chunk.NEWLINE);
        }

        private void addSummaryStatisticsEventType(Document document, List<EventTypePerformanceData> allEventTypesData)
                        throws DocumentException {
                Paragraph summaryHeader = new Paragraph("Summary Statistics:", HEADER_FONT); // Changed to Summary
                                                                                             // Statistics
                summaryHeader.setSpacingAfter(10f);
                document.add(summaryHeader);

                // Calculate overall metrics
                long totalEventsHeld = allEventTypesData.stream().mapToInt(EventTypePerformanceData::getEventsHeld)
                                .sum();
                long totalRegisteredParticipants = allEventTypesData.stream()
                                .mapToInt(EventTypePerformanceData::getTotalRegistrations).sum();
                double overallAvgFillRate = allEventTypesData.stream()
                                .mapToDouble(EventTypePerformanceData::getAvgFillRate)
                                .average().orElse(0.0);

                // Calculate overall average attendance rate, handling nulls
                double overallAvgAttendanceRate = allEventTypesData.stream()
                                .filter(d -> d.getAvgAttendanceRate() != null)
                                .mapToDouble(EventTypePerformanceData::getAvgAttendanceRate)
                                .average().orElse(0.0);

                // Find highest/lowest attended by average attendance rate, handling nulls
                EventTypePerformanceData highestAttended = allEventTypesData.stream()
                                .filter(d -> d.getAvgAttendanceRate() != null)
                                .max(Comparator.comparingDouble(EventTypePerformanceData::getAvgAttendanceRate))
                                .orElse(null);
                EventTypePerformanceData lowestAttended = allEventTypesData.stream()
                                .filter(d -> d.getAvgAttendanceRate() != null)
                                .min(Comparator.comparingDouble(EventTypePerformanceData::getAvgAttendanceRate))
                                .orElse(null);

                ReportGeneratorUtils.addKeyValueLine(document, "Total Events Held", String.valueOf(totalEventsHeld),
                                10);
                ReportGeneratorUtils.addKeyValueLine(document, "Total Registered Participants",
                                String.valueOf(totalRegisteredParticipants), 10);
                ReportGeneratorUtils.addKeyValueLine(document, "Overall Avg. Registration Fill Rate",
                                String.format("%.2f%%", overallAvgFillRate), 10);
                ReportGeneratorUtils.addKeyValueLine(document, "Overall Avg. Actual Attendance Rate",
                                String.format("%.2f%%", overallAvgAttendanceRate), 10);

                ReportGeneratorUtils.addKeyValueLine(document, "Highest Attended Event Type",
                                String.valueOf(highestAttended.getEventType()) + " (Avg. Attendance: "
                                                + String.format("%.2f%%", highestAttended.getAvgFillRate()) + ")",
                                10);
                ReportGeneratorUtils.addKeyValueLine(document, "Lowest Attended Event Type",
                                String.valueOf(lowestAttended.getEventType()) + " (Avg. Attendance: "
                                                + String.format("%.2f%%", lowestAttended.getAvgFillRate()) + ")",
                                10);

                document.add(Chunk.NEWLINE);
        }

        private void addEventPerformanceMetricsTable(Document document,
                        List<EventTypePerformanceData> allEventTypesData)
                        throws DocumentException {
                Paragraph tableHeader = new Paragraph("Event Type Performance Metrics Table", HEADER_FONT);
                tableHeader.setSpacingAfter(10f);
                document.add(tableHeader);

                float[] columnWidths = { 0.5f, 2f, 1f, 1.5f, 1.2f, 1.2f, 1.2f };
                PdfPTable table = new PdfPTable(columnWidths);
                table.setWidthPercentage(100);
                table.setSpacingAfter(HEADER_SPACING);

                String[] headers = { "No", "Event Type", "Events Held", "Total Registrations", "Avg. Reg/Event",
                                "Avg. Fill Rate (%)",
                                "Avg. Attendance Rate (%)" };
                for (String header : headers) {
                        PdfPCell headerCell = new PdfPCell(new Phrase(header, NORMAL_FONT)); // Use NORMAL_FONT for
                                                                                             // headers as per
                                                                                             // sample
                        headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        headerCell.setPadding(5);
                        table.addCell(headerCell);
                }
                int i = 0;
                // Add data rows
                for (EventTypePerformanceData data : allEventTypesData) {
                        i++;
                        PdfPCell noCell = new PdfPCell(
                                        new Phrase(new Phrase(String.valueOf(i), SMALL_FONT)));
                        noCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        noCell.setPadding(5);
                        table.addCell(noCell);

                        table.addCell(new PdfPCell(new Phrase(data.getEventType(), SMALL_FONT)));

                        PdfPCell eventHeldCell = new PdfPCell(
                                        new Phrase(new Phrase(String.valueOf(data.getEventsHeld()), SMALL_FONT)));
                        eventHeldCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        eventHeldCell.setPadding(5);
                        table.addCell(eventHeldCell);

                        PdfPCell totalRegistrationCell = new PdfPCell(
                                        new Phrase(String.valueOf(data.getTotalRegistrations()), SMALL_FONT));
                        totalRegistrationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        totalRegistrationCell.setPadding(5);
                        table.addCell(totalRegistrationCell);

                        PdfPCell avgRegPerEventCell = new PdfPCell(
                                        new Phrase(String.format("%.2f", data.getAvgRegPerEvent()), SMALL_FONT));
                        avgRegPerEventCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        avgRegPerEventCell.setPadding(5);
                        table.addCell(avgRegPerEventCell);

                        PdfPCell avgFillRateCell = new PdfPCell(
                                        new Phrase(String.format("%.2f%%", data.getAvgFillRate()), SMALL_FONT));
                        avgFillRateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        avgFillRateCell.setPadding(5);
                        table.addCell(avgFillRateCell);

                        String attendanceRate = (data.getAvgAttendanceRate() != null)
                                        ? String.format("%.2f%%", data.getAvgAttendanceRate())
                                        : "N/A";

                        PdfPCell attendanceRateCell = new PdfPCell(new Phrase(attendanceRate, SMALL_FONT));
                        attendanceRateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        attendanceRateCell.setPadding(5);
                        table.addCell(attendanceRateCell);

                }

                document.add(table);
                document.add(Chunk.NEWLINE);

        }

        private void addVisualizationsEventType(Document document, List<EventTypePerformanceData> top10ByRegistrations,
                        List<EventTypePerformanceData> top10ByAttendance) throws DocumentException, IOException {
                Paragraph vizHeader = new Paragraph("Visualizations", HEADER_FONT);
                vizHeader.setSpacingAfter(10f);
                document.add(vizHeader);

                // --- A. Bar Chart: Total Registrations by Event Type ---
                Paragraph barChartTitle = new Paragraph("A. Total Registrations by Event Type (Top 10)",
                                SUBHEADER_FONT);
                barChartTitle.setSpacingAfter(5f);
                document.add(barChartTitle);

                Paragraph barChartDesc = new Paragraph(
                                "This chart displays the total number of registrations for the top 10 most registered event types.",
                                DESCRIPTION_FONT);
                barChartDesc.setAlignment(Element.ALIGN_JUSTIFIED);
                barChartDesc.setSpacingAfter(5f);
                document.add(barChartDesc);
                document.add(Chunk.NEWLINE);

                // Prepare data for bar chart
                LinkedHashMap<String, Long> barChartData = new LinkedHashMap<>();
                for (EventTypePerformanceData d : top10ByRegistrations) {
                        barChartData.put(d.getEventType(), (long) d.getTotalRegistrations());
                }
                try {
                        com.itextpdf.text.Image barChartImage = ReportGeneratorUtils.generateBarChartImage(
                                        "Total Registrations by Event Type (Top 10)", barChartData, "EventTypes",
                                        PlotOrientation.HORIZONTAL,
                                        "Event Type", "Total Registrations");
                        document.add(barChartImage);
                        document.add(Chunk.NEWLINE);
                } catch (Exception e) {
                        document.add(new Paragraph("Error loading Bar Chart."));
                }
                document.newPage();
                // --- B. Bar chart 2 : Avg. Attendance Rate (%) by Event Type ---
                Paragraph lineChartTitle = new Paragraph("\nB. Avg. Attendance Rate (%) by Event Type (Top 10)",
                                SUBHEADER_FONT);
                lineChartTitle.setSpacingAfter(5f);
                document.add(lineChartTitle);

                Paragraph lineChartDesc = new Paragraph(
                                "This chart highlights the average attendance rate for the top 10 event types by attendance, providing insight into the consistency or drop-off in engagement for these popular formats.",
                                DESCRIPTION_FONT);
                lineChartDesc.setAlignment(Element.ALIGN_JUSTIFIED);
                lineChartDesc.setSpacingAfter(5f);
                document.add(lineChartDesc);
                document.add(Chunk.NEWLINE);

                // Prepare data for line chart
                LinkedHashMap<String, Double> lineChartData = new LinkedHashMap<>();
                for (EventTypePerformanceData d : top10ByAttendance) {
                        if (d.getAvgAttendanceRate() != null) {
                                lineChartData.put(d.getEventType(), d.getAvgAttendanceRate());
                        }
                }

                try {
                        com.itextpdf.text.Image lineChartImage = ReportGeneratorUtils.generateBarChartImage(
                                        "Avg. Attendance Rate (%) by Event Type (Top 10)",
                                        lineChartData.entrySet().stream().collect(
                                                        java.util.stream.Collectors.toMap(
                                                                        java.util.Map.Entry::getKey,
                                                                        e -> e.getValue().longValue(),
                                                                        (a, b) -> a,
                                                                        LinkedHashMap::new)),
                                        "EventTypes",
                                        PlotOrientation.HORIZONTAL,
                                        "Event Type", "Avg. Attendance Rate (%)");
                        document.add(lineChartImage);
                        document.add(Chunk.NEWLINE);
                } catch (Exception e) {
                        document.add(new Paragraph("Error loading Bar Chart for Attendance Rate."));
                }

                document.add(Chunk.NEWLINE);
        }

        private void addEventTypesAssumptionsAndFormulas(Document document) throws DocumentException {

                List<Chunk> assumtiopnChunks = new ArrayList<>();

                assumtiopnChunks.add(new Chunk(
                                "\u2022 Data is based only on **COMPLETED** events within the specified date range. Events with other statuses (e.g., Active, Pending, Cancelled) are excluded from all calculations.\n",
                                ASSUMPTION_FONT));
                assumtiopnChunks.add(new Chunk(
                                "\u2022 The 'Total Registrations' metric sums all unique user registrations associated with events. It represents interest shown, but not necessarily actual physical attendance.\n",
                                ASSUMPTION_FONT));
                assumtiopnChunks.add(new Chunk(
                                "\u2022 The 'Expected Participants' for an event represents the pre-defined target or maximum capacity planned for that event. This value is set during event creation and may or may not align with venue physical capacity.\n",
                                ASSUMPTION_FONT));
                assumtiopnChunks.add(new Chunk(
                                "\u2022 The 'Total Checked-in Users' (used for Average Actual Attendance Rate) relies on accurate check-in data collected at the event. If this data is unavailable for an event, its attendance rate will be marked as 'N/A'.\n",
                                ASSUMPTION_FONT));

                addAssumptions(document, assumtiopnChunks);

                List<Chunk> forumlaChunks = new ArrayList<>();

                forumlaChunks.add(new Chunk("\u2022 Avg. Reg/Event: ", FORMULA_LABEL_FONT));
                forumlaChunks.add(new Chunk("Total Registrations / Number of Events Held\n", FORMULA_FONT));

                forumlaChunks.add(new Chunk("\u2022 Avg. Fill Rate (%): ", FORMULA_LABEL_FONT));
                forumlaChunks.add(new Chunk("(Total Registrations / Total Expected Participants) * 100%\n",
                                FORMULA_FONT));

                forumlaChunks.add(new Chunk("\u2022 Avg. Attendance Rate (%): ", FORMULA_LABEL_FONT));
                forumlaChunks.add(new Chunk("(Total Checked-in Users / Total Registrations) * 100%\n", FORMULA_FONT));

                addFormulas(document, forumlaChunks);

        }

        private void addAssumptions(Document document, List<Chunk> chunks) throws DocumentException {
                Paragraph assumptionsTitle = new Paragraph("Assumptions:",
                                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY));
                assumptionsTitle.setSpacingAfter(5f);
                document.add(assumptionsTitle);

                Paragraph assumptionsList = new Paragraph();
                for (Chunk chunk : chunks) {
                        assumptionsList.add(chunk);
                }

                document.add(assumptionsList);
                document.add(Chunk.NEWLINE);

        }

        private void addFormulas(Document document, List<Chunk> chunks) throws DocumentException {
                Paragraph formulasTitle = new Paragraph("Formulas:",
                                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY));
                formulasTitle.setSpacingAfter(5f);
                document.add(formulasTitle);

                Paragraph formulasList = new Paragraph();

                for (Chunk chunk : chunks) {
                        formulasList.add(chunk);
                }

                document.add(formulasList);

        }

      
}