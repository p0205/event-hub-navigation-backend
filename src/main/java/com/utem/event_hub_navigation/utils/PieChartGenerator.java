package com.utem.event_hub_navigation.utils;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.general.DefaultPieDataset;

import com.itextpdf.text.Element;
import com.itextpdf.text.Image;

public class PieChartGenerator {

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
            "{0} - {1} ({2})",        // Format
                new DecimalFormat("#"),            // Count format
                new DecimalFormat("0.00%")         // Percentage format
        ));
 // Format labels as "Category
                                                                                   // (Value)"
        plot.setLabelBackgroundPaint(new Color(220, 220, 220, 180)); // Light grey background for labels with some
                                                                     // transparency
        plot.setLabelOutlinePaint(null); // No outline for labels
        plot.setLabelShadowPaint(null); // No shadow for labels
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12)); // Set a common font

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
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 15));
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.CENTER);

        // Customize the legend
        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 12));
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

}
