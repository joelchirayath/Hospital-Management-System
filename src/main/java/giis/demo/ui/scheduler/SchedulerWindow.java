package giis.demo.ui.scheduler;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import giis.demo.service.scheduler.*;

public class SchedulerWindow extends JFrame {

    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };
    
    private static final String[] DAY_NAMES = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    
    private static final long serialVersionUID = 1L;
    
    // Colors
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color ACCENT_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
    private final Color BORDER_COLOR = new Color(220, 220, 220);
    private final Color PANEL_BACKGROUND = Color.WHITE;
    
    // Fonts
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font DAY_FONT = new Font("Segoe UI", Font.BOLD, 14);
    
    // Main panels
    private JPanel contentPane;
    private JPanel panelHeader;
    private JPanel panelMain;
    private JPanel panelDates;
    private JPanel panelDays;
    private JPanel panelRight;
    private JButton btnCancel;
    private JButton btnConfirm;
    
    // Worker selection components
    private JPanel panelDatesLeft;
    private JPanel panelMiddle;
    private JPanel panelTxtWorker;
    private JLabel lblWorker;
    private JComboBox<WorkerDTO> comboBoxWorker;
    private JPanel panelDatesRight;
    private JRadioButton rdBtnNurse;
    private JRadioButton rdBtnDoctor;
    private ButtonGroup typeWorker = new ButtonGroup();
    
    // Date selection components
    private JPanel panelStartEnd;
    private JPanel panelWeek;
    private JPanel panelStartDay;
    private JPanel panelEndDay;
    private JLabel lblStartDay;
    private JSpinner spinnerDayStart;
    private JComboBox<String> comboBoxMonthStart;
    private JSpinner spinnerYearStart;
    private JLabel lblEndDate;
    private JSpinner spinnerDayEnd;
    private JComboBox<String> comboBoxMonthEnd;
    private JSpinner spinnerYearEnd;
    
    // Day components arrays for easier management
    private JRadioButton[] dayButtons;
    private JSpinner[] startHourSpinners;
    private JSpinner[] startMinuteSpinners;
    private JSpinner[] endHourSpinners;
    private JSpinner[] endMinuteSpinners;
    private JPanel[] dayPanels;
    
    private SchedulerController controller;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                SchedulerWindow frame = new SchedulerWindow();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public SchedulerWindow() {
        controller = new SchedulerControllerImpl();
        initializeDayArrays(); 
        initializeWindow();
        initializeComponents();
        setupEventHandlers();
    }

    private void initializeWindow() {
        setTitle("Work Schedule Manager");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1500, 900);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(BACKGROUND_COLOR);
        contentPane.setLayout(new BorderLayout(0, 10));
        setContentPane(contentPane);
        
        contentPane.add(getPanelHeader(), BorderLayout.NORTH);
        contentPane.add(getPanelMain(), BorderLayout.CENTER);
        
        updateDaySpinnerLimits(getSpinnerDayStart(), getComboBoxMonthStart(), getSpinnerYearStart());
        updateDaySpinnerLimits(getSpinnerDayEnd(), getComboBoxMonthEnd(), getSpinnerYearEnd());
        fillComboBoxWorkerWithNurses();
    }

    private void initializeDayArrays() {
        dayButtons = new JRadioButton[7];
        startHourSpinners = new JSpinner[7];
        startMinuteSpinners = new JSpinner[7];
        endHourSpinners = new JSpinner[7];
        endMinuteSpinners = new JSpinner[7];
        dayPanels = new JPanel[7];
        
        // Initialize day components
        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            dayButtons[i] = createDayRadioButton(DAY_NAMES[i].toUpperCase(), dayIndex);
            startHourSpinners[i] = createTimeSpinner(8, 0, 23, 1);
            startMinuteSpinners[i] = createTimeSpinner(0, 0, 59, 15);
            endHourSpinners[i] = createTimeSpinner(15, 0, 23, 1);
            endMinuteSpinners[i] = createTimeSpinner(0, 0, 59, 15);
        }
    }

    private void initializeComponents() {
        // Apply consistent styling to all components
        styleSpinner(getSpinnerDayStart());
        styleSpinner(getSpinnerDayEnd());
        styleSpinner(getSpinnerYearStart());
        styleSpinner(getSpinnerYearEnd());
        
        for (JSpinner spinner : startHourSpinners) styleSpinner(spinner);
        for (JSpinner spinner : startMinuteSpinners) styleSpinner(spinner);
        for (JSpinner spinner : endHourSpinners) styleSpinner(spinner);
        for (JSpinner spinner : endMinuteSpinners) styleSpinner(spinner);
        
        styleComboBox(getComboBoxMonthStart());
        styleComboBox(getComboBoxMonthEnd());
        styleComboBox(getComboBoxWorker());
    }

    private void setupEventHandlers() {
        // Year spinners synchronization
        getSpinnerYearStart().addChangeListener(e -> {
            int startYear = (int) getSpinnerYearStart().getValue();
            int endYear = (int) getSpinnerYearEnd().getValue();
            int maxYear = startYear + 10;
            SpinnerNumberModel newEndModel = new SpinnerNumberModel(
                Math.max(startYear, endYear), startYear, maxYear, 1);
            getSpinnerYearEnd().setModel(newEndModel);
            updateDaySpinnerLimits(getSpinnerDayEnd(), getComboBoxMonthEnd(), getSpinnerYearEnd());
            updateDaySpinnerLimits(getSpinnerDayStart(), getComboBoxMonthStart(), getSpinnerYearStart());
        });
    }

    // Styling methods
    private void styleSpinner(JSpinner spinner) {
        spinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        spinner.setFont(LABEL_FONT);
        spinner.setPreferredSize(new Dimension(60, 35));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        comboBox.setFont(LABEL_FONT);
        comboBox.setBackground(Color.WHITE);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker()),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        if (title != null) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(PRIMARY_COLOR);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            panel.add(titleLabel, BorderLayout.NORTH);
        }
        
        return panel;
    }

    private JRadioButton createDayRadioButton(String text, int dayIndex) {
        JRadioButton radioButton = new JRadioButton(text);
        radioButton.setFont(DAY_FONT);
        radioButton.setForeground(PRIMARY_COLOR);
        radioButton.setBackground(PANEL_BACKGROUND);
        radioButton.setSelected(true);
        radioButton.setPreferredSize(new Dimension(120, 25));
        
        radioButton.addActionListener(e -> {
            boolean enabled = radioButton.isSelected();
            startHourSpinners[dayIndex].setEnabled(enabled);
            startMinuteSpinners[dayIndex].setEnabled(enabled);
            endHourSpinners[dayIndex].setEnabled(enabled);
            endMinuteSpinners[dayIndex].setEnabled(enabled);
        });
        
        return radioButton;
    }

    private JSpinner createTimeSpinner(int initial, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, min, max, step));
        styleSpinner(spinner);
        spinner.setPreferredSize(new Dimension(60, 35));
        return spinner;
    }

    // Component creation methods
    private JPanel getPanelHeader() {
        if (panelHeader == null) {
            panelHeader = createSectionPanel(null);
            panelHeader.setLayout(new BorderLayout());
            
            JLabel titleLabel = new JLabel("Work Schedule Management");
            titleLabel.setFont(TITLE_FONT);
            titleLabel.setForeground(PRIMARY_COLOR);
            panelHeader.add(titleLabel, BorderLayout.WEST);
            panelHeader.add(getPanelRight(), BorderLayout.EAST);
        }
        return panelHeader;
    }

    private JPanel getPanelMain() {
        if (panelMain == null) {
            panelMain = new JPanel(new BorderLayout(0, 15));
            panelMain.setBackground(BACKGROUND_COLOR);
            
            
            if (getPanelDates().getParent() == null) {
                panelMain.add(getPanelDates(), BorderLayout.NORTH);
            }
            if (getPanelDays().getParent() == null) {
                panelMain.add(getPanelDays(), BorderLayout.CENTER);
            }
        }
        return panelMain;
    }

    private JPanel getPanelDates() {
        if (panelDates == null) {
            panelDates = createSectionPanel("Schedule Configuration");
            panelDates.setLayout(new GridLayout(1, 3, 15, 0));
            panelDates.add(getPanelDatesLeft());
            panelDates.add(getPanelMiddle());
            panelDates.add(getPanelDatesRight());
        }
        return panelDates;
    }

    private JPanel getPanelDays() {
        if (panelDays == null) {
            panelDays = createSectionPanel("Weekly Schedule");
            panelDays.setLayout(new BorderLayout(0, 10));
            panelDays.add(getPanelStartEnd(), BorderLayout.NORTH);
            panelDays.add(getPanelWeek(), BorderLayout.CENTER);
        }
        return panelDays;
    }

    private JPanel getPanelRight() {
        if (panelRight == null) {
            panelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            panelRight.setBackground(PANEL_BACKGROUND);
            panelRight.add(getButton_1());
            panelRight.add(getButton_2());
        }
        return panelRight;
    }

    private JButton getButton_1() {
        if (btnCancel == null) {
            btnCancel = createStyledButton("Cancel", WARNING_COLOR);
            btnCancel.addActionListener(e -> {
                // TODO: volver a la ventana anterior
            });
        }
        return btnCancel;
    }

    private JButton getButton_2() {
        if (btnConfirm == null) {
            btnConfirm = createStyledButton("Confirm Schedule", ACCENT_COLOR);
            btnConfirm.addActionListener(e -> {
                if (!validateDateRange()) return;
                if (!validateAllDaysTime()) return;
                
                WeekDTO infoSemanal = saveInfoIntoDTO();
                List<WorkingDayDTO> diasAGuardar = controller.createWorkDays(infoSemanal);
                controller.saveWorkingDays(diasAGuardar);
                
                JOptionPane.showMessageDialog(null, 
                    diasAGuardar.size() + " working days have been created successfully", 
                    "Schedule Confirmation", 
                    JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            });
        }
        return btnConfirm;
    }

    private JPanel getPanelDatesLeft() {
        if (panelDatesLeft == null) {
            panelDatesLeft = new JPanel(new BorderLayout(0, 5));
            panelDatesLeft.setBackground(PANEL_BACKGROUND);
            panelDatesLeft.add(getPanelTxtWorker(), BorderLayout.NORTH);
        }
        return panelDatesLeft;
    }

    private JPanel getPanelMiddle() {
        if (panelMiddle == null) {
            panelMiddle = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            panelMiddle.setBackground(PANEL_BACKGROUND);
            
            JLabel typeLabel = new JLabel("Worker Type:");
            typeLabel.setFont(LABEL_FONT);
            typeLabel.setForeground(Color.DARK_GRAY);
            panelMiddle.add(typeLabel);
            
            panelMiddle.add(getRdBtnNurse());
            panelMiddle.add(getRdBtnDoctor());
            
            typeWorker.add(getRdBtnNurse());
            typeWorker.add(getRdBtnDoctor());
        }
        return panelMiddle;
    }

    private JPanel getPanelTxtWorker() {
        if (panelTxtWorker == null) {
            panelTxtWorker = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panelTxtWorker.setBackground(PANEL_BACKGROUND);
            panelTxtWorker.add(getLblWorker());
        }
        return panelTxtWorker;
    }

    private JLabel getLblWorker() {
        if (lblWorker == null) {
            lblWorker = new JLabel("Create schedule for:");
            lblWorker.setFont(LABEL_FONT);
            lblWorker.setForeground(Color.DARK_GRAY);
        }
        return lblWorker;
    }

    private JComboBox<WorkerDTO> getComboBoxWorker() {
        if (comboBoxWorker == null) {
            comboBoxWorker = new JComboBox<>();
            comboBoxWorker.setPreferredSize(new Dimension(300, 35));
            comboBoxWorker.setEditable(true);
        }
        return comboBoxWorker;
    }

    private JPanel getPanelDatesRight() {
        if (panelDatesRight == null) {
            panelDatesRight = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panelDatesRight.setBackground(PANEL_BACKGROUND);
            panelDatesRight.add(getComboBoxWorker());
        }
        return panelDatesRight;
    }

    private JPanel getPanelStartEnd() {
        if (panelStartEnd == null) {
            panelStartEnd = new JPanel(new GridLayout(1, 2, 20, 0));
            panelStartEnd.setBackground(PANEL_BACKGROUND);
            panelStartEnd.add(getPanelStartDay());
            panelStartEnd.add(getPanelEndDay());
        }
        return panelStartEnd;
    }

    private JPanel getPanelWeek() {
        if (panelWeek == null) {
            panelWeek = new JPanel(new GridLayout(7, 1, 0, 8));
            panelWeek.setBackground(PANEL_BACKGROUND);
            
            // VERIFICAR QUE dayButtons NO SEA NULL
            if (dayButtons == null) {
                initializeDayArrays();
            }
            
            for (int i = 0; i < 7; i++) {
                dayPanels[i] = createDayPanel(i);
                panelWeek.add(dayPanels[i]);
            }
        }
        return panelWeek;
    }

    private JPanel createDayPanel(int dayIndex) {
        JPanel dayPanel = new JPanel(new BorderLayout());
        dayPanel.setBackground(PANEL_BACKGROUND);
        dayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JPanel dayContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dayContent.setBackground(PANEL_BACKGROUND);
        
        // Day radio button - VERIFICAR NULL
        if (dayButtons != null && dayButtons[dayIndex] != null) {
            dayContent.add(dayButtons[dayIndex]);
        }
        
        // From label and spinners
        JLabel fromLabel = new JLabel("From:");
        fromLabel.setFont(LABEL_FONT);
        dayContent.add(fromLabel);
        
        if (startHourSpinners != null && startHourSpinners[dayIndex] != null) {
            dayContent.add(startHourSpinners[dayIndex]);
        }
        
        JLabel colon1 = new JLabel(":");
        colon1.setFont(LABEL_FONT);
        dayContent.add(colon1);
        
        if (startMinuteSpinners != null && startMinuteSpinners[dayIndex] != null) {
            dayContent.add(startMinuteSpinners[dayIndex]);
        }
        
        // To label and spinners
        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(LABEL_FONT);
        dayContent.add(toLabel);
        
        if (endHourSpinners != null && endHourSpinners[dayIndex] != null) {
            dayContent.add(endHourSpinners[dayIndex]);
        }
        
        JLabel colon2 = new JLabel(":");
        colon2.setFont(LABEL_FONT);
        dayContent.add(colon2);
        
        if (endMinuteSpinners != null && endMinuteSpinners[dayIndex] != null) {
            dayContent.add(endMinuteSpinners[dayIndex]);
        }
        
        dayPanel.add(dayContent, BorderLayout.CENTER);
        return dayPanel;
    }

    private JPanel getPanelStartDay() {
        if (panelStartDay == null) {
            panelStartDay = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            panelStartDay.setBackground(PANEL_BACKGROUND);
            
            lblStartDay = new JLabel("Start Date:");
            lblStartDay.setFont(LABEL_FONT);
            panelStartDay.add(lblStartDay);
            panelStartDay.add(getSpinnerDayStart());
            panelStartDay.add(getComboBoxMonthStart());
            panelStartDay.add(getSpinnerYearStart());
        }
        return panelStartDay;
    }

    private JPanel getPanelEndDay() {
        if (panelEndDay == null) {
            panelEndDay = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            panelEndDay.setBackground(PANEL_BACKGROUND);
            
            lblEndDate = new JLabel("End Date:");
            lblEndDate.setFont(LABEL_FONT);
            panelEndDay.add(lblEndDate);
            panelEndDay.add(getSpinnerDayEnd());
            panelEndDay.add(getComboBoxMonthEnd());
            panelEndDay.add(getSpinnerYearEnd());
        }
        return panelEndDay;
    }

    // Date component getters
    private JSpinner getSpinnerDayStart() {
        if (spinnerDayStart == null) {
            spinnerDayStart = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
            spinnerDayStart.setPreferredSize(new Dimension(60, 35));
        }
        return spinnerDayStart;
    }

    private JComboBox<String> getComboBoxMonthStart() {
        if (comboBoxMonthStart == null) {
            comboBoxMonthStart = new JComboBox<>(MONTH_NAMES);
            comboBoxMonthStart.setPreferredSize(new Dimension(120, 35));
            comboBoxMonthStart.addActionListener(e -> 
                updateDaySpinnerLimits(getSpinnerDayStart(), getComboBoxMonthStart(), getSpinnerYearStart()));
        }
        return comboBoxMonthStart;
    }

    private JSpinner getSpinnerYearStart() {
        if (spinnerYearStart == null) {
            spinnerYearStart = new JSpinner(getYearSpinnerModel());
            spinnerYearStart.setPreferredSize(new Dimension(80, 35));
        }
        return spinnerYearStart;
    }

    private JSpinner getSpinnerDayEnd() {
        if (spinnerDayEnd == null) {
            spinnerDayEnd = new JSpinner(new SpinnerNumberModel(2, 1, 31, 1));
            spinnerDayEnd.setPreferredSize(new Dimension(60, 35));
        }
        return spinnerDayEnd;
    }

    private JComboBox<String> getComboBoxMonthEnd() {
        if (comboBoxMonthEnd == null) {
            comboBoxMonthEnd = new JComboBox<>(MONTH_NAMES);
            comboBoxMonthEnd.setPreferredSize(new Dimension(120, 35));
            comboBoxMonthEnd.addActionListener(e -> 
                updateDaySpinnerLimits(getSpinnerDayEnd(), getComboBoxMonthEnd(), getSpinnerYearEnd()));
        }
        return comboBoxMonthEnd;
    }

    private JSpinner getSpinnerYearEnd() {
        if (spinnerYearEnd == null) {
            spinnerYearEnd = new JSpinner(getYearSpinnerModel());
            spinnerYearEnd.setPreferredSize(new Dimension(80, 35));
            spinnerYearEnd.addChangeListener(e -> 
                updateDaySpinnerLimits(getSpinnerDayEnd(), getComboBoxMonthEnd(), getSpinnerYearEnd()));
        }
        return spinnerYearEnd;
    }

    // Worker type radio buttons
    private JRadioButton getRdBtnNurse() {
        if (rdBtnNurse == null) {
            rdBtnNurse = new JRadioButton("Nurse");
            rdBtnNurse.setFont(LABEL_FONT);
            rdBtnNurse.setBackground(PANEL_BACKGROUND);
            rdBtnNurse.setSelected(true);
            rdBtnNurse.addActionListener(e -> {
                comboBoxWorker.removeAllItems();
                fillComboBoxWorkerWithNurses();
            });
        }
        return rdBtnNurse;
    }

    private JRadioButton getRdBtnDoctor() {
        if (rdBtnDoctor == null) {
            rdBtnDoctor = new JRadioButton("Doctor");
            rdBtnDoctor.setFont(LABEL_FONT);
            rdBtnDoctor.setBackground(PANEL_BACKGROUND);
            rdBtnDoctor.addActionListener(e -> {
                comboBoxWorker.removeAllItems();
                fillComboBoxWorkerWithDoctors();
            });
        }
        return rdBtnDoctor;
    }

    // Individual day component getters for backward compatibility
    private JRadioButton getRdBtnMonday() { 
        if (dayButtons == null || dayButtons[0] == null) {
            initializeDayArrays();
        }
        return dayButtons[0]; 
    }
    
    private JRadioButton getRdBtnTuesday() { 
        if (dayButtons == null || dayButtons[1] == null) {
            initializeDayArrays();
        }
        return dayButtons[1]; 
    }
    
    private JRadioButton getRdBtnWednesday() { 
        if (dayButtons == null || dayButtons[2] == null) {
            initializeDayArrays();
        }
        return dayButtons[2]; 
    }
    
    private JRadioButton getRdBtnThursday() { 
        if (dayButtons == null || dayButtons[3] == null) {
            initializeDayArrays();
        }
        return dayButtons[3]; 
    }
    
    private JRadioButton getRdBtnFriday() { 
        if (dayButtons == null || dayButtons[4] == null) {
            initializeDayArrays();
        }
        return dayButtons[4]; 
    }
    
    private JRadioButton getRdBtnSaturday() { 
        if (dayButtons == null || dayButtons[5] == null) {
            initializeDayArrays();
        }
        return dayButtons[5]; 
    }
    
    private JRadioButton getRdBtnSunday() { 
        if (dayButtons == null || dayButtons[6] == null) {
            initializeDayArrays();
        }
        return dayButtons[6]; 
    }

    // Spinner getters with null checks
    private JSpinner getSpinnerStartHourDay1() { 
        if (startHourSpinners == null || startHourSpinners[0] == null) {
            initializeDayArrays();
        }
        return startHourSpinners[0]; 
    }
    
    private JSpinner getSpinnerStartMinuteDay1() { 
        if (startMinuteSpinners == null || startMinuteSpinners[0] == null) {
            initializeDayArrays();
        }
        return startMinuteSpinners[0]; 
    }
    
    private JSpinner getSpinnerEndHourDay1() { 
        if (endHourSpinners == null || endHourSpinners[0] == null) {
            initializeDayArrays();
        }
        return endHourSpinners[0]; 
    }
    
    private JSpinner getSpinnerEndMinuteDay1() { 
        if (endMinuteSpinners == null || endMinuteSpinners[0] == null) {
            initializeDayArrays();
        }
        return endMinuteSpinners[0]; 
    }

    // Repeat the same pattern for all other day spinners...
    private JSpinner getSpinnerStartHourDay2() { 
        if (startHourSpinners == null || startHourSpinners[1] == null) initializeDayArrays();
        return startHourSpinners[1]; 
    }
    private JSpinner getSpinnerStartMinuteDay2() { 
        if (startMinuteSpinners == null || startMinuteSpinners[1] == null) initializeDayArrays();
        return startMinuteSpinners[1]; 
    }
    private JSpinner getSpinnerEndHourDay2() { 
        if (endHourSpinners == null || endHourSpinners[1] == null) initializeDayArrays();
        return endHourSpinners[1]; 
    }
    private JSpinner getSpinnerEndMinuteDay2() { 
        if (endMinuteSpinners == null || endMinuteSpinners[1] == null) initializeDayArrays();
        return endMinuteSpinners[1]; 
    }

    private JSpinner getSpinnerStartHourDay3() { 
        if (startHourSpinners == null || startHourSpinners[2] == null) initializeDayArrays();
        return startHourSpinners[2]; 
    }
    private JSpinner getSpinnerStartMinuteDay3() { 
        if (startMinuteSpinners == null || startMinuteSpinners[2] == null) initializeDayArrays();
        return startMinuteSpinners[2]; 
    }
    private JSpinner getSpinnerEndHourDay3() { 
        if (endHourSpinners == null || endHourSpinners[2] == null) initializeDayArrays();
        return endHourSpinners[2]; 
    }
    private JSpinner getSpinnerEndMinuteDay3() { 
        if (endMinuteSpinners == null || endMinuteSpinners[2] == null) initializeDayArrays();
        return endMinuteSpinners[2]; 
    }

    private JSpinner getSpinnerStartHourDay4() { 
        if (startHourSpinners == null || startHourSpinners[3] == null) initializeDayArrays();
        return startHourSpinners[3]; 
    }
    private JSpinner getSpinnerStartMinuteDay4() { 
        if (startMinuteSpinners == null || startMinuteSpinners[3] == null) initializeDayArrays();
        return startMinuteSpinners[3]; 
    }
    private JSpinner getSpinnerEndHourDay4() { 
        if (endHourSpinners == null || endHourSpinners[3] == null) initializeDayArrays();
        return endHourSpinners[3]; 
    }
    private JSpinner getSpinnerEndMinuteDay4() { 
        if (endMinuteSpinners == null || endMinuteSpinners[3] == null) initializeDayArrays();
        return endMinuteSpinners[3]; 
    }

    private JSpinner getSpinnerStartHourDay5() { 
        if (startHourSpinners == null || startHourSpinners[4] == null) initializeDayArrays();
        return startHourSpinners[4]; 
    }
    private JSpinner getSpinnerStartMinuteDay5() { 
        if (startMinuteSpinners == null || startMinuteSpinners[4] == null) initializeDayArrays();
        return startMinuteSpinners[4]; 
    }
    private JSpinner getSpinnerEndHourDay5() { 
        if (endHourSpinners == null || endHourSpinners[4] == null) initializeDayArrays();
        return endHourSpinners[4]; 
    }
    private JSpinner getSpinnerEndMinuteDay5() { 
        if (endMinuteSpinners == null || endMinuteSpinners[4] == null) initializeDayArrays();
        return endMinuteSpinners[4]; 
    }

    private JSpinner getSpinnerStartHourDay6() { 
        if (startHourSpinners == null || startHourSpinners[5] == null) initializeDayArrays();
        return startHourSpinners[5]; 
    }
    private JSpinner getSpinnerStartMinuteDay6() { 
        if (startMinuteSpinners == null || startMinuteSpinners[5] == null) initializeDayArrays();
        return startMinuteSpinners[5]; 
    }
    private JSpinner getSpinnerEndHourDay6() { 
        if (endHourSpinners == null || endHourSpinners[5] == null) initializeDayArrays();
        return endHourSpinners[5]; 
    }
    private JSpinner getSpinnerEndMinuteDay6() { 
        if (endMinuteSpinners == null || endMinuteSpinners[5] == null) initializeDayArrays();
        return endMinuteSpinners[5]; 
    }

    private JSpinner getSpinnerStartHourDay7() { 
        if (startHourSpinners == null || startHourSpinners[6] == null) initializeDayArrays();
        return startHourSpinners[6]; 
    }
    private JSpinner getSpinnerStartMinuteDay7() { 
        if (startMinuteSpinners == null || startMinuteSpinners[6] == null) initializeDayArrays();
        return startMinuteSpinners[6]; 
    }
    private JSpinner getSpinnerEndHourDay7() { 
        if (endHourSpinners == null || endHourSpinners[6] == null) initializeDayArrays();
        return endHourSpinners[6]; 
    }
    private JSpinner getSpinnerEndMinuteDay7() { 
        if (endMinuteSpinners == null || endMinuteSpinners[6] == null) initializeDayArrays();
        return endMinuteSpinners[6]; 
    }

    // Business logic methods (unchanged functionality)
    private void updateDaySpinnerLimits(JSpinner daySpinner, JComboBox<String> monthComboBox, JSpinner yearSpinner) {
        try {
            int selectedMonthIndex = monthComboBox.getSelectedIndex();
            if (selectedMonthIndex == -1) return;
            
            int selectedMonth = selectedMonthIndex + 1;
            int selectedYear = (int) yearSpinner.getValue();
            YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
            int maxDays = yearMonth.lengthOfMonth();
            
            // Leap year check for February
            if (selectedMonth == 2) {
                boolean isLeapYear = Year.of(selectedYear).isLeap();
                maxDays = isLeapYear ? 29 : 28;
            }
            
            SpinnerNumberModel model = (SpinnerNumberModel) daySpinner.getModel();
            int currentDay = (int) model.getValue();
            
            if (currentDay > maxDays) {
                model.setValue(maxDays);
            }
            
            model.setMaximum(maxDays);
            model.setMinimum(1);
            
        } catch (Exception e) {
            System.err.println("Error updating spinner limits: " + e.getMessage());
        }
    }

    private SpinnerNumberModel getYearSpinnerModel() {
        int currentYear = Year.now().getValue();
        int maxYear = currentYear + 10;
        return new SpinnerNumberModel(currentYear, currentYear, maxYear, 1);
    }

    private boolean validateDateRange() {
        int startDay = (int) getSpinnerDayStart().getValue();
        String startMonth = (String) getComboBoxMonthStart().getSelectedItem();
        int startYear = (int) getSpinnerYearStart().getValue();
        
        int endDay = (int) getSpinnerDayEnd().getValue();
        String endMonth = (String) getComboBoxMonthEnd().getSelectedItem();
        int endYear = (int) getSpinnerYearEnd().getValue();
        
        int startMonthNum = getMonthNumber(startMonth);
        int endMonthNum = getMonthNumber(endMonth);
        
        LocalDate startDate = LocalDate.of(startYear, startMonthNum, startDay);
        LocalDate endDate = LocalDate.of(endYear, endMonthNum, endDay);
        
        if (endDate.isBefore(startDate)) {
            JOptionPane.showMessageDialog(this, 
                "End date must be after start date", 
                "Date Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    private int getMonthNumber(String monthName) {
        if (monthName == null) return -1;
        switch (monthName.toLowerCase()) {
            case "january": return 1;
            case "february": return 2;
            case "march": return 3;
            case "april": return 4;
            case "may": return 5;
            case "june": return 6;
            case "july": return 7;
            case "august": return 8;
            case "september": return 9;
            case "october": return 10;
            case "november": return 11;
            case "december": return 12;
            default: return -1;
        }
    }

    private boolean validateDayTime(int dayNumber) {
        int startHour, startMinute, endHour, endMinute;
        
        int index = dayNumber - 1;
        startHour = (int) startHourSpinners[index].getValue();
        startMinute = (int) startMinuteSpinners[index].getValue();
        endHour = (int) endHourSpinners[index].getValue();
        endMinute = (int) endMinuteSpinners[index].getValue();
        
        int startTotalMinutes = startHour * 60 + startMinute;
        int endTotalMinutes = endHour * 60 + endMinute;
        
        return endTotalMinutes > startTotalMinutes;
    }

    private boolean validateAllDaysTime() {
        for (int i = 1; i <= 7; i++) {
            JRadioButton dayButton = getDayRadioButton(i);
            if (dayButton != null && dayButton.isSelected() && !validateDayTime(i)) {
                JOptionPane.showMessageDialog(this, 
                    "Error in " + DAY_NAMES[i-1] + ": End time must be after start time", 
                    "Time Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private JRadioButton getDayRadioButton(int dayNumber) {
        if (dayButtons == null || dayButtons[dayNumber - 1] == null) {
            initializeDayArrays();
        }
        return dayButtons[dayNumber - 1];
    }

    private void fillComboBoxWorkerWithNurses() {
        List<WorkerDTO> workers = controller.getNurses();
        for (WorkerDTO DTO : workers) {
            comboBoxWorker.addItem(DTO);
        }
    }

    private void fillComboBoxWorkerWithDoctors() {
        List<WorkerDTO> workers = controller.getDoctors();
        for (WorkerDTO DTO : workers) {
            comboBoxWorker.addItem(DTO);
        }
    }

    protected WeekDTO saveInfoIntoDTO() {
        WorkerDTO worker = (WorkerDTO) getComboBoxWorker().getSelectedItem();
        
        WorkerDTO workerWithType = new WorkerDTO();
        workerWithType.setDni(worker.getDni());
        workerWithType.setName(worker.getName());
        workerWithType.setSurname(worker.getSurname());
        
        if (getRdBtnNurse().isSelected()) {
            workerWithType.setType("Nurse");
        } else if (getRdBtnDoctor().isSelected()) {
            workerWithType.setType("Doctor");
        }
        
        int startDay = (int) getSpinnerDayStart().getValue();
        String startMonth = (String) getComboBoxMonthStart().getSelectedItem();
        int startYear = (int) getSpinnerYearStart().getValue();
        
        int endDay = (int) getSpinnerDayEnd().getValue();
        String endMonth = (String) getComboBoxMonthEnd().getSelectedItem();
        int endYear = (int) getSpinnerYearEnd().getValue();
        
        // Get day information from arrays
        boolean monday = getRdBtnMonday().isSelected();
        int hourStartMonday = (int) getSpinnerStartHourDay1().getValue();
        int minuteStartMonday = (int) getSpinnerStartMinuteDay1().getValue();
        int hourEndMonday = (int) getSpinnerEndHourDay1().getValue();
        int minuteEndMonday = (int) getSpinnerEndMinuteDay1().getValue();

        boolean tuesday = getRdBtnTuesday().isSelected();
        int hourStartTuesday = (int) getSpinnerStartHourDay2().getValue();
        int minuteStartTuesday = (int) getSpinnerStartMinuteDay2().getValue();
        int hourEndTuesday = (int) getSpinnerEndHourDay2().getValue();
        int minuteEndTuesday = (int) getSpinnerEndMinuteDay2().getValue();

        boolean wednesday = getRdBtnWednesday().isSelected();
        int hourStartWednesday = (int) getSpinnerStartHourDay3().getValue();
        int minuteStartWednesday = (int) getSpinnerStartMinuteDay3().getValue();
        int hourEndWednesday = (int) getSpinnerEndHourDay3().getValue();
        int minuteEndWednesday = (int) getSpinnerEndMinuteDay3().getValue();

        boolean thursday = getRdBtnThursday().isSelected();
        int hourStartThursday = (int) getSpinnerStartHourDay4().getValue();
        int minuteStartThursday = (int) getSpinnerStartMinuteDay4().getValue();
        int hourEndThursday = (int) getSpinnerEndHourDay4().getValue();
        int minuteEndThursday = (int) getSpinnerEndMinuteDay4().getValue();

        boolean friday = getRdBtnFriday().isSelected();
        int hourStartFriday = (int) getSpinnerStartHourDay5().getValue();
        int minuteStartFriday = (int) getSpinnerStartMinuteDay5().getValue();
        int hourEndFriday = (int) getSpinnerEndHourDay5().getValue();
        int minuteEndFriday = (int) getSpinnerEndMinuteDay5().getValue();

        boolean saturday = getRdBtnSaturday().isSelected();
        int hourStartSaturday = (int) getSpinnerStartHourDay6().getValue();
        int minuteStartSaturday = (int) getSpinnerStartMinuteDay6().getValue();
        int hourEndSaturday = (int) getSpinnerEndHourDay6().getValue();
        int minuteEndSaturday = (int) getSpinnerEndMinuteDay6().getValue();

        boolean sunday = getRdBtnSunday().isSelected();
        int hourStartSunday = (int) getSpinnerStartHourDay7().getValue();
        int minuteStartSunday = (int) getSpinnerStartMinuteDay7().getValue();
        int hourEndSunday = (int) getSpinnerEndHourDay7().getValue();
        int minuteEndSunday = (int) getSpinnerEndMinuteDay7().getValue();

        return new WeekDTO(
            workerWithType, 
            startDay, endDay, startMonth, endMonth, startYear, endYear,
            monday, hourStartMonday, minuteStartMonday, hourEndMonday, minuteEndMonday,
            tuesday, hourStartTuesday, minuteStartTuesday, hourEndTuesday, minuteEndTuesday,
            wednesday, hourStartWednesday, minuteStartWednesday, hourEndWednesday, minuteEndWednesday,
            thursday, hourStartThursday, minuteStartThursday, hourEndThursday, minuteEndThursday,
            friday, hourStartFriday, minuteStartFriday, hourEndFriday, minuteEndFriday,
            saturday, hourStartSaturday, minuteStartSaturday, hourEndSaturday, minuteEndSaturday,
            sunday, hourStartSunday, minuteStartSunday, hourEndSunday, minuteEndSunday
        );
    }

 
   
}