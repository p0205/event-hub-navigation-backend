package com.utem.event_hub_navigation.utils;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.TabSettings;
import com.itextpdf.text.TabStop;

public class ReportGeneratorUtils {

    private static final com.itextpdf.text.Font NORMAL_FONT = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL);

    public static Image generatePieChartImage(String title, Map<String, Long> data)
            throws IOException, com.itextpdf.text.BadElementException {
        // 1. Create a Dataset
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        // 2. Create a JFreeChart object
        JFreeChart pieChart = ChartFactory.createPieChart(
                title, // Chart title
                dataset, // Data
                true, // Include legend
                true, // Generate tooltips
                false // Generate URLs
        );

        // 3. Customize the chart (optional)
        customizeChart(pieChart);

        // 4. Generate the image bytes
        ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
        EncoderUtil.writeBufferedImage(pieChart.createBufferedImage(500, 300), ImageFormat.PNG, chartOutputStream);

        // 4. Convert to iText Image
        Image chartImage = Image.getInstance(chartOutputStream.toByteArray());
        chartImage.setAlignment(Element.ALIGN_CENTER);
        // chartImage.setSpacingBefore(10f);
        // chartImage.setSpacingAfter(10f);
        return chartImage;

    }

    @SuppressWarnings("unchecked")
    private static void customizeChart(JFreeChart chart) {
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();

        // Set background paint to be transparent or white for a clean look
        plot.setBackgroundPaint(Color.WHITE); // Or null for transparency

        // Optional: Remove outline around the plot area
        plot.setOutlinePaint(null);

        // Adjust label options for clarity
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} - {1} ({2})", // Format
                new DecimalFormat("#"), // Count format
                new DecimalFormat("0.00%") // Percentage format
        ));
        // Format labels as "Category
        // (Value)"
        plot.setLabelBackgroundPaint(new Color(220, 220, 220, 180)); // Light grey background for labels with some
                                                                     // transparency
        plot.setLabelOutlinePaint(null); // No outline for labels
        plot.setLabelShadowPaint(null); // No shadow for labels
        plot.setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12)); // Set a common font

        // Set the gap between slices
        plot.setExplodePercent("", 0.0); // Ensure no slices are exploded by default
        plot.setSectionOutlinesVisible(true); // Make slice outlines visible
        // plot.setSectionOutlinePaint(Color.WHITE); // Set slice outline color to white
        // for (String key : ((DefaultPieDataset<String>) plot.getDataset()).getKeys())
        // {
        // plot.setSectionOutlineStroke(key, new BasicStroke(1.0f)); // Set outline
        // thickness for each section
        // }

        // Remove shadow for a flatter design
        plot.setShadowPaint(null);

        // Customize the title font and alignment
        chart.getTitle().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.CENTER);

        // Customize the legend
        chart.getLegend().setItemFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        chart.getLegend().setBackgroundPaint(Color.WHITE); // White background for legend
        chart.getLegend().setFrame(BlockBorder.NONE); // Remove legend border
        chart.getLegend().setPosition(RectangleEdge.RIGHT);

        // Optional: Customize colors for sections (if needed to match specific Google
        // Chart colors)
        // You would typically iterate through your data keys and assign colors
        // For example:
        int i = 0;
        for (String key : ((DefaultPieDataset<String>) plot.getDataset()).getKeys()) {
            plot.setSectionPaint(key, getChartColor(i)); // Implement a helper to get colors
            i++;
        }
    }

    // Helper method example to get approximate Chart colors

    private static Color getChartColor(int index) {
        Color[] colors = {
                new Color(51, 102, 204), new Color(220, 57, 18), new Color(255, 153, 0),
                new Color(102, 163, 0), new Color(46, 117, 181), new Color(92, 136, 188),
                new Color(148, 187, 220), new Color(0, 153, 143), new Color(124, 252, 0),
                new Color(255, 99, 71)
        };
        return colors[index % colors.length];
    }

    // Add Information Key-value pair in pdf
    public static void addKeyValueLine(Document document, String key, String value, float leftIndentation)
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

    public static Image generateBarChartImage(String title, Map<String, Long> data)
            throws IOException, com.itextpdf.text.BadElementException {
        // 1. Create a Dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Venues", entry.getKey());
        }

        // 2. Create a JFreeChart object
        JFreeChart barChart = ChartFactory.createBarChart(
                title, // Chart title
                "Utilization Rate Range", // X-axis label
                "Number of Venues", // Y-axis label
                dataset, // Data
                PlotOrientation.VERTICAL, // Orientation
                false, // Include legend
                true, // Generate tooltips
                false // Generate URLs
        );

        // 3. Customize the chart
        customizeBarChart(barChart);

        // 4. Generate the image bytes
        ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
        EncoderUtil.writeBufferedImage(barChart.createBufferedImage(500, 300), ImageFormat.PNG, chartOutputStream);

        // 5. Convert to iText Image
        Image chartImage = Image.getInstance(chartOutputStream.toByteArray());
        chartImage.setAlignment(Element.ALIGN_CENTER);
        return chartImage;
    }

    private static void customizeBarChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();

        // Set background paint
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);

        // Customize the renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(51, 102, 204)); // Set bar color
        renderer.setDrawBarOutline(true);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());

        // Customize the domain axis (X-axis)
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        // Customize the range axis (Y-axis)
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        // Customize the title
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 15));
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.CENTER);
    }

    public static Image generateScatterPlotImage(String title, Map<String, Double> xValues, Map<String, Double> yValues)
            throws IOException, com.itextpdf.text.BadElementException {
        // Create dataset
        XYSeries series = new XYSeries("Venues");
        for (String key : xValues.keySet()) {
            series.add(xValues.get(key), yValues.get(key));
        }
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Create chart
        JFreeChart scatterPlot = ChartFactory.createScatterPlot(
                title, // Chart title
                "Venue Capacity", // X-axis label
                "Overall Space Utilization Rate (%)", // Y-axis label
                dataset, // Data
                PlotOrientation.VERTICAL, // Orientation
                true, // Include legend
                true, // Generate tooltips
                false // Generate URLs
        );

        // Customize the chart and handle zero data
        customizeScatterPlot(scatterPlot, yValues);

        // Generate the image bytes
        ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
        EncoderUtil.writeBufferedImage(scatterPlot.createBufferedImage(500, 300), ImageFormat.PNG, chartOutputStream);

        // Convert to iText Image
        Image chartImage = Image.getInstance(chartOutputStream.toByteArray());
        chartImage.setAlignment(Element.ALIGN_CENTER);
        return chartImage;
    }

    private static void customizeScatterPlot(JFreeChart chart, Map<String, Double> yValues) {
        XYPlot plot = chart.getXYPlot();

        // Set background paint
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);

        // Customize the renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        renderer.setSeriesPaint(0, new Color(51, 102, 204)); // Set point color
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8)); // Set point shape
        plot.setRenderer(renderer);

        // Check if all Y values are zero
        boolean allYValuesZero = yValues.values().stream().allMatch(value -> value == 0.0);

        // Customize the range axis (Y-axis)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        if (allYValuesZero) {
            // Set custom range when all values are zero
            rangeAxis.setRange(0, 100);
            rangeAxis.setTickUnit(new NumberTickUnit(10));
            rangeAxis.setAutoRange(false);
        }
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        // Customize the domain axis (X-axis)
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        // Customize the title
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 15));
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.CENTER);

        // Customize the legend
        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 12));
        chart.getLegend().setBackgroundPaint(Color.WHITE);
        chart.getLegend().setFrame(BlockBorder.NONE);
    }
}