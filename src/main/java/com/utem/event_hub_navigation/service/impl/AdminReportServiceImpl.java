package com.utem.event_hub_navigation.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.utem.event_hub_navigation.utils.ReportGeneratorUtils;
import com.utem.event_hub_navigation.utils.SupabaseStorageService;

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
    private SupabaseStorageService supabaseStorageService;

    // --- Mock Data Class (for demonstration purposes) ---
   

    /**
     * Generates a mock Venue Utilization Report PDF using iText 5 syntax.
     * This function orchestrates the creation of different report sections.
     *
     * @return A byte array containing the generated PDF.
     * @throws DocumentException If there's an error creating or adding elements to the PDF.
     * @throws IOException If there's an error reading image data (for mock charts).
     */
    public void generateMockVenueUtilizationReport() throws DocumentException, IOException {
        Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, byteArrayOutputStream);

        document.open();

        List<VenueUtilizationData> selectedVenues = createMockVenueData();

        addReportHeader(document);
        addFilterSummary(document, "01 January 2025 - 30 June 2025", selectedVenues, createMockVenueData().size());
        addSummaryStatistics(document, selectedVenues);
        
        // Add page break before summary visualizations
        document.newPage();
        addSummaryVisualizations(document, selectedVenues);
        
        // Add page break before detailed statistics
        document.newPage();
        addDetailedTable(document, selectedVenues);
        addAssumptionsAndFormulas(document);

        document.close();
        // return byteArrayOutputStream.toByteArray();

        String fileUrl = supabaseStorageService.uploadFile(byteArrayOutputStream.toByteArray(), "event-report", "venue-utilization.pdf");

    }

    /**
     * Creates and returns a list of mock VenueUtilizationData objects.
     */
    private List<VenueUtilizationData> createMockVenueData() {
        List<VenueUtilizationData> mockData = new ArrayList<>();
        mockData.add(new VenueUtilizationData("Auditorium C01", 300, 180, 75.0, 10, 2500, 83.3, 62.5));
        // mockData.add(new VenueUtilizationData("Lab B.1.2", 25, 150, 85.7, 40, 850, 85.0, 72.8));
        mockData.add(new VenueUtilizationData("Meeting Rm F.3.4", 12, 40, 28.6, 8, 50, 52.1, 14.9));
        mockData.add(new VenueUtilizationData("Classroom K-201", 30, 100, 71.4, 20, 450, 75.0, 53.6));
        mockData.add(new VenueUtilizationData("Seminar Room A-3", 50, 160, 91.4, 18, 700, 77.8, 71.1));
        mockData.add(new VenueUtilizationData("Computer Lab S-10", 40, 120, 85.7, 25, 800, 80.0, 68.6));
        mockData.add(new VenueUtilizationData("Studio D-5", 15, 60, 42.8, 12, 100, 55.6, 23.8));
        mockData.add(new VenueUtilizationData("Lecture Theatre T-1", 250, 100, 41.7, 5, 800, 64.0, 26.7));
        mockData.add(new VenueUtilizationData("Breakout Room 7", 8, 20, 14.3, 4, 20, 62.5, 8.9));
        mockData.add(new VenueUtilizationData("Conference Room M-1", 20, 70, 50.0, 7, 150, 78.6, 39.3));
        return mockData;
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

        // addKeyValueLine(document, "Selected Venues", dateRange, 0);


        
        document.add(Chunk.NEWLINE);
        document.add(new LineSeparator());
        document.add(Chunk.NEWLINE);
    }

    private void addSummaryStatistics(Document document, List<VenueUtilizationData> selectedVenueData) throws DocumentException {
        Paragraph summaryHeader = new Paragraph("Summary Statistics:", HEADER_FONT);
        summaryHeader.setSpacingAfter(10f);
        document.add(summaryHeader);

        long totalEventSessions = selectedVenueData.stream().mapToInt(d -> d.getEventSessions()).sum();
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

        String[] headers = {"Venue Name", "Capacity", "Hours Booked", "Time Util. (%)", "Sessions", "Total Registered Attendance", "Avg. Registered Seat Occupancy (%)", "Overall Space Util. (%)"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, NORMAL_FONT)); // Use Phrase to apply font
            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setPadding(5); // Add some padding
            table.addCell(headerCell);
        }

        // Populate table with data
        for (VenueUtilizationData data : selectedVenueData) {
            table.addCell(new PdfPCell(new Phrase(data.getVenueName(), SMALL_FONT)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(data.getVenueCapacity()), SMALL_FONT)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(data.getTotalHoursBooked()), SMALL_FONT)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.1f%%", data.getTimeUtilizationRate()), SMALL_FONT)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(data.getEventSessions()), SMALL_FONT)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(data.getTotalRegisteredAttendance()), SMALL_FONT)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.1f%%", data.getAverageRegisteredSeatOccupancy()), SMALL_FONT)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.1f%%", data.getOverallSpaceUtilizationRate()), SMALL_FONT)));
        }
        document.add(table);
     
    }

    private void addAssumptionsAndFormulas(Document document) throws DocumentException {
        Paragraph assumptionsTitle = new Paragraph("Assumptions:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY));
        assumptionsTitle.setSpacingAfter(5f);
        document.add(assumptionsTitle);

        Paragraph assumptionsList = new Paragraph();
        Font assumptionFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
        assumptionsList.add(new Chunk("\u2022 Availability Definition: Total 'Available Hours' are calculated based on the university's defined operating hours for venues (e.g., Monday-Friday, 8:00 AM - 10:00 PM), excluding public holidays and university-wide closure days.\n", assumptionFont));
        assumptionsList.add(new Chunk("\u2022 Attendance Basis: Attendance metrics are derived from the number of registered attendees for each event. It is important to note that these figures may overestimate actual physical presence in the venue.\n", assumptionFont));
        assumptionsList.add(new Chunk("\u2022 Event Status: Only 'Confirmed' or 'Approved' events are included in utilization calculations; 'Draft', 'Cancelled', or 'Pending' events are excluded.\n", assumptionFont));
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
        formulasList.add(new Chunk("(Total Registered Attendance for All Events in Venue) / (Venue Capacity * Number of Event Sessions) * 100\n", formulaFont));
        formulasList.add(new Chunk("\u2022 Overall Space Utilization Rate (%): ", formulaLabelFont));
        formulasList.add(new Chunk("(Time Utilization Rate (%) * Average Registered Seat Occupancy (%)) / 100\n", formulaFont));
        document.add(formulasList);
    }
}