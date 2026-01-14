package giis.demo.ui.manager;

import giis.demo.service.appointment.AppointmentDTO;
import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.OfficeAvailabilitySegment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;

public class OfficeAvailabilityWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    // ===== THEME COLORS =====
    private static final Color PRIMARY = new Color(33, 150, 243);          // Light blue
    private static final Color PRIMARY_DARK = new Color(25, 118, 210);
    private static final Color BG_MAIN = new Color(232, 244, 253);        // Window background
    private static final Color BG_PANEL = new Color(243, 249, 255);       // Panel background
    private static final Color BORDER_COLOR = new Color(187, 222, 251);   // Soft blue border
    private static final Color TEXT_MUTED = new Color(90, 110, 130);

    private final AppointmentModel appointmentModel = new AppointmentModel();

    private OfficeAvailabilityHeaderPanel headerPanel;

    // Offices: main combo + backing data for multi-selection
    private JComboBox<String> officeCombo;
    private java.util.List<String> allOffices = new ArrayList<>();
    private java.util.List<String> multiSelectedOffices = new ArrayList<>();

    // From date selectors
    private JComboBox<Integer> fromDayCombo;
    private JComboBox<String> fromMonthCombo;
    private JComboBox<Integer> fromYearCombo;

    // To date selectors
    private JComboBox<Integer> toDayCombo;
    private JComboBox<String> toMonthCombo;
    private JComboBox<Integer> toYearCombo;

    private JComboBox<String> fromTimeCombo;
    private JComboBox<String> toTimeCombo;
    private JCheckBox aggregateCheck;
    private JCheckBox busyDaysOnlyCheck;
    private JLabel statusLabel;

    private OfficeAvailabilityChartPanel chartPanel;

    // For dialog title / context
    private String currentOfficeLabelForDialog = "All Offices";

    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private static final String OFFICE_ALL = "All Offices";
    private static final String OFFICE_MULTI = "Select multiple...";

    public OfficeAvailabilityWindow() {
        setTitle("Office Availability");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUi();
        loadOffices();
        setDefaultDatesAndTimes();
        applyFilter(); // initial load

        pack();
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(BG_MAIN);
        setContentPane(root);

        // ===== HEADER TEXT =====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Office Availability Timeline");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(PRIMARY_DARK);

        JLabel subtitle = new JLabel("See free (green) and occupied (red) time for each day and time range");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        // ===== FILTER AREA (grouped, themed) =====
        JPanel filterContainer = new JPanel();
        filterContainer.setLayout(new BoxLayout(filterContainer, BoxLayout.Y_AXIS));
        filterContainer.setOpaque(true);
        filterContainer.setBackground(BG_PANEL);
        filterContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        "Filters"),
                new EmptyBorder(6, 8, 8, 8)));

        // ---- First row: Office + From date + To date ----
        JPanel firstRow = new JPanel(new GridLayout(1, 3, 12, 0));
        firstRow.setOpaque(false);

        // Office group
        JPanel officePanel = new JPanel(new BorderLayout(6, 4));
        officePanel.setOpaque(false);
        officePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                "Office"));

        JLabel officeLabel = new JLabel("Office:");
        officeLabel.setForeground(TEXT_MUTED);

        officeCombo = new JComboBox<>();
        officeCombo.setPreferredSize(new Dimension(170, 24));
        officeCombo.setBackground(Color.WHITE);
        officeCombo.addActionListener(e -> handleOfficeComboSelection());

        JPanel officeInner = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        officeInner.setOpaque(false);
        officeInner.add(officeLabel);
        officeInner.add(officeCombo);
        officePanel.add(officeInner, BorderLayout.CENTER);

        // From date group
        JPanel fromDateGroup = new JPanel(new BorderLayout(4, 2));
        fromDateGroup.setOpaque(false);
        fromDateGroup.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                "From date"));
        JPanel fromDatePanel = buildDatePanel(true);
        fromDateGroup.add(fromDatePanel, BorderLayout.CENTER);

        // To date group
        JPanel toDateGroup = new JPanel(new BorderLayout(4, 2));
        toDateGroup.setOpaque(false);
        toDateGroup.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                "To date"));
        JPanel toDatePanel = buildDatePanel(false);
        toDateGroup.add(toDatePanel, BorderLayout.CENTER);

        firstRow.add(officePanel);
        firstRow.add(fromDateGroup);
        firstRow.add(toDateGroup);

        // ---- Second row: Time, options, buttons ----
        JPanel timeOptionsPanel = new JPanel(new GridBagLayout());
        timeOptionsPanel.setOpaque(false);
        timeOptionsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                "Time window & options"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        // Time from
        c.gridx = 0;
        c.gridy = 0;
        JLabel fromTimeLabel = new JLabel("Time from:");
        fromTimeLabel.setForeground(TEXT_MUTED);
        timeOptionsPanel.add(fromTimeLabel, c);

        c.gridx = 1;
        fromTimeCombo = new JComboBox<>(buildTimeOptions(false));
        ((JComponent) fromTimeCombo).setPreferredSize(new Dimension(80, 24));
        fromTimeCombo.setBackground(Color.WHITE);
        timeOptionsPanel.add(fromTimeCombo, c);

        // Time to
        c.gridx = 2;
        JLabel toTimeLabel = new JLabel("Time to:");
        toTimeLabel.setForeground(TEXT_MUTED);
        timeOptionsPanel.add(toTimeLabel, c);

        c.gridx = 3;
        toTimeCombo = new JComboBox<>(buildTimeOptions(true));
        ((JComponent) toTimeCombo).setPreferredSize(new Dimension(80, 24));
        toTimeCombo.setBackground(Color.WHITE);
        timeOptionsPanel.add(toTimeCombo, c);

        // Checkboxes
        c.gridx = 0;
        c.gridy = 1;
        aggregateCheck = new JCheckBox("Aggregate busy slots");
        aggregateCheck.setSelected(true);
        aggregateCheck.setOpaque(false);
        aggregateCheck.setForeground(TEXT_MUTED);
        timeOptionsPanel.add(aggregateCheck, c);

        c.gridx = 1;
        busyDaysOnlyCheck = new JCheckBox("Show only days with usage");
        busyDaysOnlyCheck.setSelected(false);
        busyDaysOnlyCheck.setOpaque(false);
        busyDaysOnlyCheck.setForeground(TEXT_MUTED);
        timeOptionsPanel.add(busyDaysOnlyCheck, c);

        // Buttons (Reset + Apply) on the right
        c.gridx = 4;
        c.gridy = 0;
        c.gridheight = 2;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.EAST;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttonPanel.setOpaque(false);

        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetButton.setFocusPainted(false);
        resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetButton.addActionListener(e -> resetFilters());

        JButton applyButton = new JButton("Apply");
        applyButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        applyButton.setBackground(PRIMARY_DARK);
        applyButton.setForeground(Color.BLACK);
        applyButton.setFocusPainted(false);
        applyButton.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        applyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyButton.addActionListener(e -> applyFilter());

        buttonPanel.add(resetButton);
        buttonPanel.add(applyButton);
        timeOptionsPanel.add(buttonPanel, c);

        filterContainer.add(firstRow);
        filterContainer.add(Box.createVerticalStrut(6));
        filterContainer.add(timeOptionsPanel);

        // Wrap header + filters vertically in NORTH
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(header);
        topPanel.add(Box.createVerticalStrut(6));
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        topPanel.add(sep);
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(filterContainer);

        root.add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: CHART (scrollable) =====
        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.setOpaque(false);

        chartPanel = new OfficeAvailabilityChartPanel();
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setAppointmentClickListener((date, segment) -> {
            OfficeAvailabilityDetailsDialog dialog =
                    new OfficeAvailabilityDetailsDialog(
                            this,
                            appointmentModel,
                            currentOfficeLabelForDialog,
                            date,
                            segment);
            dialog.setVisible(true);
        });

        JScrollPane scroll = new JScrollPane(chartPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        "Daily availability timeline"),
                new EmptyBorder(4, 4, 4, 4)));

        // Fixed header with legend + time axis
        headerPanel = new OfficeAvailabilityHeaderPanel();
        headerPanel.setBackground(BG_PANEL);
        scroll.setColumnHeaderView(headerPanel);

        center.add(scroll, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // ===== STATUS BAR =====
        statusLabel = new JLabel(" ", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_MUTED);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(250, 252, 255));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(4, 8, 4, 8)));
        root.add(statusLabel, BorderLayout.SOUTH);
    }

    /**
     * Build small panel with three JComboBoxes: Day, Month (name), Year.
     */
    private JPanel buildDatePanel(boolean isFrom) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panel.setOpaque(false);

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        // Day combo: 1..31
        Integer[] days = new Integer[31];
        for (int i = 0; i < 31; i++) {
            days[i] = i + 1;
        }
        JComboBox<Integer> dayCombo = new JComboBox<>(days);
        dayCombo.setPreferredSize(new Dimension(50, 24));
        dayCombo.setBackground(Color.WHITE);

        // Month combo: month names
        JComboBox<String> monthCombo = new JComboBox<>(MONTH_NAMES);
        monthCombo.setPreferredSize(new Dimension(100, 24));
        monthCombo.setBackground(Color.WHITE);

        // Year combo: [currentYear - 10, currentYear + 5]
        int minYear = currentYear - 10;
        int maxYear = currentYear + 5;
        Integer[] years = new Integer[maxYear - minYear + 1];
        for (int y = minYear, i = 0; y <= maxYear; y++, i++) {
            years[i] = y;
        }
        JComboBox<Integer> yearCombo = new JComboBox<>(years);
        yearCombo.setPreferredSize(new Dimension(80, 24));
        yearCombo.setBackground(Color.WHITE);

        panel.add(dayCombo);
        panel.add(monthCombo);
        panel.add(yearCombo);

        if (isFrom) {
            fromDayCombo = dayCombo;
            fromMonthCombo = monthCombo;
            fromYearCombo = yearCombo;
        } else {
            toDayCombo = dayCombo;
            toMonthCombo = monthCombo;
            toYearCombo = yearCombo;
        }

        return panel;
    }

    // ==== Offices: load + combo behaviour ====

    private void loadOffices() {
        try {
            allOffices.clear();
            List<String> rooms = appointmentModel.getAllRooms();
            if (rooms != null) {
                allOffices.addAll(rooms);
            }

            officeCombo.removeAllItems();
            officeCombo.addItem(OFFICE_ALL);
            officeCombo.addItem(OFFICE_MULTI);
            for (String r : allOffices) {
                officeCombo.addItem(r);
            }

            officeCombo.setSelectedItem(OFFICE_ALL);
            multiSelectedOffices.clear();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading offices/rooms: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleOfficeComboSelection() {
        Object sel = officeCombo.getSelectedItem();
        if (sel == null) {
            return;
        }
        String selected = sel.toString();

        if (OFFICE_MULTI.equals(selected)) {
            // Open multi-select dialog
            showMultiOfficeDialog();
        } else if (OFFICE_ALL.equals(selected)) {
            // Clear custom multi-selection and use all
            multiSelectedOffices.clear();
        } else {
            // Single office selected, override multi-selection
            multiSelectedOffices.clear();
            multiSelectedOffices.add(selected);
        }
    }

    private void showMultiOfficeDialog() {
        JDialog dialog = new JDialog(this, "Select offices", true);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setSize(300, 320);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_MAIN);

        JLabel label = new JLabel("Select one or more offices:");
        label.setBorder(new EmptyBorder(8, 8, 0, 8));
        label.setForeground(TEXT_MUTED);
        dialog.add(label, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String office : allOffices) {
            model.addElement(office);
        }

        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        list.setBackground(Color.WHITE);

        // Preselect any currently chosen offices (excluding ALL)
        if (!multiSelectedOffices.isEmpty()) {
            java.util.List<Integer> indices = new ArrayList<>();
            for (String sel : multiSelectedOffices) {
                int idx = allOffices.indexOf(sel);
                if (idx >= 0) {
                    indices.add(idx);
                }
            }
            int[] arr = indices.stream().mapToInt(i -> i).toArray();
            list.setSelectedIndices(arr);
        }

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(new LineBorder(new Color(220, 220, 220)));
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton cancelBtn = new JButton("Cancel");
        JButton okBtn = new JButton("OK");

        cancelBtn.setFocusPainted(false);
        okBtn.setFocusPainted(false);

        cancelBtn.addActionListener(e -> dialog.dispose());
        okBtn.addActionListener(e -> {
            java.util.List<String> selOffices = list.getSelectedValuesList();
            if (selOffices.isEmpty()) {
                // if none selected -> revert to ALL
                multiSelectedOffices.clear();
                officeCombo.setSelectedItem(OFFICE_ALL);
            } else {
                multiSelectedOffices = new ArrayList<>(selOffices);
                officeCombo.setSelectedItem(OFFICE_MULTI);
            }
            dialog.dispose();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(okBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void setDefaultDatesAndTimes() {
        LocalDate today = LocalDate.now();
        LocalDate plus5 = today.plusDays(5);

        if (fromDayCombo != null) {
            fromDayCombo.setSelectedItem(today.getDayOfMonth());
        }
        if (fromMonthCombo != null) {
            fromMonthCombo.setSelectedItem(MONTH_NAMES[today.getMonthValue() - 1]);
        }
        if (fromYearCombo != null) {
            fromYearCombo.setSelectedItem(today.getYear());
        }

        if (toDayCombo != null) {
            toDayCombo.setSelectedItem(plus5.getDayOfMonth());
        }
        if (toMonthCombo != null) {
            toMonthCombo.setSelectedItem(MONTH_NAMES[plus5.getMonthValue() - 1]);
        }
        if (toYearCombo != null) {
            toYearCombo.setSelectedItem(plus5.getYear());
        }

        if (fromTimeCombo != null) {
            fromTimeCombo.setSelectedItem("00:00");
        }
        if (toTimeCombo != null) {
            toTimeCombo.setSelectedItem("24:00");
        }

        if (aggregateCheck != null) {
            aggregateCheck.setSelected(true);
        }
        if (busyDaysOnlyCheck != null) {
            busyDaysOnlyCheck.setSelected(false);
        }

        if (officeCombo != null) {
            officeCombo.setSelectedItem(OFFICE_ALL);
        }
        multiSelectedOffices.clear();
    }

    private void resetFilters() {
        setDefaultDatesAndTimes();
        applyFilter();
    }

    private String[] buildTimeOptions(boolean include24) {
        java.util.List<String> options = new java.util.ArrayList<>();
        for (int h = 0; h < 24; h++) {
            String label = String.format("%02d:00", h);
            options.add(label);
        }
        if (include24) {
            options.add("24:00"); // UI-only; internally treated as 23:59
        }
        return options.toArray(new String[0]);
    }

    // ==== Apply filter: use multiSelectedOffices to actually filter ====

    private void applyFilter() {
        // 1) Determine which offices to include
        List<String> officesToUse;

        if (!multiSelectedOffices.isEmpty()) {
            officesToUse = new ArrayList<>(multiSelectedOffices);
        } else {
            Object sel = officeCombo.getSelectedItem();
            String selected = (sel == null) ? OFFICE_ALL : sel.toString();
            if (OFFICE_ALL.equals(selected) || OFFICE_MULTI.equals(selected)) {
                officesToUse = new ArrayList<>(allOffices); // all real offices
            } else {
                officesToUse = Collections.singletonList(selected);
            }
        }

        String officeStatusText;
        if (officesToUse.isEmpty()) {
            officeStatusText = "No offices";
        } else if (officesToUse.size() == 1) {
            officeStatusText = officesToUse.get(0);
        } else if (officesToUse.size() == allOffices.size()) {
            officeStatusText = "All Offices";
        } else {
            officeStatusText = "Multiple offices";
        }

        LocalDate fromDate;
        LocalDate toDate;

        try {
            fromDate = buildDateFromSelectors(fromDayCombo, fromMonthCombo, fromYearCombo);
            toDate = buildDateFromSelectors(toDayCombo, toMonthCombo, toYearCombo);
        } catch (IllegalArgumentException | DateTimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid from/to date combination.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (toDate.isBefore(fromDate)) {
            JOptionPane.showMessageDialog(this,
                    "End date must be on or after start date.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fromTimeStr = (String) fromTimeCombo.getSelectedItem();
        String toTimeStr = (String) toTimeCombo.getSelectedItem();

        LocalTime windowStart;
        LocalTime windowEnd;
        try {
            windowStart = LocalTime.parse(fromTimeStr);
            if ("24:00".equals(toTimeStr)) {
                windowEnd = LocalTime.of(23, 59);
            } else {
                windowEnd = LocalTime.parse(toTimeStr);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid time format.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!windowEnd.isAfter(windowStart)) {
            JOptionPane.showMessageDialog(this,
                    "End time must be after start time.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (headerPanel != null) {
            headerPanel.setTimeWindow(windowStart, windowEnd);
        }

        // 2) Fetch appointments for exactly those offices
        List<AppointmentDTO> raw = new ArrayList<>();
        for (String office : officesToUse) {
            List<AppointmentDTO> part = appointmentModel.getAppointmentsByOfficeAndDateRange(
                    office, fromDate.toString(), toDate.toString());
            if (part != null && !part.isEmpty()) {
                raw.addAll(part);
            }
        }

        if (raw.isEmpty()) {
            chartPanel.clearData();
            statusLabel.setText("No usage data available for selected offices and date range.");
            currentOfficeLabelForDialog = officeStatusText;
            return;
        }

        boolean aggregate = aggregateCheck.isSelected();

        // 3) Build availability timeline from these appointments
        Map<LocalDate, List<OfficeAvailabilitySegment>> availability =
                buildAvailability(raw, fromDate, toDate, windowStart, windowEnd, aggregate);

        // Optionally remove fully-free days
        if (busyDaysOnlyCheck.isSelected()) {
            availability.entrySet().removeIf(e -> {
                List<OfficeAvailabilitySegment> segs = e.getValue();
                if (segs == null || segs.isEmpty()) return true;
                boolean anyBusy = segs.stream().anyMatch(OfficeAvailabilitySegment::isBusy);
                return !anyBusy;
            });

            if (availability.isEmpty()) {
                chartPanel.clearData();
                statusLabel.setText("No days with usage found for the selected filters.");
                currentOfficeLabelForDialog = officeStatusText;
                return;
            }
        }

        chartPanel.setData(availability, windowStart, windowEnd);
        currentOfficeLabelForDialog = officeStatusText;

        statusLabel.setText(String.format(
                "Offices: %s | %s to %s | Time: %s–%s | Appointments: %d",
                officeStatusText, fromDate, toDate, fromTimeStr, toTimeStr, raw.size()
        ));
    }

    /**
     * Builds a LocalDate from three JComboBoxes (day, month name, year).
     */
    private LocalDate buildDateFromSelectors(JComboBox<Integer> dayCb,
                                             JComboBox<String> monthCb,
                                             JComboBox<Integer> yearCb) {
        if (dayCb == null || monthCb == null || yearCb == null) {
            throw new IllegalArgumentException("Date selectors not initialized");
        }

        Integer day = (Integer) dayCb.getSelectedItem();
        String monthName = (String) monthCb.getSelectedItem();
        Integer year = (Integer) yearCb.getSelectedItem();

        if (day == null || monthName == null || year == null) {
            throw new IllegalArgumentException("Date selectors contain null");
        }

        int month = monthNameToNumber(monthName);
        if (month == -1) {
            throw new IllegalArgumentException("Invalid month name: " + monthName);
        }

        return LocalDate.of(year, month, day);
    }

    private int monthNameToNumber(String name) {
        if (name == null) return -1;
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(name.trim())) {
                return i + 1; // 1-based month
            }
        }
        return -1;
    }

    // ==== Availability builder (from raw appointments, for selected offices) ====

    private Map<LocalDate, List<OfficeAvailabilitySegment>> buildAvailability(
            List<AppointmentDTO> appointments,
            LocalDate fromDate,
            LocalDate toDate,
            LocalTime windowStart,
            LocalTime windowEnd,
            boolean aggregateBusy
    ) {
        Map<LocalDate, List<OfficeAvailabilitySegment>> result = new LinkedHashMap<>();

        // Group appointments by date
        Map<LocalDate, List<AppointmentDTO>> byDate = new HashMap<>();
        for (AppointmentDTO dto : appointments) {
            try {
                if (dto.getDate() == null) continue;
                LocalDate d = LocalDate.parse(dto.getDate());
                if (d.isBefore(fromDate) || d.isAfter(toDate)) continue;
                byDate.computeIfAbsent(d, k -> new ArrayList<>()).add(dto);
            } catch (Exception ignored) { }
        }

        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            List<AppointmentDTO> dayAppts = byDate.getOrDefault(d, Collections.emptyList());
            List<OfficeAvailabilitySegment> segmentsForDay =
                    buildDaySegments(d, dayAppts, windowStart, windowEnd, aggregateBusy);
            result.put(d, segmentsForDay);
        }

        return result;
    }

    private static class Interval {
        LocalTime start;
        LocalTime end;
        List<AppointmentDTO> apps = new ArrayList<>();
    }

    private List<OfficeAvailabilitySegment> buildDaySegments(
            LocalDate date,
            List<AppointmentDTO> dayAppts,
            LocalTime windowStart,
            LocalTime windowEnd,
            boolean aggregateBusy
    ) {
        List<OfficeAvailabilitySegment> segments = new ArrayList<>();

        // Build basic intervals from appointments
        List<Interval> intervals = new ArrayList<>();
        for (AppointmentDTO dto : dayAppts) {
            try {
                if (dto.getStart_time() == null || dto.getEnd_time() == null)
                    continue;
                LocalTime s = LocalTime.parse(dto.getStart_time());
                LocalTime e = LocalTime.parse(dto.getEnd_time());
                if (!e.isAfter(s)) continue;

                // Clamp to window
                LocalTime cs = s.isBefore(windowStart) ? windowStart : s;
                LocalTime ce = e.isAfter(windowEnd) ? windowEnd : e;
                if (!ce.isAfter(cs)) continue;

                Interval in = new Interval();
                in.start = cs;
                in.end = ce;
                in.apps.add(dto);
                intervals.add(in);
            } catch (Exception ignored) { }
        }

        if (intervals.isEmpty()) {
            // Entire day free
            segments.add(createSegment(date, windowStart, windowEnd, false, Collections.emptyList()));
            return segments;
        }

        // Sort by start
        intervals.sort(Comparator.comparing(i -> i.start));

        List<Interval> mergedBusy = new ArrayList<>();

        if (aggregateBusy) {
            // Merge overlapping intervals into bigger busy blocks
            Interval current = null;
            for (Interval in : intervals) {
                if (current == null) {
                    current = copyInterval(in);
                } else if (!in.start.isAfter(current.end)) {
                    // Overlaps or touches
                    if (in.end.isAfter(current.end)) {
                        current.end = in.end;
                    }
                    current.apps.addAll(in.apps);
                } else {
                    mergedBusy.add(current);
                    current = copyInterval(in);
                }
            }
            if (current != null) {
                mergedBusy.add(current);
            }
        } else {
            // Keep 1 busy block per appointment (still sorted)
            mergedBusy.addAll(intervals);
        }

        // Build free segments between busy ones
        List<OfficeAvailabilitySegment> allSegments = new ArrayList<>();

        LocalTime cursor = windowStart;

        for (Interval b : mergedBusy) {
            if (b.start.isAfter(cursor)) {
                // free block before this busy
                allSegments.add(createSegment(date, cursor, b.start, false, Collections.emptyList()));
            }
            // busy block
            allSegments.add(createSegment(date, b.start, b.end, true, new ArrayList<>(b.apps)));
            cursor = b.end;
        }

        if (cursor.isBefore(windowEnd)) {
            allSegments.add(createSegment(date, cursor, windowEnd, false, Collections.emptyList()));
        }

        return allSegments;
    }

    private Interval copyInterval(Interval in) {
        Interval c = new Interval();
        c.start = in.start;
        c.end = in.end;
        c.apps.addAll(in.apps);
        return c;
    }

    /**
     * Helper to create an OfficeAvailabilitySegment using your actual constructor.
     */
    private OfficeAvailabilitySegment createSegment(
            LocalDate date,
            LocalTime from,
            LocalTime to,
            boolean busy,
            List<AppointmentDTO> apps
    ) {
        OfficeAvailabilitySegment seg = new OfficeAvailabilitySegment(date, from, to, busy);
        if (apps != null) {
            for (AppointmentDTO dto : apps) {
                seg.addAppointment(dto); // also updates appointmentCount
            }
        }
        return seg;
    }
}
