package giis.demo.ui.manager;

import giis.demo.service.appointment.AppointmentDiseaseStatsDTO;
import giis.demo.service.appointment.TimeInterval;
import giis.demo.service.manager.ReportsController;
import giis.demo.service.manager.ReportsControllerImpl;
import giis.demo.util.Database;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

public class DiseaseTrendWindow extends JFrame {

    private static final int MAX_SELECTED_DISEASES = 5;

    // ===== BLUE THEME COLORS =====
    private static final Color COLOR_PRIMARY = new Color(33, 150, 243);
    private static final Color COLOR_PRIMARY_DARK = new Color(25, 118, 210);
    private static final Color COLOR_PRIMARY_LIGHT = new Color(227, 242, 253);
    private static final Color COLOR_BACKGROUND = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(189, 189, 189);
    private static final Color COLOR_LABEL_DARK = new Color(55, 71, 79);

    private final ReportsController controller = new ReportsControllerImpl();
    private final Database db = new Database();

    private JPanel filterPanel;

    private final JComboBox<TimeInterval> intervalCombo = new JComboBox<>(TimeInterval.values());

    private final JComboBox<Integer> fromYearCombo = new JComboBox<>();
    private final JComboBox<Integer> fromMonthCombo = new JComboBox<>();
    private final JComboBox<Integer> fromDayCombo = new JComboBox<>();
    private final JComboBox<Integer> toYearCombo = new JComboBox<>();
    private final JComboBox<Integer> toMonthCombo = new JComboBox<>();
    private final JComboBox<Integer> toDayCombo = new JComboBox<>();

    private final JComboBox<String> fromTimeCombo = new JComboBox<>();
    private final JComboBox<String> toTimeCombo = new JComboBox<>();
    private JLabel timeLabel;
    private JLabel timeToText; // separator label between time combos

    private JLabel fromLabel, toLabel;

    private final DefaultListModel<DiseaseItem> diseaseListModel = new DefaultListModel<>();
    private final JList<DiseaseItem> diseaseList = new JList<>(diseaseListModel);

    // Full ICD-10 list (for filtering)
    private final java.util.List<DiseaseItem> allDiseases = new ArrayList<>();
    private final JTextField diseaseSearchField = new JTextField(16);
    private final JComboBox<String> icdChapterCombo = new JComboBox<>();
    private final JCheckBox onlyWithAppointmentsCheckBox = new JCheckBox("Only with appointments");

    private final AppointmentDiseaseChartPanel chartPanel = new AppointmentDiseaseChartPanel();
    private final JLabel xAxisLabel = new JLabel("", SwingConstants.CENTER);

    // Legend in top blue panel
    private JPanel legendPanel;
    private JPanel legendContentPanel;

    private boolean suppressEvents = false;

    public DiseaseTrendWindow() {
        setTitle("Appointments by Disease Type");
        setSize(1300, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUi();
        initDateCombos();
        loadDiseases();
        updateIntervalUi();
        updateLegendBox();
    }

    // ====== GENERAL STYLING HELPERS ======
    private void stylePrimaryButton(JButton button) {
        button.setBackground(new Color(245, 245, 245));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, Color.BLACK),
                new EmptyBorder(6, 18, 6, 18)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 15f));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(COLOR_PRIMARY_DARK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, COLOR_BORDER),
                new EmptyBorder(4, 10, 4, 10)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 13f));
    }

    private void styleFlatCombo(JComboBox<?> combo) {
        combo.setBackground(Color.WHITE);
        combo.setBorder(new MatteBorder(1, 1, 1, 1, COLOR_BORDER));
        combo.setFont(combo.getFont().deriveFont(Font.PLAIN, 14f));
        combo.setPreferredSize(new Dimension(120, 34));
    }

    private void styleFlatSmallCombo(JComboBox<?> combo) {
        combo.setBackground(Color.WHITE);
        combo.setBorder(new MatteBorder(1, 1, 1, 1, COLOR_BORDER));
        combo.setFont(combo.getFont().deriveFont(Font.PLAIN, 14f));
        combo.setPreferredSize(new Dimension(85, 32));
    }

    private void styleFilterLabel(JLabel label) {
        label.setForeground(COLOR_LABEL_DARK);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 14f));
    }

    private void initUi() {
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // === TOP FILTER BAR (interval + date/time) ===
        filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(COLOR_PRIMARY_LIGHT);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(8, 20, 8, 20)
        ));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 8, 4, 8);

        // Title row (centered)
        JLabel title = new JLabel("Appointments by Disease Type");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 19f));
        title.setForeground(COLOR_PRIMARY_DARK);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        filterPanel.add(title, c);

        // For all following rows: left align, stretch horizontally
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.weightx = 1.0;

        // Initialize labels
        fromLabel = new JLabel("From:");
        toLabel = new JLabel("To:");
        timeLabel = new JLabel("Time (24h):");

        JLabel intervalLabel = new JLabel("Interval:");
        styleFilterLabel(intervalLabel);
        styleFlatCombo(intervalCombo);
        intervalCombo.setPreferredSize(new Dimension(160, 34));

        JButton applyButton = new JButton("Apply");
        stylePrimaryButton(applyButton);

        JButton resetButton = new JButton("Reset");
        styleSecondaryButton(resetButton);

        JButton todayButton = new JButton("Today");
        styleSecondaryButton(todayButton);

        // LEFT-align interval row
        JPanel intervalRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        intervalRow.setOpaque(false);
        intervalRow.add(intervalLabel);
        intervalRow.add(intervalCombo);
        intervalRow.add(Box.createHorizontalStrut(24));
        intervalRow.add(applyButton);
        intervalRow.add(resetButton);
        intervalRow.add(todayButton);

        c.gridx = 0;
        c.gridy = 1;
        filterPanel.add(intervalRow, c);

        // Prepare combos styling and sizes for date/time
        styleFlatSmallCombo(fromYearCombo);
        styleFlatSmallCombo(fromMonthCombo);
        styleFlatSmallCombo(fromDayCombo);
        styleFlatSmallCombo(toYearCombo);
        styleFlatSmallCombo(toMonthCombo);
        styleFlatSmallCombo(toDayCombo);
        styleFlatCombo(fromTimeCombo);
        styleFlatCombo(toTimeCombo);

        // From row (left aligned)
        styleFilterLabel(fromLabel);
        JPanel fromRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        fromRow.setOpaque(false);
        fromRow.add(fromLabel);
        fromRow.add(fromYearCombo);
        fromRow.add(fromMonthCombo);
        fromRow.add(fromDayCombo);

        c.gridy = 2;
        c.gridx = 0;
        filterPanel.add(fromRow, c);

        // To row (left aligned)
        styleFilterLabel(toLabel);
        JPanel toRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        toRow.setOpaque(false);
        toRow.add(toLabel);
        toRow.add(toYearCombo);
        toRow.add(toMonthCombo);
        toRow.add(toDayCombo);

        c.gridy = 3;
        c.gridx = 0;
        filterPanel.add(toRow, c);

        // Fill time combos once (00:00–23:00)
        fromTimeCombo.removeAllItems();
        toTimeCombo.removeAllItems();
        for (int h = 0; h < 24; h++) {
            String label = String.format("%02d:00", h);
            fromTimeCombo.addItem(label);
            toTimeCombo.addItem(label);
        }
        fromTimeCombo.setSelectedItem("00:00");
        toTimeCombo.setSelectedItem("23:00");

        // Hour range row (left aligned)
        styleFilterLabel(timeLabel);
        timeToText = new JLabel("to");
        styleFilterLabel(timeToText);

        JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        timeRow.setOpaque(false);
        timeRow.add(timeLabel);
        timeRow.add(fromTimeCombo);
        timeRow.add(timeToText);
        timeRow.add(toTimeCombo);

        c.gridy = 4;
        c.gridx = 0;
        filterPanel.add(timeRow, c);

        // ===== LEGEND BOX IN TOP BLUE PANEL (RIGHT SIDE) =====
        legendPanel = new JPanel(new BorderLayout());
        legendPanel.setBackground(Color.WHITE);
        legendPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                "Legend"
        ));
        legendPanel.setPreferredSize(new Dimension(320, 170)); // bigger legend box

        legendContentPanel = new JPanel();
        legendContentPanel.setOpaque(false);
        legendContentPanel.setLayout(new BoxLayout(legendContentPanel, BoxLayout.Y_AXIS));
        legendContentPanel.setBorder(new EmptyBorder(4, 8, 4, 8));

        JScrollPane legendScroll = new JScrollPane(legendContentPanel);
        legendScroll.setBorder(null);
        legendScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        legendScroll.getViewport().setBackground(Color.WHITE);
        legendPanel.add(legendScroll, BorderLayout.CENTER);

        GridBagConstraints cLegend = new GridBagConstraints();
        cLegend.gridx = 1;
        cLegend.gridy = 0;
        cLegend.gridheight = 5;
        cLegend.insets = new Insets(4, 16, 4, 4);
        cLegend.anchor = GridBagConstraints.NORTH;
        cLegend.fill = GridBagConstraints.BOTH;
        cLegend.weightx = 0;
        cLegend.weighty = 1.0;
        filterPanel.add(legendPanel, cLegend);

        add(filterPanel, BorderLayout.NORTH);

        // === LEFT ICD-10 PANEL ===
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));
        leftPanel.setPreferredSize(new Dimension(430, 100));
        leftPanel.setMinimumSize(new Dimension(360, 100));
        leftPanel.setBackground(COLOR_BACKGROUND);

        // Header row: label + All/None
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel diseaseLabel = new JLabel("ICD-10 Diseases – select up to 5");
        diseaseLabel.setFont(diseaseLabel.getFont().deriveFont(Font.BOLD, 13.5f));
        diseaseLabel.setForeground(COLOR_LABEL_DARK);
        headerRow.add(diseaseLabel, BorderLayout.WEST);

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        selectPanel.setOpaque(false);
        JButton btnSelectAll = new JButton("All");
        JButton btnClear = new JButton("None");
        styleSecondaryButton(btnSelectAll);
        styleSecondaryButton(btnClear);
        selectPanel.add(btnSelectAll);
        selectPanel.add(btnClear);
        headerRow.add(selectPanel, BorderLayout.EAST);

        // ICD-10 chapter filter
        icdChapterCombo.addItem("All chapters");
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            icdChapterCombo.addItem(ch + " *");
        }
        styleFlatCombo(icdChapterCombo);
        icdChapterCombo.setPreferredSize(new Dimension(190, 30));

        JPanel chapterPanel = new JPanel(new BorderLayout(6, 0));
        chapterPanel.setOpaque(false);
        JLabel chapterLabel = new JLabel("ICD-10 chapter:");
        styleFilterLabel(chapterLabel);
        chapterPanel.add(chapterLabel, BorderLayout.WEST);
        chapterPanel.add(icdChapterCombo, BorderLayout.CENTER);

        // Search/filter row
        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.setOpaque(false);
        JLabel filterLabel = new JLabel("Filter:");
        styleFilterLabel(filterLabel);
        diseaseSearchField.setBorder(new MatteBorder(1, 1, 1, 1, COLOR_BORDER));
        diseaseSearchField.setFont(diseaseSearchField.getFont().deriveFont(Font.PLAIN, 13.5f));
        diseaseSearchField.setPreferredSize(new Dimension(230, 28));
        searchPanel.add(filterLabel, BorderLayout.WEST);
        searchPanel.add(diseaseSearchField, BorderLayout.CENTER);

        // Only-with-appointments checkbox row
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkboxPanel.setOpaque(false);
        onlyWithAppointmentsCheckBox.setOpaque(false);
        onlyWithAppointmentsCheckBox.setForeground(COLOR_LABEL_DARK);
        onlyWithAppointmentsCheckBox.setFont(onlyWithAppointmentsCheckBox.getFont().deriveFont(Font.PLAIN, 13.5f));
        checkboxPanel.add(onlyWithAppointmentsCheckBox);

        // Stack left controls vertically using BoxLayout
        JPanel leftHeaderContainer = new JPanel();
        leftHeaderContainer.setLayout(new BoxLayout(leftHeaderContainer, BoxLayout.Y_AXIS));
        leftHeaderContainer.setOpaque(false);
        leftHeaderContainer.add(headerRow);
        leftHeaderContainer.add(Box.createVerticalStrut(6));
        leftHeaderContainer.add(chapterPanel);
        leftHeaderContainer.add(Box.createVerticalStrut(6));
        leftHeaderContainer.add(searchPanel);
        leftHeaderContainer.add(Box.createVerticalStrut(4));
        leftHeaderContainer.add(checkboxPanel);

        leftPanel.add(leftHeaderContainer, BorderLayout.NORTH);

        // Live filtering hooks
        diseaseSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyDiseaseFilter();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                applyDiseaseFilter();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                applyDiseaseFilter();
            }
        });

        icdChapterCombo.addActionListener(e -> {
            if (suppressEvents) return;
            applyDiseaseFilter();
        });

        onlyWithAppointmentsCheckBox.addActionListener(e -> {
            if (suppressEvents) return;
            applyDiseaseFilter();
        });

        diseaseList.setCellRenderer(new DiseaseCheckBoxRenderer());
        diseaseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        diseaseList.setFont(diseaseList.getFont().deriveFont(Font.PLAIN, 13.5f));

        diseaseList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = diseaseList.locationToIndex(e.getPoint());
                if (idx >= 0) {
                    DiseaseItem item = diseaseListModel.get(idx);
                    if (!item.selected) {
                        if (getSelectedDiseaseCount() >= MAX_SELECTED_DISEASES) {
                            JOptionPane.showMessageDialog(
                                    DiseaseTrendWindow.this,
                                    "You can select up to " + MAX_SELECTED_DISEASES + " diseases.",
                                    "Selection limit",
                                    JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        }
                        item.selected = true;
                    } else {
                        item.selected = false;
                    }
                    diseaseList.repaint(diseaseList.getCellBounds(idx, idx));
                    reloadData();
                }
            }
        });

        // All = select first 5
        btnSelectAll.addActionListener(e -> {
            int count = 0;
            for (int i = 0; i < diseaseListModel.size(); i++) {
                DiseaseItem item = diseaseListModel.get(i);
                item.selected = count < MAX_SELECTED_DISEASES;
                if (item.selected) {
                    count++;
                }
            }
            diseaseList.repaint();
            reloadData();
        });

        // None
        btnClear.addActionListener(e -> {
            for (DiseaseItem item : allDiseases) {
                item.selected = false;
            }
            diseaseList.repaint();
            chartPanel.setData(Collections.emptyList());
            xAxisLabel.setText("No diseases selected");
            updateLegendBox();
        });

        JScrollPane scroll = new JScrollPane(diseaseList);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARY_LIGHT),
                "ICD-10 Codes"
        ));
        leftPanel.add(scroll, BorderLayout.CENTER);

        // === CHART PANEL ===
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARY_LIGHT),
                "Trend of Appointments by Disease"
        ));
        chartContainer.setBackground(Color.WHITE);
        chartPanel.setBackground(Color.WHITE);
        chartContainer.add(chartPanel, BorderLayout.CENTER);
        chartContainer.setMinimumSize(new Dimension(500, 300));

        // When user double-clicks a bar/point, open details popup
        chartPanel.setPointSelectionListener((diseaseName, bucketLabel) ->
                onChartPointDoubleClicked(diseaseName, bucketLabel));

        // === SPLIT PANE ===
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, chartContainer);
        split.setDividerLocation(430);
        split.setResizeWeight(0.35);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        split.setBorder(new EmptyBorder(0, 0, 0, 0));

        add(split, BorderLayout.CENTER);

        // === BOTTOM STATUS LABEL ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(6, 10, 10, 10));
        bottomPanel.setBackground(COLOR_BACKGROUND);
        xAxisLabel.setFont(xAxisLabel.getFont().deriveFont(Font.ITALIC, 12f));
        xAxisLabel.setForeground(new Color(97, 97, 97));
        bottomPanel.add(xAxisLabel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // === WIRING ACTIONS ===
        applyButton.addActionListener(e -> {
            reloadData();
            if (onlyWithAppointmentsCheckBox.isSelected()) applyDiseaseFilter();
        });

        resetButton.addActionListener(e -> resetAllFiltersAndSelections());

        todayButton.addActionListener(e -> showTopDiseasesForToday());

        intervalCombo.addActionListener(e -> {
            if (suppressEvents) return;
            updateIntervalUi();
            reloadData();
            if (onlyWithAppointmentsCheckBox.isSelected()) applyDiseaseFilter();
        });

        fromYearCombo.addActionListener(e -> {
            if (suppressEvents) return;
            TimeInterval t = (TimeInterval) intervalCombo.getSelectedItem();
            if (t == TimeInterval.DAY || t == TimeInterval.HOUR) {
                fillDays(fromYearCombo, fromMonthCombo, fromDayCombo);
            }
            reloadData();
            if (onlyWithAppointmentsCheckBox.isSelected()) applyDiseaseFilter();
        });
        fromMonthCombo.addActionListener(e -> {
            if (suppressEvents) return;
            TimeInterval t = (TimeInterval) intervalCombo.getSelectedItem();
            if (t == TimeInterval.DAY || t == TimeInterval.HOUR) {
                fillDays(fromYearCombo, fromMonthCombo, fromDayCombo);
            }
            reloadData();
            if (onlyWithAppointmentsCheckBox.isSelected()) applyDiseaseFilter();
        });
        toYearCombo.addActionListener(e -> {
            if (suppressEvents) return;
            TimeInterval t = (TimeInterval) intervalCombo.getSelectedItem();
            if (t == TimeInterval.DAY) {
                fillDays(toYearCombo, toMonthCombo, toDayCombo);
            }
            reloadData();
            if (onlyWithAppointmentsCheckBox.isSelected()) applyDiseaseFilter();
        });
        toMonthCombo.addActionListener(e -> {
            if (suppressEvents) return;
            TimeInterval t = (TimeInterval) intervalCombo.getSelectedItem();
            if (t == TimeInterval.DAY) {
                fillDays(toYearCombo, toMonthCombo, toDayCombo);
            }
            reloadData();
            if (onlyWithAppointmentsCheckBox.isSelected()) applyDiseaseFilter();
        });

        fromDayCombo.addActionListener(e -> {
            if (suppressEvents) return;
            TimeInterval t = (TimeInterval) intervalCombo.getSelectedItem();
            if (t == TimeInterval.DAY || t == TimeInterval.HOUR) {
                reloadData();
                if (onlyWithAppointmentsCheckBox.isSelected()) applyDiseaseFilter();
            }
        });
        toDayCombo.addActionListener(e -> {
            if (suppressEvents) return;
            TimeInterval t = (TimeInterval) intervalCombo.getSelectedItem();
            if (t == TimeInterval.DAY) {
                reloadData();
                if (onlyWithAppointmentsCheckBox.isSelected()) applyDiseaseFilter();
            }
        });

        fromTimeCombo.addActionListener(e -> {
            if (suppressEvents) return;
            TimeInterval t = (TimeInterval) intervalCombo.getSelectedItem();
            if (t == TimeInterval.HOUR) {
                reloadData();
                if (onlyWithAppointmentsCheckBox.isSelected()) applyDiseaseFilter();
            }
        });
        toTimeCombo.addActionListener(e -> {
            if (suppressEvents) return;
            TimeInterval t = (TimeInterval) intervalCombo.getSelectedItem();
            if (t == TimeInterval.HOUR) {
                reloadData();
                if (onlyWithAppointmentsCheckBox.isSelected()) applyDiseaseFilter();
            }
        });

        setupMonthRenderer(fromMonthCombo);
        setupMonthRenderer(toMonthCombo);
    }

    // === load diseases ===
    private void loadDiseases() {
        allDiseases.clear();
        diseaseListModel.clear();

        String sql = "SELECT code, description FROM icd10_codes ORDER BY code";
        List<Map<String, Object>> rows = db.executeQueryMap(sql);
        for (Map<String, Object> r : rows) {
            String code = Objects.toString(r.get("code"), "");
            String desc = Objects.toString(r.get("description"), "");
            allDiseases.add(new DiseaseItem(code, code + " - " + desc, false));
        }

        applyDiseaseFilter();
        updateLegendBox();
    }

    private void applyDiseaseFilter() {
        String text = diseaseSearchField.getText();
        String filter = (text == null) ? "" : text.trim().toLowerCase(Locale.ROOT);

        String chapter = (String) icdChapterCombo.getSelectedItem();
        String chapterPrefix = null;
        if (chapter != null && !chapter.startsWith("All")) {
            chapterPrefix = chapter.substring(0, 1);
        }

        Set<String> allowedCodes = null;
        if (onlyWithAppointmentsCheckBox.isSelected()) {
            allowedCodes = getDiseaseCodesWithAppointmentsInCurrentFilters();
        }

        diseaseListModel.clear();
        for (DiseaseItem item : allDiseases) {
            if (chapterPrefix != null && !item.code.startsWith(chapterPrefix)) {
                continue;
            }
            if (!filter.isEmpty()) {
                String codeLower = item.code.toLowerCase(Locale.ROOT);
                String labelLower = item.label.toLowerCase(Locale.ROOT);
                if (!codeLower.contains(filter) && !labelLower.contains(filter)) {
                    continue;
                }
            }
            if (allowedCodes != null && !allowedCodes.contains(item.code)) {
                continue;
            }

            diseaseListModel.addElement(item);
        }
    }

    private Set<String> getDiseaseCodesWithAppointmentsInCurrentFilters() {
        try {
            String sql =
                    "SELECT DISTINCT mr.icd10_code AS code " +
                    "FROM medical_records mr " +
                    "JOIN appointments a ON mr.appointment_id = a.id";
            List<Map<String, Object>> rows = db.executeQueryMap(sql);
            Set<String> codes = new HashSet<>();
            for (Map<String, Object> r : rows) {
                Object codeObj = r.get("code");
                if (codeObj != null) {
                    codes.add(codeObj.toString());
                }
            }
            return codes;
        } catch (Exception ex) {
            return null;
        }
    }

    // === date helpers ===
    private void initDateCombos() {
        suppressEvents = true;
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        for (int y = 2020; y <= year + 1; y++) {
            fromYearCombo.addItem(y);
            toYearCombo.addItem(y);
        }
        for (int m = 1; m <= 12; m++) {
            fromMonthCombo.addItem(m);
            toMonthCombo.addItem(m);
        }
        fillDays(fromYearCombo, fromMonthCombo, fromDayCombo);
        fillDays(toYearCombo, toMonthCombo, toDayCombo);
        fromYearCombo.setSelectedItem(year);
        fromMonthCombo.setSelectedItem(today.getMonthValue());
        fromDayCombo.setSelectedItem(today.getDayOfMonth());
        toYearCombo.setSelectedItem(year);
        toMonthCombo.setSelectedItem(today.getMonthValue());
        toDayCombo.setSelectedItem(today.getDayOfMonth());
        suppressEvents = false;
    }

    private void fillDays(JComboBox<Integer> y, JComboBox<Integer> m, JComboBox<Integer> d) {
        d.removeAllItems();
        if (y.getSelectedItem() == null || m.getSelectedItem() == null) return;
        YearMonth ym = YearMonth.of((Integer) y.getSelectedItem(), (Integer) m.getSelectedItem());
        for (int i = 1; i <= ym.lengthOfMonth(); i++) d.addItem(i);
        if (d.getItemCount() > 0 && d.getSelectedItem() == null) d.setSelectedIndex(0);
    }

    private void setupMonthRenderer(JComboBox<Integer> combo) {
        combo.setRenderer((list, value, index, isSel, hasFocus) -> {
            JLabel l = new JLabel();
            if (value != null) {
                String[] m = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                l.setText(m[value - 1]);
            }
            l.setOpaque(true);
            if (isSel) {
                l.setBackground(list.getSelectionBackground());
                l.setForeground(list.getSelectionForeground());
            } else {
                l.setBackground(list.getBackground());
                l.setForeground(list.getForeground());
            }
            return l;
        });
    }

    // === interval logic ===
    private void updateIntervalUi() {
        suppressEvents = true;
        TimeInterval t = (TimeInterval) intervalCombo.getSelectedItem();
        if (t == null) t = TimeInterval.DAY;

        boolean isHour = (t == TimeInterval.HOUR);
        boolean isMonth = (t == TimeInterval.MONTH);
        boolean isYear = (t == TimeInterval.YEAR);

        // From label always
        fromLabel.setVisible(true);

        // To row hidden entirely for HOUR
        toLabel.setVisible(!isHour);
        toYearCombo.setVisible(!isHour);
        toMonthCombo.setVisible(!isHour && !isYear);
        toDayCombo.setVisible(!isHour && !(isYear || isMonth));

        // From row
        fromYearCombo.setVisible(true);
        fromMonthCombo.setVisible(!isYear);
        fromDayCombo.setVisible(!(isYear || isMonth));

        // Time range only for HOUR
        timeLabel.setVisible(isHour);
        fromTimeCombo.setVisible(isHour);
        toTimeCombo.setVisible(isHour);
        if (timeToText != null) {
            timeToText.setVisible(isHour);
        }

        suppressEvents = false;
        revalidate();
        repaint();
    }

    // === reload chart ===
    private void reloadData() {
        if (suppressEvents) return;
        TimeInterval it = (TimeInterval) intervalCombo.getSelectedItem();
        if (it == null) it = TimeInterval.DAY;

        List<String> codes = getSelectedDiseaseCodes();
        if (codes.isEmpty()) {
            chartPanel.setData(Collections.emptyList());
            xAxisLabel.setText("No diseases selected");
            updateLegendBox();
            return;
        }

        Integer fy = (Integer) fromYearCombo.getSelectedItem();
        Integer fm = (Integer) fromMonthCombo.getSelectedItem();
        Integer fd = (Integer) fromDayCombo.getSelectedItem();
        Integer ty = (Integer) toYearCombo.getSelectedItem();
        Integer tm = (Integer) toMonthCombo.getSelectedItem();
        Integer td = (Integer) toDayCombo.getSelectedItem();

        LocalDate from;
        LocalDate to;

        if (it == TimeInterval.YEAR) {
            if (fy == null || ty == null) return;
            from = LocalDate.of(fy, 1, 1);
            to = LocalDate.of(ty, 12, 31);
        } else if (it == TimeInterval.MONTH) {
            if (fy == null || fm == null || ty == null || tm == null) return;
            from = LocalDate.of(fy, fm, 1);
            to = YearMonth.of(ty, tm).atEndOfMonth();
        } else if (it == TimeInterval.DAY) {
            if (fy == null || fm == null || fd == null || ty == null || tm == null || td == null) return;
            from = LocalDate.of(fy, fm, fd);
            to = LocalDate.of(ty, tm, td);
        } else if (it == TimeInterval.HOUR) {
            if (fy == null || fm == null || fd == null) return;
            from = LocalDate.of(fy, fm, fd);
            to = from;
        } else {
            return;
        }

        if (to.isBefore(from)) {
            JOptionPane.showMessageDialog(this, "'To' must not be before 'From'.",
                    "Input error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fromTime = null, toTime = null;
        if (it == TimeInterval.HOUR) {
            fromTime = (String) fromTimeCombo.getSelectedItem();
            toTime = (String) toTimeCombo.getSelectedItem();
        }

        chartPanel.setData(controller.getAppointmentsByDisease(
                from, to, it, codes, fromTime, toTime, null));

        xAxisLabel.setText("Grouping: " + it + " | Range: " + from + " to " + to +
                " | Diseases: " + codes.size() + " selected");

        updateLegendBox();
    }

    // === RESET BUTTON LOGIC ===
    private void resetAllFiltersAndSelections() {
        suppressEvents = true;
        LocalDate today = LocalDate.now();

        intervalCombo.setSelectedItem(TimeInterval.DAY);

        int year = today.getYear();
        fromYearCombo.setSelectedItem(year);
        toYearCombo.setSelectedItem(year);
        fromMonthCombo.setSelectedItem(today.getMonthValue());
        toMonthCombo.setSelectedItem(today.getMonthValue());
        fillDays(fromYearCombo, fromMonthCombo, fromDayCombo);
        fillDays(toYearCombo, toMonthCombo, toDayCombo);
        fromDayCombo.setSelectedItem(today.getDayOfMonth());
        toDayCombo.setSelectedItem(today.getDayOfMonth());

        fromTimeCombo.setSelectedItem("00:00");
        toTimeCombo.setSelectedItem("23:00");

        icdChapterCombo.setSelectedIndex(0);
        diseaseSearchField.setText("");
        onlyWithAppointmentsCheckBox.setSelected(false);

        for (DiseaseItem item : allDiseases) {
            item.selected = false;
        }

        suppressEvents = false;

        applyDiseaseFilter();
        diseaseList.repaint();
        updateIntervalUi();

        chartPanel.setData(Collections.emptyList());
        xAxisLabel.setText("No diseases selected");
        updateLegendBox();
    }

    // === TODAY BUTTON LOGIC ===
    private void showTopDiseasesForToday() {
        LocalDate today = LocalDate.now();

        suppressEvents = true;
        intervalCombo.setSelectedItem(TimeInterval.DAY);

        int year = today.getYear();
        fromYearCombo.setSelectedItem(year);
        toYearCombo.setSelectedItem(year);
        fromMonthCombo.setSelectedItem(today.getMonthValue());
        toMonthCombo.setSelectedItem(today.getMonthValue());
        fillDays(fromYearCombo, fromMonthCombo, fromDayCombo);
        fillDays(toYearCombo, toMonthCombo, toDayCombo);
        fromDayCombo.setSelectedItem(today.getDayOfMonth());
        toDayCombo.setSelectedItem(today.getDayOfMonth());

        fromTimeCombo.setSelectedItem("00:00");
        toTimeCombo.setSelectedItem("23:00");

        icdChapterCombo.setSelectedIndex(0);
        diseaseSearchField.setText("");
        onlyWithAppointmentsCheckBox.setSelected(true);

        suppressEvents = false;
        applyDiseaseFilter();
        updateIntervalUi();

        // Query stats for today, all diseases
        List<AppointmentDiseaseStatsDTO> stats = controller.getAppointmentsByDisease(
                today, today, TimeInterval.DAY,
                Collections.emptyList(), // all diseases
                null, null,
                null
        );

        if (stats == null || stats.isEmpty()) {
            resetAllFiltersAndSelections();
            JOptionPane.showMessageDialog(
                    this,
                    "No appointments found for today.",
                    "No data",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Aggregate counts per diseaseName
        Map<String, Integer> countsByDisease = new HashMap<>();
        for (AppointmentDiseaseStatsDTO dto : stats) {
            if (dto.getCount() <= 0) continue;
            String name = dto.getDiseaseName();
            if (name == null || name.trim().isEmpty() || "No data".equalsIgnoreCase(name.trim())) {
                continue;
            }
            countsByDisease.put(name, countsByDisease.getOrDefault(name, 0) + dto.getCount());
        }

        if (countsByDisease.isEmpty()) {
            resetAllFiltersAndSelections();
            JOptionPane.showMessageDialog(
                    this,
                    "No appointments found for today.",
                    "No data",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Sort diseases by count desc and take top 3
        List<String> diseaseNames = new ArrayList<>(countsByDisease.keySet());
        diseaseNames.sort((a, b) -> Integer.compare(countsByDisease.get(b), countsByDisease.get(a)));

        List<String> topCodes = new ArrayList<>();
        int limit = Math.min(3, diseaseNames.size());
        for (int i = 0; i < limit; i++) {
            String name = diseaseNames.get(i);
            String code = name;
            int idx = name.indexOf(" - ");
            if (idx > 0) {
                code = name.substring(0, idx).trim();
            }
            if (!topCodes.contains(code)) {
                topCodes.add(code);
            }
        }

        if (topCodes.isEmpty()) {
            resetAllFiltersAndSelections();
            JOptionPane.showMessageDialog(
                    this,
                    "No appointments found for today.",
                    "No data",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Apply selection to disease list
        for (DiseaseItem item : allDiseases) {
            item.selected = topCodes.contains(item.code);
        }

        applyDiseaseFilter();
        diseaseList.repaint();
        reloadData(); // will also refresh legend
    }

    private int getSelectedDiseaseCount() {
        int c = 0;
        for (DiseaseItem item : allDiseases) {
            if (item.selected) c++;
        }
        return c;
    }

    private List<String> getSelectedDiseaseCodes() {
        List<String> list = new ArrayList<>();
        for (DiseaseItem item : allDiseases) {
            if (item.selected) list.add(item.code);
        }
        return list;
    }

    // Selected DiseaseItem objects
    private List<DiseaseItem> getSelectedDiseaseItems() {
        List<DiseaseItem> list = new ArrayList<>();
        for (DiseaseItem item : allDiseases) {
            if (item.selected) {
                list.add(item);
            }
        }
        return list;
    }

    // Update legend box content in the top blue panel (with color code)
    private void updateLegendBox() {
        if (legendContentPanel == null) return;

        legendContentPanel.removeAll();

        List<DiseaseItem> selected = getSelectedDiseaseItems();
        if (selected.isEmpty()) {
            JLabel noSel = new JLabel("No diseases selected.");
            noSel.setFont(noSel.getFont().deriveFont(Font.ITALIC, 12f));
            noSel.setForeground(new Color(120, 120, 120));
            legendContentPanel.add(noSel);
        } else {
            for (DiseaseItem item : selected) {
                // Row panel
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                row.setOpaque(false);

                // Color box (taken from chart's color map)
                Color color = chartPanel.getColorForDisease(item.label);
                JLabel colorBox = new JLabel();
                colorBox.setOpaque(true);
                colorBox.setBackground(color);
                colorBox.setPreferredSize(new Dimension(14, 14));
                colorBox.setBorder(new MatteBorder(1, 1, 1, 1, Color.DARK_GRAY));

                JLabel lbl = new JLabel(item.label);
                lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 12.5f));
                lbl.setForeground(COLOR_LABEL_DARK);

                row.add(colorBox);
                row.add(lbl);

                legendContentPanel.add(row);
            }
        }

        legendContentPanel.revalidate();
        legendContentPanel.repaint();
    }

    private static class DiseaseItem {
        final String code, label;
        boolean selected;

        DiseaseItem(String c, String l, boolean s) {
            code = c;
            label = l;
            selected = s;
        }

        public String toString() {
            return label;
        }
    }

    private static class DiseaseCheckBoxRenderer implements ListCellRenderer<DiseaseItem> {
        public Component getListCellRendererComponent(JList<? extends DiseaseItem> list,
                                                      DiseaseItem val, int idx, boolean isSel, boolean hasFocus) {
            JCheckBox cb = new JCheckBox();
            if (val != null) {
                cb.setText(val.label);
                cb.setSelected(val.selected);
            }
            cb.setOpaque(true);
            cb.setFocusPainted(false);
            cb.setFont(cb.getFont().deriveFont(Font.PLAIN, 13.5f));
            if (isSel) {
                cb.setBackground(list.getSelectionBackground());
                cb.setForeground(list.getSelectionForeground());
            } else {
                cb.setBackground(list.getBackground());
                cb.setForeground(list.getForeground());
            }
            return cb;
        }
    }

    // === Double-click handler on chart ===
    private void onChartPointDoubleClicked(String diseaseLabel, String bucketLabel) {
        TimeInterval interval = (TimeInterval) intervalCombo.getSelectedItem();
        if (interval == null) interval = TimeInterval.DAY;

        // diseaseLabel is "CODE - Description" -> extract ICD-10 code
        String code = diseaseLabel;
        int idx = diseaseLabel.indexOf(" - ");
        if (idx > 0) {
            code = diseaseLabel.substring(0, idx).trim();
        }

        LocalDate[] range = computeBucketRange(interval, bucketLabel);
        if (range == null) {
            JOptionPane.showMessageDialog(this,
                    "Cannot resolve the time bucket for details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate from = range[0];
        LocalDate to = range[1];

        String fromTime = null;
        String toTime = null;

        if (interval == TimeInterval.HOUR) {
            // bucketLabel like "09:00"
            try {
                int hour = Integer.parseInt(bucketLabel.substring(0, 2));
                fromTime = String.format("%02d:00", hour);
                toTime   = String.format("%02d:59", hour);
            } catch (Exception ignored) {
                // fall back to no time filter
            }
        }

        // Build query
        StringBuilder sql = new StringBuilder(
                "SELECT a.date, a.start_time, a.end_time, a.office, " +
                        "p.name AS patient_name, a.notes " +
                "FROM medical_records mr " +
                "JOIN appointments a ON mr.appointment_id = a.id " +
                "JOIN patients p ON a.patient_id = p.id " +
                "WHERE mr.icd10_code = ? " +
                "AND date(a.date) BETWEEN ? AND ? "
        );
        java.util.List<Object> params = new java.util.ArrayList<>();
        params.add(code);
        params.add(from.toString());
        params.add(to.toString());

        if (fromTime != null && toTime != null) {
            sql.append("AND time(a.start_time) >= time(?) AND time(a.start_time) <= time(?) ");
            params.add(fromTime);
            params.add(toTime);
        }

        sql.append("ORDER BY a.date, a.start_time");

        java.util.List<java.util.Map<String, Object>> rows =
                db.executeQueryMap(sql.toString(), params.toArray());

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No appointments for " + diseaseLabel + " in " + bucketLabel + ".",
                    "No data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        showAppointmentsDialog(diseaseLabel + " – " + bucketLabel, rows);
    }

    /**
     * Convert the bucketLabel back into a [from, to] LocalDate range.
     * Must mirror the formats from AppointmentStatisticsModel.toBucket().
     */
    private LocalDate[] computeBucketRange(TimeInterval interval, String bucketLabel) {
        try {
            switch (interval) {
                case DAY:
                    // bucketLabel = "yyyy-MM-dd"
                    LocalDate d = LocalDate.parse(bucketLabel);
                    return new LocalDate[]{d, d};

                case MONTH: {
                    // bucketLabel = "Sep 2025"
                    String[] parts = bucketLabel.split("\\s+");
                    if (parts.length != 2) return null;
                    int year = Integer.parseInt(parts[1]);
                    int month = monthFromShortName(parts[0]);
                    if (month == 0) return null;
                    java.time.YearMonth ym = java.time.YearMonth.of(year, month);
                    return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
                }

                case YEAR: {
                    int year = Integer.parseInt(bucketLabel.trim());
                    return new LocalDate[]{LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)};
                }

                case WEEK: {
                    // bucketLabel example: "Sep 2025 - Week 1 (1 - 7)"
                    int spaceIdx = bucketLabel.indexOf(' ');
                    int yearIdx = bucketLabel.indexOf(' ', spaceIdx + 1);
                    if (spaceIdx < 0 || yearIdx < 0) return null;

                    String monthStr = bucketLabel.substring(0, spaceIdx);
                    String yearStr = bucketLabel.substring(spaceIdx + 1, yearIdx);
                    int month = monthFromShortName(monthStr);
                    int year = Integer.parseInt(yearStr.trim());
                    if (month == 0) return null;

                    int parenOpen = bucketLabel.indexOf('(');
                    int dashIdx = bucketLabel.indexOf('-', parenOpen);
                    int parenClose = bucketLabel.indexOf(')', dashIdx);
                    if (parenOpen < 0 || dashIdx < 0 || parenClose < 0) return null;

                    String startDayStr = bucketLabel.substring(parenOpen + 1, dashIdx).trim();
                    String endDayStr = bucketLabel.substring(dashIdx + 1, parenClose).trim();
                    int startDay = Integer.parseInt(startDayStr);
                    int endDay = Integer.parseInt(endDayStr);

                    java.time.YearMonth ym = java.time.YearMonth.of(year, month);
                    LocalDate from = ym.atDay(startDay);
                    LocalDate to = ym.atDay(endDay);
                    return new LocalDate[]{from, to};
                }

                case HOUR: {
                    // For HOUR, we just use the current "From" day from the UI
                    Integer fy = (Integer) fromYearCombo.getSelectedItem();
                    Integer fm = (Integer) fromMonthCombo.getSelectedItem();
                    Integer fd = (Integer) fromDayCombo.getSelectedItem();
                    if (fy == null || fm == null || fd == null) return null;
                    LocalDate day = LocalDate.of(fy, fm, fd);
                    return new LocalDate[]{day, day};
                }

                default:
                    return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private int monthFromShortName(String shortName) {
        String[] m = {"Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"};
        for (int i = 0; i < m.length; i++) {
            if (m[i].equalsIgnoreCase(shortName)) return i + 1;
        }
        return 0;
    }

    private void showAppointmentsDialog(String title,
                                        java.util.List<java.util.Map<String, Object>> rows) {
        JDialog dlg = new JDialog(this, title, true);
        dlg.setSize(780, 320);
        dlg.setLocationRelativeTo(this);

        String[] columns = {"Date", "Start", "End", "Office", "Patient", "Notes"};
        Object[][] data = new Object[rows.size()][columns.length];

        for (int i = 0; i < rows.size(); i++) {
            java.util.Map<String, Object> r = rows.get(i);
            data[i][0] = Objects.toString(r.get("date"), "");
            data[i][1] = Objects.toString(r.get("start_time"), "");
            data[i][2] = Objects.toString(r.get("end_time"), "");
            data[i][3] = Objects.toString(r.get("office"), "");
            data[i][4] = Objects.toString(r.get("patient_name"), "");
            data[i][5] = Objects.toString(r.get("notes"), "");
        }

        JTable table = new JTable(data, columns);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        dlg.add(new JScrollPane(table), BorderLayout.CENTER);
        dlg.setVisible(true);
    }

}
