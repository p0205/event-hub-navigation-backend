package com.utem.event_hub_navigation.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.utem.event_hub_navigation.model.VenueUtilizationData;
import com.utem.event_hub_navigation.repo.VenueRepo;
import com.utem.event_hub_navigation.utils.ReportGeneratorUtils;

@Service
public class AdminReportServiceImpl {
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
   
    // You mentioned adding this, but it was commented out in your example.
    // If you enable it, ensure Font.FontFamily.COURIER is suitable for your unicode characters.
    // private static Font UNICODE_STAR_FONT= new Font(Font.FontFamily.COURIER, 10,
    // Font.NORMAL,
    // BaseColor.DARK_GRAY);

    private static final float HEADER_SPACING = 20f;

    @Autowired
    private VenueRepo venueRepo;

    // --- Mock Data Class (for demonstration purposes) ---
   

    /**
     * Generates a mock Venue Utilization Report PDF using iText 5 syntax.
     * This function orchestrates the creation of different report sections.
     *
     * @return A byte array containing the generated PDF.
     * @throws DocumentException If there's an error creating or adding elements to the PDF.
     * @throws IOException If there's an error reading image data (for mock charts).
     */
    public byte[] generateVenueUtilizationReport(LocalDateTime startDateTime, LocalDateTime endDateTime, List<Integer> venueIds) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, byteArrayOutputStream);

        document.open();

        List<VenueUtilizationData> utilizationData = venueRepo.getVenueUtilizationData(startDateTime, endDateTime);
        
         // Filter venues if venueIds is provided
         if (venueIds != null && !venueIds.isEmpty()) {

            utilizationData = utilizationData.stream()
                .filter(data -> venueIds.contains(data.getVenueId()))
                .toList();
        }

        addReportHeader(document);
        addFilterSummary(document, startDateTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + " - " + 
            endDateTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), utilizationData, utilizationData.size());
        addSummaryStatistics(document, utilizationData);
        
       
        // Add page break before summary visualizations
        document.newPage();
        addSummaryVisualizations(document, utilizationData);
        
        // Add page break before detailed statistics
        document.newPage();
        addDetailedTable(document, utilizationData);
        addAssumptionsAndFormulas(document);

        document.close();
        
        // // Upload to storage
        // supabaseStorageService.uploadFile(byteArrayOutputStream.toByteArray(), "event-report", "venue-utilization.pdf");
        
        // Return the PDF bytes
        return byteArrayOutputStream.toByteArray();
    }



    private void addReportHeader(Document document) throws DocumentException {
        // Create a table with 2 columns for the header
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 1f});

        // Faculty Information (left side)
        Paragraph uniInfo = new Paragraph("Universiti Teknikal Malaysia Melaka (UTeM)", SMALL_FONT);
        PdfPCell uniCell = new PdfPCell(uniInfo);
        uniCell.setBorder(0);
        uniCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTable.addCell(uniCell);

        // Generated DateTime (right side)
        Paragraph generatedDateTime = new Paragraph(
                "Report Generated On: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) +
                        " at " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                SMALL_FONT);
        PdfPCell dateCell = new PdfPCell(generatedDateTime);
        dateCell.setBorder(0);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerTable.addCell(dateCell);

        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        // Report Title
        Paragraph title = new Paragraph("Venue Utilization Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);


        // Faculty Information
        Paragraph facultyInfo = new Paragraph("Faculty of Information & Communication Technology", NORMAL_FONT);
        facultyInfo.setAlignment(Element.ALIGN_CENTER);
        facultyInfo.setSpacingAfter(HEADER_SPACING); // Use HEADER_SPACING for main section separation
        document.add(facultyInfo);

    }

    private void addFilterSummary(Document document, String dateRange, List<VenueUtilizationData> selectedVenueData, int totalAvailableVenues) throws DocumentException {
        Paragraph filterHeader = new Paragraph("Report Filters:", SUBHEADER_FONT);
        filterHeader.setSpacingAfter(5f);
        document.add(filterHeader);

        ReportGeneratorUtils.addKeyValueLine(document, "Date Range", dateRange, 0);
        ReportGeneratorUtils.addKeyValueLine(document, "Number of Venues Included", String.valueOf(selectedVenueData.size()), 0);
        
        document.add(Chunk.NEWLINE);
        document.add(new LineSeparator());
        document.add(Chunk.NEWLINE);
    }

    private void addSummaryStatistics(Document document, List<VenueUtilizationData> selectedVenueData) throws DocumentException {
        Paragraph summaryHeader = new Paragraph("Summary Statistics:", HEADER_FONT);
        summaryHeader.setSpacingAfter(10f);
        document.add(summaryHeader);

        long totalEventSessions = selectedVenueData.stream().mapToLong(d -> d.getEventSessions()).sum();
        double avgTimeUtil = selectedVenueData.stream().mapToDouble(d -> d.getTimeUtilizationRate()).average().orElse(0.0);
        double avgSeatOccupancy = selectedVenueData.stream().mapToDouble(d -> d.getAverageRegisteredSeatOccupancy()).average().orElse(0.0);
        double avgOverallSpaceUtil = selectedVenueData.stream().mapToDouble(d -> d.getOverallSpaceUtilizationRate()).average().orElse(0.0);

        VenueUtilizationData mostUtilized = selectedVenueData.stream()
                .max((v1, v2) -> Double.compare(v1.getOverallSpaceUtilizationRate(), v2.getOverallSpaceUtilizationRate()))
                .orElse(null);
        VenueUtilizationData leastUtilized = selectedVenueData.stream()
                .min((v1, v2) -> Double.compare(v1.getOverallSpaceUtilizationRate(), v2.getOverallSpaceUtilizationRate()))
                .orElse(null);


                ReportGeneratorUtils.addKeyValueLine(document, "Total Event Sessions", String.valueOf(totalEventSessions), 0);
                ReportGeneratorUtils.addKeyValueLine(document, "Average Time Utilization Rate", String.format("%.1f%%", avgTimeUtil), 0);
                ReportGeneratorUtils.addKeyValueLine(document, "Average Registered Seat Occupancy", String.format("%.1f%%", avgSeatOccupancy), 0);
                ReportGeneratorUtils.addKeyValueLine(document, "Average Space Utilization Rate", String.format("%.1f%%", avgOverallSpaceUtil), 0);

      
        if (mostUtilized != null) {
            ReportGeneratorUtils.addKeyValueLine(document, "Most Utilized Venue", mostUtilized.getVenueName() + 
            " (Space Utilization: " + String.format("%.1f%%", mostUtilized.getOverallSpaceUtilizationRate()) + ")", 0);

        }
        if (leastUtilized != null) {
            ReportGeneratorUtils.addKeyValueLine(document, "Least Utilized Venue", leastUtilized.getVenueName() + 
            " (Space Utilization: " + String.format("%.1f%%", leastUtilized.getOverallSpaceUtilizationRate()) + ")", 0);
        }
      
        document.add(Chunk.NEWLINE);
        document.add(new LineSeparator());
        document.add(Chunk.NEWLINE);
    }

    private void addSummaryVisualizations(Document document, List<VenueUtilizationData> selectedVenueData) throws DocumentException, IOException {
        Paragraph vizHeader = new Paragraph("Summary Visualizations:", HEADER_FONT);
        vizHeader.setSpacingAfter(10f);
        document.add(vizHeader);

        // --- Histogram ---
        Paragraph histogramTitle = new Paragraph("\nHistogram: Distribution of Overall Space Utilization Rates", SUBHEADER_FONT);
    
        document.add(histogramTitle);

        Paragraph histogramDesc = new Paragraph("This histogram shows the distribution of venues across different utilization rate ranges, helping identify whether most venues are efficiently utilized, underutilized, or over-utilized.", DESCRIPTION_FONT);
        histogramDesc.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(histogramDesc);
        

        // Generate histogram data
        Map<String, Long> histogramData = createHistogramData(selectedVenueData);
        
        // Generate and add the histogram chart using bar chart
        Image histogramChart = ReportGeneratorUtils.generateBarChartImage("Venue Utilization Distribution", histogramData);
        histogramChart.scaleToFit(500f, 300f);
        document.add(histogramChart);
        document.add(Chunk.NEWLINE);

        document.newPage();
        // --- Scatter Plot ---
        Paragraph scatterPlotTitle = new Paragraph("\nScatter Plot: Overall Space Utilization vs. Venue Capacity", SUBHEADER_FONT);
        document.add(scatterPlotTitle);

        Paragraph scatterPlotDesc = new Paragraph("This scatter plot displays each selected venue as a point, with its capacity on the X-axis and its overall space utilization on the Y-axis. This visualization helps identify the relationship between venue size and utilization efficiency, showing if specific capacity ranges are consistently under or over-performing.", DESCRIPTION_FONT);
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
        Image scatterPlotChart = ReportGeneratorUtils.generateScatterPlotImage("Venue Capacity vs. Utilization", xValues, yValues);
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

        // Count venues in each range
        Map<String, Long> histogramData = new HashMap<>();
        for (String range : ranges) {
            histogramData.put(range, 0L);
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
            } else {
                histogramData.put("81-100%", histogramData.get("81-100%") + 1);
            }
        }

        return histogramData;
    }



    private void addDetailedTable(Document document, List<VenueUtilizationData> selectedVenueData) throws DocumentException {
        Paragraph tableHeader = new Paragraph("Detailed Statistics:", HEADER_FONT);
        tableHeader.setSpacingAfter(10f);
        document.add(tableHeader);

        // Define column widths for 8 columns (adjust as needed for content)
        // In iText 5, column widths are floats representing relative widths.
        float[] columnWidths = {1.8f, 0.8f, 1f, 1f, 0.8f, 1.2f, 1.2f, 1.2f};
        PdfPTable table = new PdfPTable(columnWidths); // Use PdfPTable for iText 5
        table.setWidthPercentage(100); // Set table width to 100% of page width
        table.setSpacingAfter(HEADER_SPACING);

        String[] headers = {"Venue Name", "Capacity", "Hours Booked", "Time Util. (%)", "Sessions", "Total Registered Participants", "Avg. Registered Seat Occupancy (%)", "Overall Space Util. (%)"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, NORMAL_FONT)); // Use Phrase to apply font
            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setPadding(5); // Add some padding
            table.addCell(headerCell);
        }

        // Populate table with data
        for (VenueUtilizationData data : selectedVenueData) {
            // Venue Name - align left
            PdfPCell venueNameCell = new PdfPCell(new Phrase(data.getVenueName(), SMALL_FONT));
            venueNameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            venueNameCell.setPadding(5);
            table.addCell(venueNameCell);
            
            // All other columns - align center
            PdfPCell capacityCell = new PdfPCell(new Phrase(String.valueOf(data.getVenueCapacity()), SMALL_FONT));
            capacityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            capacityCell.setPadding(5);
            table.addCell(capacityCell);
            
            PdfPCell hoursCell = new PdfPCell(new Phrase(String.valueOf(data.getTotalHoursBooked()), SMALL_FONT));
            hoursCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hoursCell.setPadding(5);
            table.addCell(hoursCell);
            
            PdfPCell timeUtilCell = new PdfPCell(new Phrase(String.format("%.1f", data.getTimeUtilizationRate()), SMALL_FONT));
            timeUtilCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            timeUtilCell.setPadding(5);
            table.addCell(timeUtilCell);
            
            PdfPCell sessionsCell = new PdfPCell(new Phrase(String.valueOf(data.getEventSessions()), SMALL_FONT));
            sessionsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            sessionsCell.setPadding(5);
            table.addCell(sessionsCell);
            
            PdfPCell participantsCell = new PdfPCell(new Phrase(String.valueOf(data.getTotalRegisteredAttendance()), SMALL_FONT));
            participantsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            participantsCell.setPadding(5);
            table.addCell(participantsCell);
            
            PdfPCell avgOccupancyCell = new PdfPCell(new Phrase(String.format("%.1f", data.getAverageRegisteredSeatOccupancy()), SMALL_FONT));
            avgOccupancyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            avgOccupancyCell.setPadding(5);
            table.addCell(avgOccupancyCell);
            
            PdfPCell overallUtilCell = new PdfPCell(new Phrase(String.format("%.1f", data.getOverallSpaceUtilizationRate()), SMALL_FONT));
            overallUtilCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            overallUtilCell.setPadding(5);
            table.addCell(overallUtilCell);
        }
        document.add(table);
     
    }

    private void addAssumptionsAndFormulas(Document document) throws DocumentException {
        Paragraph assumptionsTitle = new Paragraph("Assumptions:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY));
        assumptionsTitle.setSpacingAfter(5f);
        document.add(assumptionsTitle);

        Paragraph assumptionsList = new Paragraph();
        Font assumptionFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
        assumptionsList.add(new Chunk("\u2022 Availability Definition: Total 'Available Hours' are calculated based on the university's defined operating hours for venues (Monday-Thursday, 8:00 AM - 6:00 PM, Friday 8:00 AM - 12:15 PM, 2:45 PM - 5:00 PM).\n", assumptionFont));
        assumptionsList.add(new Chunk("\u2022 Participants Basis: Participation metrics are derived from the number of registered participants for each event. It is important to note that these figures may overestimate actual physical presence in the venue.\n", assumptionFont));
        assumptionsList.add(new Chunk("\u2022 Event Status: Only 'Active' events are included in utilization calculations.\n", assumptionFont));
        document.add(assumptionsList);
        document.add(Chunk.NEWLINE);

        Paragraph formulasTitle = new Paragraph("Formulas:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY));
        formulasTitle.setSpacingAfter(5f);
        document.add(formulasTitle);

        Paragraph formulasList = new Paragraph();
        Font formulaFont = new Font(Font.FontFamily.COURIER, 9, Font.NORMAL, BaseColor.GRAY);
        Font formulaLabelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
        formulasList.add(new Chunk("\u2022 Time Utilization Rate (%): ", formulaLabelFont));
        formulasList.add(new Chunk("(Sum of (Event End Time - Event Start Time)) / Total Defined Available Hours for Period * 100\n", formulaFont));
        formulasList.add(new Chunk("\u2022 Average Registered Seat Occupancy (%): ", formulaLabelFont));
        formulasList.add(new Chunk("(Total Registered Participants for All Events in Venue) / (Venue Capacity * Number of Event Sessions) * 100\n", formulaFont));
        formulasList.add(new Chunk("\u2022 Overall Space Utilization Rate (%): ", formulaLabelFont));
        formulasList.add(new Chunk("(Time Utilization Rate (%) * Average Registered Seat Occupancy (%)) / 100\n", formulaFont));
        document.add(formulasList);
    }
}