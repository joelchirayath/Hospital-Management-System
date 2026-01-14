package giis.demo.ui.medicalRecord;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.medicalRecord.MedicalRecordDTO;
import giis.demo.service.patient.PatientDTO;
import giis.demo.ui.receptionist.NewVaccineAppointmentWindow; // NEW IMPORT
import giis.demo.ui.vaccine.FastVaccineWindow;

public class MedicalRecordWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	// Colores y fuentes consistentes con DoctorMenu
	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Color LOGOUT_COLOR = new Color(192, 57, 43);
	private final Color SUCCESS_COLOR = new Color(39, 174, 96);
	private final Color WARNING_COLOR = new Color(243, 156, 18);
	private final Color FAST_VACCINE_COLOR = new Color(155, 89, 182); // NEW:
																		// Purple
																		// color
																		// for
																		// fast
																		// vaccine
																		// button

	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font WELCOME_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font INFO_FONT = new Font("Segoe UI", Font.PLAIN, 12);

	private AppointmentModel model;
	private PatientDTO patient;
	private JTabbedPane tabbedPane;

	// Components for Diseases tab
	private JTable medicalRecordsTable;
	private DefaultTableModel diseasesTableModel;

	// Components for Appointments tab
	private JTable appointmentsTable;
	private DefaultTableModel appointmentsTableModel;

	// Components for Familiar Antecedents tab
	private JTable familiarAntecedentsTable;
	private DefaultTableModel antecedentsTableModel;

	// Components for Personal Problems tab
	private JTable personalProblemsTable;
	private DefaultTableModel problemsTableModel;

	// Components for Vaccines tab
	private JTable vaccinesTable;
	private DefaultTableModel vaccinesTableModel;

	private JButton closeButton;
	private JButton fastVaccineButton; // NEW: Fast vaccine button

	public MedicalRecordWindow(AppointmentModel model, PatientDTO patient) {
		this.model = model;
		this.patient = patient;
		initializeUI();
		loadMedicalRecords();
		loadAppointmentsWithCauses();
		loadFamiliarAntecedents();
		loadPersonalProblems();
		loadVaccines();
	}

	private void initializeUI() {
		// Validar que patient no sea null
		if (patient == null) {
			setTitle("Medical Record - No Patient Selected");
			throw new IllegalArgumentException("Patient cannot be null");
		} else {
			setTitle("Medical Record - " + patient.getName());
		}

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(200, 200, 1300, 700);
		setLocationRelativeTo(null);

		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		contentPane.setBackground(BACKGROUND_COLOR);
		setContentPane(contentPane);

		// Title
		JLabel titleLabel = new JLabel(
				"Medical Record - " + patient.getFullName(), JLabel.CENTER);
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(PRIMARY_COLOR);
		titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
		contentPane.add(titleLabel, BorderLayout.NORTH);

		// Patient Information Panel
		contentPane.add(createPatientInfoPanel(), BorderLayout.NORTH);

		// Tabbed Pane for different sections
		tabbedPane = new JTabbedPane();
		tabbedPane.setBackground(BACKGROUND_COLOR);
		tabbedPane.setFont(SECTION_FONT);

		tabbedPane.addTab("Diseases (ICD-10)", createDiseasesPanel());
		tabbedPane.addTab("Appointments & Causes", createAppointmentsPanel());
		tabbedPane.addTab("Familiar Antecedents",
				createFamiliarAntecedentsPanel());
		tabbedPane.addTab("Personal Problems", createPersonalProblemsPanel());
		tabbedPane.addTab("Vaccines", createVaccinesPanel());

		contentPane.add(tabbedPane, BorderLayout.CENTER);

		// Buttons Panel
		contentPane.add(createButtonsPanel(), BorderLayout.SOUTH);
	}

	private JPanel createPatientInfoPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 3, 10, 8));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(
				BorderFactory
						.createCompoundBorder(
								BorderFactory.createTitledBorder(
										BorderFactory.createLineBorder(
												BORDER_COLOR, 1),
										"Patient Information",
										TitledBorder.DEFAULT_JUSTIFICATION,
										TitledBorder.DEFAULT_POSITION,
										SECTION_FONT, PRIMARY_COLOR),
								BorderFactory.createEmptyBorder(10, 10, 10,
										10)));

		addStyledDetailRow(panel, "Name:", patient.getFullName());
		addStyledDetailRow(panel, "DNI:", patient.getDni());
		addStyledDetailRow(panel, "Date of Birth:",
				formatDate(patient.getDate_of_birth()));
		addStyledDetailRow(panel, "Phone:",
				patient.getPhone() != null ? patient.getPhone() : "N/A");
		addStyledDetailRow(panel, "Email:",
				patient.getEmail() != null ? patient.getEmail() : "N/A");
		addStyledDetailRow(panel, "Address:",
				patient.getAddress() != null ? patient.getAddress() : "N/A");

		return panel;
	}

	private JPanel createDiseasesPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		String[] columnNames = { "Diagnosis Date", "ICD-10 Code",
				"Disease Description", "Category", "Diagnosing Doctor",
				"Appointment Date", "Notes" };

		diseasesTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		medicalRecordsTable = new JTable(diseasesTableModel);
		medicalRecordsTable
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		medicalRecordsTable.getTableHeader().setReorderingAllowed(false);
		medicalRecordsTable.getTableHeader().setFont(LABEL_FONT);
		medicalRecordsTable.setFont(INFO_FONT);
		medicalRecordsTable.setBackground(PANEL_BACKGROUND);
		medicalRecordsTable.setRowHeight(25);

		medicalRecordsTable.getColumnModel().getColumn(0)
				.setPreferredWidth(100);
		medicalRecordsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		medicalRecordsTable.getColumnModel().getColumn(2)
				.setPreferredWidth(200);
		medicalRecordsTable.getColumnModel().getColumn(3)
				.setPreferredWidth(120);
		medicalRecordsTable.getColumnModel().getColumn(4)
				.setPreferredWidth(150);
		medicalRecordsTable.getColumnModel().getColumn(5)
				.setPreferredWidth(100);
		medicalRecordsTable.getColumnModel().getColumn(6)
				.setPreferredWidth(150);

		JScrollPane scrollPane = new JScrollPane(medicalRecordsTable);
		scrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				"Diseases History (ICD-10)", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, SECTION_FONT, PRIMARY_COLOR));
		scrollPane.getViewport().setBackground(PANEL_BACKGROUND);

		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createAppointmentsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		String[] columnNames = { "Date", "Time", "Office", "Doctor",
				"Consultation Cause", "Recorded Date", "Status",
				"Check-in/out" };

		appointmentsTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		appointmentsTable = new JTable(appointmentsTableModel);
		appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		appointmentsTable.getTableHeader().setReorderingAllowed(false);
		appointmentsTable.getTableHeader().setFont(LABEL_FONT);
		appointmentsTable.setFont(INFO_FONT);
		appointmentsTable.setBackground(PANEL_BACKGROUND);
		appointmentsTable.setRowHeight(25);

		appointmentsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		appointmentsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		appointmentsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
		appointmentsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
		appointmentsTable.getColumnModel().getColumn(4).setPreferredWidth(250);
		appointmentsTable.getColumnModel().getColumn(5).setPreferredWidth(120);
		appointmentsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
		appointmentsTable.getColumnModel().getColumn(7).setPreferredWidth(120);

		JScrollPane scrollPane = new JScrollPane(appointmentsTable);
		scrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				"Appointments History with Consultation Causes",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, SECTION_FONT, PRIMARY_COLOR));
		scrollPane.getViewport().setBackground(PANEL_BACKGROUND);

		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createFamiliarAntecedentsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		String[] columnNames = { "Recorded Date", "Familiar Antecedent",
				"Doctor", "Notes" };

		antecedentsTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		familiarAntecedentsTable = new JTable(antecedentsTableModel);
		familiarAntecedentsTable
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		familiarAntecedentsTable.getTableHeader().setReorderingAllowed(false);
		familiarAntecedentsTable.getTableHeader().setFont(LABEL_FONT);
		familiarAntecedentsTable.setFont(INFO_FONT);
		familiarAntecedentsTable.setBackground(PANEL_BACKGROUND);
		familiarAntecedentsTable.setRowHeight(25);

		familiarAntecedentsTable.getColumnModel().getColumn(0)
				.setPreferredWidth(120);
		familiarAntecedentsTable.getColumnModel().getColumn(1)
				.setPreferredWidth(300);
		familiarAntecedentsTable.getColumnModel().getColumn(2)
				.setPreferredWidth(150);
		familiarAntecedentsTable.getColumnModel().getColumn(3)
				.setPreferredWidth(200);

		JScrollPane scrollPane = new JScrollPane(familiarAntecedentsTable);
		scrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				"Familiar Antecedents History",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, SECTION_FONT, PRIMARY_COLOR));
		scrollPane.getViewport().setBackground(PANEL_BACKGROUND);

		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createPersonalProblemsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		String[] columnNames = { "Recorded Date", "Personal Problem", "Doctor",
				"Notes" };

		problemsTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		personalProblemsTable = new JTable(problemsTableModel);
		personalProblemsTable
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		personalProblemsTable.getTableHeader().setReorderingAllowed(false);
		personalProblemsTable.getTableHeader().setFont(LABEL_FONT);
		personalProblemsTable.setFont(INFO_FONT);
		personalProblemsTable.setBackground(PANEL_BACKGROUND);
		personalProblemsTable.setRowHeight(25);

		personalProblemsTable.getColumnModel().getColumn(0)
				.setPreferredWidth(120);
		personalProblemsTable.getColumnModel().getColumn(1)
				.setPreferredWidth(300);
		personalProblemsTable.getColumnModel().getColumn(2)
				.setPreferredWidth(150);
		personalProblemsTable.getColumnModel().getColumn(3)
				.setPreferredWidth(200);

		JScrollPane scrollPane = new JScrollPane(personalProblemsTable);
		scrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				"Personal Problems History", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, SECTION_FONT, PRIMARY_COLOR));
		scrollPane.getViewport().setBackground(PANEL_BACKGROUND);

		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createVaccinesPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		String[] columnNames = { "Scheduled Date", "Vaccine Name",
				"Dose Number", "Status", "Administered Date", "Doctor",
				"Needs Booster" };

		vaccinesTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		vaccinesTable = new JTable(vaccinesTableModel);
		vaccinesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		vaccinesTable.getTableHeader().setReorderingAllowed(false);
		vaccinesTable.getTableHeader().setFont(LABEL_FONT);
		vaccinesTable.setFont(INFO_FONT);
		vaccinesTable.setBackground(PANEL_BACKGROUND);
		vaccinesTable.setRowHeight(25);

		vaccinesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		vaccinesTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		vaccinesTable.getColumnModel().getColumn(2).setPreferredWidth(80);
		vaccinesTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		vaccinesTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		vaccinesTable.getColumnModel().getColumn(5).setPreferredWidth(150);
		vaccinesTable.getColumnModel().getColumn(6).setPreferredWidth(80);

		JScrollPane scrollPane = new JScrollPane(vaccinesTable);
		scrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				"Vaccination History", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, SECTION_FONT, PRIMARY_COLOR));
		scrollPane.getViewport().setBackground(PANEL_BACKGROUND);

		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); // Added
																			// spacing
																			// between
																			// buttons
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(new EmptyBorder(15, 0, 0, 0));

		// NEW: Fast Vaccine button
		fastVaccineButton = createStyledButton("Fast Vaccine",
				FAST_VACCINE_COLOR);
		fastVaccineButton.addActionListener(e -> {
			openFastVaccineWindow();
			reloadVaccines();
		});

		closeButton = createStyledButton("Close", LOGOUT_COLOR);
		closeButton.addActionListener(e -> dispose());

		// Add buttons in order: Fast Vaccine, then Close
		panel.add(fastVaccineButton);
		panel.add(closeButton);

		return panel;
	}

	// NEW METHOD: Reload vaccines data
	public void reloadVaccines() {
		if (patient == null) {
			return; // No patient selected
		}

		System.out.println("Reloading vaccines data for patient: "
				+ patient.getFullName() + " (ID: " + patient.getId() + ")");

		try {
			// Clear the current vaccines table
			vaccinesTableModel.setRowCount(0);

			// Load vaccines data again
			List<Object[]> vaccines = model
					.getVaccinesByPatient(patient.getId());

			List<Object[]> sortedVaccines = vaccines.stream()
					.sorted((v1, v2) -> {
						String date1 = (String) v1[0];
						String date2 = (String) v2[0];
						LocalDate localDate1 = safeParseDate(date1);
						LocalDate localDate2 = safeParseDate(date2);
						return localDate2.compareTo(localDate1);
					}).collect(Collectors.toList());

			// Repopulate the table
			for (Object[] vaccine : sortedVaccines) {
				if (vaccine.length >= 8) {
					String scheduledDate = formatDate((String) vaccine[0]);
					String vaccineName = (String) vaccine[1];
					Integer doseNumber = (Integer) vaccine[2];
					String status = (String) vaccine[3];
					String administeredDate = formatDate((String) vaccine[4]);
					String doctor = vaccine[5] != null ? (String) vaccine[5]
							: "N/A";
					Integer needsBooster = (Integer) vaccine[6];
					String notes = vaccine[7] != null ? (String) vaccine[7]
							: "";

					Object[] rowData = { scheduledDate, vaccineName,
							doseNumber != null ? "Dose " + doseNumber : "N/A",
							getVaccineStatus(status), administeredDate, doctor,
							getBoosterStatus(needsBooster), notes };
					vaccinesTableModel.addRow(rowData);
				}
			}

			if (vaccines.isEmpty()) {
				Object[] rowData = { "No data", "No vaccines recorded", "", "",
						"", "", "", "" };
				vaccinesTableModel.addRow(rowData);
			}

			// Switch to vaccines tab to show updated data
			tabbedPane.setSelectedIndex(4); // Vaccines tab is index 4

			// Show success message
//			JOptionPane.showMessageDialog(this,
//					"Vaccines data reloaded successfully!\n\n"
//							+ "Total vaccines found: " + vaccines.size(),
//					"Vaccines Updated", JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception e) {
			e.printStackTrace();
			Object[] rowData = { "Error",
					"Error reloading vaccines: " + e.getMessage(), "", "", "",
					"", "", "" };
			vaccinesTableModel.addRow(rowData);

			JOptionPane.showMessageDialog(this,
					"Error reloading vaccines data:\n" + e.getMessage(),
					"Reload Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// NEW METHOD: Open fast vaccine window
	private void openFastVaccineWindow() {
		if (patient == null) {
			JOptionPane.showMessageDialog(this, "No patient selected!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		System.out.println("Opening Fast Vaccine window for patient: "
				+ patient.getFullName() + " (ID: " + patient.getId() + ")");

		// Create and show the FastVaccineWindow
		FastVaccineWindow fastVaccineWindow = new FastVaccineWindow(this, model,
				patient);
		fastVaccineWindow.setVisible(true);
	}

	// NEW METHOD: Helper to clear vaccine window fields
	private void clearVaccineWindowFields(NewVaccineAppointmentWindow window) {
		// Try to reset/clear fields if the window has such methods
		// This depends on the actual implementation of
		// NewVaccineAppointmentWindow
		try {
			// Clear date fields
			java.lang.reflect.Field dateField = window.getClass()
					.getDeclaredField("dateField");
			dateField.setAccessible(true);
			if (dateField.get(window) instanceof javax.swing.JTextField) {
				((javax.swing.JTextField) dateField.get(window)).setText("");
			}
		} catch (Exception e) {
			// Ignore if fields don't exist or can't be accessed
			System.out.println(
					"Note: Could not clear all fields in vaccine window");
		}

		// Show message to user indicating fields are cleared for new entry
		javax.swing.JOptionPane.showMessageDialog(window,
				"Vaccine appointment form opened for " + patient.getFullName()
						+ ".\n\nPlease fill in the vaccine details. All fields start empty for new entry.",
				"New Vaccine Appointment",
				javax.swing.JOptionPane.INFORMATION_MESSAGE);
	}

	// Métodos auxiliares para crear componentes estilizados
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

		button.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				button.setBackground(color.brighter());
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				button.setBackground(color);
			}
		});

		return button;
	}

	private void addStyledDetailRow(JPanel panel, String label, String value) {
		JLabel keyLabel = new JLabel(label);
		keyLabel.setFont(LABEL_FONT);
		keyLabel.setForeground(Color.DARK_GRAY);

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(LABEL_FONT);
		valueLabel.setForeground(Color.BLACK);

		panel.add(keyLabel);
		panel.add(valueLabel);
	}

	// ========== MÉTODOS DE LÓGICA (PERMANECEN EXACTAMENTE IGUAL) ==========

	private void addDetailRow(JPanel panel, String label, String value) {
		JLabel keyLabel = new JLabel(label);
		keyLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

		panel.add(keyLabel);
		panel.add(valueLabel);
	}

	private String formatDate(String date) {
		if (date == null) {
			return "N/A";
		}
		try {
			LocalDate localDate = LocalDate.parse(date);
			return localDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
		} catch (Exception e) {
			return date;
		}
	}

	private String formatDateTime(String dateTime) {
		if (dateTime == null) {
			return "N/A";
		}
		try {
			if (dateTime.length() >= 16) {
				return dateTime.substring(0, 16);
			}
			return dateTime;
		} catch (Exception e) {
			return dateTime;
		}
	}

	private String getAttendanceStatus(Integer attended) {
		if (attended == null) {
			return "Scheduled";
		}
		return attended == 1 ? "Attended" : "No Show";
	}

	private String getCheckInOutTimes(String checkIn, String checkOut) {
		if (checkIn == null && checkOut == null) {
			return "-";
		}
		if (checkIn != null && checkOut == null) {
			return checkIn + " / -";
		}
		if (checkIn == null && checkOut != null) {
			return "- / " + checkOut;
		}
		return checkIn + " / " + checkOut;
	}

	private String getBoosterStatus(Integer needsBooster) {
		if (needsBooster == null) {
			return "No";
		}
		return needsBooster == 1 ? "Yes" : "No";
	}

	private String getVaccineStatus(String status) {
		if (status == null) {
			return "Scheduled";
		}
		switch (status.toLowerCase()) {
		case "scheduled":
			return "Scheduled";
		case "administred":
			return "Administred";
		case "cancelled":
			return "Cancelled";
		default:
			return status;
		}
	}

	private LocalDate safeParseDate(String dateString) {
		if (dateString == null || dateString.equals("N/A")
				|| dateString.equals("No data")) {
			return LocalDate.MIN;
		}
		try {
			return LocalDate.parse(dateString);
		} catch (Exception e) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter
						.ofPattern("MMM d, yyyy");
				return LocalDate.parse(dateString, formatter);
			} catch (Exception e2) {
				return LocalDate.MIN;
			}
		}
	}

	private void loadMedicalRecords() {
		List<MedicalRecordDTO> records = model
				.getMedicalRecordsByPatient(patient.getId());

		List<MedicalRecordDTO> sortedRecords = records.stream()
				.sorted((r1, r2) -> {
					LocalDate date1 = safeParseDate(
							r1.getFormattedDiagnosisDate());
					LocalDate date2 = safeParseDate(
							r2.getFormattedDiagnosisDate());
					return date2.compareTo(date1);
				}).collect(Collectors.toList());

		diseasesTableModel.setRowCount(0);

		for (MedicalRecordDTO record : sortedRecords) {
			Object[] rowData = { record.getFormattedDiagnosisDate(),
					record.getIcd10_code(), record.getIcd10_description(),
					record.getIcd10_chapter(), record.getDoctorFullName(),
					formatDate(record.getAppointment_date()),
					record.getNotes() != null ? record.getNotes() : "" };
			diseasesTableModel.addRow(rowData);
		}
	}

	private void loadAppointmentsWithCauses() {
		try {
			List<Object[]> appointments = model
					.getAppointmentsWithCauses(patient.getId());

			List<Object[]> sortedAppointments = appointments.stream()
					.sorted((a1, a2) -> {
						String date1 = (String) a1[0];
						String date2 = (String) a2[0];
						LocalDate localDate1 = safeParseDate(date1);
						LocalDate localDate2 = safeParseDate(date2);
						return localDate2.compareTo(localDate1);
					}).collect(Collectors.toList());

			appointmentsTableModel.setRowCount(0);

			for (Object[] appointment : sortedAppointments) {
				String date = formatDate((String) appointment[0]);
				String time = appointment[1] + " - " + appointment[2];
				String office = (String) appointment[3];
				String doctor = (String) appointment[4];
				String cause = (String) appointment[5];
				String causeDate = formatDateTime((String) appointment[6]);
				Integer attended = (Integer) appointment[7];
				String checkIn = (String) appointment[8];
				String checkOut = (String) appointment[9];

				Object[] rowData = { date, time, office, doctor, cause,
						"No cause recorded".equals(cause) ? "N/A" : causeDate,
						getAttendanceStatus(attended),
						getCheckInOutTimes(checkIn, checkOut) };
				appointmentsTableModel.addRow(rowData);
			}

			if (appointments.isEmpty()) {
				Object[] rowData = { "No data", "", "", "",
						"No appointments found", "", "", "" };
				appointmentsTableModel.addRow(rowData);
			}

		} catch (Exception e) {
			Object[] rowData = { "Error", "", "", "",
					"Error loading appointments: " + e.getMessage(), "", "",
					"" };
			appointmentsTableModel.addRow(rowData);
		}
	}

	private void loadFamiliarAntecedents() {
		try {
			List<Object[]> antecedents = model
					.getFamiliarAntecedentsAsList(patient.getId());

			List<Object[]> sortedAntecedents = antecedents.stream()
					.sorted((a1, a2) -> {
						String date1 = (String) a1[0];
						String date2 = (String) a2[0];
						LocalDate localDate1 = safeParseDate(date1);
						LocalDate localDate2 = safeParseDate(date2);
						return localDate2.compareTo(localDate1);
					}).collect(Collectors.toList());

			antecedentsTableModel.setRowCount(0);

			for (Object[] antecedent : sortedAntecedents) {
				if (antecedent.length >= 4) {
					String date = formatDateTime((String) antecedent[0]);
					String antecedentText = (String) antecedent[1];
					String doctor = (String) antecedent[2];
					String notes = antecedent[3] != null
							? (String) antecedent[3]
							: "";

					Object[] rowData = { date, antecedentText, doctor, notes };
					antecedentsTableModel.addRow(rowData);
				}
			}

			if (antecedents.isEmpty()) {
				Object[] rowData = { "No data",
						"No familiar antecedents recorded", "", "" };
				antecedentsTableModel.addRow(rowData);
			}

		} catch (Exception e) {
			Object[] rowData = { "Error",
					"Error loading familiar antecedents: " + e.getMessage(), "",
					"" };
			antecedentsTableModel.addRow(rowData);
		}
	}

	private void loadPersonalProblems() {
		try {
			List<Object[]> problems = model
					.getPersonalProblemsAsList(patient.getId());

			List<Object[]> sortedProblems = problems.stream()
					.sorted((p1, p2) -> {
						String date1 = (String) p1[0];
						String date2 = (String) p2[0];
						LocalDate localDate1 = safeParseDate(date1);
						LocalDate localDate2 = safeParseDate(date2);
						return localDate2.compareTo(localDate1);
					}).collect(Collectors.toList());

			problemsTableModel.setRowCount(0);

			for (Object[] problem : sortedProblems) {
				if (problem.length >= 4) {
					String date = formatDateTime((String) problem[0]);
					String problemText = (String) problem[1];
					String doctor = (String) problem[2];
					String notes = problem[3] != null ? (String) problem[3]
							: "";

					Object[] rowData = { date, problemText, doctor, notes };
					problemsTableModel.addRow(rowData);
				}
			}

			if (problems.isEmpty()) {
				Object[] rowData = { "No data", "No personal problems recorded",
						"", "" };
				problemsTableModel.addRow(rowData);
			}

		} catch (Exception e) {
			Object[] rowData = { "Error",
					"Error loading personal problems: " + e.getMessage(), "",
					"" };
			problemsTableModel.addRow(rowData);
		}
	}

	private void loadVaccines() {
		try {
			List<Object[]> vaccines = model
					.getVaccinesByPatient(patient.getId());

			List<Object[]> sortedVaccines = vaccines.stream()
					.sorted((v1, v2) -> {
						String date1 = (String) v1[0];
						String date2 = (String) v2[0];
						LocalDate localDate1 = safeParseDate(date1);
						LocalDate localDate2 = safeParseDate(date2);
						return localDate2.compareTo(localDate1);
					}).collect(Collectors.toList());

			vaccinesTableModel.setRowCount(0);

			for (Object[] vaccine : sortedVaccines) {
				if (vaccine.length >= 8) {
					String scheduledDate = formatDate((String) vaccine[0]);
					String vaccineName = (String) vaccine[1];
					Integer doseNumber = (Integer) vaccine[2];
					String status = (String) vaccine[3];
					String administeredDate = formatDate((String) vaccine[4]);
					String doctor = vaccine[5] != null ? (String) vaccine[5]
							: "N/A";
					Integer needsBooster = (Integer) vaccine[6];
					String notes = vaccine[7] != null ? (String) vaccine[7]
							: "";

					Object[] rowData = { scheduledDate, vaccineName,
							doseNumber != null ? "Dose " + doseNumber : "N/A",
							getVaccineStatus(status), administeredDate, doctor,
							getBoosterStatus(needsBooster), notes };
					vaccinesTableModel.addRow(rowData);
				}
			}

			if (vaccines.isEmpty()) {
				Object[] rowData = { "No data", "No vaccines recorded", "", "",
						"", "", "", "" };
				vaccinesTableModel.addRow(rowData);
			}

		} catch (Exception e) {
			Object[] rowData = { "Error",
					"Error loading vaccines: " + e.getMessage(), "", "", "", "",
					"", "" };
			vaccinesTableModel.addRow(rowData);
		}
	}
}