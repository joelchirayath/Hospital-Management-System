package giis.demo.ui.vaccine;

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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.VaccineAppointmentDTO;
import giis.demo.service.appointment.VaccineDTO;
import giis.demo.service.doctor.DoctorDTO;
import giis.demo.service.patient.PatientDTO;
import giis.demo.ui.receptionist.searchers.VaccineSearcher;

public class VaccineAppointmentDetailWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color VACCINE_COLOR = new Color(39, 174, 96);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;

	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font INFO_FONT = new Font("Segoe UI", Font.PLAIN, 12);

	private AppointmentModel model;
	private VaccineAppointmentDTO vaccineAppointment;
	private PatientDTO patient;
	private DoctorDTO currentDoctor; // NEW FIELD

	// Components for Planned Vaccines
	private DefaultListModel<VaccineDTO> plannedVaccinesModel;
	private JList<VaccineDTO> plannedVaccinesList;
	private JButton administerButton;

	// Components for Administered Vaccines
	private DefaultListModel<VaccineDTO> administeredVaccinesModel;
	private JList<VaccineDTO> administeredVaccinesList;

	// Components for Add New Vaccine
	private JButton addNewVaccineButton;

	// MODIFIED CONSTRUCTOR
	public VaccineAppointmentDetailWindow(AppointmentModel model,
			VaccineAppointmentDTO vaccineAppointment, DoctorDTO currentDoctor) {
		this.model = model;
		this.vaccineAppointment = vaccineAppointment;
		this.currentDoctor = currentDoctor; // Store the doctor
		loadPatientDetails();
		initializeUI();
	}

	private void loadPatientDetails() {
		this.patient = model.getPatientById(vaccineAppointment.getPatientId());
	}

	private void initializeUI() {
		setTitle("Vaccine Appointment Details - "
				+ vaccineAppointment.getPatientName());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(200, 200, 800, 700);
		setLocationRelativeTo(null);
		setResizable(true);

		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		contentPane.setBackground(BACKGROUND_COLOR);
		setContentPane(contentPane);

		// Title
		JLabel titleLabel = new JLabel("Vaccine Appointment Details",
				JLabel.CENTER);
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(PRIMARY_COLOR);
		titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
		contentPane.add(titleLabel, BorderLayout.NORTH);

		// Main content panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(BACKGROUND_COLOR);

		// Patient Information Section
		mainPanel.add(createStyledPanel("Patient Information",
				createPatientInfoPanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Appointment Information Section
		mainPanel.add(createStyledPanel("Appointment Information",
				createAppointmentInfoPanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Planned Vaccines Section
		mainPanel.add(createStyledPanel("Planned Vaccines",
				createPlannedVaccinesPanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Add New Vaccine Section
		mainPanel.add(
				createStyledPanel("Add New Vaccine", createAddVaccinePanel()));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Administered Vaccines Section
		mainPanel
				.add(createStyledPanel("Vaccines to be Saved to Medical Record",
						createAdministeredVaccinesPanel()));

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		// Buttons panel
		contentPane.add(createButtonsPanel(), BorderLayout.SOUTH);

		loadVaccinesData();
	}

	private JPanel createStyledPanel(String title, JPanel contentPanel) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(
				BorderFactory
						.createCompoundBorder(
								BorderFactory.createTitledBorder(
										BorderFactory.createLineBorder(
												BORDER_COLOR, 1),
										title,
										TitledBorder.DEFAULT_JUSTIFICATION,
										TitledBorder.DEFAULT_POSITION,
										SECTION_FONT, PRIMARY_COLOR),
								BorderFactory.createEmptyBorder(10, 10, 10,
										10)));

		contentPanel.setBackground(PANEL_BACKGROUND);
		panel.add(contentPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createPatientInfoPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		if (patient != null) {
			addStyledDetailRow(panel, "Name:", patient.getFullName());
			addStyledDetailRow(panel, "DNI:", patient.getDni());
			addStyledDetailRow(panel, "Patient ID:",
					String.valueOf(patient.getId()));
			addStyledDetailRow(panel, "Phone:",
					patient.getPhone() != null ? patient.getPhone() : "N/A");
			addStyledDetailRow(panel, "Email:",
					patient.getEmail() != null ? patient.getEmail() : "N/A");

			String dob = "N/A";
			if (patient.getDate_of_birth() != null) {
				try {
					LocalDate birthDate = LocalDate
							.parse(patient.getDate_of_birth());
					dob = birthDate.format(
							DateTimeFormatter.ofPattern("MMMM d, yyyy"));
				} catch (Exception e) {
					dob = patient.getDate_of_birth();
				}
			}
			addStyledDetailRow(panel, "Date of Birth:", dob);
		} else {
			addStyledDetailRow(panel, "Error:",
					"Patient information not available");
		}

		return panel;
	}

	private JPanel createAppointmentInfoPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		String formattedDate = "N/A";
		try {
			LocalDate date = LocalDate.parse(vaccineAppointment.getDate());
			formattedDate = date
					.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
		} catch (Exception e) {
			formattedDate = vaccineAppointment.getDate();
		}

		addStyledDetailRow(panel, "Date:", formattedDate);
		addStyledDetailRow(panel, "Time:", vaccineAppointment.getHour());
		addStyledDetailRow(panel, "Doctor:",
				"Dr. " + vaccineAppointment.getDoctorName());
		addStyledDetailRow(panel, "Specialization:",
				vaccineAppointment.getDoctorSpecialization());

		return panel;
	}

	private JPanel createPlannedVaccinesPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		plannedVaccinesModel = new DefaultListModel<>();
		plannedVaccinesList = new JList<>(plannedVaccinesModel);
		plannedVaccinesList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		plannedVaccinesList.setBackground(PANEL_BACKGROUND);
		plannedVaccinesList.setFont(INFO_FONT);
		plannedVaccinesList.setCellRenderer(new VaccineListCellRenderer());

		JScrollPane listScroll = new JScrollPane(plannedVaccinesList);
		listScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
		listScroll.setPreferredSize(new Dimension(0, 150));

		administerButton = createStyledButton("Administer Vaccine",
				VACCINE_COLOR);
		administerButton.addActionListener(e -> administerVaccine());
		administerButton.setEnabled(false);

		// Enable administer button when a vaccine is selected
		plannedVaccinesList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				administerButton.setEnabled(
						plannedVaccinesList.getSelectedIndex() != -1);
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBackground(PANEL_BACKGROUND);
		buttonPanel.add(administerButton);

		panel.add(listScroll, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createAddVaccinePanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel infoLabel = new JLabel("Add a new vaccine to this appointment:");
		infoLabel.setFont(LABEL_FONT);
		infoLabel.setForeground(Color.DARK_GRAY);

		addNewVaccineButton = createStyledButton("Add New Vaccine",
				PRIMARY_COLOR);
		addNewVaccineButton.addActionListener(e -> addNewVaccine());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.setBackground(PANEL_BACKGROUND);
		buttonPanel.add(addNewVaccineButton);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(PANEL_BACKGROUND);
		contentPanel.add(infoLabel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(buttonPanel);

		panel.add(contentPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createAdministeredVaccinesPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		administeredVaccinesModel = new DefaultListModel<>();
		administeredVaccinesList = new JList<>(administeredVaccinesModel);
		administeredVaccinesList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		administeredVaccinesList.setBackground(PANEL_BACKGROUND);
		administeredVaccinesList.setFont(INFO_FONT);
		administeredVaccinesList.setCellRenderer(new VaccineListCellRenderer());

		JLabel infoLabel = new JLabel(
				"<html><b>Note:</b> These vaccines will be saved to the patient's medical record when you click 'Save to Medical Record'</html>");
		infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		infoLabel.setForeground(Color.DARK_GRAY);
		infoLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

		JScrollPane listScroll = new JScrollPane(administeredVaccinesList);
		listScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
		listScroll.setPreferredSize(new Dimension(0, 150));

		JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
		contentPanel.setBackground(PANEL_BACKGROUND);
		contentPanel.add(infoLabel, BorderLayout.NORTH);
		contentPanel.add(listScroll, BorderLayout.CENTER);

		panel.add(contentPanel, BorderLayout.CENTER);
		return panel;
	}

	// MODIFIED BUTTONS PANEL WITH SAVE BUTTON
	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(new EmptyBorder(15, 0, 0, 0));

		// Create left panel for Save button
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		leftPanel.setBackground(BACKGROUND_COLOR);

		// Create Save button
		JButton saveButton = createStyledButton("Save to Medical Record",
				new Color(39, 174, 96)); // Green color
		saveButton.addActionListener(e -> saveToMedicalRecord());
		saveButton.setToolTipText(
				"Save all administered vaccines to patient's medical record");

		leftPanel.add(saveButton);

		// Create right panel for Close button
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		rightPanel.setBackground(BACKGROUND_COLOR);

		JButton closeButton = createStyledButton("Close",
				new Color(192, 57, 43));
		closeButton.addActionListener(e -> dispose());

		rightPanel.add(closeButton);

		// Add both panels
		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(rightPanel, BorderLayout.EAST);

		return panel;
	}

	// NEW METHOD TO SAVE TO MEDICAL RECORD
	private void saveToMedicalRecord() {
		// Check if there are administered vaccines
		if (administeredVaccinesModel.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"No vaccines have been administered yet.\n"
							+ "Please administer vaccines first before saving to medical record.",
					"No Vaccines Administered",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// Check if doctor is available
		if (currentDoctor == null) {
			JOptionPane.showMessageDialog(this,
					"Doctor information is not available.\n"
							+ "Cannot save to medical record.",
					"Doctor Information Missing", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Confirm with user
		int response = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to save "
						+ administeredVaccinesModel.size()
						+ " administered vaccine(s) to the patient's medical record?\n\n"
						+ "This will:\n"
						+ "1. Save all vaccines to the medical record database\n"
						+ "2. Mark the appointment as completed\n"
						+ "3. Clear the current administered vaccines list\n\n"
						+ "This action cannot be undone.",
				"Confirm Save to Medical Record", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (response == JOptionPane.YES_OPTION) {
			try {
				// Get all administered vaccines
				List<VaccineDTO> vaccinesToSave = new ArrayList<>();
				for (int i = 0; i < administeredVaccinesModel.size(); i++) {
					vaccinesToSave
							.add(administeredVaccinesModel.getElementAt(i));
				}

				// Save to medical record
				boolean success = model.saveVaccinesToMedicalRecord(
						vaccineAppointment.getId(),
						vaccineAppointment.getPatientId(), vaccinesToSave,
						currentDoctor.getId(), currentDoctor.getFullName());

				if (success) {
					// Clear administered list after saving
					administeredVaccinesModel.clear();

					JOptionPane.showMessageDialog(this, "Successfully saved "
							+ vaccinesToSave.size()
							+ " vaccine(s) to the patient's medical record!\n\n"
							+ "Appointment marked as completed.", "Success",
							JOptionPane.INFORMATION_MESSAGE);

					// Close window after successful save
					dispose();
				} else {
					JOptionPane.showMessageDialog(this,
							"Failed to save vaccines to medical record.\n"
									+ "Please try again or contact system administrator.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}

			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						"Error saving to medical record: " + e.getMessage()
								+ "\n\nPlease try again or contact system administrator.",
						"Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	// MODIFIED LOAD VACCINES METHOD
	private void loadVaccinesData() {
		try {
			// Load planned vaccines for this appointment
			List<VaccineDTO> plannedVaccines = model
					.getPlannedVaccinesForAppointment(
							vaccineAppointment.getId());

			for (VaccineDTO vaccine : plannedVaccines) {
				plannedVaccinesModel.addElement(vaccine);
			}

			// Don't load administered vaccines from database
			// We want the doctor to administer fresh each time

			System.out.println("Loaded " + plannedVaccinesModel.size()
					+ " planned vaccines.");

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"Error loading vaccines: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// MODIFIED ADD NEW VACCINE METHOD
	private void addNewVaccine() {
		// Open the VaccineSearcher dialog
		VaccineSearcher vaccineSearcher = new VaccineSearcher(null, model);
		vaccineSearcher.setVisible(true);

		VaccineDTO selectedVaccine = vaccineSearcher.getSelectedVaccine();
		if (selectedVaccine != null) {
			try {
				// Add to planned vaccines list only (not to database yet)
				plannedVaccinesModel.addElement(selectedVaccine);

				JOptionPane.showMessageDialog(this, "Vaccine '"
						+ selectedVaccine.getName()
						+ "' added to appointment!\n"
						+ "Remember to administer and save to medical record.",
						"Vaccine Added", JOptionPane.INFORMATION_MESSAGE);

			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						"Error adding vaccine: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// MODIFIED ADMINISTER VACCINE METHOD
	private void administerVaccine() {
		int selectedIndex = plannedVaccinesList.getSelectedIndex();
		if (selectedIndex != -1) {
			VaccineDTO vaccine = plannedVaccinesModel
					.getElementAt(selectedIndex);

			int response = JOptionPane.showConfirmDialog(this,
					"Are you sure you want to administer '" + vaccine.getName()
							+ "' (" + vaccine.getDoseType() + ")?\n\n"
							+ "This will move it to the 'Vaccines to be Saved' list.\n"
							+ "Remember to click 'Save to Medical Record' when done with all vaccines.",
					"Confirm Administration", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (response == JOptionPane.YES_OPTION) {
				try {
					// Move from planned to administered list
					plannedVaccinesModel.removeElementAt(selectedIndex);

					// Create a new VaccineDTO with the current date for
					// administered date
					VaccineDTO administeredVaccine = new VaccineDTO();
					administeredVaccine.setName(vaccine.getName());
					administeredVaccine.setDoseType(vaccine.getDoseType());
					administeredVaccine
							.setRecommendedDoses(vaccine.getRecommendedDoses());
					// Set administered date to current date
					administeredVaccine
							.setAdministeredDate(LocalDate.now().toString());

					administeredVaccinesModel.addElement(administeredVaccine);

					JOptionPane.showMessageDialog(this, "Vaccine '"
							+ vaccine.getName() + "' marked as administered.\n"
							+ "Don't forget to save to medical record when finished with all vaccines!",
							"Vaccine Administered",
							JOptionPane.INFORMATION_MESSAGE);

				} catch (Exception e) {
					JOptionPane.showMessageDialog(this,
							"Error administering vaccine: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private JLabel createStyledLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(LABEL_FONT);
		label.setForeground(Color.DARK_GRAY);
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

	private void addStyledDetailRow(JPanel panel, String label, String value) {
		JLabel keyLabel = createStyledLabel(label);
		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(LABEL_FONT);
		valueLabel.setForeground(Color.BLACK);

		panel.add(keyLabel);
		panel.add(valueLabel);
	}

	// Custom cell renderer for vaccine list
	private class VaccineListCellRenderer
			extends javax.swing.DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if (value instanceof VaccineDTO) {
				VaccineDTO vaccine = (VaccineDTO) value;
				StringBuilder text = new StringBuilder("<html>");
				text.append("<b>").append(vaccine.getName()).append("</b>");

				if (vaccine.getDoseType() != null
						&& !vaccine.getDoseType().isEmpty()) {
					text.append(" - ").append(vaccine.getDoseType());
				}

				if (vaccine.getRecommendedDoses() > 0) {
					text.append(" (").append(vaccine.getRecommendedDoses())
							.append(" doses recommended)");
				}

				// Show administered date if available
				if (vaccine.getAdministeredDate() != null
						&& !vaccine.getAdministeredDate().isEmpty()) {
					text.append(
							"<br><font size='-1' color='gray'>Administered: ")
							.append(vaccine.getAdministeredDate())
							.append("</font>");
				}

				text.append("</html>");
				setText(text.toString());
			}

			return this;
		}
	}
}