package giis.demo.ui.receptionist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.VaccineDTO;
import giis.demo.service.doctor.DoctorDTO;
import giis.demo.service.patient.PatientDTO;
import giis.demo.ui.medicalRecord.MedicalRecordWindow;
import giis.demo.ui.receptionist.searchers.DoctorSearcher;
import giis.demo.ui.receptionist.searchers.PatientSearcher;
import giis.demo.ui.receptionist.searchers.VaccineSearcher;

public class NewVaccineAppointmentWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	private JButton addPatientButton;
	private JTextField txtPatient;
	private JButton removePatientButton;
	private JSpinner dateSpinner;
	private JSpinner timeSpinner;
	private JButton bookButton;
	private JLabel labelPatient;
	private JLabel labelVaccineInfo;
	private JCheckBox multipleVaccinesCheckBox;

	// For vaccine selection
	private JPanel vaccineSelectionPanel;
	private JButton addVaccineButton;
	private JButton removeVaccineButton;
	private JButton clearVaccinesButton;
	private JList<VaccineDTO> vaccineList;
	private DefaultListModel<VaccineDTO> vaccineListModel;
	private List<VaccineDTO> selectedVaccines;

	private final AppointmentModel model;
	private PatientDTO patientDTO;
	private JFrame parent; // Added parent field

	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd-MM-yyyy");
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

	// Color constants matching the style from NewApptWindow
	private final Color PRIMARY_COLOR = new Color(41, 128, 185); // Blue color
	private final Color SECONDARY_COLOR = new Color(46, 204, 113); // Green for
																	// vaccine
																	// specific
	private final Color WARNING_COLOR = new Color(231, 76, 60); // Red for
																// warnings
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 15);
	private final Font LIST_FONT = new Font("Segoe UI", Font.PLAIN, 12);

	private JButton addDoctorButton;
	private JTextField txtDoctor;
	private JButton removeDoctorButton;
	private JLabel labelDoctor;
	private DoctorDTO selectedDoctor;

	public NewVaccineAppointmentWindow() {
		this.model = new AppointmentModel();
		this.parent = null;
		selectedVaccines = new ArrayList<>();
		initializeUI();
	}

	public NewVaccineAppointmentWindow(AppointmentModel model) {
		this.model = model;
		this.parent = null;
		selectedVaccines = new ArrayList<>();
		initializeUI();
	}

	public NewVaccineAppointmentWindow(JFrame parent, AppointmentModel model) {
		this.parent = parent;
		this.model = model;
		selectedVaccines = new ArrayList<>();
		initializeUI();
		setTitle("New Vaccine Appointment");
		pack();
		setLocationRelativeTo(parent);
	}

	public NewVaccineAppointmentWindow(MedicalRecordWindow medicalRecordWindow,
			AppointmentModel model) {
		// Llama al constructor principal con el frame padre y el modelo
		this.parent = medicalRecordWindow;
		this.model = model;
		selectedVaccines = new ArrayList<>();
		initializeUI();

		// Configura título específico para este contexto
		setTitle("Fast Vaccine Appointment - From Medical Record");

		// Aquí puedes agregar lógica adicional si es necesario
		System.out.println("Fast Vaccine window created from Medical Record");
		pack();
		setLocationRelativeTo(medicalRecordWindow);
	}

	private void initializeUI() {
		setTitle("Receptionist - Schedule Vaccine Appointment");
		setMinimumSize(new Dimension(850, 700)); // Increased height for vaccine
													// list
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
		mainPanel.setBackground(PANEL_BACKGROUND);

		JLabel header = new JLabel("New Vaccine Appointment");
		header.setFont(new Font("Segoe UI", Font.BOLD, 20));
		header.setForeground(PRIMARY_COLOR);
		header.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
		mainPanel.add(header, BorderLayout.NORTH);

		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setBackground(PANEL_BACKGROUND);
		formPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 220, 220)),
				BorderFactory.createEmptyBorder(15, 15, 15, 15)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;

		int row = 0;

		addSectionLabel("Patient Information", formPanel, gbc, row++);
		addPatientRow(formPanel, gbc, row++);
		addDoctorRow(formPanel, gbc, row++);

		addSectionLabel("Vaccine Details", formPanel, gbc, row++);

		// Add vaccine selection panel
		addVaccineSelectionRow(formPanel, gbc, row++);

		addSectionLabel("Schedule Information", formPanel, gbc, row++);

		// Suggested date label
		LocalDate suggestedDate = LocalDate.now().plusDays(14); // Suggest 2
																// weeks from
																// now
		JLabel suggestedDateLabel = new JLabel(
				"Suggested date: " + suggestedDate
						.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		suggestedDateLabel.setFont(LABEL_FONT);
		suggestedDateLabel.setForeground(PRIMARY_COLOR);
		suggestedDateLabel
				.setBorder(BorderFactory.createEmptyBorder(2, 0, 8, 0));
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 2;
		formPanel.add(suggestedDateLabel, gbc);
		gbc.gridwidth = 1;
		row++;

		// Date Selection
		dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null,
				Calendar.DAY_OF_MONTH));
		JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner,
				"dd-MM-yyyy");
		dateSpinner.setEditor(dateEditor);
		dateSpinner.setFont(LABEL_FONT);
		addFormRow(formPanel, gbc, row++, "Appointment Date:", dateSpinner);

		// Time Selection
		timeSpinner = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner,
				"HH:mm");
		timeSpinner.setEditor(timeEditor);
		timeSpinner.setFont(LABEL_FONT);
		addFormRow(formPanel, gbc, row++, "Appointment Time:", timeSpinner);

		// Button Panel
		JPanel buttonPanel = new JPanel(
				new FlowLayout(FlowLayout.CENTER, 20, 10));
		buttonPanel.setBackground(PANEL_BACKGROUND);

		bookButton = new JButton("Schedule Vaccine Appointment");
		styleButton(bookButton, SECONDARY_COLOR);
		bookButton.setPreferredSize(new Dimension(250, 40));
		bookButton.addActionListener(this::bookVaccineAppointment);
		buttonPanel.add(bookButton);

		mainPanel.add(formPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		add(mainPanel);

		pack();
		if (parent != null) {
			setLocationRelativeTo(parent);
		} else {
			setLocationRelativeTo(null);
		}
		setVisible(true);
	}

	private void addPatientRow(JPanel panel, GridBagConstraints gbc, int row) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.2;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;

		if (labelPatient == null) {
			labelPatient = new JLabel("Patient:");
			labelPatient.setFont(HEADER_FONT);
			labelPatient.setForeground(PRIMARY_COLOR);
		}
		panel.add(labelPatient, gbc);

		JPanel patientPanel = new JPanel(new BorderLayout(5, 0));
		patientPanel.setBackground(PANEL_BACKGROUND);

		if (addPatientButton == null) {
			addPatientButton = new JButton("Select patient");
			styleButton(addPatientButton, PRIMARY_COLOR);
			addPatientButton.setPreferredSize(new Dimension(140, 30));
			addPatientButton.addActionListener(e -> openAddPatientWindow());
		}

		if (txtPatient == null) {
			txtPatient = new JTextField();
			txtPatient.setEditable(false);
			txtPatient.setBackground(Color.WHITE);
			txtPatient.setFont(LABEL_FONT);
			txtPatient.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(210, 210, 210)),
					BorderFactory.createEmptyBorder(6, 8, 6, 8)));
		}

		if (removePatientButton == null) {
			removePatientButton = new JButton("Clear");
			styleButton(removePatientButton, new Color(189, 195, 199));
			removePatientButton.setPreferredSize(new Dimension(80, 30));
			removePatientButton.addActionListener(e -> removePatient());
			removePatientButton.setEnabled(false);
		}

		patientPanel.add(addPatientButton, BorderLayout.WEST);
		patientPanel.add(txtPatient, BorderLayout.CENTER);
		patientPanel.add(removePatientButton, BorderLayout.EAST);

		gbc.gridx = 1;
		gbc.weightx = 0.8;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(patientPanel, gbc);
	}

	private void addVaccineSelectionRow(JPanel panel, GridBagConstraints gbc,
			int row) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.2;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;

		JLabel vaccineLabel = new JLabel("Vaccines:");
		vaccineLabel.setFont(HEADER_FONT);
		vaccineLabel.setForeground(PRIMARY_COLOR);
		panel.add(vaccineLabel, gbc);

		// Create vaccine selection panel
		vaccineSelectionPanel = new JPanel(new BorderLayout(5, 5));
		vaccineSelectionPanel.setBackground(PANEL_BACKGROUND);

		// Create button panel for vaccine actions
		JPanel vaccineButtonPanel = new JPanel(
				new FlowLayout(FlowLayout.LEFT, 5, 0));
		vaccineButtonPanel.setBackground(PANEL_BACKGROUND);

		// Add vaccine button
		addVaccineButton = new JButton("Add Vaccine");
		styleButton(addVaccineButton, SECONDARY_COLOR);
		addVaccineButton.setPreferredSize(new Dimension(140, 30));
		addVaccineButton.addActionListener(e -> openAddVaccineWindow());

		// Remove selected vaccine button
		removeVaccineButton = new JButton("Remove Selected");
		styleButton(removeVaccineButton, new Color(231, 76, 60)); // Red color
		removeVaccineButton.setPreferredSize(new Dimension(140, 30));
		removeVaccineButton.addActionListener(e -> removeSelectedVaccine());
		removeVaccineButton.setEnabled(false);

		// Clear all vaccines button
		clearVaccinesButton = new JButton("Clear All");
		styleButton(clearVaccinesButton, new Color(189, 195, 199)); // Gray
																	// color
		clearVaccinesButton.setPreferredSize(new Dimension(100, 30));
		clearVaccinesButton.addActionListener(e -> clearAllVaccines());
		clearVaccinesButton.setEnabled(false);

		vaccineButtonPanel.add(addVaccineButton);
		vaccineButtonPanel.add(removeVaccineButton);
		vaccineButtonPanel.add(clearVaccinesButton);

		// Create list model and JList for displaying selected vaccines
		vaccineListModel = new DefaultListModel<>();
		vaccineList = new JList<>(vaccineListModel);
		vaccineList.setFont(LIST_FONT);
		vaccineList.setBackground(Color.WHITE);
		vaccineList.setSelectionBackground(SECONDARY_COLOR.brighter());
		vaccineList.setSelectionForeground(Color.BLACK);
		vaccineList.setVisibleRowCount(4);

		// Add selection listener to enable/remove button
		vaccineList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				removeVaccineButton.setEnabled(!vaccineList.isSelectionEmpty());
			}
		});

		// Create scroll pane for the vaccine list
		JScrollPane scrollPane = new JScrollPane(vaccineList);
		scrollPane.setPreferredSize(new Dimension(400, 120));
		scrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(210, 210, 210)),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// Add components to vaccine selection panel
		vaccineSelectionPanel.add(vaccineButtonPanel, BorderLayout.NORTH);
		vaccineSelectionPanel.add(scrollPane, BorderLayout.CENTER);

		gbc.gridx = 1;
		gbc.weightx = 0.8;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(vaccineSelectionPanel, gbc);
	}

	private void addSectionLabel(String text, JPanel panel,
			GridBagConstraints gbc, int row) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;

		JLabel section = new JLabel(text);
		section.setFont(SECTION_FONT);
		section.setForeground(PRIMARY_COLOR.darker());
		section.setBorder(BorderFactory.createEmptyBorder(10, 2, 5, 2));

		panel.add(section, gbc);
		gbc.gridwidth = 1;
	}

	private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
			String label, Component input) {
		// Label column
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.2;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		if (label != null && !label.isEmpty()) {
			JLabel jLabel = new JLabel(label);
			jLabel.setFont(LABEL_FONT);
			jLabel.setForeground(new Color(60, 60, 60));
			panel.add(jLabel, gbc);
		}

		// Input column
		gbc.gridx = 1;
		gbc.weightx = 0.8;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(input, gbc);
	}

	private void styleButton(JButton button, Color color) {
		button.setFont(BUTTON_FONT);
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(color.darker()),
				BorderFactory.createEmptyBorder(8, 15, 8, 15)));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void openAddPatientWindow() {
		if (parent != null) {
			new PatientSearcher(this, model).setVisible(true);
		} else {
			new PatientSearcher(this).setVisible(true);
		}
	}

	private void openAddVaccineWindow() {
		if (parent != null) {
			VaccineSearcher vaccineSearcher = new VaccineSearcher(this, model);
			vaccineSearcher.setVisible(true);
		} else {
			VaccineSearcher vaccineSearcher = new VaccineSearcher(this, model);
			vaccineSearcher.setVisible(true);
		}
	}

	public void setSelectedPatient(String patientName, String patientId,
			PatientDTO dto) {
		this.patientDTO = dto;
		txtPatient.setText(patientName + " (ID: " + patientId + ")");
		removePatientButton.setEnabled(true);

		// Show informational message for fast vaccine mode
		if (parent instanceof MedicalRecordWindow) {
			JOptionPane.showMessageDialog(this, "Patient '" + patientName
					+ "' pre-filled from medical record.\n"
					+ "Please select doctor, vaccines, and schedule details.",
					"Fast Vaccine Mode", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void removePatient() {
		txtPatient.setText("");
		removePatientButton.setEnabled(false);
		patientDTO = null;
	}

	private void removeSelectedVaccine() {
		int selectedIndex = vaccineList.getSelectedIndex();
		if (selectedIndex != -1) {
			vaccineListModel.remove(selectedIndex);
			selectedVaccines.remove(selectedIndex);

			if (vaccineListModel.isEmpty()) {
				removeVaccineButton.setEnabled(false);
				clearVaccinesButton.setEnabled(false);
			}
		}
	}

	private void clearAllVaccines() {
		vaccineListModel.clear();
		selectedVaccines.clear();
		removeVaccineButton.setEnabled(false);
		clearVaccinesButton.setEnabled(false);
	}

	private void bookVaccineAppointment(ActionEvent e) {
		try {
			// Validate patient selection
			if (patientDTO == null || txtPatient.getText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please select a patient.",
						"Patient Required", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Validate doctor selection
			if (selectedDoctor == null) {
				JOptionPane.showMessageDialog(this, "Please select a doctor.",
						"Doctor Required", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Validate vaccine selection
			if (selectedVaccines.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Please select at least one vaccine.",
						"Vaccine Required", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Get selected date and time
			String date = dateFormat.format((Date) dateSpinner.getValue());
			String time = timeFormat.format((Date) timeSpinner.getValue());

			// Convert date to database format (yyyy-MM-dd)
			String dbDate = LocalDate
					.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
					.toString();

			// Validate date (must be today or in the future)
			Date selectedDate = (Date) dateSpinner.getValue();
			Date today = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(today);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			today = cal.getTime();

			cal.setTime(selectedDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			selectedDate = cal.getTime();

			if (selectedDate.before(today)) {
				int choice = JOptionPane.showConfirmDialog(this,
						"The selected date is in the past.\nDo you want to proceed anyway?",
						"Past Date Warning", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);

				if (choice != JOptionPane.YES_OPTION) {
					return;
				}
			}

			// Check if doctor is already booked at this time
			if (model.isDoctorBookedForVaccine(selectedDoctor.getId(), dbDate,
					time)) {
				JOptionPane.showMessageDialog(this,
						"The selected doctor is already booked for a vaccine appointment at this time.",
						"Doctor Unavailable", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Check if patient is already booked at this time
			if (model.isPatientBookedForVaccine(patientDTO.getId(), dbDate,
					time)) {
				JOptionPane.showMessageDialog(this,
						"The patient already has a vaccine appointment scheduled at this time.",
						"Patient Unavailable", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Create summary message
			StringBuilder summary = new StringBuilder();
			summary.append("VACCINE APPOINTMENT SUMMARY\n");
			summary.append("===========================\n\n");
			summary.append("Patient: ").append(txtPatient.getText())
					.append("\n");
			summary.append("Doctor: ").append(txtDoctor.getText()).append("\n");

			summary.append("Selected Vaccines (")
					.append(selectedVaccines.size()).append("):\n");
			for (VaccineDTO vaccine : selectedVaccines) {
				summary.append("  • ").append(vaccine.getName())
						.append(" (Recommended Doses: ")
						.append(vaccine.getRecommendedDoses())
						.append(", Dose Type: ").append(vaccine.getDoseType())
						.append(")\n");
			}

			summary.append("Date: ").append(date).append("\n");
			summary.append("Time: ").append(time).append("\n");

			summary.append("\nDo you confirm this vaccine appointment?");

			int confirm = JOptionPane.showConfirmDialog(this,
					summary.toString(), "Confirm Vaccine Appointment",
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

			if (confirm != JOptionPane.YES_OPTION) {
				JOptionPane.showMessageDialog(this,
						"Vaccine appointment cancelled.", "Cancelled",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			// Save the vaccine appointment to the database
			Integer vaccineAppointmentId = model.saveVaccineAppointment(
					selectedDoctor.getId(), patientDTO.getId(), dbDate, time);

			if (vaccineAppointmentId != null) {
				// Save each vaccine to the appointment
				for (VaccineDTO vaccine : selectedVaccines) {
					model.saveVaccineToAppointment(vaccineAppointmentId,
							vaccine.getName(), vaccine.getDoseType());
				}

				// Show success message
				StringBuilder successMessage = new StringBuilder();
				successMessage.append(
						"Vaccine appointment scheduled successfully!\n\n")
						.append("✓ Patient: ").append(txtPatient.getText())
						.append("\n✓ Doctor: ").append(txtDoctor.getText())
						.append("\n✓ Vaccines: ")
						.append(selectedVaccines.size()).append(" selected\n");

				for (VaccineDTO vaccine : selectedVaccines) {
					successMessage.append("  - ").append(vaccine.getName())
							.append(" (").append(vaccine.getDoseType())
							.append(")\n");
				}

				successMessage.append("✓ Date: ").append(date)
						.append("\n✓ Time: ").append(time)
						.append("\n\nAppointment ID: ")
						.append(vaccineAppointmentId);

				JOptionPane.showMessageDialog(this, successMessage.toString(),
						"Success", JOptionPane.INFORMATION_MESSAGE);

				dispose();
			} else {
				JOptionPane.showMessageDialog(this,
						"Error saving vaccine appointment to database.",
						"Database Error", JOptionPane.ERROR_MESSAGE);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Error scheduling vaccine appointment:\n" + ex.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new NewVaccineAppointmentWindow();
		});
	}

	private void addDoctorRow(JPanel panel, GridBagConstraints gbc, int row) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.2;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;

		if (labelDoctor == null) {
			labelDoctor = new JLabel("Doctor:");
			labelDoctor.setFont(HEADER_FONT);
			labelDoctor.setForeground(PRIMARY_COLOR);
		}
		panel.add(labelDoctor, gbc);

		JPanel doctorPanel = new JPanel(new BorderLayout(5, 0));
		doctorPanel.setBackground(PANEL_BACKGROUND);

		if (addDoctorButton == null) {
			addDoctorButton = new JButton("Select doctor");
			styleButton(addDoctorButton, PRIMARY_COLOR);
			addDoctorButton.setPreferredSize(new Dimension(140, 30));
			addDoctorButton.addActionListener(e -> openAddDoctorWindow());
		}

		if (txtDoctor == null) {
			txtDoctor = new JTextField();
			txtDoctor.setEditable(false);
			txtDoctor.setBackground(Color.WHITE);
			txtDoctor.setFont(LABEL_FONT);
			txtDoctor.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(210, 210, 210)),
					BorderFactory.createEmptyBorder(6, 8, 6, 8)));
		}

		if (removeDoctorButton == null) {
			removeDoctorButton = new JButton("Clear");
			styleButton(removeDoctorButton, new Color(189, 195, 199));
			removeDoctorButton.setPreferredSize(new Dimension(80, 30));
			removeDoctorButton.addActionListener(e -> removeDoctor());
			removeDoctorButton.setEnabled(false);
		}

		doctorPanel.add(addDoctorButton, BorderLayout.WEST);
		doctorPanel.add(txtDoctor, BorderLayout.CENTER);
		doctorPanel.add(removeDoctorButton, BorderLayout.EAST);

		gbc.gridx = 1;
		gbc.weightx = 0.8;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(doctorPanel, gbc);
	}

	private void openAddDoctorWindow() {
		if (parent != null) {
			new DoctorSearcher(this, model).setVisible(true);
		} else {
			new DoctorSearcher(this).setVisible(true);
		}
	}

	public void setSelectedDoctor(DoctorDTO doctor) {
		this.selectedDoctor = doctor;
		if (doctor != null) {
			txtDoctor.setText(
					doctor.getFullName() + " (ID: " + doctor.getId() + ")");
			removeDoctorButton.setEnabled(true);
		}
	}

	private void removeDoctor() {
		txtDoctor.setText("");
		removeDoctorButton.setEnabled(false);
		selectedDoctor = null;
	}

	public void setSelectedVaccine(VaccineDTO vaccine) {
		if (vaccine != null) {
			// Check if vaccine is already in the list
			boolean alreadyExists = selectedVaccines.stream()
					.anyMatch(v -> v.getName().equals(vaccine.getName()));

			if (!alreadyExists) {
				selectedVaccines.add(vaccine);
				vaccineListModel.addElement(vaccine);
				removeVaccineButton.setEnabled(true);
				clearVaccinesButton.setEnabled(true);
			} else {
				JOptionPane.showMessageDialog(this,
						"Vaccine '" + vaccine.getName()
								+ "' is already in the list.",
						"Duplicate Vaccine", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}