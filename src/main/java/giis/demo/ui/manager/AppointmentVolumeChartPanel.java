package giis.demo.ui.manager;

import giis.demo.service.appointment.AppointmentTimeSeriesDTO;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Appointment volume chart (bar chart) using JFreeChart.
 *
 * - X-axis: bucket labels (year/month/day/hour)
 * - Y-axis: number of appointments
 * - JFreeChart bar chart with:
 *   - mouse wheel zoom
 *   - rubber-band zoom
 *   - integer Y-axis ticks
 *   - rotated X labels to avoid overlap
 */
public class AppointmentVolumeChartPanel extends JPanel {

    private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    private final JFreeChart chart;
    private final ChartPanel chartPanel;

    public AppointmentVolumeChartPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        chart = ChartFactory.createBarChart(
                null,
                "Time",
                "Number of Appointments",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(250, 252, 255));
        plot.setRangeGridlinePaint(new Color(220, 225, 240));

        // X-axis labels rotated to avoid overlap
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4));

        // Y-axis integers
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);

        // Bar renderer styling
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.08); // thinner bars for dense data
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, new Color(90, 139, 240));

        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setPopupMenu(null); // keep UI clean
        add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * Public API: controller/window calls this to set data.
     */
    public void setData(List<AppointmentTimeSeriesDTO> list) {
        dataset.clear();
        if (list == null || list.isEmpty()) {
            chart.setTitle("No data to display for the selected filters.");
            return;
        } else {
            chart.setTitle("");
        }

        // Merge buckets if repeated
        Map<String, Integer> merged = new LinkedHashMap<>();
        for (AppointmentTimeSeriesDTO dto : list) {
            String bucket = dto.getBucketLabel();
            int count = Math.max(0, dto.getCount());
            merged.put(bucket, merged.getOrDefault(bucket, 0) + count);
        }

        List<String> orderedBuckets = new ArrayList<>(merged.keySet());
        for (String b : orderedBuckets) {
            dataset.addValue(merged.get(b), "Appointments", b);
        }

        // Let JFreeChart recompute auto range
        CategoryPlot plot = chart.getCategoryPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.configure();
    }

    // Optional: expose zoom controls similar to line chart

    public void zoomIn() {
        chartPanel.zoomInBoth(chartPanel.getWidth() / 2.0, chartPanel.getHeight() / 2.0);
    }

    public void zoomOut() {
        chartPanel.zoomOutBoth(chartPanel.getWidth() / 2.0, chartPanel.getHeight() / 2.0);
    }

    public void resetZoom() {
        chartPanel.restoreAutoBounds();
    }
}
