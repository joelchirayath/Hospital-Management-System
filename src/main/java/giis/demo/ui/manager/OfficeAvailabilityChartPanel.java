package giis.demo.ui.manager;

import giis.demo.service.appointment.AppointmentDTO;
import giis.demo.service.appointment.OfficeAvailabilitySegment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class OfficeAvailabilityChartPanel extends JPanel {

    // Listener interface for double-clicks on busy segments
    public interface AppointmentClickListener {
        void onBusySegmentDoubleClicked(LocalDate date, OfficeAvailabilitySegment segment);
    }

    private Map<LocalDate, java.util.List<OfficeAvailabilitySegment>> data = new LinkedHashMap<>();
    private java.util.List<LocalDate> orderedDates = new ArrayList<>();
    private LocalTime windowStart = LocalTime.of(0, 0);
    private LocalTime windowEnd = LocalTime.of(23, 59);

    private AppointmentClickListener clickListener;

    private static class SegmentRect {
        Rectangle rect;
        OfficeAvailabilitySegment segment;
        LocalDate date;
    }

    private final java.util.List<SegmentRect> segmentRects = new ArrayList<>();

    public OfficeAvailabilityChartPanel() {
        setBackground(Color.WHITE);
        setToolTipText("");

        // Handle double-clicks on busy segments
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    handleDoubleClick(e.getPoint());
                }
            }
        });
    }

    public void setAppointmentClickListener(AppointmentClickListener listener) {
        this.clickListener = listener;
    }

    public void setData(Map<LocalDate, java.util.List<OfficeAvailabilitySegment>> data,
                        LocalTime windowStart,
                        LocalTime windowEnd) {
        this.data = (data != null) ? data : new LinkedHashMap<>();
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;

        orderedDates = new ArrayList<>(this.data.keySet());
        orderedDates.sort(Comparator.naturalOrder());

        updatePreferredSize();
        repaint();
    }

    public void clearData() {
        this.data = new LinkedHashMap<>();
        this.orderedDates = new ArrayList<>();
        updatePreferredSize();
        repaint();
    }

    private void updatePreferredSize() {
        int dayCount = orderedDates.size();
        int rowHeight = 26;
        int paddingTop = 10;   // no legend/axis here
        int paddingBottom = 20;
        int height = paddingTop + paddingBottom + dayCount * (rowHeight + 8);
        if (height < 120)
            height = 120;

        setPreferredSize(new Dimension(800, height));
        revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        segmentRects.clear();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int paddingLeft = 90;
        int paddingRight = 20;
        int paddingTop = 10;
        int rowHeight = 24;
        int rowGap = 8;

        long totalMinutes = ChronoUnit.MINUTES.between(windowStart, windowEnd);
        if (totalMinutes <= 0) {
            g2.dispose();
            return;
        }

        int y = paddingTop;

        for (LocalDate date : orderedDates) {
            java.util.List<OfficeAvailabilitySegment> segments = data.get(date);
            if (segments == null)
                continue;

            // Day label
            String dateLabel = date.toString();
            g2.setColor(new Color(90, 90, 90));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            int labelY = y + rowHeight - 6;
            g2.drawString(dateLabel, 10, labelY);

            // Background row line
            int rowCenterY = y + rowHeight / 2;
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(paddingLeft, rowCenterY, width - paddingRight, rowCenterY);

            for (OfficeAvailabilitySegment seg : segments) {
                long startMin = ChronoUnit.MINUTES.between(windowStart, seg.getFrom());
                long endMin = ChronoUnit.MINUTES.between(windowStart, seg.getTo());
                if (startMin < 0)
                    startMin = 0;
                if (endMin > totalMinutes)
                    endMin = totalMinutes;
                if (endMin <= startMin)
                    continue;

                int x1 = paddingLeft + (int) ((startMin * 1.0 / totalMinutes) * (width - paddingLeft - paddingRight));
                int x2 = paddingLeft + (int) ((endMin * 1.0 / totalMinutes) * (width - paddingLeft - paddingRight));
                int wRect = x2 - x1;
                if (wRect < 1)
                    wRect = 1;

                Rectangle rect = new Rectangle(x1, y + 4, wRect, rowHeight - 8);

                if (seg.isBusy()) {
                    g2.setColor(new Color(239, 83, 80));
                } else {
                    g2.setColor(new Color(129, 199, 132));
                }
                g2.fill(rect);

                if (seg.isBusy()) {
                    g2.setColor(new Color(183, 28, 28));
                } else {
                    g2.setColor(new Color(46, 125, 50));
                }
                g2.draw(rect);

                SegmentRect sr = new SegmentRect();
                sr.rect = rect;
                sr.segment = seg;
                sr.date = date;
                segmentRects.add(sr);
            }

            y += rowHeight + rowGap;
        }

        g2.dispose();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Point p = event.getPoint();
        for (SegmentRect sr : segmentRects) {
            if (sr.rect.contains(p)) {
                OfficeAvailabilitySegment seg = sr.segment;
                StringBuilder sb = new StringBuilder("<html>");
                sb.append("<b>").append(sr.date).append("</b><br>");
                sb.append(seg.getFrom()).append(" - ").append(seg.getTo()).append("<br>");
                if (seg.isBusy()) {
                    int count = seg.getAppointmentCount();
                    sb.append("Busy");
                    if (count > 0) {
                        sb.append(" (").append(count).append(" appointment");
                        if (count > 1) sb.append("s");
                        sb.append(")");
                    }
                    java.util.List<AppointmentDTO> apps = seg.getAppointments();
                    if (apps != null && !apps.isEmpty()) {
                        AppointmentDTO first = apps.get(0);
                        sb.append("<br>First: ")
                          .append(first.getFullPatientName());
                        if (first.getStart_time() != null) {
                            sb.append(" @ ").append(first.getStart_time());
                        }
                    }
                } else {
                    sb.append("Free");
                }
                sb.append("</html>");
                return sb.toString();
            }
        }
        return null;
    }

    // ----------------- Internal: double-click handling -----------------

    private void handleDoubleClick(Point p) {
        if (clickListener == null)
            return;

        for (SegmentRect sr : segmentRects) {
            if (sr.rect.contains(p) && sr.segment.isBusy()) {
                clickListener.onBusySegmentDoubleClicked(sr.date, sr.segment);
                break;
            }
        }
    }
}
