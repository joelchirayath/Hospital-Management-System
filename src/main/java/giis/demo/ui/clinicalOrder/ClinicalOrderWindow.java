package giis.demo.ui.clinicalOrder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.doctor.DoctorDTO;

public class ClinicalOrderWindow extends JDialog {
	private static final long serialVersionUID = 1L;

	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Color SUCCESS_COLOR = new Color(39, 174, 96);
	private final Color LOGOUT_COLOR = new Color(192, 57, 43);

	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

	private JComboBox<String> doctorComboBox;
	private JTextField conceptField;
	private JTextArea descriptionArea;
	private JButton saveButton;
	private JButton cancelButton;
	private JButton uploadButton;
	private JLabel fileLabel;

	private AppointmentModel model;
	private DoctorDTO currentDoctor;
	private Integer patientId;
	private Integer appointmentId;

	private File selectedFile;
	private byte[] fileData;

	public ClinicalOrderWindow(JFrame parent, AppointmentModel model,
		DoctorDTO currentDoctor, Integer patientId, Integer appointmentId) {
		super(parent, "Create Clinical Order", true);
		this.model = model;
		this.currentDoctor = currentDoctor;
		this.patientId = patientId;
		this.appointmentId = appointmentId;
		initializeUI();
	}

	private void initializeUI() {
		setSize(600, 700);
		setLocationRelativeTo(getParent());
		setResizable(false);

		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
		contentPane.setBackground(BACKGROUND_COLOR);
		setContentPane(contentPane);

		// Header
		JLabel headerLabel = new JLabel("Create Clinical Order", JLabel.CENTER);
		headerLabel.setFont(TITLE_FONT);
		headerLabel.setForeground(PRIMARY_COLOR);
		headerLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
		contentPane.add(headerLabel, BorderLayout.NORTH);

		// Main content
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(BACKGROUND_COLOR);

		// Patient details
		mainPanel.add(createPatientDetailsPanel());
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Clinical order form
		mainPanel.add(createClinicalOrderPanel());
		mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Upload section
		mainPanel.add(createUploadPanel());

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setBorder(null);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		// Buttons panel
		contentPane.add(createButtonsPanel(), BorderLayout.SOUTH);

		loadDoctors();
	}

	private JPanel createPatientDetailsPanel() {
		JPanel panel = createStyledPanel("Patient details & History", null);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel patientIdLabel = new JLabel("Patient ID: " + patientId);
		patientIdLabel.setFont(LABEL_FONT);

		JLabel appointmentIdLabel = new JLabel(
			"Appointment ID: " + appointmentId);
		appointmentIdLabel.setFont(LABEL_FONT);

		JLabel doctorLabel = new JLabel(
			"Order initiated by: Dr. " + currentDoctor.getFullName());
		doctorLabel.setFont(LABEL_FONT);

		panel.add(patientIdLabel);
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		panel.add(appointmentIdLabel);
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		panel.add(doctorLabel);

		return panel;
	}

	private JPanel createClinicalOrderPanel() {
		JPanel panel = createStyledPanel("New clinical order", null);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// Doctors selection
		JPanel doctorPanel = new JPanel(new BorderLayout(10, 5));
		doctorPanel.setBackground(PANEL_BACKGROUND);
		doctorPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

		JLabel doctorLabel = new JLabel("Doctors:");
		doctorLabel.setFont(SECTION_FONT);
		doctorComboBox = new JComboBox<>();
		doctorComboBox.setFont(LABEL_FONT);

		doctorPanel.add(doctorLabel, BorderLayout.NORTH);
		doctorPanel.add(doctorComboBox, BorderLayout.CENTER);

		// Concept field
		JPanel conceptPanel = new JPanel(new BorderLayout(10, 5));
		conceptPanel.setBackground(PANEL_BACKGROUND);
		conceptPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

		JLabel conceptLabel = new JLabel("Concept:");
		conceptLabel.setFont(SECTION_FONT);
		conceptField = new JTextField();

		conceptPanel.add(conceptLabel, BorderLayout.NORTH);
		conceptPanel.add(conceptField, BorderLayout.CENTER);

		// Description area
		JPanel descriptionPanel = new JPanel(new BorderLayout(10, 5));
		descriptionPanel.setBackground(PANEL_BACKGROUND);

		JLabel descriptionLabel = new JLabel("Description:");
		descriptionLabel.setFont(SECTION_FONT);
		descriptionArea = new JTextArea(8, 30);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

		descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
		descriptionPanel.add(descriptionScroll, BorderLayout.CENTER);

		panel.add(doctorPanel);
		panel.add(conceptPanel);
		panel.add(descriptionPanel);

		return panel;
	}

	private JPanel createUploadPanel() {
		JPanel panel = createStyledPanel("Upload File", null);
		panel.setLayout(new BorderLayout(10, 10));

		uploadButton = createStyledButton("Upload File", PRIMARY_COLOR);
		uploadButton.addActionListener(e -> handleUpload());

		fileLabel = new JLabel("No file selected");
		fileLabel.setFont(LABEL_FONT);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(uploadButton);
		buttonPanel.add(fileLabel);

		panel.add(buttonPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setBackground(BACKGROUND_COLOR);

		saveButton = createStyledButton("Send", SUCCESS_COLOR);
		saveButton.addActionListener(e -> saveClinicalOrder());

		cancelButton = createStyledButton("Cancel", LOGOUT_COLOR);
		cancelButton.addActionListener(e -> dispose());

		panel.add(saveButton);
		panel.add(cancelButton);

		return panel;
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

		if (contentPanel != null) {
			panel.add(contentPanel, BorderLayout.CENTER);
		}
		return panel;
	}

	private JButton createStyledButton(String text, Color color) {
		JButton button = new JButton(text);
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setFont(BUTTON_FONT);
		button.setFocusPainted(false);
		return button;
	}

	private void loadDoctors() {
		List<DoctorDTO> doctors = model.getAllDoctorsForClinicalOrders(
			currentDoctor.getId());
		doctorComboBox.addItem("Select Doctor...");

		for (DoctorDTO doctor : doctors) {
			doctorComboBox.addItem(doctor.getFullName() + " ("
				+ doctor.getSpecialization() + ") - ID: " + doctor.getId());
		}
	}

	private void handleUpload() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select file to upload");

		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			fileLabel.setText(selectedFile.getName());

			try {
				fileData = Files.readAllBytes(selectedFile.toPath());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
					"Error reading file: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
				fileLabel.setText("No file selected");
				selectedFile = null;
				fileData = null;
			}
		}
	}

	private void saveClinicalOrder() {
		if (doctorComboBox.getSelectedIndex() == 0) {
			showError("Please select a doctor");
			return;
		}

		String concept = conceptField.getText().trim();
		if (concept.isEmpty()) {
			showError("Please enter a concept");
			conceptField.requestFocus();
			return;
		}

		String description = descriptionArea.getText().trim();
		if (description.isEmpty()) {
			showError("Please enter order instructions");
			descriptionArea.requestFocus();
			return;
		}

		try {
			String selectedDoctor = (String) doctorComboBox.getSelectedItem();
			int assignedDoctorId = extractDoctorId(selectedDoctor);

			String fileName = selectedFile != null ? selectedFile.getName()
				: null;

			model.saveClinicalOrder(patientId, appointmentId,
				currentDoctor.getId(), assignedDoctorId, concept, description,
				fileName, fileData);

			JOptionPane.showMessageDialog(this,
				"Clinical order created successfully!\nNotification sent to the assigned doctor.",
				"Success", JOptionPane.INFORMATION_MESSAGE);

			dispose();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
				"Error saving clinical order: " + e.getMessage(), "Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Validation Error",
			JOptionPane.WARNING_MESSAGE);
	}

	private int extractDoctorId(String doctorText) {
		try {
			String[] parts = doctorText.split("ID: ");
			if (parts.length > 1) {
				return Integer.parseInt(parts[1].trim());
			}
		} catch (Exception e) {
		}
		return -1;
	}
}