package giis.demo.ui.receptionist.searchers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.patient.PatientDTO;
import giis.demo.ui.doctor.DoctorMenu;
import giis.demo.ui.receptionist.NewVaccineAppointmentWindow;

public class PatientSearcher extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField searchField;
	private JButton btnClearSearch;
	private JButton btnCancel;
	private JButton btnSelect;
	private JList<PatientDTO> patientList;
	private DefaultListModel<PatientDTO> listModel;
	private NewVaccineAppointmentWindow vaccineWindow;
	private DoctorMenu doctorMenu; // NEW FIELD

	private final AppointmentModel model;

	// Color constants matching the style from NewApptWindow
	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color SECONDARY_COLOR = new Color(46, 204, 113);
	private final Color WARNING_COLOR = new Color(231, 76, 60);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font LIST_FONT = new Font("Segoe UI", Font.PLAIN, 13);

	// Timer para evitar búsquedas demasiado frecuentes
	private javax.swing.Timer searchTimer;
	private static final int SEARCH_DELAY = 300; // milisegundos

	// NEW CONSTRUCTOR: For DoctorMenu
	public PatientSearcher(DoctorMenu doctorMenu, AppointmentModel model) {
		this.doctorMenu = doctorMenu;
		this.vaccineWindow = null; // No vaccine window
		this.model = model;
		initializeUI();
		setupSearchTimer();
		updateSelectButtonText();
	}

	// Existing constructors
	public PatientSearcher(NewVaccineAppointmentWindow vaccineWindow) {
		this.vaccineWindow = vaccineWindow;
		this.doctorMenu = null; // No doctor menu
		this.model = new AppointmentModel();
		initializeUI();
		setupSearchTimer();
		updateSelectButtonText();
	}

	public PatientSearcher(NewVaccineAppointmentWindow vaccineWindow,
			AppointmentModel model) {
		this.vaccineWindow = vaccineWindow;
		this.doctorMenu = null; // No doctor menu
		this.model = model;
		initializeUI();
		setupSearchTimer();
		updateSelectButtonText();
	}

	private void initializeUI() {
		setTitle("Search Patients");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(700, 600));
		setPreferredSize(new Dimension(800, 700));

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
		contentPane.setBackground(BACKGROUND_COLOR);
		contentPane.setLayout(new BorderLayout(0, 15));
		setContentPane(contentPane);

		// Header Panel
		contentPane.add(createHeaderPanel(), BorderLayout.NORTH);

		// Search Panel
		contentPane.add(createSearchPanel(), BorderLayout.CENTER);

		// Button Panel
		contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);
	}

	private void setupSearchTimer() {
		searchTimer = new javax.swing.Timer(SEARCH_DELAY, e -> {
			performSearch();
		});
		searchTimer.setRepeats(false); // Solo se ejecuta una vez después del
										// delay
	}

	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(PANEL_BACKGROUND);
		headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

		JLabel lblTitle = new JLabel("Search and Select Patients");
		lblTitle.setFont(TITLE_FONT);
		lblTitle.setForeground(PRIMARY_COLOR);

		// Update title based on context
		if (doctorMenu != null) {
			lblTitle.setText("Search Patient for Medical Record");
		} else if (vaccineWindow != null) {
			lblTitle.setText("Search Patient for Vaccine Appointment");
		}

		headerPanel.add(lblTitle, BorderLayout.WEST);

		return headerPanel;
	}

	private JPanel createSearchPanel() {
		JPanel searchPanel = new JPanel(new BorderLayout(0, 15));
		searchPanel.setBackground(PANEL_BACKGROUND);
		searchPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(20, 20, 20, 20)));

		// Search input area
		JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
		searchInputPanel.setBackground(PANEL_BACKGROUND);

		JLabel lblSearch = new JLabel("Search by name, surname, or DNI:");
		lblSearch.setFont(HEADER_FONT);
		lblSearch.setForeground(PRIMARY_COLOR);
		searchInputPanel.add(lblSearch, BorderLayout.NORTH);

		JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
		inputPanel.setBackground(PANEL_BACKGROUND);

		searchField = new JTextField();
		searchField.setFont(LABEL_FONT);
		searchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));

		// DocumentListener para detectar cambios en tiempo real
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				scheduleSearch();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				scheduleSearch();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				scheduleSearch();
			}
		});

		inputPanel.add(searchField, BorderLayout.CENTER);

		btnClearSearch = createStyledButton("Clear", new Color(149, 165, 166));
		btnClearSearch.setPreferredSize(new Dimension(100, 40));
		btnClearSearch.addActionListener(e -> clearSearch());
		inputPanel.add(btnClearSearch, BorderLayout.EAST);

		searchInputPanel.add(inputPanel, BorderLayout.CENTER);

		// Update instructions based on context
		String instructions = "Type to search (searches with each keystroke)";
		if (doctorMenu != null) {
			instructions = "Search for a patient to view their medical record";
		}

		JLabel lblInstructions = new JLabel(instructions);
		lblInstructions.setFont(LABEL_FONT);
		lblInstructions.setForeground(Color.DARK_GRAY);
		lblInstructions.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
		searchInputPanel.add(lblInstructions, BorderLayout.SOUTH);

		searchPanel.add(searchInputPanel, BorderLayout.NORTH);

		// Results List
		JPanel resultsPanel = new JPanel(new BorderLayout());
		resultsPanel.setBackground(PANEL_BACKGROUND);

		JLabel lblResults = new JLabel("Search Results:");
		lblResults.setFont(HEADER_FONT);
		lblResults.setForeground(PRIMARY_COLOR);
		lblResults.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		resultsPanel.add(lblResults, BorderLayout.NORTH);

		// Initialize list model and JList
		listModel = new DefaultListModel<>();
		patientList = new JList<>(listModel);
		patientList.setFont(LIST_FONT);
		patientList.setBackground(Color.WHITE);
		patientList.setSelectionMode(
				javax.swing.ListSelectionModel.SINGLE_SELECTION);
		patientList.setCellRenderer(new PatientListCellRenderer());

		JScrollPane scrollPane = new JScrollPane(patientList);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		scrollPane.setPreferredSize(new Dimension(700, 350));

		resultsPanel.add(scrollPane, BorderLayout.CENTER);

		// Selection info label
		JLabel lblSelectionInfo = new JLabel("No patient selected");
		lblSelectionInfo.setFont(LABEL_FONT);
		lblSelectionInfo.setForeground(PRIMARY_COLOR);
		lblSelectionInfo
				.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		resultsPanel.add(lblSelectionInfo, BorderLayout.SOUTH);

		// Add listener to update selection count
		patientList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				PatientDTO selected = patientList.getSelectedValue();
				if (selected != null) {
					lblSelectionInfo
							.setText("Selected: " + selected.getFullName());
					btnSelect.setEnabled(true);
				} else {
					lblSelectionInfo.setText("No patient selected");
					btnSelect.setEnabled(false);
				}
			}
		});

		searchPanel.add(resultsPanel, BorderLayout.CENTER);

		return searchPanel;
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel(
				new FlowLayout(FlowLayout.RIGHT, 15, 0));
		buttonPanel.setBackground(PANEL_BACKGROUND);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

		btnCancel = createStyledButton("Cancel", WARNING_COLOR);
		btnCancel.addActionListener(e -> dispose());

		btnSelect = createStyledButton("Select Patient", SECONDARY_COLOR);
		btnSelect.setEnabled(false);
		btnSelect.addActionListener(e -> selectPatient());

		buttonPanel.add(btnCancel);
		buttonPanel.add(btnSelect);

		return buttonPanel;
	}

	private JButton createStyledButton(String text, Color color) {
		JButton button = new JButton(text);
		button.setFont(BUTTON_FONT);
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(color.darker()),
				BorderFactory.createEmptyBorder(10, 25, 10, 25)));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		button.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				button.setBackground(color.brighter());
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent evt) {
				button.setBackground(color);
			}
		});

		return button;
	}

	// NEW METHOD: Update select button text based on context
	private void updateSelectButtonText() {
		if (btnSelect != null) {
			if (doctorMenu != null) {
				btnSelect.setText("Open Medical Record");
			} else {
				btnSelect.setText("Select Patient");
			}
		}
	}

	private void scheduleSearch() {
		// Reiniciar el timer cada vez que se escribe algo
		if (searchTimer.isRunning()) {
			searchTimer.restart();
		} else {
			searchTimer.start();
		}
	}

	private void performSearch() {
		String searchText = searchField.getText().trim();

		// Si el campo está vacío, limpiar la lista
		if (searchText.isEmpty()) {
			SwingUtilities.invokeLater(() -> {
				listModel.clear();
				btnSelect.setEnabled(false);
			});
			return;
		}

		// Ejecutar la búsqueda en un hilo separado para no bloquear la UI
		new Thread(() -> {
			try {
				// Usar el método getPossiblePatients del AppointmentModel
				Optional<List<PatientDTO>> possiblePatients = model
						.getPossiblePatients(searchText);

				SwingUtilities.invokeLater(() -> {
					listModel.clear();

					if (possiblePatients.isPresent()
							&& !possiblePatients.get().isEmpty()) {
						for (PatientDTO patient : possiblePatients.get()) {
							listModel.addElement(patient);
						}

						System.out.println("Found "
								+ possiblePatients.get().size()
								+ " patients for search: '" + searchText + "'");

						// Seleccionar el primer resultado automáticamente si
						// hay resultados
						if (listModel.size() > 0) {
							patientList.setSelectedIndex(0);
						}
					} else {
						System.out.println("No patients found for search: '"
								+ searchText + "'");
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(PatientSearcher.this,
							"Error searching patients: " + e.getMessage(),
							"Search Error", JOptionPane.ERROR_MESSAGE);
				});
			}
		}).start();
	}

	private void clearSearch() {
		searchField.setText("");
		listModel.clear();
		btnSelect.setEnabled(false);
		System.out.println("Search cleared");
	}

	private void selectPatient() {
		PatientDTO selectedPatient = patientList.getSelectedValue();

		if (selectedPatient == null) {
			JOptionPane.showMessageDialog(this,
					"Please select a patient from the list.",
					"No Patient Selected", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// NEW LOGIC: Check context and handle accordingly
		if (doctorMenu != null) {
			// Handle DoctorMenu context - open medical record
			handleDoctorMenuSelection(selectedPatient);
		} else if (vaccineWindow != null) {
			// Handle VaccineWindow context - original logic
			handleVaccineWindowSelection(selectedPatient);
		} else {
			// No context - just show selection
			JOptionPane.showMessageDialog(this,
					"Patient '" + selectedPatient.getFullName() + "' selected.",
					"Patient Selected", JOptionPane.INFORMATION_MESSAGE);
			dispose();
		}
	}

	private void handleDoctorMenuSelection(PatientDTO selectedPatient) {

		if (doctorMenu != null) {
			doctorMenu.openMedicalRecordForPatient(selectedPatient);
		} else {
			JOptionPane.showMessageDialog(this,
					"Would open medical record for:\n" + "Patient: "
							+ selectedPatient.getFullName() + "\n" + "DNI: "
							+ selectedPatient.getDni() + "\n" + "ID: "
							+ selectedPatient.getId(),
					"Medical Record Access", JOptionPane.INFORMATION_MESSAGE);
		}

		dispose();
	}

	// NEW METHOD: Handle selection for VaccineWindow (original logic)
	private void handleVaccineWindowSelection(PatientDTO selectedPatient) {
		// Set the selected patient in the vaccine window
		vaccineWindow.setSelectedPatient(selectedPatient.getFullName(),
				String.valueOf(selectedPatient.getId()), selectedPatient);

		JOptionPane.showMessageDialog(this,
				"Patient '" + selectedPatient.getFullName()
						+ "' selected successfully!",
				"Patient Selected", JOptionPane.INFORMATION_MESSAGE);

		dispose();
	}

	// Custom cell renderer for the patient list
	private class PatientListCellRenderer extends JLabel
			implements ListCellRenderer<PatientDTO> {
		public PatientListCellRenderer() {
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends PatientDTO> list, PatientDTO patient, int index,
				boolean isSelected, boolean cellHasFocus) {

			if (patient != null) {
				String displayText = String.format("%s %s (DNI: %s, ID: %d)",
						patient.getName() != null ? patient.getName() : "",
						patient.getSurname() != null ? patient.getSurname()
								: "",
						patient.getDni() != null ? patient.getDni() : "N/A",
						patient.getId());

				setText(displayText);

				// Alternate row colors for better readability
				if (index % 2 == 0) {
					setBackground(isSelected ? PRIMARY_COLOR
							: new Color(240, 248, 255));
				} else {
					setBackground(isSelected ? PRIMARY_COLOR : Color.WHITE);
				}

				setForeground(isSelected ? Color.WHITE : Color.BLACK);

				// Tooltip with full information
				StringBuilder tooltip = new StringBuilder("<html><b>");
				tooltip.append(
						patient.getName() != null ? patient.getName() : "");
				tooltip.append(" ");
				tooltip.append(
						patient.getSurname() != null ? patient.getSurname()
								: "");
				tooltip.append("</b><br>");
				tooltip.append("DNI: ").append(
						patient.getDni() != null ? patient.getDni() : "N/A")
						.append("<br>");
				tooltip.append("ID: ").append(patient.getId()).append("<br>");

				if (patient.getPhone() != null
						&& !patient.getPhone().isEmpty()) {
					tooltip.append("Phone: ").append(patient.getPhone())
							.append("<br>");
				}

				if (patient.getEmail() != null
						&& !patient.getEmail().isEmpty()) {
					tooltip.append("Email: ").append(patient.getEmail())
							.append("<br>");
				}

				if (patient.getGender() != null
						&& !patient.getGender().isEmpty()) {
					tooltip.append("Gender: ").append(patient.getGender())
							.append("<br>");
				}

				if (patient.getDate_of_birth() != null
						&& !patient.getDate_of_birth().isEmpty()) {
					tooltip.append("Birth Date: ")
							.append(patient.getDate_of_birth());
				}

				tooltip.append("</html>");
				setToolTipText(tooltip.toString());
			} else {
				setText("No patient data");
				setBackground(isSelected ? PRIMARY_COLOR : Color.WHITE);
				setForeground(isSelected ? Color.WHITE : Color.BLACK);
			}

			return this;
		}
	}

	@Override
	public void dispose() {
		// Detener el timer cuando se cierre la ventana
		if (searchTimer != null && searchTimer.isRunning()) {
			searchTimer.stop();
		}
		super.dispose();
	}

	public static void main(String[] args) {
		// Test the PatientSearcher window
		SwingUtilities.invokeLater(() -> {
			PatientSearcher searcher = new PatientSearcher(null);
			searcher.setVisible(true);
		});
	}
}