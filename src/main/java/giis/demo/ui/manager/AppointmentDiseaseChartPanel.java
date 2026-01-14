package giis.demo.ui.manager;

import giis.demo.service.appointment.AppointmentDiseaseStatsDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentDiseaseChartPanel extends JPanel {

    private List<AppointmentDiseaseStatsDTO> data = Collections.emptyList();
    private List<String> buckets = Collections.emptyList();     // ordered, filtered
    private List<String> diseases = Collections.emptyList();    // ordered
    private Map<String, Integer> valueByDiseaseBucket = Collections.emptyMap();
    private Map<String, Color> colorByDisease = Collections.emptyMap();
    private Map<String, Stroke> strokeByDisease = Collections.emptyMap();

    private int maxCount = 0;
    private int effectiveMax = 10;
    private int[] yTicks = new int[0];

    private final java.util.List<PointInfo> points = new ArrayList<>();
    private PointInfo selectedPoint;

    public AppointmentDiseaseChartPanel() {
        setBackground(Color.WHITE);
        setToolTipText("");

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setToolTipText(points.stream()
                        .filter(p -> p.hit.contains(e.getPoint()))
                        .map(p -> String.format("%s\n%d appointments\n%s", p.disease, p.count, p.bucket))
                        .findFirst().orElse(null));
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PointInfo hit = points.stream()
                        .filter(p -> p.hit.contains(e.getPoint()))
                        .findFirst()
                        .orElse(null);

                selectedPoint = hit;
                repaint();

                // Double-click: notify listener
                if (e.getClickCount() == 2 && hit != null && pointSelectionListener != null) {
                    pointSelectionListener.pointDoubleClicked(hit.disease, hit.bucket);
                }
            }
        });

    }

    public void setData(List<AppointmentDiseaseStatsDTO> d) {
        this.data = (d != null) ? d : Collections.emptyList();
        selectedPoint = null;
        preprocess();
        repaint();
    }

    private static String key(String disease, String bucket) {
        return disease + "||" + bucket;
    }

    private void preprocess() {
        if (data.isEmpty()) {
            buckets = diseases = Collections.emptyList();
            valueByDiseaseBucket = Collections.emptyMap();
            colorByDisease = Collections.emptyMap();
            strokeByDisease = Collections.emptyMap();
            maxCount = 0;
            effectiveMax = 10;
            yTicks = buildNiceTicks(0, effectiveMax);
            return;
        }

        // original ordered buckets & diseases
        List<String> allBucketsOrdered = data.stream()
                .map(AppointmentDiseaseStatsDTO::getBucketLabel)
                .distinct()
                .collect(Collectors.toList());

        diseases = data.stream()
                .map(AppointmentDiseaseStatsDTO::getDiseaseName)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Integer> tmp = new HashMap<>();
        Map<String, Integer> bucketSums = new LinkedHashMap<>();
        int localMax = 0;

        for (AppointmentDiseaseStatsDTO dto : data) {
            String disease = dto.getDiseaseName();
            String bucket = dto.getBucketLabel();
            int c = Math.max(0, dto.getCount());
            tmp.put(key(disease, bucket), c);
            bucketSums.put(bucket, bucketSums.getOrDefault(bucket, 0) + c);
            localMax = Math.max(localMax, c);
        }

        // Remove "empty" buckets that sum to 0 (keeps time order)
        buckets = allBucketsOrdered.stream()
                .filter(b -> bucketSums.getOrDefault(b, 0) > 0)
                .collect(Collectors.toList());
        if (buckets.isEmpty()) buckets = allBucketsOrdered; // fallback: show at least the axis

        valueByDiseaseBucket = tmp;
        maxCount = localMax;

        // Add headroom so bars don't touch top
        int paddedMax = (int) Math.ceil(maxCount * 1.12);
        effectiveMax = Math.max(6, paddedMax);
        yTicks = buildNiceTicks(0, effectiveMax);

        colorByDisease = buildColorMapStable(diseases);
        strokeByDisease = buildStrokeMapStable(diseases); // currently only used for legend
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        points.clear();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padL = 70, padR = 16, padT = 24, padB = 72;
        int w = Math.max(0, getWidth() - padL - padR);
        int h = Math.max(0, getHeight() - padT - padB);
        int x0 = padL;
        int y0 = padT + h;

        if (w <= 0 || h <= 0 || buckets.isEmpty() || diseases.isEmpty()) {
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("No appointments available for the selected filters.", 20, 30);
            g2.dispose();
            return;
        }

        double yScale = (effectiveMax > 0) ? (double) h / effectiveMax : 1.0;
        double groupW = (buckets.isEmpty()) ? 0.0 : (double) w / buckets.size();

        // Grid
        g2.setColor(new Color(235, 235, 235));
        g2.setStroke(new BasicStroke(1f));
        for (int t : yTicks) {
            int y = (int) (y0 - t * yScale);
            g2.drawLine(x0, y, x0 + w, y);
        }

        // Axes
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawLine(x0, y0, x0 + w, y0); // X
        g2.drawLine(x0, y0, x0, y0 - h); // Y

        // Y labels
        g2.setFont(getFont().deriveFont(11f));
        for (int t : yTicks) {
            int y = (int) (y0 - t * yScale);
            String label = String.valueOf(t);
            int tw = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, x0 - 8 - tw, y + 4);
        }

        // === BAR SERIES ===
        // each bucket is a "group", inside we have bars for each disease
        int diseaseCount = Math.max(1, diseases.size());
        double innerFrac = 0.8;

        for (int i = 0; i < buckets.size(); i++) {
            String bucket = buckets.get(i);

            double groupStart = x0 + i * groupW;
            double groupInnerW = groupW * innerFrac;
            double innerStart = groupStart + (groupW - groupInnerW) / 2.0;
            double rawBarW = groupInnerW / diseaseCount;
            int barW = Math.max(4, (int) Math.floor(rawBarW));

            for (int j = 0; j < diseases.size(); j++) {
                String disease = diseases.get(j);
                Integer c = valueByDiseaseBucket.get(key(disease, bucket));
                if (c == null || c <= 0) continue;

                int barHeight = (int) Math.round(c * yScale);
                if (barHeight <= 0) continue;

                int xLeft = (int) Math.round(innerStart + j * rawBarW);
                int yTop = y0 - barHeight;

                Color color = colorByDisease.getOrDefault(disease, Color.GRAY);

                // fill bar
                g2.setColor(color);
                g2.fillRect(xLeft, yTop, barW, barHeight);

                // outline
                g2.setColor(color.darker());
                g2.setStroke(new BasicStroke(1.1f));
                g2.drawRect(xLeft, yTop, barW, barHeight);

                // hit area & metadata for tooltip
                Rectangle hit = new Rectangle(xLeft, yTop, barW, barHeight);
                int cx = xLeft + barW / 2;
                int cy = yTop;
                points.add(new PointInfo(hit, disease, bucket, c, cx, cy, color));
            }
        }

        // highlight selected bar, if any
        if (selectedPoint != null) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            Rectangle r = selectedPoint.hit;
            g2.drawRect(r.x, r.y, r.width, r.height);
        }

        drawXAxisLabels(g2, x0, y0, w);
        if (selectedPoint != null) drawPinnedTooltip(g2, selectedPoint);

        g2.dispose();
    }

    private void drawXAxisLabels(Graphics2D g2, int x0, int y0, int w) {
        g2.setFont(getFont().deriveFont(10f));

        if (buckets.isEmpty()) return;
        double groupW = (double) w / buckets.size();

        int step = Math.max(1, (int) Math.ceil(buckets.size() / 12.0));

        for (int i = 0; i < buckets.size(); i += step) {
            String lbl = buckets.get(i);
            if (lbl.length() > 22) lbl = lbl.substring(0, 20) + "…";

            int x = (int) Math.round(x0 + groupW * (i + 0.5)); // center of group

            // rotate ~30° to avoid overlap
            Graphics2D gRot = (Graphics2D) g2.create();
            gRot.rotate(Math.toRadians(30), x, y0 + 4);
            gRot.drawString(lbl, x - gRot.getFontMetrics().stringWidth(lbl) / 2, y0 + 16);
            gRot.dispose();

            // small tick
            g2.drawLine(x, y0, x, y0 + 3);
        }
    }

    private void drawPinnedTooltip(Graphics2D g2, PointInfo p) {
        String[] lines = {p.disease, "Appointments: " + p.count, p.bucket};
        Font f = getFont().deriveFont(11f);
        g2.setFont(f);

        int w = 0;
        for (String s : lines) w = Math.max(w, g2.getFontMetrics().stringWidth(s));
        w += 16;
        int lh = g2.getFontMetrics().getHeight();
        int h = lh * lines.length + 10;

        int x = Math.min(p.x + 12, getWidth() - w - 6);
        int y = Math.max(p.y - h - 8, 6);

        g2.setColor(new Color(255, 255, 255, 245));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(150, 150, 150));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        int ty = y + g2.getFontMetrics().getAscent() + 4;
        g2.setColor(Color.BLACK);
        for (String s : lines) {
            g2.drawString(s, x + 8, ty);
            ty += lh;
        }
    }

    private static Map<String, Color> buildColorMapStable(List<String> names) {
        Map<String, Color> m = new LinkedHashMap<>();
        if (names == null || names.isEmpty()) {
            return m;
        }

        // Golden ratio to distribute hues nicely
        float goldenRatio = 0.6180339887f;
        float h = 0.0f;

        for (String s : names) {
            // advance hue
            h = (h + goldenRatio) % 1.0f;
            // decent saturation & brightness for visibility
            Color c = Color.getHSBColor(h, 0.65f, 0.95f);
            m.put(s, c);
        }
        return m;
    }

    private static Map<String, Stroke> buildStrokeMapStable(List<String> names) {
        Map<String, Stroke> m = new LinkedHashMap<>();
        BasicStroke solid = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        float[][] dashes = {
                null, new float[]{6f, 3f}, new float[]{3f, 3f}, new float[]{10f, 4f, 2f, 4f}
        };
        int i = 0;
        for (String s : names) {
            float[] dash = dashes[i % dashes.length];
            if (dash == null) m.put(s, solid);
            else m.put(s, new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
            i++;
        }
        return m;
    }

    /** Build "nice" ticks from 0..max with step 1/2/5 scaling. */
    private static int[] buildNiceTicks(int min, int max) {
        max = Math.max(min + 1, max);
        int rawSteps = 6; // ~6 grid lines
        double span = max - min;
        double step = Math.pow(10, Math.floor(Math.log10(span / rawSteps)));
        double[] candidates = {1, 2, 5, 10};
        double best = step;
        for (double c : candidates) {
            double s = c * step;
            if (span / s <= rawSteps + 1) {
                best = s;
                break;
            }
        }
        int niceMax = (int) (Math.ceil(max / best) * best);
        java.util.List<Integer> ticks = new ArrayList<>();
        for (int v = 0; v <= niceMax; v += (int) best) ticks.add(v);
        return ticks.stream().mapToInt(i -> i).toArray();
    }
    
    // --- callback for double-click on a point/bar ---
    public interface PointSelectionListener {
        void pointDoubleClicked(String diseaseName, String bucketLabel);
    }

    private PointSelectionListener pointSelectionListener;

    public void setPointSelectionListener(PointSelectionListener listener) {
        this.pointSelectionListener = listener;
    }
    
    // --- expose colors to window for legend ---
    public Color getColorForDisease(String diseaseLabel) {
        if (diseaseLabel == null) {
            return Color.GRAY;
        }

        // First try full label (e.g. "E11 - Type 2 diabetes mellitus")
        Color c = colorByDisease.get(diseaseLabel);
        if (c != null) {
            return c;
        }

        // Fallback: if map uses just the code, try "CODE" part
        int idx = diseaseLabel.indexOf(" - ");
        if (idx > 0) {
            String codePart = diseaseLabel.substring(0, idx).trim();
            c = colorByDisease.get(codePart);
            if (c != null) {
                return c;
            }
        }

        return Color.GRAY;
    }



    private static class PointInfo {
        final Rectangle hit;
        final String disease, bucket;
        final int count, x, y;
        final Color color;

        PointInfo(Rectangle hit, String disease, String bucket, int count, int x, int y, Color color) {
            this.hit = hit;
            this.disease = disease;
            this.bucket = bucket;
            this.count = count;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
}
