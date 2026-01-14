package giis.demo.ui.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import giis.demo.jdbc.models.apointment.disease.MedicalRecordModel;
import giis.demo.jdbc.models.apointment.prescription.MedicationModel;
import giis.demo.jdbc.models.apointment.prescription.PrescriptionModel;
import giis.demo.service.appointment.AppointmentDTO;
import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.icd10.ICDDTO;
import giis.demo.service.appointment.prescription.MedicationDTO;
import giis.demo.service.appointment.prescription.PrescriptionDTO;
import giis.demo.service.doctor.DoctorDTO;
import giis.demo.service.patient.PatientDTO;
import giis.demo.ui.clinicalOrder.ClinicalOrderWindow;
import giis.demo.ui.medicalRecord.MedicalRecordWindow;

public class AppointmentDetailWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Color LOGOUT_COLOR = new Color(192, 57, 43);
	private final Color SUCCESS_COLOR = new Color(39, 174, 96);
	private final Color WARNING_COLOR = new Color(243, 156, 18);

	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font WELCOME_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font INFO_FONT = new Font("Segoe UI", Font.PLAIN, 12);

	private AppointmentModel model;
	private AppointmentDTO appointment;
	private JLabel attendanceLabel;
	private JLabel checkInLabel;
	private JLabel checkOutLabel;
	private JButton markAttendedButton;
	private JButton markNotAttendedButton;
	private JButton checkInButton;
	private JButton checkOutButton;
	private JButton editCheckInButton;
	private JButton editCheckOutButton;

	// Componentes para Consultation Cause
	private JComboBox<String> consultationCauseCombo;
	private JTextField consultationCauseNotes;
	private JButton addConsultationCauseButton;
	private DefaultListModel<String> consultationCauseListModel;
	private JList<String> consultationCauseList;
	private JButton deleteConsultationCauseButton;
	private JTextField consultationCauseOtherField;
	private JPanel consultationCauseOtherPanel;

	// Componentes para Familiar Antecedents
	private JComboBox<String> familiarAntecedentCombo;
	private JTextField familiarAntecedentNotes;
	private JButton addFamiliarAntecedentButton;
	private DefaultListModel<String> familiarAntecedentListModel;
	private JList<String> familiarAntecedentList;
	private JButton deleteFamiliarAntecedentButton;
	private JTextField familiarAntecedentOtherField;
	private JPanel familiarAntecedentOtherPanel;

	// Componentes para Personal Problems
	private JComboBox<String> personalProblemCombo;
	private JTextField personalProblemNotes;
	private JButton addPersonalProblemButton;
	private DefaultListModel<String> personalProblemListModel;
	private JList<String> personalProblemList;
	private JButton deletePersonalProblemButton;
	private JTextField personalProblemOtherField;
	private JPanel personalProblemOtherPanel;

	// Componentes para Illnesses
	private JComboBox<ICDDTO> illnessesCombo;
	private JTextField illnessesNotes;
	private JButton addIllnessButton;
	private DefaultListModel<ICDDTO> illnessesListModel;
	private JList<ICDDTO> illnessesList;
	private JButton deleteIllnessButton;
	private List<ICDDTO> selectedIllnesses = new ArrayList<>();
	private MedicalRecordModel recordModel;

	// Componentes para Treatments/Prescriptions
	private PrescriptionModel prescModel;
	private MedicationModel medicationModel;
	private JTextField prescriptionSearchField;
	private JTextField medicationSearchField;
	private JComboBox<PrescriptionDTO> prescriptionsCombo;
	private JComboBox<MedicationDTO> medicationsCombo;
	private JTextField treatmentsNotes;
	private JButton addPrescriptionButton;
	private JButton addMedicationButton;
	private DefaultListModel<PrescriptionDTO> prescriptionsListModel;
	private DefaultListModel<MedicationDTO> medicationsListModel;
	private JList<PrescriptionDTO> prescriptionsList;
	private JList<MedicationDTO> medicationsList;
	private JButton deletePrescriptionButton;
	private JButton deleteMedicationButton;

	private JButton saveToMedicalRecordButton;
	private JButton closeButton;
	private JButton editButton;

	private boolean hasUnsavedChanges = false;

	public AppointmentDetailWindow(AppointmentModel model,
		AppointmentDTO appointment) {
		this.prescModel = new PrescriptionModel(model.db);
		this.medicationModel = new MedicationModel(model.db);
		this.recordModel = new MedicalRecordModel(model.db);
		this.model = model;
		this.appointment = appointment;
		initializeUI();
		loadPatientDetails();
		loadComboBoxOptions();
	}

	private void initializeUI() {
		setTitle("Appointment Details - " + appointment.getFullPatientName());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				handleClose();
			}
		});
		setBounds(200, 200, 1000, 1000);
		setLocationRelativeTo(null);
		setResizable(true);

		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		contentPane.setBackground(BACKGROUND_COLOR);
		setContentPane(contentPane);

		// Title
		JLabel titleLabel = new JLabel("Appointment Details", JLabel.CENTER);
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(PRIMARY_COLOR);
		titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
		contentPane.add(titleLabel, BorderLayout.NORTH);

		// Main content panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(BACKGROUND_COLOR);

		// Appointment Information
		mainPanel.add(createStyledPanel("Appointment Information",
			createAppointmentInfoPanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Check-in/Check-out Times
		mainPanel.add(createStyledPanel("Check-in / Check-out Times",
			createCheckTimesPanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Patient Information
		mainPanel.add(
			createStyledPanel("Patient Information", createPatientInfoPanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Attendance Section
		mainPanel.add(createStyledPanel("Attendance", createAttendancePanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Notes
		mainPanel.add(
			createStyledPanel("Appointment Notes", createNotesPanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Consultation Cause Section
		mainPanel.add(createStyledPanel("Consultation Cause",
			createConsultationCausePanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Illnesses Section
		mainPanel.add(createStyledPanel("Illnesses", createIllnessesPanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Treatments Section
		mainPanel.add(createStyledPanel("Treatments & Medications",
			createTreatmentsPanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Familiar Antecedents Section
		mainPanel.add(createStyledPanel("Familiar Antecedents",
			createFamiliarAntecedentsPanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Personal Problems Section
		mainPanel.add(createStyledPanel("Personal Problems",
			createPersonalProblemsPanel()));

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		// Buttons panel
		contentPane.add(createButtonsPanel(), BorderLayout.SOUTH);

		updateAttendanceDisplay();
		updateCheckTimesDisplay();
		updateDeleteButtons();
	}

	private JPanel createStyledPanel(String title, JPanel contentPanel) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1), title,
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, SECTION_FONT, PRIMARY_COLOR),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		contentPanel.setBackground(PANEL_BACKGROUND);
		panel.add(contentPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createAppointmentInfoPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		String formattedDate = "N/A";
		try {
			LocalDate date = LocalDate.parse(appointment.getDate());
			formattedDate = date.format(
				DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
		} catch (Exception e) {
			formattedDate = appointment.getDate();
		}

		addStyledDetailRow(panel, "Date:", formattedDate);
		addStyledDetailRow(panel, "Scheduled Time:", appointment.getTimeSlot());
		addStyledDetailRow(panel, "Actual Time:",
			appointment.getActualTimeSlot());
		addStyledDetailRow(panel, "Office:", appointment.getOffice());
		addStyledDetailRow(panel, "Status:",
			getStatusDisplay(appointment.getStatus()));

		return panel;
	}

	private JPanel createCheckTimesPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel timesPanel = new JPanel(new GridLayout(2, 2, 10, 15));
		timesPanel.setBackground(PANEL_BACKGROUND);

		// Check-in row
		timesPanel.add(createStyledLabel("Check-in Time:"));
		JPanel checkInPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		checkInPanel.setBackground(PANEL_BACKGROUND);
		checkInLabel = createStatusLabel();
		checkInButton = createStyledButton("Check-in Now", PRIMARY_COLOR);
		checkInButton.addActionListener(e -> checkInNow());
		editCheckInButton = createStyledButton("Edit", WARNING_COLOR);
		editCheckInButton.addActionListener(e -> editCheckInTime());

		checkInPanel.add(checkInLabel);
		checkInPanel.add(checkInButton);
		checkInPanel.add(editCheckInButton);
		timesPanel.add(checkInPanel);

		// Check-out row
		timesPanel.add(createStyledLabel("Check-out Time:"));
		JPanel checkOutPanel = new JPanel(
			new FlowLayout(FlowLayout.LEFT, 5, 0));
		checkOutPanel.setBackground(PANEL_BACKGROUND);
		checkOutLabel = createStatusLabel();
		checkOutButton = createStyledButton("Check-out Now", PRIMARY_COLOR);
		checkOutButton.addActionListener(e -> checkOutNow());
		editCheckOutButton = createStyledButton("Edit", WARNING_COLOR);
		editCheckOutButton.addActionListener(e -> editCheckOutTime());

		checkOutPanel.add(checkOutLabel);
		checkOutPanel.add(checkOutButton);
		checkOutPanel.add(editCheckOutButton);
		timesPanel.add(checkOutPanel);

		panel.add(timesPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createPatientInfoPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		addStyledDetailRow(panel, "Name:", appointment.getFullPatientName());
		addStyledDetailRow(panel, "DNI:", appointment.getPatient_dni());
		addStyledDetailRow(panel, "Patient ID:",
			String.valueOf(appointment.getPatient_id()));
		addStyledDetailRow(panel, "Phone:", "Loading...");
		addStyledDetailRow(panel, "Email:", "Loading...");
		addStyledDetailRow(panel, "Date of Birth:", "Loading...");

		return panel;
	}

	private JPanel createAttendancePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		statusPanel.setBackground(PANEL_BACKGROUND);
		statusPanel.add(createStyledLabel("Current Status: "));

		attendanceLabel = createStatusLabel();
		statusPanel.add(attendanceLabel);

		JPanel buttonsPanel = new JPanel(
			new FlowLayout(FlowLayout.LEFT, 10, 0));
		buttonsPanel.setBackground(PANEL_BACKGROUND);
		buttonsPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

		markAttendedButton = createStyledButton("Mark as Attended",
			SUCCESS_COLOR);
		markAttendedButton.addActionListener(e -> markAttendance(1));

		markNotAttendedButton = createStyledButton("Mark as Not Attended",
			LOGOUT_COLOR);
		markNotAttendedButton.addActionListener(e -> markAttendance(0));

		buttonsPanel.add(markAttendedButton);
		buttonsPanel.add(markNotAttendedButton);

		panel.add(statusPanel, BorderLayout.NORTH);
		panel.add(buttonsPanel, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createNotesPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JTextArea notesArea = new JTextArea(4, 30);
		notesArea.setText(
			appointment.getNotes() != null ? appointment.getNotes()
				: "No notes available");
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesArea.setEditable(false);
		notesArea.setBackground(PANEL_BACKGROUND);
		notesArea.setFont(INFO_FONT);
		notesArea.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_COLOR, 1),
			BorderFactory.createEmptyBorder(8, 8, 8, 8)));

		JScrollPane notesScroll = new JScrollPane(notesArea);
		notesScroll.setBorder(null);

		panel.add(notesScroll, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createConsultationCausePanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setBackground(PANEL_BACKGROUND);

		consultationCauseCombo = createStyledComboBox();
		consultationCauseCombo.addItem("Select consultation cause...");
		consultationCauseCombo.addActionListener(
			e -> handleConsultationCauseSelection());

		addConsultationCauseButton = createStyledButton("Add", PRIMARY_COLOR);
		addConsultationCauseButton.addActionListener(
			e -> addConsultationCause());

		topPanel.add(consultationCauseCombo, BorderLayout.CENTER);
		topPanel.add(addConsultationCauseButton, BorderLayout.EAST);

		consultationCauseOtherPanel = new JPanel(new BorderLayout(5, 5));
		consultationCauseOtherPanel.setBackground(PANEL_BACKGROUND);
		consultationCauseOtherPanel.add(
			createStyledLabel("Specify other cause:"), BorderLayout.NORTH);
		consultationCauseOtherField = createStyledTextField();
		consultationCauseOtherPanel.add(consultationCauseOtherField,
			BorderLayout.CENTER);
		consultationCauseOtherPanel.setVisible(false);

		JPanel notesPanel = new JPanel(new BorderLayout(5, 5));
		notesPanel.setBackground(PANEL_BACKGROUND);
		notesPanel.add(createStyledLabel("Notes:"), BorderLayout.NORTH);
		consultationCauseNotes = createStyledTextField();
		notesPanel.add(consultationCauseNotes, BorderLayout.CENTER);

		consultationCauseListModel = new DefaultListModel<>();
		consultationCauseList = new JList<>(consultationCauseListModel);
		consultationCauseList.setSelectionMode(
			ListSelectionModel.SINGLE_SELECTION);
		consultationCauseList.addListSelectionListener(
			e -> updateDeleteButtons());
		consultationCauseList.setBackground(PANEL_BACKGROUND);
		consultationCauseList.setFont(INFO_FONT);

		JScrollPane listScroll = createStyledScrollPane(consultationCauseList,
			"Added Consultation Causes");

		deleteConsultationCauseButton = createStyledButton("Delete Selected",
			LOGOUT_COLOR);
		deleteConsultationCauseButton.addActionListener(
			e -> deleteConsultationCause());
		deleteConsultationCauseButton.setEnabled(false);

		JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		deletePanel.setBackground(PANEL_BACKGROUND);
		deletePanel.add(deleteConsultationCauseButton);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(PANEL_BACKGROUND);
		contentPanel.add(topPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(consultationCauseOtherPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(notesPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(listScroll);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(deletePanel);

		panel.add(contentPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createFamiliarAntecedentsPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setBackground(PANEL_BACKGROUND);

		familiarAntecedentCombo = createStyledComboBox();
		familiarAntecedentCombo.addItem("Select familiar antecedent...");
		familiarAntecedentCombo.addActionListener(
			e -> handleFamiliarAntecedentSelection());

		addFamiliarAntecedentButton = createStyledButton("Add", PRIMARY_COLOR);
		addFamiliarAntecedentButton.addActionListener(
			e -> addFamiliarAntecedent());

		topPanel.add(familiarAntecedentCombo, BorderLayout.CENTER);
		topPanel.add(addFamiliarAntecedentButton, BorderLayout.EAST);

		familiarAntecedentOtherPanel = new JPanel(new BorderLayout(5, 5));
		familiarAntecedentOtherPanel.setBackground(PANEL_BACKGROUND);
		familiarAntecedentOtherPanel.add(
			createStyledLabel("Specify other antecedent:"), BorderLayout.NORTH);
		familiarAntecedentOtherField = createStyledTextField();
		familiarAntecedentOtherPanel.add(familiarAntecedentOtherField,
			BorderLayout.CENTER);
		familiarAntecedentOtherPanel.setVisible(false);

		JPanel notesPanel = new JPanel(new BorderLayout(5, 5));
		notesPanel.setBackground(PANEL_BACKGROUND);
		notesPanel.add(createStyledLabel("Notes:"), BorderLayout.NORTH);
		familiarAntecedentNotes = createStyledTextField();
		notesPanel.add(familiarAntecedentNotes, BorderLayout.CENTER);

		familiarAntecedentListModel = new DefaultListModel<>();
		familiarAntecedentList = new JList<>(familiarAntecedentListModel);
		familiarAntecedentList.setSelectionMode(
			ListSelectionModel.SINGLE_SELECTION);
		familiarAntecedentList.addListSelectionListener(
			e -> updateDeleteButtons());
		familiarAntecedentList.setBackground(PANEL_BACKGROUND);
		familiarAntecedentList.setFont(INFO_FONT);

		JScrollPane listScroll = createStyledScrollPane(familiarAntecedentList,
			"Added Familiar Antecedents");

		deleteFamiliarAntecedentButton = createStyledButton("Delete Selected",
			LOGOUT_COLOR);
		deleteFamiliarAntecedentButton.addActionListener(
			e -> deleteFamiliarAntecedent());
		deleteFamiliarAntecedentButton.setEnabled(false);

		JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		deletePanel.setBackground(PANEL_BACKGROUND);
		deletePanel.add(deleteFamiliarAntecedentButton);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(PANEL_BACKGROUND);
		contentPanel.add(topPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(familiarAntecedentOtherPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(notesPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(listScroll);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(deletePanel);

		panel.add(contentPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createPersonalProblemsPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setBackground(PANEL_BACKGROUND);

		personalProblemCombo = createStyledComboBox();
		personalProblemCombo.addItem("Select personal problem...");
		personalProblemCombo.addActionListener(
			e -> handlePersonalProblemSelection());

		addPersonalProblemButton = createStyledButton("Add", PRIMARY_COLOR);
		addPersonalProblemButton.addActionListener(e -> addPersonalProblem());

		topPanel.add(personalProblemCombo, BorderLayout.CENTER);
		topPanel.add(addPersonalProblemButton, BorderLayout.EAST);

		personalProblemOtherPanel = new JPanel(new BorderLayout(5, 5));
		personalProblemOtherPanel.setBackground(PANEL_BACKGROUND);
		personalProblemOtherPanel.add(
			createStyledLabel("Specify other problem:"), BorderLayout.NORTH);
		personalProblemOtherField = createStyledTextField();
		personalProblemOtherPanel.add(personalProblemOtherField,
			BorderLayout.CENTER);
		personalProblemOtherPanel.setVisible(false);

		JPanel notesPanel = new JPanel(new BorderLayout(5, 5));
		notesPanel.setBackground(PANEL_BACKGROUND);
		notesPanel.add(createStyledLabel("Notes:"), BorderLayout.NORTH);
		personalProblemNotes = createStyledTextField();
		notesPanel.add(personalProblemNotes, BorderLayout.CENTER);

		personalProblemListModel = new DefaultListModel<>();
		personalProblemList = new JList<>(personalProblemListModel);
		personalProblemList.setSelectionMode(
			ListSelectionModel.SINGLE_SELECTION);
		personalProblemList.addListSelectionListener(
			e -> updateDeleteButtons());
		personalProblemList.setBackground(PANEL_BACKGROUND);
		personalProblemList.setFont(INFO_FONT);

		JScrollPane listScroll = createStyledScrollPane(personalProblemList,
			"Added Personal Problems");

		deletePersonalProblemButton = createStyledButton("Delete Selected",
			LOGOUT_COLOR);
		deletePersonalProblemButton.addActionListener(
			e -> deletePersonalProblem());
		deletePersonalProblemButton.setEnabled(false);

		JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		deletePanel.setBackground(PANEL_BACKGROUND);
		deletePanel.add(deletePersonalProblemButton);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(PANEL_BACKGROUND);
		contentPanel.add(topPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(personalProblemOtherPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(notesPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(listScroll);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(deletePanel);

		panel.add(contentPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createIllnessesPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setBackground(PANEL_BACKGROUND);

		addIllnessButton = createStyledButton("Add New Disease", PRIMARY_COLOR);
		addIllnessButton.setPreferredSize(new Dimension(150, 35));
		addIllnessButton.addActionListener(e -> addNewDisease());

		topPanel.add(addIllnessButton, BorderLayout.WEST);

		illnessesListModel = new DefaultListModel<>();
		illnessesList = new JList<>(illnessesListModel);
		illnessesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		illnessesList.addListSelectionListener(e -> updateDeleteButtons());
		illnessesList.setBackground(PANEL_BACKGROUND);
		illnessesList.setFont(INFO_FONT);

		illnessesList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);
				if (value instanceof ICDDTO) {
					ICDDTO icd = (ICDDTO) value;
					setText(icd.getCode() + " - " + icd.getDescription() + " ("
						+ icd.getCategory() + ")");
				}
				return this;
			}
		});

		JScrollPane listScroll = createStyledScrollPane(illnessesList,
			"Added Illnesses");

		deleteIllnessButton = createStyledButton("Remove Selected",
			LOGOUT_COLOR);
		deleteIllnessButton.addActionListener(e -> deleteIllness());
		deleteIllnessButton.setEnabled(false);

		JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		deletePanel.setBackground(PANEL_BACKGROUND);
		deletePanel.add(deleteIllnessButton);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(PANEL_BACKGROUND);
		contentPanel.add(topPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(listScroll);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(deletePanel);

		panel.add(contentPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createTreatmentsPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel mainContentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
		mainContentPanel.setBackground(PANEL_BACKGROUND);

		JPanel prescriptionsColumn = createPrescriptionColumn();
		JPanel medicationsColumn = createMedicationColumn();

		mainContentPanel.add(prescriptionsColumn);
		mainContentPanel.add(medicationsColumn);

		JPanel notesPanel = new JPanel(new BorderLayout(5, 5));
		notesPanel.setBackground(PANEL_BACKGROUND);
		notesPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
		notesPanel.add(createStyledLabel("Treatment Notes:"),
			BorderLayout.NORTH);
		treatmentsNotes = createStyledTextField();
		notesPanel.add(treatmentsNotes, BorderLayout.CENTER);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(PANEL_BACKGROUND);
		contentPanel.add(mainContentPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		contentPanel.add(notesPanel);

		panel.add(contentPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createPrescriptionColumn() {
		JPanel column = new JPanel(new BorderLayout(5, 5));
		column.setBackground(PANEL_BACKGROUND);

		// Search panel para prescriptions
		JPanel prescriptionSearchPanel = new JPanel(new BorderLayout(5, 5));
		prescriptionSearchPanel.setBackground(PANEL_BACKGROUND);
		prescriptionSearchPanel.add(createStyledLabel("Search Prescriptions:"),
			BorderLayout.NORTH);
		prescriptionSearchField = createStyledTextField();
		prescriptionSearchField	.getDocument()
								.addDocumentListener(
									new javax.swing.event.DocumentListener() {
										@Override
										public void changedUpdate(
											javax.swing.event.DocumentEvent e) {
											searchPrescriptions();
										}

										@Override
										public void removeUpdate(
											javax.swing.event.DocumentEvent e) {
											searchPrescriptions();
										}

										@Override
										public void insertUpdate(
											javax.swing.event.DocumentEvent e) {
											searchPrescriptions();
										}
									});
		prescriptionSearchPanel.add(prescriptionSearchField,
			BorderLayout.CENTER);

		// ComboBox para prescriptions
		JPanel prescriptionComboPanel = new JPanel(new BorderLayout(5, 5));
		prescriptionComboPanel.setBackground(PANEL_BACKGROUND);
		prescriptionComboPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		prescriptionComboPanel.add(createStyledLabel("Select Prescription:"),
			BorderLayout.NORTH);
		prescriptionsCombo = createStyledComboBox();
		prescriptionComboPanel.add(prescriptionsCombo, BorderLayout.CENTER);

		// Botón para agregar prescription
		JPanel addButtonPanel = new JPanel(
			new FlowLayout(FlowLayout.LEFT, 0, 0));
		addButtonPanel.setBackground(PANEL_BACKGROUND);
		addButtonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		addPrescriptionButton = createStyledButton("Add Prescription",
			PRIMARY_COLOR);
		addPrescriptionButton.addActionListener(e -> addPrescription());
		addButtonPanel.add(addPrescriptionButton);

		// Lista de prescriptions
		prescriptionsListModel = new DefaultListModel<>();
		prescriptionsList = new JList<>(prescriptionsListModel);
		prescriptionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		prescriptionsList.addListSelectionListener(e -> updateDeleteButtons());
		prescriptionsList.setBackground(PANEL_BACKGROUND);
		prescriptionsList.setFont(INFO_FONT);
		prescriptionsList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);
				if (value instanceof PrescriptionDTO) {
					PrescriptionDTO prescription = (PrescriptionDTO) value;
					setText(prescription.getName()
						+ (prescription.getNotes() != null
							&& !prescription.getNotes().isEmpty()
								? " - " + prescription.getNotes()
								: ""));
				}
				return this;
			}
		});
		JScrollPane prescriptionsScroll = createStyledScrollPane(
			prescriptionsList, "Added Prescriptions");
		prescriptionsScroll.setPreferredSize(new Dimension(0, 150));

		// Botón para eliminar prescription
		deletePrescriptionButton = createStyledButton("Delete Selected",
			LOGOUT_COLOR);
		deletePrescriptionButton.addActionListener(e -> deletePrescription());
		deletePrescriptionButton.setEnabled(false);

		JPanel prescriptionDeletePanel = new JPanel(
			new FlowLayout(FlowLayout.RIGHT));
		prescriptionDeletePanel.setBackground(PANEL_BACKGROUND);
		prescriptionDeletePanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		prescriptionDeletePanel.add(deletePrescriptionButton);

		// Layout para columna de prescriptions
		JPanel prescriptionContentPanel = new JPanel();
		prescriptionContentPanel.setLayout(
			new BoxLayout(prescriptionContentPanel, BoxLayout.Y_AXIS));
		prescriptionContentPanel.setBackground(PANEL_BACKGROUND);
		prescriptionContentPanel.add(prescriptionSearchPanel);
		prescriptionContentPanel.add(prescriptionComboPanel);
		prescriptionContentPanel.add(addButtonPanel);
		prescriptionContentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		prescriptionContentPanel.add(prescriptionsScroll);
		prescriptionContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		prescriptionContentPanel.add(prescriptionDeletePanel);

		column.add(prescriptionContentPanel, BorderLayout.CENTER);
		return column;
	}

	private JPanel createMedicationColumn() {
		JPanel column = new JPanel(new BorderLayout(5, 5));
		column.setBackground(PANEL_BACKGROUND);

		// Search panel para medications
		JPanel medicationSearchPanel = new JPanel(new BorderLayout(5, 5));
		medicationSearchPanel.setBackground(PANEL_BACKGROUND);
		medicationSearchPanel.add(createStyledLabel("Search Medications:"),
			BorderLayout.NORTH);
		medicationSearchField = createStyledTextField();
		medicationSearchField	.getDocument()
								.addDocumentListener(
									new javax.swing.event.DocumentListener() {
										@Override
										public void changedUpdate(
											javax.swing.event.DocumentEvent e) {
											searchMedications();
										}

										@Override
										public void removeUpdate(
											javax.swing.event.DocumentEvent e) {
											searchMedications();
										}

										@Override
										public void insertUpdate(
											javax.swing.event.DocumentEvent e) {
											searchMedications();
										}
									});
		medicationSearchPanel.add(medicationSearchField, BorderLayout.CENTER);

		// ComboBox para medications
		JPanel medicationComboPanel = new JPanel(new BorderLayout(5, 5));
		medicationComboPanel.setBackground(PANEL_BACKGROUND);
		medicationComboPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		medicationComboPanel.add(createStyledLabel("Select Medication:"),
			BorderLayout.NORTH);
		medicationsCombo = createStyledComboBox();
		medicationComboPanel.add(medicationsCombo, BorderLayout.CENTER);

		// Botón para agregar medication
		JPanel addButtonPanel = new JPanel(
			new FlowLayout(FlowLayout.LEFT, 0, 0));
		addButtonPanel.setBackground(PANEL_BACKGROUND);
		addButtonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		addMedicationButton = createStyledButton("Add Medication",
			PRIMARY_COLOR);
		addMedicationButton.addActionListener(e -> addMedication());
		addButtonPanel.add(addMedicationButton);

		// Lista de medications
		medicationsListModel = new DefaultListModel<>();
		medicationsList = new JList<>(medicationsListModel);
		medicationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		medicationsList.addListSelectionListener(e -> updateDeleteButtons());
		medicationsList.setBackground(PANEL_BACKGROUND);
		medicationsList.setFont(INFO_FONT);
		medicationsList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);
				if (value instanceof MedicationDTO) {
					MedicationDTO medication = (MedicationDTO) value;
					String text = medication.getMedication_name();
					if (medication.getAmount() > 0) {
						text += " - " + medication.getAmount() + "mg";
					}
					if (medication.getDuration() > 0) {
						text += " for " + medication.getDuration() + " days";
					}
					if (medication.getInterval_hours() > 0) {
						text += " every " + medication.getInterval_hours()
							+ "h";
					}
					if (medication.getNotes() != null
						&& !medication.getNotes().isEmpty()) {
						text += " - " + medication.getNotes();
					}
					setText(text);
				}
				return this;
			}
		});
		JScrollPane medicationsScroll = createStyledScrollPane(medicationsList,
			"Added Medications");
		medicationsScroll.setPreferredSize(new Dimension(0, 150));

		// Botón para eliminar medication
		deleteMedicationButton = createStyledButton("Delete Selected",
			LOGOUT_COLOR);
		deleteMedicationButton.addActionListener(e -> deleteMedication());
		deleteMedicationButton.setEnabled(false);

		JPanel medicationDeletePanel = new JPanel(
			new FlowLayout(FlowLayout.RIGHT));
		medicationDeletePanel.setBackground(PANEL_BACKGROUND);
		medicationDeletePanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		medicationDeletePanel.add(deleteMedicationButton);

		// Layout para columna de medications
		JPanel medicationContentPanel = new JPanel();
		medicationContentPanel.setLayout(
			new BoxLayout(medicationContentPanel, BoxLayout.Y_AXIS));
		medicationContentPanel.setBackground(PANEL_BACKGROUND);
		medicationContentPanel.add(medicationSearchPanel);
		medicationContentPanel.add(medicationComboPanel);
		medicationContentPanel.add(addButtonPanel);
		medicationContentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		medicationContentPanel.add(medicationsScroll);
		medicationContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		medicationContentPanel.add(medicationDeletePanel);

		column.add(medicationContentPanel, BorderLayout.CENTER);
		return column;
	}

	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(new EmptyBorder(15, 0, 0, 0));

		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		leftPanel.setBackground(BACKGROUND_COLOR);

		JButton clinicalOrderButton = createStyledButton(
			"Create Clinical Order", PRIMARY_COLOR);
		clinicalOrderButton.addActionListener(e -> createClinicalOrder());
		leftPanel.add(clinicalOrderButton);

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		rightPanel.setBackground(BACKGROUND_COLOR);

		saveToMedicalRecordButton = createStyledButton("Save to Medical Record",
			SUCCESS_COLOR);
		saveToMedicalRecordButton.addActionListener(e -> saveToMedicalRecord());

		closeButton = createStyledButton("Close", LOGOUT_COLOR);
		closeButton.addActionListener(e -> handleClose());

		editButton = createStyledButton("Edit Notes", WARNING_COLOR);
		editButton.addActionListener(e -> editNotes());

		rightPanel.add(saveToMedicalRecordButton);
		rightPanel.add(editButton);
		rightPanel.add(closeButton);

		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(rightPanel, BorderLayout.CENTER);

		return panel;
	}

	private void createClinicalOrder() {
		int doctorId = model.getDoctorIdForAppointment(appointment.getId());
		DoctorDTO currentDoctor = model.getDoctorById(doctorId);

		if (currentDoctor != null) {
			ClinicalOrderWindow clinicalOrderWindow = new ClinicalOrderWindow(
				this, model, currentDoctor, appointment.getPatient_id(),
				appointment.getId());
			clinicalOrderWindow.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(this,
				"Unable to determine current doctor", "Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private JLabel createStyledLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(LABEL_FONT);
		label.setForeground(Color.DARK_GRAY);
		return label;
	}

	private JLabel createStatusLabel() {
		JLabel label = new JLabel();
		label.setFont(new Font("Segoe UI", Font.BOLD, 12));
		label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		return label;
	}

	private JButton createStyledButton(String text, Color color) {
		JButton button = new JButton(text);
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setFont(BUTTON_FONT);
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(color.darker(), 2),
			BorderFactory.createEmptyBorder(8, 15, 8, 15)));
		button.setFocusPainted(false);
		button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				button.setBackground(color.brighter());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(color);
			}
		});

		return button;
	}

	private JTextField createStyledTextField() {
		JTextField field = new JTextField();
		field.setFont(INFO_FONT);
		field.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_COLOR, 1),
			BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		return field;
	}

	private JComboBox createStyledComboBox() {
		JComboBox combo = new JComboBox();
		combo.setFont(INFO_FONT);
		combo.setBackground(PANEL_BACKGROUND);
		combo.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_COLOR, 1),
			BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		return combo;
	}

	private JScrollPane createStyledScrollPane(JList list, String title) {
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(BORDER_COLOR, 1), title,
			TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
			LABEL_FONT, PRIMARY_COLOR));
		scrollPane.setPreferredSize(new Dimension(0, 150));
		return scrollPane;
	}

	private void addStyledDetailRow(JPanel panel, String label, String value) {
		JLabel keyLabel = createStyledLabel(label);
		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(LABEL_FONT);
		valueLabel.setForeground(Color.BLACK);

		panel.add(keyLabel);
		panel.add(valueLabel);
	}

	private void addDetailRow(JPanel panel, String label, String value) {
		JLabel keyLabel = new JLabel(label);
		keyLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

		panel.add(keyLabel);
		panel.add(valueLabel);
	}

	private String getStatusDisplay(String status) {
		if (status == null || status.isEmpty()) {
			return "Scheduled";
		}
		switch (status.toLowerCase()) {
		case "scheduled":
			return "Scheduled";
		case "completed":
			return "Completed";
		case "cancelled":
			return "Cancelled";
		case "no_show":
			return "No Show";
		default:
			return status;
		}
	}

	private void updateAttendanceDisplay() {
		String statusText;
		Color color;

		if (appointment.getAttended() == null) {
			statusText = "Not Recorded";
			color = Color.GRAY;
		} else if (appointment.getAttended() == 1) {
			statusText = "Attended";
			color = SUCCESS_COLOR;
		} else {
			statusText = "Did Not Attend";
			color = LOGOUT_COLOR;
		}

		attendanceLabel.setText(statusText);
		attendanceLabel.setForeground(color);
	}

	private void updateCheckTimesDisplay() {
		if (appointment.hasCheckedIn()) {
			checkInLabel.setText(appointment.getFormattedCheckInTime());
			checkInLabel.setForeground(SUCCESS_COLOR);
			checkInButton.setEnabled(false);
			editCheckInButton.setEnabled(true);
		} else {
			checkInLabel.setText("Not checked in");
			checkInLabel.setForeground(LOGOUT_COLOR);
			checkInButton.setEnabled(true);
			editCheckInButton.setEnabled(true);
		}

		if (appointment.hasCheckedOut()) {
			checkOutLabel.setText(appointment.getFormattedCheckOutTime());
			checkOutLabel.setForeground(PRIMARY_COLOR);
			checkOutButton.setEnabled(false);
			editCheckOutButton.setEnabled(true);
		} else {
			checkOutLabel.setText("Not checked out");
			checkOutLabel.setForeground(LOGOUT_COLOR);
			checkOutButton.setEnabled(appointment.hasCheckedIn());
			editCheckOutButton.setEnabled(true);
		}
	}

	private void checkInNow() {
		int response = JOptionPane.showConfirmDialog(this,
			"Register check-in with current time?", "Check-in Confirmation",
			JOptionPane.YES_NO_OPTION);

		if (response == JOptionPane.YES_OPTION) {
			try {
				model.checkIn(appointment.getId());
				appointment.setCheck_in_time(
					LocalTime	.now()
								.format(DateTimeFormatter.ofPattern("HH:mm")));
				updateCheckTimesDisplay();

				JOptionPane.showMessageDialog(this,
					"Check-in registered successfully!", "Success",
					JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
					"Error registering check-in: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void checkOutNow() {
		int response = JOptionPane.showConfirmDialog(this,
			"Register check-out with current time?", "Check-out Confirmation",
			JOptionPane.YES_NO_OPTION);

		if (response == JOptionPane.YES_OPTION) {
			try {
				model.checkOut(appointment.getId());
				appointment.setCheck_out_time(
					LocalTime	.now()
								.format(DateTimeFormatter.ofPattern("HH:mm")));
				updateCheckTimesDisplay();

				JOptionPane.showMessageDialog(this,
					"Check-out registered successfully!", "Success",
					JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
					"Error registering check-out: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void editCheckInTime() {
		String currentTime = appointment.hasCheckedIn()
			? appointment.getCheck_in_time()
			: LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

		String newTime = JOptionPane.showInputDialog(this,
			"Enter check-in time (HH:mm):", currentTime);

		if (newTime != null && !newTime.trim().isEmpty()) {
			if (isValidTime(newTime)) {
				try {
					model.checkIn(appointment.getId(), newTime.trim());
					appointment.setCheck_in_time(newTime.trim());
					updateCheckTimesDisplay();

					JOptionPane.showMessageDialog(this,
						"Check-in time updated successfully!", "Success",
						JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this,
						"Error updating check-in time: " + e.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this,
					"Invalid time format. Please use HH:mm format (e.g., 09:30)",
					"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void editCheckOutTime() {
		String currentTime = appointment.hasCheckedOut()
			? appointment.getCheck_out_time()
			: LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

		String newTime = JOptionPane.showInputDialog(this,
			"Enter check-out time (HH:mm):", currentTime);

		if (newTime != null && !newTime.trim().isEmpty()) {
			if (isValidTime(newTime)) {
				try {
					model.checkOut(appointment.getId(), newTime.trim());
					appointment.setCheck_out_time(newTime.trim());
					updateCheckTimesDisplay();

					JOptionPane.showMessageDialog(this,
						"Check-out time updated successfully!", "Success",
						JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this,
						"Error updating check-out time: " + e.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this,
					"Invalid time format. Please use HH:mm format (e.g., 09:30)",
					"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private boolean isValidTime(String time) {
		try {
			LocalTime.parse(time);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	private void markAttendance(int attended) {
		int response = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to mark this appointment as "
				+ (attended == 1 ? "ATTENDED" : "DID NOT ATTEND") + "?",
			"Confirm Attendance", JOptionPane.YES_NO_OPTION);

		if (response == JOptionPane.YES_OPTION) {
			try {
				model.updateAttendance(appointment.getId(), attended);
				appointment.setAttended(attended);
				updateAttendanceDisplay();

				JOptionPane.showMessageDialog(this,
					"Attendance recorded successfully!", "Success",
					JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
					"Error recording attendance: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void handleConsultationCauseSelection() {
		String selected = (String) consultationCauseCombo.getSelectedItem();
		consultationCauseOtherPanel.setVisible("Other".equals(selected));
		if (!"Other".equals(selected)) {
			consultationCauseOtherField.setText("");
		}
	}

	private void handleFamiliarAntecedentSelection() {
		String selected = (String) familiarAntecedentCombo.getSelectedItem();
		familiarAntecedentOtherPanel.setVisible("Other".equals(selected));
		if (!"Other".equals(selected)) {
			familiarAntecedentOtherField.setText("");
		}
	}

	private void handlePersonalProblemSelection() {
		String selected = (String) personalProblemCombo.getSelectedItem();
		personalProblemOtherPanel.setVisible("Other".equals(selected));
		if (!"Other".equals(selected)) {
			personalProblemOtherField.setText("");
		}
	}

	private void loadComboBoxOptions() {
		List<String> consultationCauses = model.getConsultationCauseOptions();
		for (String cause : consultationCauses) {
			consultationCauseCombo.addItem(cause);
		}
		consultationCauseCombo.addItem("Other");

		List<String> familiarAntecedents = model.getFamiliarAntecedentOptions();
		for (String antecedent : familiarAntecedents) {
			familiarAntecedentCombo.addItem(antecedent);
		}
		familiarAntecedentCombo.addItem("Other");

		List<String> personalProblems = model.getPersonalProblemOptions();
		for (String problem : personalProblems) {
			personalProblemCombo.addItem(problem);
		}
		personalProblemCombo.addItem("Other");

		prescriptionsCombo.removeAllItems();
		prescriptionsCombo.addItem(
			new PrescriptionDTO(-1, "Select prescription...", ""));
		List<PrescriptionDTO> prescriptionOptions = prescModel.getPrescriptionOptions();
		for (PrescriptionDTO prescription : prescriptionOptions) {
			prescriptionsCombo.addItem(prescription);
		}

		medicationsCombo.removeAllItems();
		medicationsCombo.addItem(
			new MedicationDTO(-1, "Select medication...", 0, 0, 0, ""));
		List<MedicationDTO> medicationOptions = medicationModel.getMedicationOptions();
		for (MedicationDTO medication : medicationOptions) {
			medicationsCombo.addItem(medication);
		}

		loadAllPrescriptions();
		loadAllMedications();

		prescriptionsCombo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);
				if (value instanceof PrescriptionDTO) {
					PrescriptionDTO prescription = (PrescriptionDTO) value;
					setText(prescription.getName());
				}
				return this;
			}
		});

		medicationsCombo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);
				if (value instanceof MedicationDTO) {
					MedicationDTO medication = (MedicationDTO) value;
					setText(medication.getMedication_name());
				}
				return this;
			}
		});
	}

	private void addConsultationCause() {
		String cause = (String) consultationCauseCombo.getSelectedItem();
		String notes = consultationCauseNotes.getText().trim();

		if ("Select consultation cause...".equals(cause)) {
			JOptionPane.showMessageDialog(this,
				"Please select a consultation cause", "Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		if ("Other".equals(cause)) {
			String otherCause = consultationCauseOtherField.getText().trim();
			if (otherCause.isEmpty()) {
				JOptionPane.showMessageDialog(this,
					"Please specify the consultation cause", "Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			cause = otherCause;
		}

		int doctorId = model.getDoctorIdForAppointment(appointment.getId());
		String doctorName = model.getDoctorNameById(doctorId);
		model.addConsultationCause(appointment.getId(), cause, doctorId,
			doctorName, notes);

		consultationCauseListModel.addElement(
			cause + (notes.isEmpty() ? "" : " - " + notes));
		consultationCauseNotes.setText("");
		consultationCauseOtherField.setText("");
		hasUnsavedChanges = true;
		updateDeleteButtons();
	}

	private void addFamiliarAntecedent() {
		String antecedent = (String) familiarAntecedentCombo.getSelectedItem();
		String notes = familiarAntecedentNotes.getText().trim();

		if ("Select familiar antecedent...".equals(antecedent)) {
			JOptionPane.showMessageDialog(this,
				"Please select a familiar antecedent", "Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		if ("Other".equals(antecedent)) {
			String otherAntecedent = familiarAntecedentOtherField	.getText()
																	.trim();
			if (otherAntecedent.isEmpty()) {
				JOptionPane.showMessageDialog(this,
					"Please specify the familiar antecedent", "Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			antecedent = otherAntecedent;
		}

		int doctorId = model.getDoctorIdForAppointment(appointment.getId());
		String doctorName = model.getDoctorNameById(doctorId);

		model.addFamiliarAntecedent(appointment.getPatient_id(), antecedent,
			doctorId, doctorName, notes);

		familiarAntecedentListModel.addElement(
			antecedent + (notes.isEmpty() ? "" : " - " + notes));
		familiarAntecedentNotes.setText("");
		familiarAntecedentOtherField.setText("");
		hasUnsavedChanges = true;
		updateDeleteButtons();
	}

	private void addPersonalProblem() {
		String problem = (String) personalProblemCombo.getSelectedItem();
		String notes = personalProblemNotes.getText().trim();

		if ("Select personal problem...".equals(problem)) {
			JOptionPane.showMessageDialog(this,
				"Please select a personal problem", "Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		if ("Other".equals(problem)) {
			String otherProblem = personalProblemOtherField.getText().trim();
			if (otherProblem.isEmpty()) {
				JOptionPane.showMessageDialog(this,
					"Please specify the personal problem", "Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			problem = otherProblem;
		}

		int doctorId = model.getDoctorIdForAppointment(appointment.getId());
		String doctorName = model.getDoctorNameById(doctorId);

		model.addPersonalProblem(appointment.getPatient_id(), problem, doctorId,
			doctorName, notes);

		personalProblemListModel.addElement(
			problem + (notes.isEmpty() ? "" : " - " + notes));
		personalProblemNotes.setText("");
		personalProblemOtherField.setText("");
		hasUnsavedChanges = true;
		updateDeleteButtons();
	}

	private void deleteConsultationCause() {
		int selectedIndex = consultationCauseList.getSelectedIndex();
		if (selectedIndex != -1) {
			model.removeTempConsultationCause(selectedIndex);
			consultationCauseListModel.remove(selectedIndex);
			hasUnsavedChanges = true;
			updateDeleteButtons();
		}
	}

	private void deleteFamiliarAntecedent() {
		int selectedIndex = familiarAntecedentList.getSelectedIndex();
		if (selectedIndex != -1) {
			model.removeTempFamiliarAntecedent(selectedIndex);
			familiarAntecedentListModel.remove(selectedIndex);
			hasUnsavedChanges = true;
			updateDeleteButtons();
		}
	}

	private void deletePersonalProblem() {
		int selectedIndex = personalProblemList.getSelectedIndex();
		if (selectedIndex != -1) {
			model.removeTempPersonalProblem(selectedIndex);
			personalProblemListModel.remove(selectedIndex);
			hasUnsavedChanges = true;
			updateDeleteButtons();
		}
	}

	private void addPrescription() {
		PrescriptionDTO selected = (PrescriptionDTO) prescriptionsCombo.getSelectedItem();
		String notes = treatmentsNotes.getText().trim();

		if (selected == null || selected.getId() == -1) {
			JOptionPane.showMessageDialog(this, "Please select a prescription",
				"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		PrescriptionDTO newPrescription = new PrescriptionDTO(selected.getId(),
			selected.getName(), notes);
		prescriptionsListModel.addElement(newPrescription);
		treatmentsNotes.setText("");
		hasUnsavedChanges = true;
		updateDeleteButtons();
	}

	private void searchPrescriptions() {
		String searchText = prescriptionSearchField	.getText()
													.trim()
													.toLowerCase();

		if (searchText.isEmpty()) {
			loadAllPrescriptions();
			return;
		}

		List<PrescriptionDTO> allPrescriptions = prescModel.getPrescriptionOptions();
		List<PrescriptionDTO> filteredPrescriptions = new ArrayList<>();

		for (PrescriptionDTO prescription : allPrescriptions) {
			if (prescription.getName().toLowerCase().contains(searchText)) {
				filteredPrescriptions.add(prescription);
			}
		}

		updatePrescriptionsComboBox(filteredPrescriptions);
	}

	private void searchMedications() {
		String searchText = medicationSearchField	.getText()
													.trim()
													.toLowerCase();

		if (searchText.isEmpty()) {
			loadAllMedications();
			return;
		}

		List<MedicationDTO> allMedications = medicationModel.getMedicationOptions();
		List<MedicationDTO> filteredMedications = new ArrayList<>();

		for (MedicationDTO medication : allMedications) {
			if (medication	.getMedication_name()
							.toLowerCase()
							.contains(searchText)) {
				filteredMedications.add(medication);
			}
		}

		updateMedicationsComboBox(filteredMedications);
	}

	private void loadAllPrescriptions() {
		List<PrescriptionDTO> allPrescriptions = prescModel.getPrescriptionOptions();
		updatePrescriptionsComboBox(allPrescriptions);
	}

	private void loadAllMedications() {
		List<MedicationDTO> allMedications = medicationModel.getMedicationOptions();
		updateMedicationsComboBox(allMedications);
	}

	private void updatePrescriptionsComboBox(
		List<PrescriptionDTO> prescriptions) {
		prescriptionsCombo.removeAllItems();
		prescriptionsCombo.addItem(
			new PrescriptionDTO(-1, "Select prescription...", ""));

		for (PrescriptionDTO prescription : prescriptions) {
			prescriptionsCombo.addItem(prescription);
		}
	}

	private void updateMedicationsComboBox(List<MedicationDTO> medications) {
		medicationsCombo.removeAllItems();
		medicationsCombo.addItem(
			new MedicationDTO(-1, "Select medication...", 0, 0, 0, ""));

		for (MedicationDTO medication : medications) {
			medicationsCombo.addItem(medication);
		}
	}

	private void addMedication() {
		MedicationDTO selected = (MedicationDTO) medicationsCombo.getSelectedItem();

		if (selected == null || selected.getId() == -1) {
			JOptionPane.showMessageDialog(this, "Please select a medication",
				"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		MedicationDTO newMedication = new MedicationDTO(selected.getId(),
			selected.getMedication_name(), selected.getAmount(),
			selected.getDuration(), selected.getInterval_hours(),
			treatmentsNotes.getText().trim());

		MedicationDetailsDialog dialog = new MedicationDetailsDialog(
			newMedication);
		dialog.setVisible(true);

		if (dialog.isAccepted()) {
			MedicationDTO configuredMedication = dialog.getMedication();
			medicationsListModel.addElement(configuredMedication);
			treatmentsNotes.setText("");
			hasUnsavedChanges = true;
			updateDeleteButtons();

			JOptionPane.showMessageDialog(this,
				"Medication '" + configuredMedication.getMedication_name()
					+ "' added successfully!",
				"Success", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void deletePrescription() {
		int selectedIndex = prescriptionsList.getSelectedIndex();
		if (selectedIndex != -1) {
			prescriptionsListModel.remove(selectedIndex);
			hasUnsavedChanges = true;
			updateDeleteButtons();
		}
	}

	private void deleteMedication() {
		int selectedIndex = medicationsList.getSelectedIndex();
		if (selectedIndex != -1) {
			medicationsListModel.remove(selectedIndex);
			hasUnsavedChanges = true;
			updateDeleteButtons();
		}
	}

	private void updateDeleteButtons() {
		deleteConsultationCauseButton.setEnabled(
			consultationCauseList.getSelectedIndex() != -1);
		deleteFamiliarAntecedentButton.setEnabled(
			familiarAntecedentList.getSelectedIndex() != -1);
		deletePersonalProblemButton.setEnabled(
			personalProblemList.getSelectedIndex() != -1);
		deletePrescriptionButton.setEnabled(
			prescriptionsList.getSelectedIndex() != -1);
		deleteMedicationButton.setEnabled(
			medicationsList.getSelectedIndex() != -1);
		deleteIllnessButton.setEnabled(illnessesList.getSelectedIndex() != -1);
	}

	private void saveToMedicalRecord() {
		try {
			int doctorId = model.getDoctorIdForAppointment(appointment.getId());
			String doctorName = model.getDoctorNameById(doctorId);

			System.out.println("DEBUG - Before saving - Patient ID: "
				+ appointment.getPatient_id());
			PatientDTO patientBefore = model.getPatientById(
				appointment.getPatient_id());
			System.out.println("DEBUG - Before saving - Patient: "
				+ (patientBefore != null ? patientBefore.getFullName()
					: "NULL"));

			for (int i = 0; i < prescriptionsListModel.getSize(); i++) {
				PrescriptionDTO prescription = prescriptionsListModel.getElementAt(
					i);
				prescModel.savePrescription(prescription, appointment.getId());
			}

			for (int i = 0; i < medicationsListModel.getSize(); i++) {
				MedicationDTO medication = medicationsListModel.getElementAt(i);
				medicationModel.saveMedication(medication, appointment.getId());
			}

			for (int i = 0; i < illnessesListModel.getSize(); i++) {
				ICDDTO illness = illnessesListModel.getElementAt(i);
				recordModel.saveIllnessToMedicalRecord(
					appointment.getPatient_id(), appointment.getId(), doctorId,
					doctorName, illness);
			}

			model.saveToMedicalRecord(appointment.getPatient_id(),
				appointment.getId(), doctorId, doctorName);

			hasUnsavedChanges = false;
			JOptionPane.showMessageDialog(this,
				"All changes saved to medical record successfully!", "Success",
				JOptionPane.INFORMATION_MESSAGE);

			try {
				PatientDTO patient = model.getPatientById(
					appointment.getPatient_id());
				System.out.println("DEBUG - Final patient check: "
					+ (patient != null ? patient.getFullName() : "NULL"));

				if (patientBefore != null) {
					MedicalRecordWindow medicalRecordWindow = new MedicalRecordWindow(
						model, patientBefore);
					medicalRecordWindow.setVisible(true);
				} else {
					try {
						AppointmentModel freshModel = new AppointmentModel();
						PatientDTO recoveredPatient = freshModel.getPatientById(
							appointment.getPatient_id());
						if (recoveredPatient != null) {
							MedicalRecordWindow medicalRecordWindow = new MedicalRecordWindow(
								freshModel, recoveredPatient);
							medicalRecordWindow.setVisible(true);
						}
					} catch (Exception recoveryEx) {
						System.err.println(
							"Recovery also failed: " + recoveryEx.getMessage());
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
					"Error al abrir el historial médico: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
				"Error saving to medical record: " + e.getMessage(), "Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void handleClose() {
		if (hasUnsavedChanges || model.hasUnsavedChanges()) {
			int result = JOptionPane.showConfirmDialog(this,
				"You have unsaved changes. Are you sure you want to close without saving?",
				"Unsaved Changes", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

			if (result == JOptionPane.YES_OPTION) {
				dispose();
			}
		} else {
			dispose();
		}
	}

	private void loadPatientDetails() {
		SwingWorker<PatientDTO, Void> worker = new SwingWorker<PatientDTO, Void>() {
			@Override
			protected PatientDTO doInBackground() throws Exception {
				return model.getPatientById(appointment.getPatient_id());
			}

			@Override
			protected void done() {
				try {
					PatientDTO patient = get();
					if (patient != null) {
						updatePatientInfo(patient);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		worker.execute();
	}

	private void updatePatientInfo(PatientDTO patient) {
		Component[] components = getContentPane().getComponents();
		for (Component comp : components) {
			if (comp instanceof JScrollPane) {
				JScrollPane scrollPane = (JScrollPane) comp;
				JViewport viewport = scrollPane.getViewport();
				Component view = viewport.getView();
				if (view instanceof JPanel) {
					updatePatientPanel((JPanel) view, patient);
				}
			}
		}
	}

	private void updatePatientPanel(JPanel mainPanel, PatientDTO patient) {
		Component[] components = mainPanel.getComponents();
		for (Component comp : components) {
			if (comp instanceof JPanel) {
				JPanel panel = (JPanel) comp;
				Border border = panel.getBorder();
				if (border instanceof TitledBorder) {
					TitledBorder titledBorder = (TitledBorder) border;
					if ("Patient Information".equals(titledBorder.getTitle())) {
						panel.removeAll();
						panel.setLayout(new GridLayout(0, 2, 10, 5));

						addDetailRow(panel, "Name:", patient.getFullName());
						addDetailRow(panel, "DNI:", patient.getDni());
						addDetailRow(panel, "Patient ID:",
							String.valueOf(patient.getId()));
						addDetailRow(panel, "Phone:",
							patient.getPhone() != null ? patient.getPhone()
								: "N/A");
						addDetailRow(panel, "Email:",
							patient.getEmail() != null ? patient.getEmail()
								: "N/A");

						String dob = "N/A";
						if (patient.getDate_of_birth() != null) {
							try {
								LocalDate birthDate = LocalDate.parse(
									patient.getDate_of_birth());
								dob = birthDate.format(
									DateTimeFormatter.ofPattern(
										"MMMM d, yyyy"));
							} catch (Exception e) {
								dob = patient.getDate_of_birth();
							}
						}
						addDetailRow(panel, "Date of Birth:", dob);

						panel.revalidate();
						panel.repaint();
						break;
					}
				}
			}
		}
	}

	private void editNotes() {
		String newNotes = JOptionPane.showInputDialog(this,
			"Edit appointment notes:",
			appointment.getNotes() != null ? appointment.getNotes() : "");

		if (newNotes != null) {
			appointment.setNotes(newNotes);
			JOptionPane.showMessageDialog(this, "Notes updated successfully!");
		}
	}

	private void addNewDisease() {
		AddNewDiseaseWindow addDiseaseWindow = new AddNewDiseaseWindow(this);
		addDiseaseWindow.setVisible(true);
	}

	public void addIllnessFromWindow(ICDDTO selectedIllness) {
		if (selectedIllness != null) {
			for (int i = 0; i < illnessesListModel.getSize(); i++) {
				ICDDTO existingIllness = illnessesListModel.getElementAt(i);
			}

			illnessesListModel.addElement(selectedIllness);
			selectedIllnesses.add(selectedIllness);
			hasUnsavedChanges = true;
			updateDeleteButtons();
		}
	}

	private void deleteIllness() {
		int selectedIndex = illnessesList.getSelectedIndex();
		if (selectedIndex != -1) {
			ICDDTO selectedIllness = illnessesListModel.getElementAt(
				selectedIndex);
			int response = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to remove '" + selectedIllness.getCode()
					+ " - " + selectedIllness.getDescription() + "'?",
				"Confirm Removal", JOptionPane.YES_NO_OPTION);

			if (response == JOptionPane.YES_OPTION) {
				illnessesListModel.remove(selectedIndex);
				hasUnsavedChanges = true;
				updateDeleteButtons();
			}
		} else {
			JOptionPane.showMessageDialog(this,
				"Please select an illness to remove.", "No Selection",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	public void addMedicationFromWindow(MedicationDTO newMedication) {
		if (newMedication != null) {
			MedicationDetailsDialog dialog = new MedicationDetailsDialog(
				newMedication);
			dialog.setVisible(true);

			if (dialog.isAccepted()) {
				MedicationDTO configuredMedication = dialog.getMedication();
				medicationsListModel.addElement(configuredMedication);
				hasUnsavedChanges = true;
				updateDeleteButtons();

				JOptionPane.showMessageDialog(this,
					"Medication '" + configuredMedication.getMedication_name()
						+ "' added successfully!",
					"Success", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
}