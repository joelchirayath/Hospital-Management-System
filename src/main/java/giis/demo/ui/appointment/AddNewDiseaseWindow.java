package giis.demo.ui.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import giis.demo.jdbc.models.apointment.disease.ICDModel;
import giis.demo.service.appointment.icd10.ICDDTO;

@SuppressWarnings("serial")
public class AddNewDiseaseWindow extends JDialog {
	private ICDDTO selectedIllness;
	private AppointmentDetailWindow parentWindow;
	private ICDModel icdModel;

	private JComboBox<String> sectionCombo;
	private JComboBox<String> chapterCombo;
	private JTextField searchField;
	private JPanel resultsPanel;
	private JButton selectButton;
	private JButton cancelButton;

	private List<ICDDTO> currentResults;

	// Lista de chapters en números romanos
	private final List<String> CHAPTERS = Arrays.asList("All Chapters", "I", "II", "III", "IV", "V", "VI", "VII",
			"VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX", "XXI", "XXII");

	public AddNewDiseaseWindow(AppointmentDetailWindow parent) {
		super(parent, "Add New Disease - ICD Selection", true);
		this.parentWindow = parent;
		this.icdModel = new ICDModel();
		initializeUI();
		setSize(1600, 1000);
		setLocationRelativeTo(parent);
		loadInitialData();
		performSearch();
	}

	private void initializeUI() {
		setLayout(new BorderLayout(10, 10));

		// Title
		JLabel titleLabel = new JLabel("ICD Disease Selection", JLabel.CENTER);
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		add(titleLabel, BorderLayout.NORTH);

		// Search panel
		add(createSearchPanel(), BorderLayout.CENTER);

		// Buttons panel
		add(createButtonsPanel(), BorderLayout.SOUTH);
	}

	private JPanel createSearchPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Search Diseases",
				TitledBorder.LEFT, TitledBorder.TOP));

		// Top controls panel
		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
		controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Chapter filter
		JPanel chapterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		chapterPanel.add(new JLabel("Chapter:"));
		chapterCombo = new JComboBox<>();
		loadChaptersComboBox(); // Cargar los chapters
		chapterCombo.addActionListener(e -> performSearch());
		chapterPanel.add(chapterCombo);

		// Section filter
		JPanel sectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sectionPanel.add(new JLabel("Section:"));
		sectionCombo = new JComboBox<>();
		sectionCombo.addItem("All Sections");
		sectionCombo.addActionListener(e -> performSearch());
		sectionPanel.add(sectionCombo);

		// Search field
		JPanel searchInputPanel = new JPanel(new BorderLayout(5, 5));
		searchInputPanel.add(new JLabel("Search (description or category):"), BorderLayout.NORTH);
		searchField = new JTextField();
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				performSearch();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				performSearch();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				performSearch();
			}
		});
		searchInputPanel.add(searchField, BorderLayout.CENTER);

		controlsPanel.add(chapterPanel);
		controlsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		controlsPanel.add(sectionPanel);
		controlsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		controlsPanel.add(searchInputPanel);

		// Results panel
		resultsPanel = new JPanel();
		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
		JScrollPane resultsScroll = new JScrollPane(resultsPanel);
		resultsScroll.setBorder(BorderFactory.createTitledBorder("Search Results"));
		resultsScroll.getViewport().setBackground(Color.WHITE);

		panel.add(controlsPanel, BorderLayout.NORTH);
		panel.add(resultsScroll, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		selectButton = new JButton("Select Disease");
		selectButton.addActionListener(e -> selectDisease());
		selectButton.setEnabled(false);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> {
			selectedIllness = null;
			dispose();
		});

		panel.add(selectButton);
		panel.add(cancelButton);

		return panel;
	}

	private void performSearch() {
		// Verificar que haya items seleccionados para evitar NullPointerException
		if (sectionCombo.getSelectedItem() == null || chapterCombo.getSelectedItem() == null) {
			return;
		}

		String searchTerm = searchField.getText().trim();
		String selectedSection = sectionCombo.getSelectedItem().toString();
		String selectedChapter = chapterCombo.getSelectedItem().toString();

		// Si no hay filtros activos (todos los combos en "All" y campo de búsqueda
		// vacío)
		// entonces mostrar todas las enfermedades
		boolean noFiltersActive = ("All Sections".equals(selectedSection) || selectedSection == null)
				&& ("All Chapters".equals(selectedChapter) || selectedChapter == null) && (searchTerm.isEmpty());

		if (noFiltersActive) {
			// Mostrar todas las enfermedades
			currentResults = icdModel.getAllDiseases();
		} else {
			// Aplicar filtros
			if ("All Sections".equals(selectedSection)) {
				selectedSection = null;
			}
			if ("All Chapters".equals(selectedChapter)) {
				selectedChapter = null;
			}

			// Si el searchTerm está vacío, pasar null para no filtrar por texto
			String searchTermForQuery = searchTerm.isEmpty() ? null : searchTerm;

			currentResults = icdModel.searchDiseases(searchTermForQuery, selectedSection, selectedChapter);
		}

		displayResults(currentResults);
	}

	private void displayResults(List<ICDDTO> results) {
		resultsPanel.removeAll();

		if (results == null || results.isEmpty()) {
			JLabel noResultsLabel = new JLabel("No diseases found matching your criteria");
			noResultsLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
			noResultsLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			noResultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			resultsPanel.add(noResultsLabel);
		} else {
			for (ICDDTO disease : results) {
				resultsPanel.add(createDiseaseRow(disease));
				resultsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			}
		}

		resultsPanel.revalidate();
		resultsPanel.repaint();
	}

	private JPanel createDiseaseRow(ICDDTO disease) {
		JPanel rowPanel = new JPanel(new BorderLayout(10, 5));
		rowPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
		rowPanel.setBackground(Color.WHITE);

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBackground(Color.WHITE);
		leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		JLabel codeLabel = new JLabel(disease.getCode());
		codeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
		codeLabel.setForeground(new Color(0, 102, 204)); // Azul profesional

		JLabel sectionLabel = new JLabel("Sec: " + disease.getSection());
		sectionLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
		sectionLabel.setForeground(new Color(102, 102, 102)); // Gris oscuro

		leftPanel.add(codeLabel);
		leftPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		leftPanel.add(sectionLabel);

		// Right side - Description and category
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBackground(Color.WHITE);
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		// Descripción con wrap para texto largo
		JTextArea descriptionLabel = new JTextArea(disease.getDescription());
		descriptionLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
		descriptionLabel.setBackground(Color.WHITE);
		descriptionLabel.setLineWrap(true);
		descriptionLabel.setWrapStyleWord(true);
		descriptionLabel.setEditable(false);
		descriptionLabel.setFocusable(false);
		descriptionLabel.setBorder(null);

		JLabel categoryLabel = new JLabel("Cat: " + disease.getCategory());
		categoryLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
		categoryLabel.setForeground(new Color(153, 153, 153)); // Gris más claro

		rightPanel.add(descriptionLabel);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		rightPanel.add(categoryLabel);

		// Selection button
		JButton selectDiseaseButton = new JButton("Select");
		selectDiseaseButton.setPreferredSize(new Dimension(80, 30));
		selectDiseaseButton.addActionListener(e -> {
			selectedIllness = disease;
			selectButton.setEnabled(true);
			highlightSelectedRow(rowPanel);
		});

		rowPanel.add(leftPanel, BorderLayout.WEST);
		rowPanel.add(rightPanel, BorderLayout.CENTER);
		rowPanel.add(selectDiseaseButton, BorderLayout.EAST);

		return rowPanel;
	}

	private void highlightSelectedRow(JPanel selectedRow) {
		// Reset all rows to default background
		for (Component comp : resultsPanel.getComponents()) {
			if (comp instanceof JPanel) {
				comp.setBackground(Color.WHITE);
			}
		}
		// Highlight selected row
		selectedRow.setBackground(new Color(220, 240, 255));
	}

	private void selectDisease() {
		if (selectedIllness != null) {
			dispose();
		}
	}

	public ICDDTO getSelectedIllness() {
		return selectedIllness;
	}

	private void loadInitialData() {
		// Llenar el ComboBox con letras A-Z por defecto
		sectionCombo.removeAllItems();
		sectionCombo.addItem("All Sections");

		// Agregar letras mayúsculas de la A a la Z
		for (char c = 'A'; c <= 'Z'; c++) {
			sectionCombo.addItem(String.valueOf(c));
		}

		// Perform initial search to show ALL diseases
		performSearch();
	}

	// Método para cargar los chapters en el ComboBox
	private void loadChaptersComboBox() {
		chapterCombo.removeAllItems();
		for (String chapter : CHAPTERS) {
			chapterCombo.addItem(chapter);
		}
	}

	@Override
	public void dispose() {
		if (selectedIllness != null && parentWindow != null) {
			parentWindow.addIllnessFromWindow(selectedIllness);
		}
		super.dispose();
	}
}