package giis.demo.ui.manager;

import giis.demo.service.appointment.AppointmentModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.stream.IntStream;

public class ManagerWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private AppointmentModel model = new AppointmentModel();

    // Calendar state
    private int currentYear;
    private int currentMonth; // 1-12

    // UI components
    private JLabel monthLabel;
    private JLabel yearLabel;
    private JPanel calendarPanel;
    private JLabel infoLabel;
    private JList<Integer> yearList;
    private JList<String> monthList;

    // UI colors
    private static final Color COLOR_PRIMARY = new Color(25, 118, 210);
    private static final Color COLOR_TEXT_LIGHT = new Color(97, 97, 97);
    private static final Color COLOR_DAY_BORDER = new Color(224, 224, 224);
    private static final Color COLOR_HAS_APPTS_BG = new Color(232, 245, 233); // light green
    private static final Color COLOR_HAS_APPTS_BORDER = new Color(56, 142, 60);
    private static final Color COLOR_TODAY_BG = new Color(227, 242, 253);
    private static final Color COLOR_TODAY_BORDER = new Color(25, 118, 210);

    public ManagerWindow() {
        setTitle("Manager - Daily Appointments Summary");
        setSize(720, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        LocalDate today = LocalDate.now();
        currentYear = today.getYear();
        currentMonth = today.getMonthValue();

        // === MAIN PANEL ===
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);

        // === HEADER (Month / Year with arrows + Today button) ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JButton prevButton = createNavButton("◄");
        prevButton.addActionListener(e -> changeMonth(-1));

        JButton nextButton = createNavButton("►");
        nextButton.addActionListener(e -> changeMonth(1));

        // Title + Today Button together
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        titlePanel.setBackground(Color.WHITE);

        monthLabel = new JLabel();
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        monthLabel.setForeground(COLOR_PRIMARY);

        yearLabel = new JLabel();
        yearLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        yearLabel.setForeground(COLOR_TEXT_LIGHT);

        // Large Today button
        JButton todayButton = new JButton("Today");
        todayButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        todayButton.setFocusPainted(false);
        todayButton.setBackground(COLOR_PRIMARY);
        todayButton.setForeground(Color.WHITE);
        todayButton.setPreferredSize(new Dimension(90, 30));
        todayButton.setBorder(BorderFactory.createLineBorder(new Color(13, 71, 161)));
        todayButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        todayButton.addActionListener(e -> goToToday());

        titlePanel.add(monthLabel);
        titlePanel.add(yearLabel);
        titlePanel.add(todayButton);

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // === SIDE COLUMN: YEARS (LEFT) ===
        int baseYear = currentYear;
        Integer[] years = IntStream.rangeClosed(baseYear - 10, baseYear + 10)
                .boxed().toArray(Integer[]::new);

        yearList = new JList<>(years);
        yearList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        yearList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        yearList.setFixedCellHeight(22);
        yearList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Integer y = yearList.getSelectedValue();
                if (y != null && y != currentYear) {
                    currentYear = y;
                    refreshHeader();
                    refreshCalendar();
                }
            }
        });

        JPanel yearPanel = new JPanel(new BorderLayout());
        yearPanel.setBackground(Color.WHITE);

        JLabel yearTitle = new JLabel("Year");
        yearTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        yearTitle.setHorizontalAlignment(SwingConstants.CENTER);
        yearTitle.setBorder(new EmptyBorder(0, 0, 4, 0));

        yearPanel.add(yearTitle, BorderLayout.NORTH);
        yearPanel.add(new JScrollPane(yearList), BorderLayout.CENTER);
        yearPanel.setPreferredSize(new Dimension(70, 0));

        mainPanel.add(yearPanel, BorderLayout.WEST);

        // === SIDE COLUMN: MONTHS (RIGHT) ===
        String[] monthNames = java.util.Arrays.stream(Month.values())
                .map(m -> m.name().substring(0, 1) + m.name().substring(1).toLowerCase())
                .toArray(String[]::new);

        monthList = new JList<>(monthNames);
        monthList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        monthList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        monthList.setFixedCellHeight(22);
        monthList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = monthList.getSelectedIndex();
                if (index != -1 && (index + 1) != currentMonth) {
                    currentMonth = index + 1;
                    refreshHeader();
                    refreshCalendar();
                }
            }
        });

        JPanel monthPanel = new JPanel(new BorderLayout());
        monthPanel.setBackground(Color.WHITE);

        JLabel monthTitle = new JLabel("Month");
        monthTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        monthTitle.setHorizontalAlignment(SwingConstants.CENTER);
        monthTitle.setBorder(new EmptyBorder(0, 0, 4, 0));

        monthPanel.add(monthTitle, BorderLayout.NORTH);
        monthPanel.add(new JScrollPane(monthList), BorderLayout.CENTER);
        monthPanel.setPreferredSize(new Dimension(90, 0));

        mainPanel.add(monthPanel, BorderLayout.EAST);

        // === CENTER: CALENDAR GRID ===
        calendarPanel = new JPanel();
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(calendarPanel, BorderLayout.CENTER);

        // === BOTTOM: INFO LABEL ===
        infoLabel = new JLabel(" ", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        infoLabel.setForeground(COLOR_PRIMARY);
        infoLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        mainPanel.add(infoLabel, BorderLayout.SOUTH);

        // === INITIALIZATION (default to today's date) ===
        refreshHeader();
        syncYearMonthSelection();
        refreshCalendar();
        showAppointmentCount(today); // show today's appointments on first open

        setVisible(true);
    }

    // === UI HELPERS ===

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void refreshHeader() {
        Month m = Month.of(currentMonth);
        String monthName = m.name().substring(0, 1) + m.name().substring(1).toLowerCase();
        monthLabel.setText(monthName);
        yearLabel.setText(String.valueOf(currentYear));
    }

    private void changeMonth(int delta) {
        currentMonth += delta;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        } else if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        syncYearMonthSelection();
        refreshHeader();
        refreshCalendar();
    }

    private void goToToday() {
        LocalDate today = LocalDate.now();
        currentYear = today.getYear();
        currentMonth = today.getMonthValue();
        syncYearMonthSelection();
        refreshHeader();
        refreshCalendar();
        showAppointmentCount(today);
    }

    private void syncYearMonthSelection() {
        // Sync year list
        ListModel<Integer> yearModel = yearList.getModel();
        for (int i = 0; i < yearModel.getSize(); i++) {
            if (yearModel.getElementAt(i) == currentYear) {
                yearList.setSelectedIndex(i);
                yearList.ensureIndexIsVisible(i);
                break;
            }
        }
        // Sync month list
        if (currentMonth >= 1 && currentMonth <= 12) {
            monthList.setSelectedIndex(currentMonth - 1);
            monthList.ensureIndexIsVisible(currentMonth - 1);
        }
    }

    private void refreshCalendar() {
        calendarPanel.removeAll();
        calendarPanel.setLayout(new GridLayout(0, 7, 6, 6));

        // Weekday headers (Mon-Sun)
        String[] weekDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String wd : weekDays) {
            JLabel lbl = new JLabel(wd, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(new Color(120, 144, 156));
            calendarPanel.add(lbl);
        }

        LocalDate firstDay = LocalDate.of(currentYear, currentMonth, 1);
        int lengthOfMonth = firstDay.lengthOfMonth();

        // Leading blanks (ISO: Monday = 1)
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue(); // 1-7
        int leadingBlanks = firstDayOfWeek - DayOfWeek.MONDAY.getValue();
        if (leadingBlanks < 0) leadingBlanks += 7;

        for (int i = 0; i < leadingBlanks; i++) {
            calendarPanel.add(new JLabel(""));
        }

        LocalDate today = LocalDate.now();

        for (int day = 1; day <= lengthOfMonth; day++) {
            LocalDate date = LocalDate.of(currentYear, currentMonth, day);

            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFocusPainted(false);
            dayButton.setMargin(new Insets(2, 2, 2, 2));
            dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            dayButton.setBackground(Color.WHITE);
            dayButton.setForeground(COLOR_TEXT_LIGHT);
            dayButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dayButton.setBorder(BorderFactory.createLineBorder(COLOR_DAY_BORDER));
            dayButton.setOpaque(true);

            int count = 0;
            try {
                count = model.getAppointmentCountByDate(date.toString());
            } catch (Exception ex) {
                // Ignore per-day errors; keep default styling
            }

            // Style days with appointments
            if (count > 0) {
                String labelHtml = "<html><center>" + day +
                        "<br/><span style='font-size:9px; color:#2E7D32;'>(" + count + ")</span></center></html>";
                dayButton.setText(labelHtml);
                dayButton.setBackground(COLOR_HAS_APPTS_BG);
                dayButton.setBorder(BorderFactory.createLineBorder(COLOR_HAS_APPTS_BORDER));
                dayButton.setToolTipText(count == 1
                        ? "1 appointment"
                        : count + " appointments");
            } else {
                dayButton.setToolTipText("No appointments");
            }

            // Highlight today
            if (date.equals(today)) {
                dayButton.setBackground(COLOR_TODAY_BG);
                dayButton.setBorder(BorderFactory.createLineBorder(COLOR_TODAY_BORDER, 2));
            }

            // Click: show detailed count for that date
            final LocalDate selectedDate = date;
            dayButton.addActionListener(e -> showAppointmentCount(selectedDate));

            calendarPanel.add(dayButton);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    // === DISPLAY SELECTED DAY INFO (UI ONLY) ===

    private void showAppointmentCount(LocalDate date) {
        try {
            int count = model.getAppointmentCountByDate(date.toString());
            String formattedDate = date.getDayOfMonth() + " " +
                    date.getMonth().name().substring(0, 1).toUpperCase() +
                    date.getMonth().name().substring(1).toLowerCase() +
                    " " + date.getYear();

            String color = (count == 0) ? "#9E9E9E" : "#0D47A1";
            String text;

            if (count == 0) {
                text = "No appointments scheduled.";
            } else if (count == 1) {
                text = "There is <b>1</b> appointment scheduled.";
            } else {
                text = "There are <b>" + count + "</b> appointments scheduled.";
            }

            infoLabel.setText("<html><div style='text-align:center;'>📅 <b>"
                    + formattedDate + "</b><br>"
                    + "<span style='font-size:18px; color:" + color + ";'>" + text + "</span>"
                    + "</div></html>");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "❌ Error retrieving appointment count: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // For standalone testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ManagerWindow::new);
    }
}
