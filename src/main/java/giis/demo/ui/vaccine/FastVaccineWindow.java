package giis.demo.ui.vaccine;

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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.VaccineDTO;
import giis.demo.service.patient.PatientDTO;
import giis.demo.ui.medicalRecord.MedicalRecordWindow;

public class FastVaccineWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	// Colors and fonts matching MedicalRecordWindow style
	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color SECONDARY_COLOR = new Color(46, 204, 113);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font LIST_FONT = new Font("Segoe UI", Font.PLAIN, 12);
	private final Font INFO_FONT = new Font("Segoe UI", Font.PLAIN, 11);

	private AppointmentModel model;
	private PatientDTO patient;
	private MedicalRecordWindow parentWindow;

	// Search components
	private JTextField searchField;
	private JList<VaccineDTO> searchResultsList;
	private DefaultListModel<VaccineDTO> searchResultsModel;
	private JButton addButton;

	// Selected vaccines components
	private JList<VaccineDTO> selectedVaccinesList;
	private DefaultListModel<VaccineDTO> selectedVaccinesModel;
	private JButton acceptButton;
	private JButton removeButton;
	private JButton clearAllButton;

	private List<VaccineDTO> allVaccines;
	private Timer searchTimer;

	public FastVaccineWindow(MedicalRecordWindow parentWindow,
			AppointmentModel model, PatientDTO patient) {
		this.parentWindow = parentWindow;
		this.model = model;
		this.patient = patient;

		initializeWindow();
		initializeComponents();
		loadAllVaccines();
		setupSearchTimer();
	}

	private void initializeWindow() {
		setTitle("Fast Vaccine Selection - " + patient.getFullName());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(300, 150, 900, 1000);
		setLocationRelativeTo(parentWindow);

		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		contentPane.setBackground(BACKGROUND_COLOR);
		setContentPane(contentPane);

		// Title
		JLabel titleLabel = new JLabel("Fast Vaccine Selection", JLabel.CENTER);
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(PRIMARY_COLOR);
		titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
		contentPane.add(titleLabel, BorderLayout.NORTH);

		// Main content panel
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBackground(PANEL_BACKGROUND);
		mainPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(15, 15, 15, 15)));
		contentPane.add(mainPanel, BorderLayout.CENTER);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;

		int row = 0;

		// Patient info panel
		gbc.gridx = 0;
		gbc.gridy = row++;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(createPatientInfoPanel(), gbc);
		gbc.gridwidth = 1;

		// Search panel
		gbc.gridx = 0;
		gbc.gridy = row++;
		gbc.gridwidth = 2;
		mainPanel.add(createSearchPanel(), gbc);
		gbc.gridwidth = 1;

		// Search results panel
		gbc.gridx = 0;
		gbc.gridy = row++;
		gbc.gridwidth = 2;
		mainPanel.add(createSearchResultsPanel(), gbc);
		gbc.gridwidth = 1;

		// Selected vaccines panel
		gbc.gridx = 0;
		gbc.gridy = row++;
		gbc.gridwidth = 2;
		mainPanel.add(createSelectedVaccinesPanel(), gbc);

		// Buttons panel
		JPanel buttonPanel = new JPanel(
				new FlowLayout(FlowLayout.RIGHT, 15, 0));
		buttonPanel.setBackground(BACKGROUND_COLOR);
		buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

		JButton cancelButton = createStyledButton("Cancel",
				new Color(192, 57, 43));
		cancelButton.addActionListener(e -> dispose());

		acceptButton = createStyledButton("Accept", SECONDARY_COLOR);
		acceptButton.addActionListener(e -> acceptAndClose());
		acceptButton.setEnabled(false); // Disabled until at least one vaccine
										// is selected

		buttonPanel.add(cancelButton);
		buttonPanel.add(acceptButton);

		contentPane.add(buttonPanel, BorderLayout.SOUTH);
	}

	private JPanel createPatientInfoPanel() {
		JPanel panel = new JPanel(new BorderLayout());
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
										HEADER_FONT, PRIMARY_COLOR),
								BorderFactory.createEmptyBorder(10, 10, 10,
										10)));

		JLabel infoLabel = new JLabel("<html><b>" + patient.getFullName()
				+ "</b><br>" + "DNI: " + patient.getDni() + "<br>" + "ID: "
				+ patient.getId() + "</html>");
		infoLabel.setFont(LABEL_FONT);
		infoLabel.setForeground(Color.DARK_GRAY);

		panel.add(infoLabel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createSearchPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 5));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		// Search label
		JLabel searchLabel = new JLabel("Search Vaccines:");
		searchLabel.setFont(HEADER_FONT);
		searchLabel.setForeground(PRIMARY_COLOR);
		panel.add(searchLabel, BorderLayout.NORTH);

		// Search field with button panel
		JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
		searchInputPanel.setBackground(PANEL_BACKGROUND);

		// Search field
		searchField = new JTextField();
		searchField.setFont(LABEL_FONT);
		searchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR),
				BorderFactory.createEmptyBorder(8, 12, 8, 12)));

		// Add key listener for instant search
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				scheduleSearch();
			}
		});

		// Add document listener for text changes
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				scheduleSearch();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				scheduleSearch();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				scheduleSearch();
			}
		});

		searchInputPanel.add(searchField, BorderLayout.CENTER);

		// Add button
		addButton = createStyledButton("Add Selected", PRIMARY_COLOR);
		addButton.setPreferredSize(new Dimension(120, 40));
		addButton.addActionListener(e -> addSelectedVaccine());
		addButton.setEnabled(false);

		searchInputPanel.add(addButton, BorderLayout.EAST);

		panel.add(searchInputPanel, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createSearchResultsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createLineBorder(BORDER_COLOR, 1),
						"Search Results", TitledBorder.DEFAULT_JUSTIFICATION,
						TitledBorder.DEFAULT_POSITION, HEADER_FONT,
						PRIMARY_COLOR),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		// Initialize list model
		searchResultsModel = new DefaultListModel<>();
		searchResultsList = new JList<>(searchResultsModel);
		searchResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchResultsList.setFont(LIST_FONT);
		searchResultsList.setBackground(Color.WHITE);
		searchResultsList.setCellRenderer(new VaccineListCellRenderer());

		// Add selection listener to enable/disable add button
		searchResultsList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				addButton.setEnabled(!searchResultsList.isSelectionEmpty());
			}
		});

		JScrollPane scrollPane = new JScrollPane(searchResultsList);
		scrollPane.setPreferredSize(new Dimension(700, 180));
		scrollPane.getViewport().setBackground(Color.WHITE);

		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createSelectedVaccinesPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createLineBorder(BORDER_COLOR, 1),
						"Selected Vaccines", TitledBorder.DEFAULT_JUSTIFICATION,
						TitledBorder.DEFAULT_POSITION, HEADER_FONT,
						PRIMARY_COLOR),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		// Initialize selected vaccines list model
		selectedVaccinesModel = new DefaultListModel<>();
		selectedVaccinesList = new JList<>(selectedVaccinesModel);
		selectedVaccinesList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectedVaccinesList.setFont(LIST_FONT);
		selectedVaccinesList.setBackground(Color.WHITE);
		selectedVaccinesList.setCellRenderer(new VaccineListCellRenderer());

		// Add selection listener to enable/disable remove button
		selectedVaccinesList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				removeButton
						.setEnabled(!selectedVaccinesList.isSelectionEmpty());
			}
		});

		JScrollPane scrollPane = new JScrollPane(selectedVaccinesList);
		scrollPane.setPreferredSize(new Dimension(700, 150));
		scrollPane.getViewport().setBackground(Color.WHITE);

		// Button panel for selected vaccines actions
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		buttonPanel.setBackground(PANEL_BACKGROUND);

		removeButton = createStyledButton("Remove Selected",
				new Color(231, 76, 60));
		removeButton.setPreferredSize(new Dimension(140, 35));
		removeButton.addActionListener(e -> removeSelectedVaccine());
		removeButton.setEnabled(false);

		clearAllButton = createStyledButton("Clear All",
				new Color(189, 195, 199));
		clearAllButton.setPreferredSize(new Dimension(100, 35));
		clearAllButton.addActionListener(e -> clearAllVaccines());
		clearAllButton.setEnabled(false);

		buttonPanel.add(removeButton);
		buttonPanel.add(clearAllButton);

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
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
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

	private void initializeComponents() {
		allVaccines = new ArrayList<>();
	}

	private void loadAllVaccines() {
		try {
			// Load all vaccines from the model
			Optional<List<VaccineDTO>> vaccines = Optional
					.of(model.getAllVaccines());
			if (vaccines.isPresent()) {
				allVaccines = vaccines.get();
				System.out
						.println("Loaded " + allVaccines.size() + " vaccines");

				// Populate initial search results with all vaccines
				SwingUtilities.invokeLater(() -> {
					searchResultsModel.clear();
					for (VaccineDTO vaccine : allVaccines) {
						searchResultsModel.addElement(vaccine);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Error loading vaccines: " + e.getMessage(), "Load Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setupSearchTimer() {
		searchTimer = new Timer(300, e -> performSearch());
		searchTimer.setRepeats(false);
	}

	private void scheduleSearch() {
		if (searchTimer.isRunning()) {
			searchTimer.restart();
		} else {
			searchTimer.start();
		}
	}

	private void performSearch() {
		String searchText = searchField.getText().trim().toLowerCase();

		SwingUtilities.invokeLater(() -> {
			searchResultsModel.clear();

			if (searchText.isEmpty()) {
				// Show all vaccines when search is empty
				for (VaccineDTO vaccine : allVaccines) {
					searchResultsModel.addElement(vaccine);
				}
			} else {
				// Filter vaccines by search text
				for (VaccineDTO vaccine : allVaccines) {
					if (vaccine.getName().toLowerCase().contains(searchText)) {
						searchResultsModel.addElement(vaccine);
					}
				}
			}

			// Disable add button if no results
			addButton.setEnabled(false);
		});
	}

	private void addSelectedVaccine() {
		VaccineDTO selectedVaccine = searchResultsList.getSelectedValue();

		if (selectedVaccine == null) {
			JOptionPane.showMessageDialog(this,
					"Please select a vaccine from the search results.",
					"No Vaccine Selected", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Check if vaccine is already in the selected list
		boolean alreadySelected = false;
		for (int i = 0; i < selectedVaccinesModel.size(); i++) {
			VaccineDTO existingVaccine = selectedVaccinesModel.getElementAt(i);
			if (existingVaccine.getName().equals(selectedVaccine.getName())) {
				alreadySelected = true;
				break;
			}
		}

		if (alreadySelected) {
			JOptionPane.showMessageDialog(this,
					"Vaccine '" + selectedVaccine.getName()
							+ "' is already selected.",
					"Duplicate Vaccine", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Add to selected list
		selectedVaccinesModel.addElement(selectedVaccine);

		// Update buttons state
		acceptButton.setEnabled(true);
		clearAllButton.setEnabled(true);

		// Show success message
		System.out.println("Added vaccine: " + selectedVaccine.getName());
	}

	private void removeSelectedVaccine() {
		int selectedIndex = selectedVaccinesList.getSelectedIndex();

		if (selectedIndex != -1) {
			VaccineDTO removedVaccine = selectedVaccinesModel
					.getElementAt(selectedIndex);
			selectedVaccinesModel.remove(selectedIndex);

			System.out.println("Removed vaccine: " + removedVaccine.getName());

			// Update buttons state
			if (selectedVaccinesModel.isEmpty()) {
				acceptButton.setEnabled(false);
				clearAllButton.setEnabled(false);
				removeButton.setEnabled(false);
			}
		}
	}

	private void clearAllVaccines() {
		selectedVaccinesModel.clear();

		// Update buttons state
		acceptButton.setEnabled(false);
		clearAllButton.setEnabled(false);
		removeButton.setEnabled(false);

		System.out.println("Cleared all selected vaccines");
	}

	private void acceptAndClose() {
		if (selectedVaccinesModel.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"Please select at least one vaccine.",
					"No Vaccines Selected", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Collect selected vaccines
		List<VaccineDTO> selectedVaccines = new ArrayList<>();
		for (int i = 0; i < selectedVaccinesModel.size(); i++) {
			selectedVaccines.add(selectedVaccinesModel.getElementAt(i));
		}

		// Usar valores por defecto para doctor
		int doctorId = 1;
		String doctorName = "Vaccination Doctor";
		String currentDate = java.time.LocalDate.now().toString();

		// Si puedes obtener del contexto, hazlo aquí
		// try { doctorId = parentWindow.getCurrentDoctorId(); } catch { ... }
		// try { doctorName = parentWindow.getCurrentDoctorName(); } catch { ...
		// }

		// Show confirmation
		StringBuilder vaccineList = new StringBuilder();
		for (VaccineDTO v : selectedVaccines) {
			vaccineList.append("- ").append(v.getName()).append(" (")
					.append(v.getDoseType()).append(")\n");
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"<html><b>Confirm vaccine administration:</b><br><br>"
						+ "<b>Patient:</b> " + patient.getFullName() + "<br>"
						+ "<b>Doctor:</b> " + doctorName + "<br>"
						+ "<b>Date:</b> " + currentDate + "<br><br>"
						+ "<b>Vaccines to administer:</b><br>"
						+ vaccineList.toString() + "</html>",
				"Confirm Administration", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			try {
				// Guardar cada vacuna individualmente con status "administered"
				int successCount = 0;

				for (VaccineDTO vaccine : selectedVaccines) {
					try {
						// Determinar número de dosis
						int doseNumber = extractDoseNumber(
								vaccine.getDoseType());

						// Determinar si necesita booster
						int needsBooster = (vaccine
								.getRecommendedDoses() > doseNumber) ? 1 : 0;

						// Método 1: Intentar usar updateVaccineStatus si ya
						// existe un registro
						// Método 2: Insertar directamente como administrada

						// Opción más simple: Insertar directamente como
						// administrada
						// Nota: El método addVaccine probablemente crea
						// registros con status 'scheduled'
						// Necesitamos un método que cree registros como
						// 'administered'

						// Como alternativa, podemos usar SQL directo o
						// modificar el método
						// Por ahora, usaré un enfoque directo

						String sql = "INSERT INTO vaccines (patient_id, vaccine_name, dose_number, "
								+ "scheduled_date, administered_date, administered_by_doctor_id, "
								+ "administered_by_doctor_name, needs_booster, status, notes) "
								+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

						// Ejecutar SQL directamente
						try {
							model.db.executeUpdate(sql, patient.getId(),
									vaccine.getName(), doseNumber, currentDate, // scheduled
																				// date
																				// =
																				// today
									currentDate, // administered date = today
									doctorId, doctorName, needsBooster,
									"administered", // status = administered
									"Administered via Fast Vaccine Window on "
											+ currentDate);

							successCount++;
							System.out.println(
									"✓ Administered: " + vaccine.getName()
											+ " - Dose " + doseNumber);

						} catch (Exception e) {
							// Si falla el SQL directo, intentar método
							// alternativo
							System.err.println(
									"SQL direct failed, trying alternative: "
											+ e.getMessage());

							// Método alternativo: Usar addVaccine y luego
							// actualizar
							try {
								// Primero agregar la vacuna (status por defecto
								// 'scheduled')
								model.addVaccine(patient.getId(),
										vaccine.getName(), doseNumber,
										currentDate, // scheduled date
										needsBooster,
										"Administered via Fast Vaccine Window");

								// Luego buscar el ID recién insertado y
								// actualizar status
								// Esto requiere obtener el último ID insertado
								String getLastIdSql = "SELECT MAX(id) FROM vaccines WHERE patient_id = ? AND vaccine_name = ?";
								List<Object[]> results = model.db
										.executeQueryArray(getLastIdSql,
												patient.getId(),
												vaccine.getName());

								if (!results.isEmpty()
										&& results.get(0)[0] != null) {
									int vaccineId = ((Number) results.get(0)[0])
											.intValue();

									// Actualizar como administrada
									model.updateVaccineStatus(vaccineId,
											"administered", currentDate,
											doctorId, doctorName);

									successCount++;
									System.out.println(
											"✓ Administered (via update): "
													+ vaccine.getName());
								}

							} catch (Exception ex2) {
								System.err.println(
										"✗ Alternative method also failed: "
												+ ex2.getMessage());
							}
						}

					} catch (Exception e) {
						System.err.println("✗ Failed to administer: "
								+ vaccine.getName() + " - " + e.getMessage());
						e.printStackTrace();
					}
				}

				// Mostrar resultado
				if (successCount == selectedVaccines.size()) {
					JOptionPane.showMessageDialog(this,
							"Successfully administered " + successCount
									+ " vaccine(s) today.",
							"Complete Success",
							JOptionPane.INFORMATION_MESSAGE);
				} else if (successCount > 0) {
					JOptionPane.showMessageDialog(this, successCount
							+ " out of " + selectedVaccines.size()
							+ " vaccine(s) were successfully administered today.\n"
							+ (selectedVaccines.size() - successCount)
							+ " failed.", "Partial Success",
							JOptionPane.WARNING_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this,
							"Failed to administer any vaccines. Please check database connection.",
							"Complete Failure", JOptionPane.ERROR_MESSAGE);
					return; // No cerrar la ventana si falló todo
				}

				// Notificar ventana principal para recargar
				if (parentWindow != null) {
					SwingUtilities.invokeLater(() -> {
						parentWindow.reloadVaccines();
					});
				}

				// Cerrar ventana
				dispose();

			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						"Unexpected error: " + e.getMessage(), "System Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// Método auxiliar para extraer número de dosis
	private int extractDoseNumber(String doseType) {
		if (doseType == null || doseType.isEmpty()) {
			return 1;
		}

		try {
			// Buscar números en el texto (ej: "Dose 1", "Booster", "1st Dose")
			String numbers = doseType.replaceAll("[^0-9]", "");
			if (!numbers.isEmpty()) {
				return Integer.parseInt(numbers);
			}

			// Si no encuentra números, intentar determinar por texto
			String lowerType = doseType.toLowerCase();
			if (lowerType.contains("booster") || lowerType.contains("refuerzo")
					|| lowerType.contains("third")
					|| lowerType.contains("3rd")) {
				return 3;
			} else if (lowerType.contains("second")
					|| lowerType.contains("2nd")) {
				return 2;
			} else if (lowerType.contains("first")
					|| lowerType.contains("1st")) {
				return 1;
			}

			return 1; // Valor por defecto

		} catch (Exception e) {
			return 1; // Valor por defecto en caso de error
		}
	}

	// Custom cell renderer for vaccine lists
	private class VaccineListCellRenderer extends DefaultListCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if (value instanceof VaccineDTO) {
				VaccineDTO vaccine = (VaccineDTO) value;

				// Create HTML-formatted text with vaccine info
				StringBuilder displayText = new StringBuilder("<html><b>");
				displayText.append(vaccine.getName()).append("</b>");

				displayText
						.append("<br><font size='-2' color='gray'>Dose Type: ")
						.append(vaccine.getDoseType())
						.append(" | Recommended Doses: ")
						.append(vaccine.getRecommendedDoses());

				displayText.append("</font></html>");

				setText(displayText.toString());
				setFont(LIST_FONT);

				// Set background color based on selection
				if (isSelected) {
					setBackground(PRIMARY_COLOR);
					setForeground(Color.WHITE);
				} else {
					setBackground(index % 2 == 0 ? new Color(240, 248, 255)
							: Color.WHITE);
					setForeground(Color.BLACK);
				}
			}

			setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
			return this;
		}
	}

	// Timer class for delayed search
	private static class Timer extends javax.swing.Timer {
		public Timer(int delay, java.awt.event.ActionListener listener) {
			super(delay, listener);
		}
	}

	@Override
	public void dispose() {
		// Stop search timer when closing window
		if (searchTimer != null && searchTimer.isRunning()) {
			searchTimer.stop();
		}
		super.dispose();
	}
}