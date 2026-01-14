package giis.demo.ui.manager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Fixed header that shows the time axis and the legend.
 * Used as the columnHeaderView of the JScrollPane so it
 * stays in place while the days scroll vertically.
 */
public class OfficeAvailabilityHeaderPanel extends JPanel {

    private LocalTime windowStart = LocalTime.of(0, 0);
    private LocalTime windowEnd = LocalTime.of(23, 59);

    public OfficeAvailabilityHeaderPanel() {
        setBackground(Color.WHITE);
        // A bit taller so axis + legend fit comfortably
        setPreferredSize(new Dimension(800, 60));
    }

    public void setTimeWindow(LocalTime start, LocalTime end) {
        if (start != null)
            this.windowStart = start;
        if (end != null)
            this.windowEnd = end;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        long totalMinutes = ChronoUnit.MINUTES.between(windowStart, windowEnd);
        if (totalMinutes <= 0)
            return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        int paddingLeft = 90;
        int paddingRight = 20;

        // ===== TIME AXIS (upper part) =====
        g2.setColor(new Color(120, 120, 120));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        int axisY = 24; // near top so legend can sit below
        int axisX1 = paddingLeft;
        int axisX2 = width - paddingRight;

        g2.drawLine(axisX1, axisY, axisX2, axisY);

        int suggestedStepMinutes = 120;
        long ticks = totalMinutes / suggestedStepMinutes;
        if (ticks < 2)
            suggestedStepMinutes = 30; // smaller window → denser ticks

        for (long m = 0; m <= totalMinutes; m += suggestedStepMinutes) {
            int x = axisX1 + (int) ((m * 1.0 / totalMinutes) * (axisX2 - axisX1));
            g2.drawLine(x, axisY - 3, x, axisY + 3);

            LocalTime t = windowStart.plusMinutes(m);
            String label = String.format("%02d:%02d", t.getHour(), t.getMinute());
            int strW = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, x - strW / 2, axisY - 5);
        }

        // ===== LEGEND (bottom-right, single line) =====
        String busyText = "Busy / Occupied";
        String freeText = "Free / Available";

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        int boxW = 14;
        int boxH = 10;
        int gapBoxText = 4;
        int gapBetweenGroups = 18;

        int busyTextWidth = g2.getFontMetrics().stringWidth(busyText);
        int freeTextWidth = g2.getFontMetrics().stringWidth(freeText);

        int totalLegendWidth =
                boxW + gapBoxText + busyTextWidth +
                gapBetweenGroups +
                boxW + gapBoxText + freeTextWidth;

        int legendYCenter = height - 16; // bottom-ish
        int legendYBox = legendYCenter - boxH / 2;

        int legendXStart = width - paddingRight - totalLegendWidth;

        int x = legendXStart;

        // Busy group
        g2.setColor(new Color(239, 83, 80));
        g2.fillRect(x, legendYBox, boxW, boxH);
        g2.setColor(new Color(183, 28, 28));
        g2.drawRect(x, legendYBox, boxW, boxH);

        x += boxW + gapBoxText;

        g2.setColor(new Color(90, 90, 90));
        g2.drawString(busyText, x, legendYCenter + 4);

        x += busyTextWidth + gapBetweenGroups;

        // Free group
        g2.setColor(new Color(129, 199, 132));
        g2.fillRect(x, legendYBox, boxW, boxH);
        g2.setColor(new Color(46, 125, 50));
        g2.drawRect(x, legendYBox, boxW, boxH);

        x += boxW + gapBoxText;

        g2.setColor(new Color(90, 90, 90));
        g2.drawString(freeText, x, legendYCenter + 4);

        g2.dispose();
    }
}
