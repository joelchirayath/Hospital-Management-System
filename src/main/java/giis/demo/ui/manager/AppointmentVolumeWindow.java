package giis.demo.ui.manager;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.AppointmentTimeSeriesDTO;
import giis.demo.service.appointment.TimeInterval;
import giis.demo.service.manager.ReportsController;
import giis.demo.service.manager.ReportsControllerImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager analytics: view number of appointments over time.
 *
 * Interval behavior:
 * - HOUR: single day (To=From), time range enabled
 * - DAY/WEEK: full date range (Y/M/D)
 * - MONTH: only Year+Month
 * - YEAR: only Year
 */
public class AppointmentVolumeWindow extends JFrame {

    private final ReportsController reportsController = new ReportsControllerImpl();
    private final AppointmentModel appointmentModel = new AppointmentModel();

    // From / To selectors
    private JComboBox<Integer> fromYearCombo;
    private JComboBox<String> fromMonthCombo;
    private JComboBox<Integer> fromDayCombo;

    private JComboBox<Integer> toYearCombo;
    private JComboBox<String> toMonthCombo;
    private JComboBox<Integer> toDayCombo;

    // Interval & filters
    private JComboBox<TimeInterval> intervalCombo;
    private JComboBox<String> doctorCombo;

    // Time range for HOUR interval
    private JComboBox<String> fromTimeCombo;
    private JComboBox<String> toTimeCombo;

    private JLabel infoLabel;
    private AppointmentVolumeChartPanel chartPanel;

    private static final String[] MONTH_NAMES = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    public AppointmentVolumeWindow() {
        setTitle("Appointments Over Time");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        initUi();
        initDateSelectors();
        initTimeSelectors();
        loadDoctors();
        setDefaultDatesToToday();
        applyIntervalConstraints();
        reloadData();
    }

    // ==========================================================
    // UI
    // ==========================================================

    private void initUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(Color.WHITE);

        // ---------- Header ----------
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel("Appointments Over Time");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(30, 70, 140));
        title.setAlignmentX(Component.CENTER_ALIGNMENT); // center main heading

        JLabel subtitle = new JLabel("Analyze appointment volume by interval, date, and doctor.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(80, 80, 80));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT); // center subtitle

        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(8));

        // ---------- Filters Card ----------
        JPanel filtersCard = new JPanel();
        filtersCard.setLayout(new BoxLayout(filtersCard, BoxLayout.Y_AXIS));
        filtersCard.setBackground(new Color(248, 250, 255));
        filtersCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 222, 240)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // Row 1: Interval
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row1.setOpaque(false);

        JLabel intervalLabel = boldLabel("Interval:");
        intervalCombo = new JComboBox<>(TimeInterval.values());
        intervalCombo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        intervalCombo.setPreferredSize(new Dimension(180, 32));
        intervalCombo.setBackground(Color.WHITE);
        intervalCombo.setBorder(BorderFactory.createLineBorder(new Color(190, 200, 215)));

        row1.add(intervalLabel);
        row1.add(intervalCombo);

        // Row 2: From / To / Time
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        row2.setOpaque(false);

        // From
        row2.add(boldLabel("From:"));
        fromYearCombo = createCombo(80);
        fromMonthCombo = createCombo(80);
        fromDayCombo = createCombo(60);
        row2.add(fromYearCombo);
        row2.add(fromMonthCombo);
        row2.add(fromDayCombo);

        // To
        row2.add(boldLabel("To:"));
        toYearCombo = createCombo(80);
        toMonthCombo = createCombo(80);
        toDayCombo = createCombo(60);
        row2.add(toYearCombo);
        row2.add(toMonthCombo);
        row2.add(toDayCombo);

        // Time (only used for HOUR)
        row2.add(boldLabel("Time (24h):"));
        fromTimeCombo = createCombo(80);
        row2.add(fromTimeCombo);
        row2.add(boldLabel("to"));
        toTimeCombo = createCombo(80);
        row2.add(toTimeCombo);

        // Row 3: Doctor (left aligned)
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        row3.setOpaque(false);

        row3.add(boldLabel("Doctor:"));
        doctorCombo = createCombo(220);
        row3.add(doctorCombo);

        // Apply button row (centered)
        JButton applyButton = new JButton("Apply Filters");
        applyButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        applyButton.setFocusPainted(false);
        applyButton.setOpaque(true);
        applyButton.setContentAreaFilled(true);
        applyButton.setBackground(new Color(30, 120, 240));
        applyButton.setForeground(Color.BLACK);
        applyButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(20, 90, 190)),
                new EmptyBorder(6, 18, 6, 18)
        ));

        applyButton.addActionListener(e -> {
            applyIntervalConstraints();
            reloadData();
        });

        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        row4.setOpaque(false);
        row4.add(applyButton);

        // Hook interval change
        intervalCombo.addActionListener(e -> {
            applyIntervalConstraints();
            reloadData();
        });

        // Assemble filters card
        filtersCard.add(row1);
        filtersCard.add(Box.createVerticalStrut(4));
        filtersCard.add(row2);
        filtersCard.add(Box.createVerticalStrut(4));
        filtersCard.add(row3);
        filtersCard.add(Box.createVerticalStrut(4));
        filtersCard.add(row4);

        // Top container
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Color.WHITE);
        top.add(header);
        top.add(filtersCard);

        root.add(top, BorderLayout.NORTH);

        // Chart area
        chartPanel = new AppointmentVolumeChartPanel();
        chartPanel.setPreferredSize(new Dimension(900, 360));
        root.add(chartPanel, BorderLayout.CENTER);

        // Info label
        infoLabel = new JLabel(" ");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        infoLabel.setForeground(new Color(90, 90, 90));
        infoLabel.setBorder(new EmptyBorder(3, 3, 0, 3));
        root.add(infoLabel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(40, 40, 70));
        return l;
    }

    private <T> JComboBox<T> createCombo(int width) {
        JComboBox<T> cb = new JComboBox<>();
        cb.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cb.setPreferredSize(new Dimension(width, 26));
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 230)));
        return cb;
    }

    // ==========================================================
    // Time selectors (HOUR)
    // ==========================================================

    private void initTimeSelectors() {
        fromTimeCombo.removeAllItems();
        toTimeCombo.removeAllItems();
        for (int h = 0; h < 24; h++) {
            String label = String.format("%02d:00", h);
            fromTimeCombo.addItem(label);
            toTimeCombo.addItem(label);
        }
        fromTimeCombo.setSelectedItem("00:00");
        toTimeCombo.setSelectedItem("23:00");
        setTimeSelectorsEnabled(false);
    }

    private void setTimeSelectorsEnabled(boolean enabled) {
        fromTimeCombo.setEnabled(enabled);
        toTimeCombo.setEnabled(enabled);
    }

    // ==========================================================
    // Interval-specific UI rules
    // ==========================================================

    private void applyIntervalConstraints() {
        TimeInterval interval = (TimeInterval) intervalCombo.getSelectedItem();
        if (interval == null)
            interval = TimeInterval.DAY;

        if (interval == TimeInterval.HOUR) {
            LocalDate from = buildDate(fromYearCombo, fromMonthCombo, fromDayCombo);
            if (from != null) {
                toYearCombo.setSelectedItem(from.getYear());
                toMonthCombo.setSelectedItem(MONTH_NAMES[from.getMonthValue() - 1]);
                updateDayCombo(toYearCombo, toMonthCombo, toDayCombo);
                toDayCombo.setSelectedItem(from.getDayOfMonth());
            }
            enableFromDate(true, true, true);
            enableToDate(false, false, false);
            setTimeSelectorsEnabled(true);

        } else if (interval == TimeInterval.MONTH) {
            enableFromDate(true, true, false);
            enableToDate(true, true, false);
            setTimeSelectorsEnabled(false);

        } else if (interval == TimeInterval.YEAR) {
            enableFromDate(true, false, false);
            enableToDate(true, false, false);
            setTimeSelectorsEnabled(false);

        } else { // DAY / WEEK
            enableFromDate(true, true, true);
            enableToDate(true, true, true);
            setTimeSelectorsEnabled(false);
        }
    }

    private void enableFromDate(boolean year, boolean month, boolean day) {
        fromYearCombo.setEnabled(year);
        fromMonthCombo.setEnabled(month);
        fromDayCombo.setEnabled(day);
    }

    private void enableToDate(boolean year, boolean month, boolean day) {
        toYearCombo.setEnabled(year);
        toMonthCombo.setEnabled(month);
        toDayCombo.setEnabled(day);
    }

    // ==========================================================
    // Date selectors
    // ==========================================================

    private void initDateSelectors() {
        int currentYear = LocalDate.now().getYear();
        int startYear = currentYear - 20;
        int endYear = currentYear + 5;

        fromYearCombo.removeAllItems();
        toYearCombo.removeAllItems();
        for (int y = startYear; y <= endYear; y++) {
            fromYearCombo.addItem(y);
            toYearCombo.addItem(y);
        }

        fromMonthCombo.removeAllItems();
        toMonthCombo.removeAllItems();
        for (String m : MONTH_NAMES) {
            fromMonthCombo.addItem(m);
            toMonthCombo.addItem(m);
        }

        addDateComboListeners();
    }

    private void addDateComboListeners() {
        fromYearCombo.addActionListener(e -> {
            updateDayCombo(fromYearCombo, fromMonthCombo, fromDayCombo);
            if (intervalCombo.getSelectedItem() == TimeInterval.HOUR)
                applyIntervalConstraints();
        });

        fromMonthCombo.addActionListener(e -> {
            updateDayCombo(fromYearCombo, fromMonthCombo, fromDayCombo);
            if (intervalCombo.getSelectedItem() == TimeInterval.HOUR)
                applyIntervalConstraints();
        });

        toYearCombo.addActionListener(e -> updateDayCombo(toYearCombo, toMonthCombo, toDayCombo));
        toMonthCombo.addActionListener(e -> updateDayCombo(toYearCombo, toMonthCombo, toDayCombo));
    }

    private void updateDayCombo(JComboBox<Integer> yearCombo,
                                JComboBox<String> monthCombo,
                                JComboBox<Integer> dayCombo) {
        Integer year = (Integer) yearCombo.getSelectedItem();
        String monthName = (String) monthCombo.getSelectedItem();
        if (year == null || monthName == null)
            return;

        int month = monthNameToNumber(monthName);
        if (month < 1)
            return;

        int prev = dayCombo.getItemCount() > 0 && dayCombo.getSelectedItem() != null
                ? (Integer) dayCombo.getSelectedItem()
                : 1;

        dayCombo.removeAllItems();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            dayCombo.addItem(d);
        }

        if (prev <= daysInMonth)
            dayCombo.setSelectedItem(prev);
        else
            dayCombo.setSelectedItem(daysInMonth);
    }

    private int monthNameToNumber(String name) {
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(name))
                return i + 1;
        }
        return -1;
    }

    private void setDefaultDatesToToday() {
        LocalDate today = LocalDate.now();

        fromYearCombo.setSelectedItem(today.getYear());
        fromMonthCombo.setSelectedItem(MONTH_NAMES[today.getMonthValue() - 1]);
        updateDayCombo(fromYearCombo, fromMonthCombo, fromDayCombo);
        fromDayCombo.setSelectedItem(today.getDayOfMonth());

        toYearCombo.setSelectedItem(today.getYear());
        toMonthCombo.setSelectedItem(MONTH_NAMES[today.getMonthValue() - 1]);
        updateDayCombo(toYearCombo, toMonthCombo, toDayCombo);
        toDayCombo.setSelectedItem(today.getDayOfMonth());

        intervalCombo.setSelectedItem(TimeInterval.DAY);
    }

    // ==========================================================
    // Data & filtering
    // ==========================================================

    private void loadDoctors() {
        doctorCombo.addItem("All");
        try {
            List<String> doctors = appointmentModel.getAllDoctors();
            for (String fullName : doctors) {
                doctorCombo.addItem(fullName);
            }
        } catch (Exception ignored) { }
    }

    private LocalDate buildDate(JComboBox<Integer> yearCombo,
                                JComboBox<String> monthCombo,
                                JComboBox<Integer> dayCombo) {
        Integer year = (Integer) yearCombo.getSelectedItem();
        String monthName = (String) monthCombo.getSelectedItem();
        Integer day = (Integer) dayCombo.getSelectedItem();
        if (year == null || monthName == null || day == null)
            return null;
        int month = monthNameToNumber(monthName);
        if (month < 1)
            return null;
        return LocalDate.of(year, month, day);
    }

    private int parseHour(String hhmm) {
        try {
            return Integer.parseInt(hhmm.substring(0, 2));
        } catch (Exception e) {
            return 0;
        }
    }

    // (kept for backward compatibility; not used after backend filtering enabled)
    private List<AppointmentTimeSeriesDTO> filterByHourRange(List<AppointmentTimeSeriesDTO> source,
                                                             int fromHour,
                                                             int toHour) {
        List<AppointmentTimeSeriesDTO> result = new ArrayList<>();
        for (AppointmentTimeSeriesDTO dto : source) {
            String label = dto.getBucketLabel();
            if (label == null) continue;

            if (label.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                int hour = parseHour(label.substring(11, 16));
                if (hour >= fromHour && hour <= toHour) {
                    result.add(dto);
                }
            } else {
                result.add(dto); // safe fallback
            }
        }
        return result;
    }

    private void reloadData() {
        try {
            TimeInterval interval = (TimeInterval) intervalCombo.getSelectedItem();
            if (interval == null)
                interval = TimeInterval.DAY;

            LocalDate from;
            LocalDate to;

            if (interval == TimeInterval.YEAR) {
                Integer fy = (Integer) fromYearCombo.getSelectedItem();
                Integer ty = (Integer) toYearCombo.getSelectedItem();
                if (fy == null || ty == null) {
                    showInputError("Please select valid From/To years.");
                    return;
                }
                from = LocalDate.of(fy, 1, 1);
                to = LocalDate.of(ty, 12, 31);

            } else if (interval == TimeInterval.MONTH) {
                Integer fy = (Integer) fromYearCombo.getSelectedItem();
                Integer ty = (Integer) toYearCombo.getSelectedItem();
                String fmName = (String) fromMonthCombo.getSelectedItem();
                String tmName = (String) toMonthCombo.getSelectedItem();
                if (fy == null || ty == null || fmName == null || tmName == null) {
                    showInputError("Please select valid From/To months.");
                    return;
                }
                int fm = monthNameToNumber(fmName);
                int tm = monthNameToNumber(tmName);
                if (fm < 1 || tm < 1) {
                    showInputError("Invalid month.");
                    return;
                }
                from = LocalDate.of(fy, fm, 1);
                int lastDay = YearMonth.of(ty, tm).lengthOfMonth();
                to = LocalDate.of(ty, tm, lastDay);

            } else {
                from = buildDate(fromYearCombo, fromMonthCombo, fromDayCombo);
                to = buildDate(toYearCombo, toMonthCombo, toDayCombo);
            }

            if (from == null || to == null) {
                showInputError("Please select valid From/To dates.");
                return;
            }

            // Time selection (strings) – used only for HOUR
            String fromTime = "00:00";
            String toTime = "23:00";

            if (interval == TimeInterval.HOUR) {
                to = from; // enforced single day
                if (fromTimeCombo.getSelectedItem() != null)
                    fromTime = (String) fromTimeCombo.getSelectedItem();
                if (toTimeCombo.getSelectedItem() != null)
                    toTime = (String) toTimeCombo.getSelectedItem();
                int fh = parseHour(fromTime);
                int th = parseHour(toTime);
                if (th < fh) {
                    showWarning("For hourly view, 'To' time cannot be before 'From' time.");
                    return;
                }
            } else if (to.isBefore(from)) {
                showWarning("'To' date cannot be before 'From' date.");
                return;
            }

            // Filters
            Integer doctorId = null;
            String doctorName = (String) doctorCombo.getSelectedItem();
            if (doctorName != null && !"All".equalsIgnoreCase(doctorName)) {
                doctorId = appointmentModel.getDoctorIdByName(doctorName);
            }

            List<AppointmentTimeSeriesDTO> list;

            // 👉 Backend time filtering for HOUR interval using the new overload
            if (interval == TimeInterval.HOUR) {
                list = appointmentModel.getAppointmentsOverTime(
                        from.toString(), to.toString(),
                        interval, doctorId, null,
                        fromTime, toTime
                );
            } else {
                // Existing path (unchanged)
                list = reportsController.getAppointmentVolumeOverTime(
                        from, to, interval, doctorId, null
                );
            }

            if (list == null || list.isEmpty()) {
                infoLabel.setText("No appointments available for the selected filters.");
                chartPanel.setData(new ArrayList<>());
            } else {
                infoLabel.setText("Hover a bar to see the exact number of appointments.");
                chartPanel.setData(list);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading appointment data: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showInputError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Invalid range", JOptionPane.WARNING_MESSAGE);
    }
}
