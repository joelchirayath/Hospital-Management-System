package giis.demo.ui.receptionist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import giis.demo.service.doctor.AddDoctorController;
import giis.demo.service.doctor.AddDoctorControllerImpl;
import giis.demo.service.doctor.DoctorDTO;

public class AddDoctorWindow extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField searchField;
	private JComboBox<DoctorDTO> doctorComboBox;
	private JButton btnClearSearch;
	private JButton btnAdd;
	private JButton btnCancel;
	private JButton btnSelect;
	private JPanel headerPanel;
	private JPanel buttonPanel;
	private JPanel searchPanel;
	private JPanel bottomPanel;
	private JScrollPane scrollPane;
	private JPanel selectedDoctorsPanel;
	
	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color ACCENT_COLOR = new Color(46, 204, 113);
	private final Color WARNING_COLOR = new Color(231, 76, 60);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
	
	private List<DoctorDTO> selectedDoctors;
	private NewApptWindow apptWindow;
	private AddDoctorController controller;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AddDoctorWindow frame = new AddDoctorWindow(new NewApptWindow());
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public AddDoctorWindow(NewApptWindow appt) {
		this.apptWindow = appt;
		this.controller = new AddDoctorControllerImpl();
		selectedDoctors = new ArrayList<>();
		setTitle("Add Doctor");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(300, 0, 800, 700);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
		contentPane.setBackground(BACKGROUND_COLOR);
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 15));
		
		contentPane.add(getHeaderPanel(), BorderLayout.NORTH);
		contentPane.add(getSearchPanel(), BorderLayout.CENTER);
		contentPane.add(getBottomPanel(), BorderLayout.SOUTH);
		
		// Add component listener to handle resizing
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updatePanelSizes();
			}
		});
	}

	private void updatePanelSizes() {
		int totalHeight = contentPane.getHeight() - getHeaderPanel().getHeight() - 15; // Subtract header and gap
		int searchPanelHeight = totalHeight / 3;
		int bottomPanelHeight = totalHeight - searchPanelHeight;
		
		getSearchPanel().setPreferredSize(new Dimension(getSearchPanel().getPreferredSize().width, searchPanelHeight));
		getBottomPanel().setPreferredSize(new Dimension(getBottomPanel().getPreferredSize().width, bottomPanelHeight));
		
		contentPane.revalidate();
		contentPane.repaint();
	}

	private JPanel getHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = new JPanel();
			headerPanel.setBackground(PANEL_BACKGROUND);
			headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
			headerPanel.setLayout(new BorderLayout(0, 0));
			
			JLabel lblAddDoctor = new JLabel("Add Doctor");
			lblAddDoctor.setFont(TITLE_FONT);
			lblAddDoctor.setForeground(PRIMARY_COLOR);
			headerPanel.add(lblAddDoctor, BorderLayout.WEST);
			
			headerPanel.add(getButtonPanel(), BorderLayout.EAST);
		}
		return headerPanel;
	}
	
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setBackground(PANEL_BACKGROUND);
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
			
			buttonPanel.add(getBtnCancel());
			buttonPanel.add(getBtnSelect());
		}
		return buttonPanel;
	}
	
	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton("Cancel");
			btnCancel.setFont(BUTTON_FONT);
			btnCancel.setBackground(WARNING_COLOR);
			btnCancel.setForeground(Color.WHITE);
			btnCancel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(WARNING_COLOR.darker()),
				BorderFactory.createEmptyBorder(10, 25, 10, 25)
			));
			btnCancel.setFocusPainted(false);
			btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
		return btnCancel;
	}
	
	private JButton getBtnSelect() {
		if (btnSelect == null) {
			btnSelect = new JButton("Select");
			btnSelect.setFont(BUTTON_FONT);
			btnSelect.setBackground(ACCENT_COLOR);
			btnSelect.setForeground(Color.WHITE);
			btnSelect.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(ACCENT_COLOR.darker()),
				BorderFactory.createEmptyBorder(10, 25, 10, 25)
			));
			btnSelect.setFocusPainted(false);
			btnSelect.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnSelect.setEnabled(false);
			btnSelect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selectDoctorsAndClose();
				}
			});
		}
		return btnSelect;
	}
	
	private JPanel getSearchPanel() {
		if (searchPanel == null) {
			searchPanel = new JPanel();
			searchPanel.setBackground(PANEL_BACKGROUND);
			searchPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(20, 20, 20, 20)
			));
			searchPanel.setLayout(new GridBagLayout());
			
			JLabel lblSearchDoctor = new JLabel("Search:");
			lblSearchDoctor.setFont(HEADER_FONT);
			lblSearchDoctor.setForeground(PRIMARY_COLOR);
			GridBagConstraints gbc_lblSearchDoctor = new GridBagConstraints();
			gbc_lblSearchDoctor.insets = new Insets(0, 0, 0, 15);
			gbc_lblSearchDoctor.gridx = 0;
			gbc_lblSearchDoctor.gridy = 0;
			searchPanel.add(lblSearchDoctor, gbc_lblSearchDoctor);
			
			GridBagConstraints gbc_searchField = new GridBagConstraints();
			gbc_searchField.insets = new Insets(0, 0, 0, 15);
			gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
			gbc_searchField.gridx = 1;
			gbc_searchField.gridy = 0;
			gbc_searchField.weightx = 0.3;
			searchPanel.add(getSearchField(), gbc_searchField);
			
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 0, 15);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 2;
			gbc_comboBox.gridy = 0;
			gbc_comboBox.weightx = 0.4;
			searchPanel.add(getDoctorComboBox(), gbc_comboBox);
			
			GridBagConstraints gbc_addButton = new GridBagConstraints();
			gbc_addButton.insets = new Insets(0, 0, 0, 15);
			gbc_addButton.gridx = 3;
			gbc_addButton.gridy = 0;
			searchPanel.add(getBtnAdd(), gbc_addButton);
			
			GridBagConstraints gbc_clearButton = new GridBagConstraints();
			gbc_clearButton.insets = new Insets(0, 0, 0, 0);
			gbc_clearButton.gridx = 4;
			gbc_clearButton.gridy = 0;
			searchPanel.add(getBtnClearSearch(), gbc_clearButton);
		}
		return searchPanel;
	}
	
	private JTextField getSearchField() {
		if (searchField == null) {
			searchField = new JTextField();
			searchField.setFont(LABEL_FONT);
			searchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)
			));
			searchField.setPreferredSize(new Dimension(150, 42));
			searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
				public void changedUpdate(javax.swing.event.DocumentEvent e) { searchData(); }
				public void removeUpdate(javax.swing.event.DocumentEvent e) { searchData(); }
				public void insertUpdate(javax.swing.event.DocumentEvent e) { searchData(); }
			});
		}
		return searchField;
	}
	
	private JComboBox<DoctorDTO> getDoctorComboBox() {
	    if (doctorComboBox == null) {
	        doctorComboBox = new JComboBox<>();
	        doctorComboBox.setFont(LABEL_FONT);
	        doctorComboBox.setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createLineBorder(BORDER_COLOR),
	            BorderFactory.createEmptyBorder(10, 12, 10, 12)
	        ));
	        doctorComboBox.setBackground(Color.WHITE);
	        doctorComboBox.setPreferredSize(new Dimension(200, 42));
	        doctorComboBox.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                checkAddButtonState();
	            }
	        });
	    }
	    return doctorComboBox;
	}
	
	private JButton getBtnAdd() {
		if (btnAdd == null) {
			btnAdd = new JButton("Add");
			btnAdd.setFont(BUTTON_FONT);
			btnAdd.setBackground(PRIMARY_COLOR);
			btnAdd.setForeground(Color.WHITE);
			btnAdd.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(PRIMARY_COLOR.darker()),
				BorderFactory.createEmptyBorder(8, 20, 8, 20)
			));
			btnAdd.setFocusPainted(false);
			btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnAdd.setPreferredSize(new Dimension(80, 42));
			btnAdd.setEnabled(false);
			btnAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addSelectedDoctor();
				}
			});
		}
		return btnAdd;
	}
	
	private JButton getBtnClearSearch() {
		if (btnClearSearch == null) {
			btnClearSearch = new JButton("Clear");
			btnClearSearch.setFont(LABEL_FONT);
			btnClearSearch.setBackground(new Color(149, 165, 166));
			btnClearSearch.setForeground(Color.WHITE);
			btnClearSearch.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(127, 140, 141)),
				BorderFactory.createEmptyBorder(8, 15, 8, 15)
			));
			btnClearSearch.setFocusPainted(false);
			btnClearSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnClearSearch.setPreferredSize(new Dimension(80, 42));
			btnClearSearch.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearSearch();
				}
			});
		}
		return btnClearSearch;
	}
	
	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel();
			bottomPanel.setBackground(PANEL_BACKGROUND);
			bottomPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(20, 20, 20, 20)
			));
			bottomPanel.setLayout(new BorderLayout(0, 0));
			
			JLabel lblSelectedDoctors = new JLabel("Selected Doctors");
			lblSelectedDoctors.setFont(HEADER_FONT);
			lblSelectedDoctors.setForeground(PRIMARY_COLOR);
			lblSelectedDoctors.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
			bottomPanel.add(lblSelectedDoctors, BorderLayout.NORTH);
			
			bottomPanel.add(getScrollPane(), BorderLayout.CENTER);
		}
		return bottomPanel;
	}
	
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
			scrollPane.setViewportView(getSelectedDoctorsPanel());
		}
		return scrollPane;
	}
	
	private JPanel getSelectedDoctorsPanel() {
		if (selectedDoctorsPanel == null) {
			selectedDoctorsPanel = new JPanel();
			selectedDoctorsPanel.setBackground(PANEL_BACKGROUND);
			selectedDoctorsPanel.setLayout(new BoxLayout(selectedDoctorsPanel, BoxLayout.Y_AXIS));
		}
		return selectedDoctorsPanel;
	}
	
	private void searchData() {
		
		if(!getSearchField().getText().isEmpty()) {
			String fullText = getSearchField().getText().trim();
			String[] wordsArray = fullText.split("\\s+");
			
			ArrayList<DoctorDTO> doctors = new ArrayList<DoctorDTO>();
			
			for (String string : wordsArray) {
				Optional<List<DoctorDTO>> possible_doctors = controller.getpossibleDoctors(string);
				if(possible_doctors.isPresent()) {
					for (DoctorDTO doctorDto : possible_doctors.get()) {
						doctors.add(doctorDto);
					}
				}
			}
			
			System.out.println("Patient found : " );
			getDoctorComboBox().removeAllItems();
			for (DoctorDTO doctorDto : doctors) {
				getDoctorComboBox().addItem(doctorDto);
				System.out.println(doctorDto.toString());
			}
			checkAddButtonState();
		}
	}
	

	
	private void clearSearch() {
		getSearchField().setText("");
		getDoctorComboBox().removeAllItems();
		checkAddButtonState();
	}
	
	private void checkAddButtonState() {
		boolean hasSelection = getDoctorComboBox().getItemCount() > 0 && getDoctorComboBox().getSelectedItem() != null;
		getBtnAdd().setEnabled(hasSelection);
	}
	
	private void addSelectedDoctor() {
		DoctorDTO selectedDoctor = (DoctorDTO) getDoctorComboBox().getSelectedItem();
		if (selectedDoctor != null && !isDoctorAlreadySelected(selectedDoctor)) {
			selectedDoctors.add(selectedDoctor);
			addDoctorToPanel(selectedDoctor);
			clearSearch();
			checkSelectButtonState();
		}
	}
	
	private boolean isDoctorAlreadySelected(DoctorDTO doctor) {
		for (DoctorDTO selected : selectedDoctors) {
			if (selected.getId().equals(doctor.getId())) {
				return true;
			}
		}
		return false;
	}
	
	private void addDoctorToPanel(DoctorDTO doctor) {
		JPanel doctorPanel = createDoctorPanel(doctor);
		getSelectedDoctorsPanel().add(doctorPanel);
		getSelectedDoctorsPanel().revalidate();
		getSelectedDoctorsPanel().repaint();
	}
	
	private JPanel createDoctorPanel(DoctorDTO doctor) {
		JPanel panel = new JPanel();
		panel.setBackground(new Color(240, 240, 240));
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_COLOR),
			BorderFactory.createEmptyBorder(10, 15, 10, 15)
		));
		panel.setLayout(new BorderLayout());
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		
		JLabel doctorLabel = new JLabel(doctor.toString());
		doctorLabel.setFont(LABEL_FONT);
		doctorLabel.setForeground(Color.DARK_GRAY);
		panel.add(doctorLabel, BorderLayout.CENTER);
		
		JButton removeButton = new JButton("Remove");
		removeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		removeButton.setBackground(WARNING_COLOR);
		removeButton.setForeground(Color.WHITE);
		removeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		removeButton.setFocusPainted(false);
		removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeDoctor(doctor, panel);
			}
		});
		panel.add(removeButton, BorderLayout.EAST);
		
		return panel;
	}
	
	private void removeDoctor(DoctorDTO doctor, JPanel panel) {
		selectedDoctors.remove(doctor);
		getSelectedDoctorsPanel().remove(panel);
		getSelectedDoctorsPanel().revalidate();
		getSelectedDoctorsPanel().repaint();
		checkSelectButtonState();
	}
	
	private void checkSelectButtonState() {
		boolean hasSelectedDoctors = !selectedDoctors.isEmpty();
		getBtnSelect().setEnabled(hasSelectedDoctors);
	}
	
	private void selectDoctorsAndClose() {
		
		apptWindow.setSelectedDoctors(selectedDoctors);
		
		System.out.println("Selected doctors: " + selectedDoctors.size());
		for (DoctorDTO doctor : selectedDoctors) {
			System.out.println(" - " + doctor.toString());
		}
		dispose();
	}
}