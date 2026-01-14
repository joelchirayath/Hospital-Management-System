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
import giis.demo.service.doctor.DoctorDTO;
import giis.demo.ui.receptionist.NewVaccineAppointmentWindow;

public class DoctorSearcher extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField searchField;
	private JButton btnClearSearch;
	private JButton btnCancel;
	private JButton btnSelect;
	private JList<DoctorDTO> doctorList;
	private DefaultListModel<DoctorDTO> listModel;
	private NewVaccineAppointmentWindow vaccineWindow;

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

	// Timer para búsquedas con delay
	private javax.swing.Timer searchTimer;
	private static final int SEARCH_DELAY = 300; // milisegundos

	public DoctorSearcher(NewVaccineAppointmentWindow vaccineWindow) {
		this.vaccineWindow = vaccineWindow;
		this.model = new AppointmentModel();
		initializeUI();
		setupSearchTimer();
	}

	public DoctorSearcher(NewVaccineAppointmentWindow vaccineWindow,
			AppointmentModel model) {
		this.vaccineWindow = vaccineWindow;
		this.model = model;
		initializeUI();
		setupSearchTimer();
	}

	private void initializeUI() {
		setTitle("Search Doctors");
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
		searchTimer.setRepeats(false);
	}

	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(PANEL_BACKGROUND);
		headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

		JLabel lblTitle = new JLabel("Search and Select Doctors");
		lblTitle.setFont(TITLE_FONT);
		lblTitle.setForeground(PRIMARY_COLOR);
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

		JLabel lblSearch = new JLabel(
				"Search by name, surname, or specialization:");
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

		JLabel lblInstructions = new JLabel(
				"Type to search (searches with each keystroke)");
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
		doctorList = new JList<>(listModel);
		doctorList.setFont(LIST_FONT);
		doctorList.setBackground(Color.WHITE);
		doctorList.setSelectionMode(
				javax.swing.ListSelectionModel.SINGLE_SELECTION);
		doctorList.setCellRenderer(new DoctorListCellRenderer());

		JScrollPane scrollPane = new JScrollPane(doctorList);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		scrollPane.setPreferredSize(new Dimension(700, 350));

		resultsPanel.add(scrollPane, BorderLayout.CENTER);

		// Selection info label
		JLabel lblSelectionInfo = new JLabel("No doctor selected");
		lblSelectionInfo.setFont(LABEL_FONT);
		lblSelectionInfo.setForeground(PRIMARY_COLOR);
		lblSelectionInfo
				.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		resultsPanel.add(lblSelectionInfo, BorderLayout.SOUTH);

		// Add listener to update selection count
		doctorList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				DoctorDTO selected = doctorList.getSelectedValue();
				if (selected != null) {
					lblSelectionInfo
							.setText("Selected: " + selected.getFullName());
					btnSelect.setEnabled(true);
				} else {
					lblSelectionInfo.setText("No doctor selected");
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

		btnSelect = createStyledButton("Select Doctor", SECONDARY_COLOR);
		btnSelect.setEnabled(false);
		btnSelect.addActionListener(e -> selectDoctor());

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

	private void scheduleSearch() {
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

		try {
			// Asumiendo que AppointmentModel tiene un método similar para
			// doctores
			// Necesitarás implementar getPossibleDoctors en AppointmentModel
			Optional<List<DoctorDTO>> possibleDoctors = model
					.getPossibleDoctors(searchText);

			SwingUtilities.invokeLater(() -> {
				listModel.clear();

				if (possibleDoctors.isPresent()
						&& !possibleDoctors.get().isEmpty()) {
					for (DoctorDTO doctor : possibleDoctors.get()) {
						listModel.addElement(doctor);
					}

					System.out.println("Found " + possibleDoctors.get().size()
							+ " doctors for search: '" + searchText + "'");

					// Seleccionar el primer resultado automáticamente si hay
					// resultados
					if (listModel.size() > 0) {
						doctorList.setSelectedIndex(0);
					}
				} else {
					System.out.println("No doctors found for search: '"
							+ searchText + "'");
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(DoctorSearcher.this,
						"Error searching doctors: " + e.getMessage(),
						"Search Error", JOptionPane.ERROR_MESSAGE);
			});
		}
	}

	private void clearSearch() {
		searchField.setText("");
		listModel.clear();
		btnSelect.setEnabled(false);
		System.out.println("Search cleared");
	}

	private void selectDoctor() {
		DoctorDTO selectedDoctor = doctorList.getSelectedValue();

		if (selectedDoctor == null) {
			JOptionPane.showMessageDialog(this,
					"Please select a doctor from the list.",
					"No Doctor Selected", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Set the selected doctor in the vaccine window
		if (vaccineWindow != null) {
			// Necesitarás agregar un método setSelectedDoctor en
			// NewVaccineAppointmentWindow
			vaccineWindow.setSelectedDoctor(selectedDoctor);

			JOptionPane.showMessageDialog(this,
					"Doctor '" + selectedDoctor.getFullName()
							+ "' selected successfully!",
					"Doctor Selected", JOptionPane.INFORMATION_MESSAGE);

			dispose();
		} else {
			JOptionPane.showMessageDialog(this,
					"Error: Vaccine window not available.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// Custom cell renderer for the doctor list
	private class DoctorListCellRenderer extends JLabel
			implements ListCellRenderer<DoctorDTO> {
		public DoctorListCellRenderer() {
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends DoctorDTO> list, DoctorDTO doctor, int index,
				boolean isSelected, boolean cellHasFocus) {

			if (doctor != null) {
				String displayText = String.format("Dr. %s %s (ID: %d)",
						doctor.getName() != null ? doctor.getName() : "",
						doctor.getSurname() != null ? doctor.getSurname() : "",
						doctor.getId());

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
				StringBuilder tooltip = new StringBuilder("<html><b>Dr. ");
				tooltip.append(
						doctor.getName() != null ? doctor.getName() : "");
				tooltip.append(" ");
				tooltip.append(
						doctor.getSurname() != null ? doctor.getSurname() : "");
				tooltip.append("</b><br>");
				tooltip.append("ID: ").append(doctor.getId()).append("<br>");

				if (doctor.getSpecialization() != null
						&& !doctor.getSpecialization().isEmpty()) {
					tooltip.append("Specialization: ")
							.append(doctor.getSpecialization()).append("<br>");
				}

				if (doctor.getDni() != null && !doctor.getDni().isEmpty()) {
					tooltip.append("DNI: ").append(doctor.getDni())
							.append("<br>");
				}

				if (doctor.getEmail() != null && !doctor.getEmail().isEmpty()) {
					tooltip.append("Email: ").append(doctor.getEmail());
				}

				tooltip.append("</html>");
				setToolTipText(tooltip.toString());
			} else {
				setText("No doctor data");
				setBackground(isSelected ? PRIMARY_COLOR : Color.WHITE);
				setForeground(isSelected ? Color.WHITE : Color.BLACK);
			}

			return this;
		}
	}

	@Override
	public void dispose() {
		if (searchTimer != null && searchTimer.isRunning()) {
			searchTimer.stop();
		}
		super.dispose();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			DoctorSearcher searcher = new DoctorSearcher(null);
			searcher.setVisible(true);
		});
	}
}