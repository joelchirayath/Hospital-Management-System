
package giis.demo.ui.receptionist.searchers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.VaccineDTO;
import giis.demo.ui.receptionist.NewVaccineAppointmentWindow;

public class VaccineSearcher extends JDialog {
	private static final long serialVersionUID = 1L;

	private final AppointmentModel model;
	private final NewVaccineAppointmentWindow parentWindow;

	private JTextField txtSearch;
	private JTable vaccineTable;
	private DefaultTableModel tableModel;
	private TableRowSorter<TableModel> sorter;
	private JButton selectButton;
	private JButton cancelButton;

	private VaccineDTO selectedVaccine;

	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color SECONDARY_COLOR = new Color(46, 204, 113);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

	public VaccineSearcher(NewVaccineAppointmentWindow parent,
			AppointmentModel model) {
		super(parent, "Search Vaccine", true);
		this.parentWindow = parent;
		this.model = model;

		initializeUI();
		loadVaccines();
	}

	private void initializeUI() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(700, 500));
		setLocationRelativeTo(getParent());

		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		mainPanel.setBackground(PANEL_BACKGROUND);

		// Header
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(PANEL_BACKGROUND);
		headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		JLabel headerLabel = new JLabel("Search Vaccine");
		headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
		headerLabel.setForeground(PRIMARY_COLOR);
		headerPanel.add(headerLabel, BorderLayout.WEST);

		mainPanel.add(headerPanel, BorderLayout.NORTH);

		// Search panel
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
		searchPanel.setBackground(PANEL_BACKGROUND);
		searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		JLabel searchLabel = new JLabel("Search by vaccine name:");
		searchLabel.setFont(LABEL_FONT);
		searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		searchPanel.add(searchLabel);

		searchPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		txtSearch = new JTextField();
		txtSearch.setFont(LABEL_FONT);
		txtSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
		txtSearch.setAlignmentX(Component.LEFT_ALIGNMENT);

		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filterTable();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				filterTable();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				filterTable();
			}
		});

		searchPanel.add(txtSearch);
		mainPanel.add(searchPanel, BorderLayout.NORTH); // Changed from CENTER
														// to NORTH

		// Table panel
		String[] columnNames = { "Vaccine Name", "Recommended Doses" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 1) {
					return Integer.class;
				}
				return String.class;
			}
		};

		vaccineTable = new JTable(tableModel);
		vaccineTable.setFont(LABEL_FONT);
		vaccineTable.setRowHeight(30);
		vaccineTable.getTableHeader().setFont(HEADER_FONT);
		vaccineTable.getTableHeader().setBackground(PRIMARY_COLOR);
		vaccineTable.getTableHeader().setForeground(Color.WHITE);
		vaccineTable.setSelectionBackground(SECONDARY_COLOR.brighter());
		vaccineTable.setSelectionForeground(Color.BLACK);

		// Set preferred column widths for better viewing
		vaccineTable.getColumnModel().getColumn(0).setPreferredWidth(400); // Vaccine
																			// Name
		vaccineTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Recommended
																			// Doses

		vaccineTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					selectVaccine();
				}
			}
		});

		sorter = new TableRowSorter<>(vaccineTable.getModel());
		vaccineTable.setRowSorter(sorter);

		JScrollPane scrollPane = new JScrollPane(vaccineTable);
		scrollPane.setBorder(
				BorderFactory.createLineBorder(new Color(220, 220, 220)));
		scrollPane.setPreferredSize(new Dimension(650, 300)); // Set preferred
																// size
		scrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(PANEL_BACKGROUND);
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		mainPanel.add(tablePanel, BorderLayout.CENTER);

		// Button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(PANEL_BACKGROUND);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

		selectButton = new JButton("Select Vaccine");
		styleButton(selectButton, SECONDARY_COLOR);
		selectButton.addActionListener(e -> selectVaccine());
		selectButton.setEnabled(false);

		cancelButton = new JButton("Cancel");
		styleButton(cancelButton, new Color(189, 195, 199));
		cancelButton.addActionListener(e -> dispose());

		buttonPanel.add(selectButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(cancelButton);

		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Add selection listener
		vaccineTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				selectButton.setEnabled(vaccineTable.getSelectedRow() != -1);
			}
		});

		setContentPane(mainPanel);
		pack();

		// Set a maximum size to ensure scrolling works properly
		setMaximumSize(new Dimension(800, 600));
	}

	private void styleButton(JButton button, Color color) {
		button.setFont(BUTTON_FONT);
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(color.darker()),
				BorderFactory.createEmptyBorder(8, 20, 8, 20)));
		button.setFocusPainted(false);
		button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
	}

	private void loadVaccines() {
		try {
			// Clear existing rows
			tableModel.setRowCount(0);

			// Get vaccines from model
			List<VaccineDTO> vaccines = model.getAllVaccines();

			// Add vaccines to table
			for (VaccineDTO vaccine : vaccines) {
				tableModel.addRow(new Object[] { vaccine.getName(),
						vaccine.getRecommendedDoses() });
			}

			if (vaccines.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"No vaccines found in the database.", "No Data",
						JOptionPane.INFORMATION_MESSAGE);
			}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Error loading vaccines: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void filterTable() {
		String text = txtSearch.getText().trim();
		if (text.length() == 0) {
			sorter.setRowFilter(null);
		} else {
			// Search only in vaccine name column (column 0)
			sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
		}
	}

	private void selectVaccine() {
		int selectedRow = vaccineTable.getSelectedRow();
		if (selectedRow >= 0) {
			int modelRow = vaccineTable.convertRowIndexToModel(selectedRow);

			String vaccineName = tableModel.getValueAt(modelRow, 0).toString();
			Integer recommendedDoses = (Integer) tableModel.getValueAt(modelRow,
					1);

			// Show dose type selection dialog
			String doseType = showDoseTypeDialog(vaccineName);

			if (doseType != null) {
				// Create VaccineDTO with dose type
				selectedVaccine = new VaccineDTO(vaccineName, recommendedDoses);
				selectedVaccine.setDoseType(doseType); // Add dose type to DTO

				// Pass selected vaccine to parent window
				if (parentWindow != null) {
					System.out.println(
							"Selected vaccine " + selectedVaccine.toString());
					parentWindow.setSelectedVaccine(selectedVaccine);
				}

				dispose();
			}
		}
	}

	private String showDoseTypeDialog(String vaccineName) {
		// Create custom dialog for dose type selection
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JLabel messageLabel = new JLabel(
				"<html>Please select the dose type for:<br><b>" + vaccineName
						+ "</b></html>");
		messageLabel.setFont(LABEL_FONT);
		panel.add(messageLabel, BorderLayout.NORTH);

		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
		optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		String[] doseTypes = { "First Dose", "Booster Dose", "Annual Dose" };

		for (String doseType : doseTypes) {
			JButton doseButton = new JButton(doseType);
			doseButton.setFont(LABEL_FONT);
			doseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			doseButton.setMaximumSize(new Dimension(200, 35));
			doseButton.addActionListener(e -> {
				// Store the selected dose type
				doseButton.putClientProperty("selectedDoseType", doseType);
				((JDialog) doseButton.getTopLevelAncestor()).dispose();
			});
			optionsPanel.add(doseButton);
			optionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		}

		panel.add(optionsPanel, BorderLayout.CENTER);

		// Create dialog
		JDialog doseDialog = new JDialog(this, "Select Dose Type", true);
		doseDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		doseDialog.setContentPane(panel);
		doseDialog.pack();
		doseDialog.setLocationRelativeTo(this);
		doseDialog.setVisible(true);

		// Check which button was clicked
		for (Component comp : optionsPanel.getComponents()) {
			if (comp instanceof JButton) {
				JButton button = (JButton) comp;
				if (button.getClientProperty("selectedDoseType") != null) {
					return (String) button
							.getClientProperty("selectedDoseType");
				}
			}
		}

		return null; // User cancelled
	}

	public VaccineDTO getSelectedVaccine() {
		return selectedVaccine;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			// Para testing
			JFrame frame = new JFrame("Test Vaccine Searcher");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JButton testButton = new JButton("Open Vaccine Searcher");
			testButton.addActionListener(e -> {
				AppointmentModel model = new AppointmentModel();
				VaccineSearcher searcher = new VaccineSearcher(null, model);
				searcher.setVisible(true);
			});

			frame.getContentPane().add(testButton);
			frame.setSize(300, 200);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}