package giis.demo.ui.doctor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.doctor.DoctorController;
import giis.demo.service.doctor.DoctorDTO;
import giis.demo.service.patient.PatientDTO;
import giis.demo.ui.clinicalOrder.ClinicalOrdersListWindow;
import giis.demo.ui.medicalRecord.MedicalRecordWindow;
import giis.demo.ui.receptionist.searchers.PatientSearcher;

public class DoctorMenu extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Color LOGOUT_COLOR = new Color(192, 57, 43);

	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font WELCOME_FONT = new Font("Segoe UI", Font.BOLD, 16);

	private JPanel contentPane;
	private JButton btnMedicalRecord;
	private JButton btnSchedule;
	private JButton btnClinicalOrder;
	private JButton btnScheduledVaccines; // Nuevo botón
	private JButton btnLogout;
	private JPanel headerPanel;
	private JPanel mainPanel;
	private JPanel footerPanel;
	private JLabel titleLabel;
	private JLabel welcomeLabel;
	private JLabel doctorInfoLabel;
	private JLabel hospitalLabel;

	private DoctorDTO currentDoctor;
	private AppointmentModel model;
	private DoctorController controller;

	public DoctorMenu(AppointmentModel model, DoctorDTO doctor) {
		this.model = model;
		this.currentDoctor = doctor;
		initializeWindow();
		initializeComponents();
	}

	public DoctorMenu(AppointmentModel model, DoctorDTO doctor,
			DoctorController controller) {
		this.model = model;
		this.currentDoctor = doctor;
		this.controller = controller;
		initializeWindow();
		initializeComponents();
	}

	private void initializeWindow() {
		setTitle("Doctor Portal - Hospital Management System");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(300, 100, 1000, 800);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(30, 30, 30, 30));
		contentPane.setBackground(BACKGROUND_COLOR);
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		contentPane.add(getHeaderPanel(), BorderLayout.NORTH);
		contentPane.add(getMainPanel(), BorderLayout.CENTER);
		contentPane.add(getFooterPanel(), BorderLayout.SOUTH);
	}

	private void initializeComponents() {
		styleButton(getBtnMedicalRecord(), PRIMARY_COLOR);
		styleButton(getBtnSchedule(), PRIMARY_COLOR);
		styleButton(getBtnClinicalOrder(), PRIMARY_COLOR);
		styleButton(getBtnScheduledVaccines(), PRIMARY_COLOR); // Mismo azul que
																// los demás
		styleButton(getBtnLogout(), LOGOUT_COLOR);

		// Update welcome label with doctor's name
		updateDoctorInfo();
	}

	private JPanel getHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = createSectionPanel(null);
			headerPanel.setLayout(new BorderLayout());

			// Left side - Title and doctor info
			JPanel leftPanel = new JPanel(new BorderLayout());
			leftPanel.setBackground(PANEL_BACKGROUND);
			leftPanel.add(getTitleLabel(), BorderLayout.NORTH);

			JPanel infoPanel = new JPanel(new BorderLayout());
			infoPanel.setBackground(PANEL_BACKGROUND);
			infoPanel.add(getWelcomeLabel(), BorderLayout.NORTH);
			infoPanel.add(getDoctorInfoLabel(), BorderLayout.CENTER);
			leftPanel.add(infoPanel, BorderLayout.CENTER);

			// Right side - Logout button
			JPanel rightPanel = new JPanel(new BorderLayout());
			rightPanel.setBackground(PANEL_BACKGROUND);
			rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
			rightPanel.add(getBtnLogout(), BorderLayout.NORTH);

			headerPanel.add(leftPanel, BorderLayout.CENTER);
			headerPanel.add(rightPanel, BorderLayout.EAST);
		}
		return headerPanel;
	}

	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = createSectionPanel("Doctor Functions");
			// Cambiar a GridLayout de 2x2 para 4 botones
			mainPanel.setLayout(new GridLayout(2, 2, 20, 20));

			mainPanel.add(getBtnMedicalRecord());
			mainPanel.add(getBtnSchedule());
			mainPanel.add(getBtnClinicalOrder());
			mainPanel.add(getBtnScheduledVaccines()); // Añadir el nuevo botón

			setupButtonActions();
		}
		return mainPanel;
	}

	private JPanel getFooterPanel() {
		if (footerPanel == null) {
			footerPanel = new JPanel();
			footerPanel.setBackground(BACKGROUND_COLOR);
			footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

			footerPanel.add(getHospitalLabel());
		}
		return footerPanel;
	}

	private JButton getBtnMedicalRecord() {
		if (btnMedicalRecord == null) {
			btnMedicalRecord = createMenuButton("Medical Record",
					"Access patient medical records", PRIMARY_COLOR);
		}
		return btnMedicalRecord;
	}

	private JButton getBtnSchedule() {
		if (btnSchedule == null) {
			btnSchedule = createMenuButton("Schedule",
					"Manage your appointment schedule", PRIMARY_COLOR);
		}
		return btnSchedule;
	}

	private JButton getBtnClinicalOrder() {
		if (btnClinicalOrder == null) {
			btnClinicalOrder = createMenuButton("Clinical Order",
					"Manage clinical orders", PRIMARY_COLOR);
		}
		return btnClinicalOrder;
	}

	private JButton getBtnScheduledVaccines() {
		if (btnScheduledVaccines == null) {
			btnScheduledVaccines = createMenuButton("Scheduled Vaccines",
					"View and manage vaccine appointments", PRIMARY_COLOR); // Mismo
																			// azul
		}
		return btnScheduledVaccines;
	}

	private JButton getBtnLogout() {
		if (btnLogout == null) {
			btnLogout = new JButton("Log Out");
			btnLogout.setBackground(LOGOUT_COLOR);
			btnLogout.setForeground(Color.WHITE);
			btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
			btnLogout.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(LOGOUT_COLOR.darker(), 2),
					BorderFactory.createEmptyBorder(8, 15, 8, 15)));
			btnLogout.setFocusPainted(false);
			btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

			// Hover effects for logout button
			btnLogout.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					btnLogout.setBackground(LOGOUT_COLOR.brighter());
				}

				@Override
				public void mouseExited(MouseEvent e) {
					btnLogout.setBackground(LOGOUT_COLOR);
				}
			});

			// Logout action
			btnLogout.addActionListener(e -> {
				int confirm = JOptionPane.showConfirmDialog(DoctorMenu.this,
						"Are you sure you want to log out?", "Confirm Logout",
						JOptionPane.YES_NO_OPTION);

				if (confirm == JOptionPane.YES_OPTION) {
					dispose();

				}
			});
		}
		return btnLogout;
	}

	private JLabel getTitleLabel() {
		if (titleLabel == null) {
			titleLabel = new JLabel("Doctor Portal", SwingConstants.LEFT);
			titleLabel.setFont(TITLE_FONT);
			titleLabel.setForeground(PRIMARY_COLOR);
			titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
		}
		return titleLabel;
	}

	private JLabel getWelcomeLabel() {
		if (welcomeLabel == null) {
			welcomeLabel = new JLabel("Welcome,", SwingConstants.LEFT);
			welcomeLabel.setFont(WELCOME_FONT);
			welcomeLabel.setForeground(Color.DARK_GRAY);
			welcomeLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));
		}
		return welcomeLabel;
	}

	private JLabel getDoctorInfoLabel() {
		if (doctorInfoLabel == null) {
			doctorInfoLabel = new JLabel("", SwingConstants.LEFT);
			doctorInfoLabel.setFont(HEADER_FONT);
			doctorInfoLabel.setForeground(PRIMARY_COLOR);
			doctorInfoLabel
					.setBorder(BorderFactory.createEmptyBorder(2, 0, 10, 0));
		}
		return doctorInfoLabel;
	}

	private JLabel getHospitalLabel() {
		if (hospitalLabel == null) {
			hospitalLabel = new JLabel("Hospital of Oviedo - Doctor Portal");
			hospitalLabel.setFont(LABEL_FONT);
			hospitalLabel.setForeground(Color.GRAY);
		}
		return hospitalLabel;
	}

	private JButton createMenuButton(String title, String description,
			Color color) {
		JButton button = new JButton();
		button.setLayout(new BorderLayout());
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(color.darker(), 2),
				BorderFactory.createEmptyBorder(25, 20, 25, 20)));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Title
		JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
		titleLabel.setFont(BUTTON_FONT);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		// Description
		JLabel descLabel = new JLabel(description, SwingConstants.CENTER);
		descLabel.setFont(LABEL_FONT);
		descLabel.setForeground(Color.WHITE);

		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.setBackground(color);
		textPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		textPanel.add(titleLabel, BorderLayout.NORTH);
		textPanel.add(descLabel, BorderLayout.CENTER);

		button.add(textPanel, BorderLayout.CENTER);

		// Hover effects
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				button.setBackground(color.brighter());
				textPanel.setBackground(color.brighter());
				button.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(color.darker(), 2),
						BorderFactory.createEmptyBorder(25, 20, 25, 20)));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(color);
				textPanel.setBackground(color);
				button.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(color.darker(), 2),
						BorderFactory.createEmptyBorder(25, 20, 25, 20)));
			}
		});

		return button;
	}

	private void styleButton(JButton button, Color color) {
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setFont(BUTTON_FONT);
		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(color.darker()),
				BorderFactory.createEmptyBorder(15, 25, 15, 25)));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
	}

	private JPanel createSectionPanel(String title) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(25, 25, 25, 25)));

		if (title != null) {
			JLabel titleLabel = new JLabel(title);
			titleLabel.setFont(HEADER_FONT);
			titleLabel.setForeground(PRIMARY_COLOR);
			titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
			panel.add(titleLabel, BorderLayout.NORTH);
		}

		return panel;
	}

	private void setupButtonActions() {
		getBtnMedicalRecord().addActionListener(e -> {
			PatientSearcher patientSearcher = new PatientSearcher(this, model);
			patientSearcher.setVisible(true);
			patientSearcher.setLocationRelativeTo(this);
		});

		getBtnSchedule().addActionListener(e -> {

			DoctorMainWindow scheduleWindow = new DoctorMainWindow(model);
			scheduleWindow.setCurrentDoctor(currentDoctor);

			DoctorController controller = new DoctorController(model,
					scheduleWindow, currentDoctor);
			scheduleWindow.setController(controller);

			scheduleWindow.setVisible(true);

			controller.loadAppointmentsForDate(java.time.LocalDate.now());
		});

		getBtnClinicalOrder().addActionListener(e -> {
			ClinicalOrdersListWindow listWindow = new ClinicalOrdersListWindow(
					this, model, currentDoctor);
			listWindow.setVisible(true);
		});

		getBtnScheduledVaccines().addActionListener(e -> {
			// Abrir la ventana de horario de vacunas
			ScheduledVaccineWindow vaccineWindow = new ScheduledVaccineWindow(
					model);
			vaccineWindow.setCurrentDoctor(currentDoctor);
			vaccineWindow.setVisible(true);

			// Opcional: centrar la ventana relativa a esta
			vaccineWindow.setLocationRelativeTo(this);

			System.out.println("Opening vaccine schedule for Dr. "
					+ currentDoctor.getFullName());
		});
	}

	private void updateDoctorInfo() {
		if (currentDoctor != null) {
			doctorInfoLabel.setText(currentDoctor.getFullName() + " (ID: "
					+ currentDoctor.getId() + ")");
			setTitle("Doctor Portal - " + currentDoctor.getFullName()
					+ " - Hospital Management System");
		}
	}

	public void openMedicalRecordForPatient(PatientDTO selectedPatient) {
		if (selectedPatient == null) {
			JOptionPane.showMessageDialog(this, "No patient selected!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Open the MedicalRecordWindow with the selected patient
		MedicalRecordWindow medicalRecordWindow = new MedicalRecordWindow(model,
				selectedPatient);
		medicalRecordWindow.setVisible(true);
		medicalRecordWindow.setLocationRelativeTo(this);

		System.out.println("Opening medical record for patient: "
				+ selectedPatient.getFullName() + " (ID: "
				+ selectedPatient.getId() + ")");
	}

	public DoctorDTO getCurrentDoctor() {
		return currentDoctor;
	}

	public AppointmentModel getModel() {
		return model;
	}
}