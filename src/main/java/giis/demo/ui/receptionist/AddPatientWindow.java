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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import giis.demo.service.patient.AddPatientController;
import giis.demo.service.patient.AddPatientControllerImpl;
import giis.demo.service.patient.PatientDTO;

public class AddPatientWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField searchField;
	private JComboBox<PatientDTO> patientComboBox;
	private JButton btnClearSearch;
	private JButton btnCancel;
	private JButton btnSelect;
	private JTextField txtFirstName;
	private JTextField txtLastName;
	private JTextField txtDni;
	private JTextField txtPhone;
	private JTextField txtEmail;
	private JTextField txtAddress;
	private JPanel headerPanel;
	private JPanel buttonPanel;
	private JPanel searchPanel;
	private JPanel mainFormPanel;
	private JTextField txtGender;
	private JTextField txtBirthDate;
	private PatientDTO selectedPatient;

	private AddPatientController controller;
	private NewApptWindow receptionistWindow;

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
	private final Font MANDATORY_FONT = new Font("Segoe UI", Font.PLAIN, 12);

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					AddPatientWindow frame = new AddPatientWindow(
							new NewApptWindow());
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public AddPatientWindow(NewApptWindow receptionistWindow) {
		this.receptionistWindow = receptionistWindow;
		this.controller = new AddPatientControllerImpl();
		setTitle("Add Patient");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(300, 0, 800, 1000);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
		contentPane.setBackground(BACKGROUND_COLOR);
		contentPane.setLayout(new BorderLayout(0, 15));
		setContentPane(contentPane);

		// Header stays at the top
		contentPane.add(getHeaderPanel(), BorderLayout.NORTH);

		// Panel that groups search + form
		JPanel centerPanel = new JPanel();
		centerPanel.setBackground(BACKGROUND_COLOR);
		centerPanel.setLayout(new BorderLayout(0, 15));
		centerPanel.add(getSearchPanel(), BorderLayout.NORTH);
		centerPanel.add(getMainFormPanel(), BorderLayout.CENTER);

		// Make the center scrollable so nothing overlaps on small screens
		javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(
				centerPanel);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		contentPane.add(scrollPane, BorderLayout.CENTER);

		// Size handling
		setMinimumSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(900, 700));
		pack();
		setLocationRelativeTo(null);

	}

	private JPanel getHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = new JPanel();
			headerPanel.setBackground(PANEL_BACKGROUND);
			headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
			headerPanel.setLayout(new BorderLayout(0, 0));

			JLabel lblAddPatient = new JLabel("Add Patient");
			lblAddPatient.setFont(TITLE_FONT);
			lblAddPatient.setForeground(PRIMARY_COLOR);
			headerPanel.add(lblAddPatient, BorderLayout.WEST);

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
					BorderFactory.createEmptyBorder(10, 25, 10, 25)));
			btnCancel.setFocusPainted(false);
			btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnCancel.addActionListener(new ActionListener() {
				@Override
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
					BorderFactory.createEmptyBorder(10, 25, 10, 25)));
			btnSelect.setFocusPainted(false);
			btnSelect.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnSelect.setEnabled(false);
			btnSelect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectPatient();
					dispose();
				}

				private void selectPatient() {
					receptionistWindow.setPatientDTO(getPatient());

				}

				private PatientDTO getPatient() {
					selectedPatient.setAddress(getTxtAddress().getText());
					selectedPatient.setPhone(getTxtPhone().getText());
					selectedPatient.setEmail(getTxtEmail().getText());
					return selectedPatient;
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
					BorderFactory.createEmptyBorder(20, 20, 20, 20)));
			searchPanel.setLayout(new GridBagLayout());

			JLabel lblSearchPatient = new JLabel("Search Patient");
			lblSearchPatient.setFont(HEADER_FONT);
			lblSearchPatient.setForeground(PRIMARY_COLOR);
			GridBagConstraints gbc_lblSearchPatient = new GridBagConstraints();
			gbc_lblSearchPatient.insets = new Insets(0, 0, 0, 15);
			gbc_lblSearchPatient.gridx = 0;
			gbc_lblSearchPatient.gridy = 0;
			searchPanel.add(lblSearchPatient, gbc_lblSearchPatient);

			GridBagConstraints gbc_searchField = new GridBagConstraints();
			gbc_searchField.insets = new Insets(0, 0, 15, 0);
			gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
			gbc_searchField.gridx = 1;
			gbc_searchField.gridy = 0;
			searchPanel.add(getSearchField(), gbc_searchField);

			JPanel comboPanel = new JPanel();
			comboPanel.setBackground(PANEL_BACKGROUND);
			comboPanel.setLayout(new BorderLayout(5, 0));
			comboPanel.add(getBtnClearSearch(), BorderLayout.WEST);
			comboPanel.add(getPatientComboBox(), BorderLayout.CENTER);

			GridBagConstraints gbc_comboPanel = new GridBagConstraints();
			gbc_comboPanel.insets = new Insets(0, 0, 0, 0);
			gbc_comboPanel.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboPanel.gridx = 1;
			gbc_comboPanel.gridy = 1;
			searchPanel.add(comboPanel, gbc_comboPanel);
		}
		return searchPanel;
	}

	private JTextField getSearchField() {
		if (searchField == null) {
			searchField = new JTextField();
			searchField.setFont(LABEL_FONT);
			searchField.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(BORDER_COLOR),
					BorderFactory.createEmptyBorder(10, 12, 10, 12)));
			searchField.setPreferredSize(new Dimension(350, 42));
			searchField.getDocument()
					.addDocumentListener(new DocumentListener() {
						@Override
						public void changedUpdate(DocumentEvent e) {
							searchData();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							searchData();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							searchData();
						}
					});
		}
		return searchField;
	}

	private JButton getBtnClearSearch() {
		if (btnClearSearch == null) {
			btnClearSearch = new JButton("Clear");
			btnClearSearch.setFont(LABEL_FONT);
			btnClearSearch.setBackground(new Color(149, 165, 166));
			btnClearSearch.setForeground(Color.WHITE);
			btnClearSearch.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(127, 140, 141)),
					BorderFactory.createEmptyBorder(8, 15, 8, 15)));
			btnClearSearch.setFocusPainted(false);
			btnClearSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnClearSearch.setPreferredSize(new Dimension(80, 42));
			btnClearSearch.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					clearSearch();
				}
			});
		}
		return btnClearSearch;
	}

	private JComboBox<PatientDTO> getPatientComboBox() {
		if (patientComboBox == null) {
			patientComboBox = new JComboBox<>();
			patientComboBox.setFont(LABEL_FONT);
			patientComboBox.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(BORDER_COLOR),
					BorderFactory.createEmptyBorder(10, 12, 10, 12)));
			patientComboBox.setBackground(Color.WHITE);

			patientComboBox.setPreferredSize(new Dimension(350, 42));
			patientComboBox.setMinimumSize(new Dimension(350, 42));
			patientComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectedPatient = (PatientDTO) patientComboBox
							.getSelectedItem();
					if (selectedPatient != null) {
						loadPatientData(selectedPatient);
					}
				}
			});
		}
		return patientComboBox;
	}

	private JPanel getMainFormPanel() {
		if (mainFormPanel == null) {
			mainFormPanel = new JPanel();
			mainFormPanel.setBackground(PANEL_BACKGROUND);
			mainFormPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(BORDER_COLOR, 1),
					BorderFactory.createEmptyBorder(25, 25, 25, 25)));
			mainFormPanel.setLayout(new BorderLayout(0, 0));

			JPanel northPanel = new JPanel();
			northPanel.setBackground(PANEL_BACKGROUND);
			northPanel.setLayout(new BorderLayout(0, 0));
			mainFormPanel.add(northPanel, BorderLayout.NORTH);

			JLabel lblPatientInformation = new JLabel("Patient Information");
			lblPatientInformation.setFont(HEADER_FONT);
			lblPatientInformation.setForeground(PRIMARY_COLOR);
			lblPatientInformation
					.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
			northPanel.add(lblPatientInformation, BorderLayout.NORTH);

			JLabel lblMandatoryFields = new JLabel("* Mandatory fields");
			lblMandatoryFields.setFont(MANDATORY_FONT);
			lblMandatoryFields.setForeground(Color.RED);
			lblMandatoryFields
					.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
			northPanel.add(lblMandatoryFields, BorderLayout.SOUTH);

			JPanel formPanel = new JPanel();
			formPanel.setBackground(PANEL_BACKGROUND);
			mainFormPanel.add(formPanel, BorderLayout.CENTER);
			formPanel.setLayout(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(15, 15, 15, 15);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;

			int row = 0;

			// First Name
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.weightx = 0.3;
			JLabel lblFirstName = new JLabel("First Name:*");
			lblFirstName.setFont(LABEL_FONT);
			lblFirstName.setForeground(Color.DARK_GRAY);
			formPanel.add(lblFirstName, gbc);

			gbc.gridx = 1;
			gbc.gridy = row;
			gbc.weightx = 0.7;
			formPanel.add(getTxtFirstName(), gbc);

			row++;

			// Last Name
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.weightx = 0.3;
			JLabel lblLastName = new JLabel("Last Name:*");
			lblLastName.setFont(LABEL_FONT);
			lblLastName.setForeground(Color.DARK_GRAY);
			formPanel.add(lblLastName, gbc);

			gbc.gridx = 1;
			gbc.gridy = row;
			gbc.weightx = 0.7;
			formPanel.add(getTxtLastName(), gbc);

			row++;

			// DNI
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.weightx = 0.3;
			JLabel lblDni = new JLabel("DNI:*");
			lblDni.setFont(LABEL_FONT);
			lblDni.setForeground(Color.DARK_GRAY);
			formPanel.add(lblDni, gbc);

			gbc.gridx = 1;
			gbc.gridy = row;
			gbc.weightx = 0.7;
			formPanel.add(getTxtDni(), gbc);

			row++;

			// Gender (ahora como TextField no editable)
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.weightx = 0.3;
			JLabel lblGender = new JLabel("Gender:");
			lblGender.setFont(LABEL_FONT);
			lblGender.setForeground(Color.DARK_GRAY);
			formPanel.add(lblGender, gbc);

			gbc.gridx = 1;
			gbc.gridy = row;
			gbc.weightx = 0.7;
			formPanel.add(getTxtGender(), gbc); // Cambiar a TextField

			row++;

			// Birth Date (ahora como TextField no editable)
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.weightx = 0.3;
			JLabel lblBirthDate = new JLabel("Birth Date:");
			lblBirthDate.setFont(LABEL_FONT);
			lblBirthDate.setForeground(Color.DARK_GRAY);
			formPanel.add(lblBirthDate, gbc);

			gbc.gridx = 1;
			gbc.gridy = row;
			gbc.weightx = 0.7;
			formPanel.add(getTxtBirthDate(), gbc); // Cambiar a TextField

			row++;

			// Phone (movido aquí)
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.weightx = 0.3;
			JLabel lblPhone = new JLabel("Phone:");
			lblPhone.setFont(LABEL_FONT);
			lblPhone.setForeground(Color.DARK_GRAY);
			formPanel.add(lblPhone, gbc);

			gbc.gridx = 1;
			gbc.gridy = row;
			gbc.weightx = 0.7;
			formPanel.add(getTxtPhone(), gbc);

			row++;

			// Email (movido aquí)
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.weightx = 0.3;
			JLabel lblEmail = new JLabel("Email:");
			lblEmail.setFont(LABEL_FONT);
			lblEmail.setForeground(Color.DARK_GRAY);
			formPanel.add(lblEmail, gbc);

			gbc.gridx = 1;
			gbc.gridy = row;
			gbc.weightx = 0.7;
			formPanel.add(getTxtEmail(), gbc);

			row++;

			// Address (movido aquí)
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.weightx = 0.3;
			JLabel lblAddress = new JLabel("Address:");
			lblAddress.setFont(LABEL_FONT);
			lblAddress.setForeground(Color.DARK_GRAY);
			formPanel.add(lblAddress, gbc);

			gbc.gridx = 1;
			gbc.gridy = row;
			gbc.weightx = 0.7;
			formPanel.add(getTxtAddress(), gbc);
		}
		return mainFormPanel;
	}

	// Nuevo TextField para Gender
	private JTextField getTxtGender() {
		if (txtGender == null) {
			txtGender = createStyledTextField();
			txtGender.setEditable(false); // No editable
		}
		return txtGender;
	}

	private JTextField getTxtBirthDate() {
		if (txtBirthDate == null) {
			txtBirthDate = createStyledTextField();
			txtBirthDate.setEditable(false);
		}
		return txtBirthDate;
	}

	private JTextField getTxtFirstName() {
		if (txtFirstName == null) {
			txtFirstName = createStyledTextField();
		}
		return txtFirstName;
	}

	private JTextField getTxtLastName() {
		if (txtLastName == null) {
			txtLastName = createStyledTextField();
		}
		return txtLastName;
	}

	private JTextField getTxtDni() {
		if (txtDni == null) {
			txtDni = createStyledTextField();
		}
		return txtDni;
	}

	private JTextField getTxtPhone() {
		if (txtPhone == null) {
			txtPhone = createStyledTextField();
		}
		return txtPhone;
	}

	private JTextField getTxtEmail() {
		if (txtEmail == null) {
			txtEmail = createStyledTextField();
		}
		return txtEmail;
	}

	private JTextField getTxtAddress() {
		if (txtAddress == null) {
			txtAddress = createStyledTextField();
		}
		return txtAddress;
	}

	private JTextField createStyledTextField() {
		JTextField textField = new JTextField();
		textField.setFont(LABEL_FONT);
		textField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));
		textField.setPreferredSize(new Dimension(300, 42));
		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkValidity();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkValidity();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				checkValidity();
			}
		});
		return textField;
	}

	private void searchData() {
		if (!getSearchField().getText().isEmpty()) {
			String fullText = getSearchField().getText().trim();
			String[] wordsArray = fullText.split("\\s+");

			ArrayList<PatientDTO> patients = new ArrayList<PatientDTO>();

			for (String string : wordsArray) {
				Optional<List<PatientDTO>> possible_patients = controller
						.getpossiblePatients(string);
				if (possible_patients.isPresent()) {
					for (PatientDTO patientDTO : possible_patients.get()) {
						patients.add(patientDTO);
					}
				}
			}

			System.out.println("Patient found : ");
			getPatientComboBox().removeAllItems();
			for (PatientDTO patientDTO : patients) {
				getPatientComboBox().addItem(patientDTO);
				System.out.println(patientDTO.toString());
			}
		}
	}

	private void loadPatientData(PatientDTO patient) {
		if (patient != null) {
			getTxtFirstName().setText(
					patient.getName() != null ? patient.getName() : "");
			getTxtFirstName().setEditable(false);
			getTxtLastName().setText(
					patient.getSurname() != null ? patient.getSurname() : "");
			getTxtLastName().setEditable(false);
			getTxtDni()
					.setText(patient.getDni() != null ? patient.getDni() : "");
			getTxtDni().setEditable(false);
			getTxtPhone().setText(
					patient.getPhone() != null ? patient.getPhone() : "");
			getTxtGender().setText(
					patient.getGender() != null ? patient.getGender() : "");

			if (patient.getBirthDate() != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(patient.getBirthDate());
				String formattedDate = String.format("%02d/%02d/%04d",
						cal.get(Calendar.DAY_OF_MONTH),
						cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
				getTxtBirthDate().setText(formattedDate);
			} else {
				getTxtBirthDate().setText("");
			}

			getTxtPhone().setText(
					patient.getPhone() != null ? patient.getPhone() : "");
			getTxtEmail().setText(
					patient.getEmail() != null ? patient.getEmail() : "");
			getTxtAddress().setText(
					patient.getAddress() != null ? patient.getAddress() : "");

			getBtnSelect().setEnabled(true);
		}
	}

	private void clearSearch() {

		getSearchField().setText("");
		getPatientComboBox().removeAllItems();

		getTxtFirstName().setText("");
		getTxtLastName().setText("");
		getTxtDni().setText("");
		getTxtPhone().setText("");
		getTxtEmail().setText("");
		getTxtAddress().setText("");

		getTxtGender().setText("");

		getTxtBirthDate().setText("");
		getBtnSelect().setEnabled(false);

		System.out.println("All fields cleared");
	}

	private void checkValidity() {
		boolean isValid = true;
		if (getBtnSelect() != null) {
			getBtnSelect().setEnabled(isValid);
		}
	}

}