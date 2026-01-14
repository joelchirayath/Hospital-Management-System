package giis.demo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
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
import giis.demo.service.doctor.DoctorDTO;
import giis.demo.ui.doctor.DoctorLoginWindow;
import giis.demo.ui.doctor.DoctorMenu;
import giis.demo.ui.manager.ManagerDashboardWindow;
import giis.demo.ui.receptionist.ReceptionistMenu;
import giis.demo.util.Database;

public class MainMenuWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;

	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

	private JPanel contentPane;
	private JButton btnDoctors;
	private JButton btnreceptionist;
	private JButton btnManagers;
	private JPanel headerPanel;
	private JPanel mainPanel;
	private JPanel footerPanel;
	private JLabel titleLabel;
	private JLabel subtitleLabel;
	private JLabel HospitalLabel;

	private Database db;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				MainMenuWindow frame = new MainMenuWindow();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public MainMenuWindow() {
		initializeWindow();
		initializeComponents();
	}

	private void initializeWindow() {
		this.db = new Database();
		db.createDatabase(true);
		db.loadDatabase();

		setTitle("Hospital Management System");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		styleButton(getBtnDoctors(), PRIMARY_COLOR);
		styleButton(getBtnreceptionist(), PRIMARY_COLOR);
		styleButton(getBtnManagers(), PRIMARY_COLOR);
	}

	private JPanel getHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = createSectionPanel(null);
			headerPanel.setLayout(new BorderLayout());

			JPanel centerPanel = new JPanel(new BorderLayout());
			centerPanel.setBackground(PANEL_BACKGROUND);
			centerPanel.add(getTitleLabel(), BorderLayout.NORTH);
			centerPanel.add(getSubtitleLabel(), BorderLayout.CENTER);

			headerPanel.add(centerPanel, BorderLayout.CENTER);
		}
		return headerPanel;
	}

	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = createSectionPanel("Select your work:");
			mainPanel.setLayout(new GridLayout(3, 1, 20, 20));

			mainPanel.add(getBtnDoctors());
			mainPanel.add(getBtnreceptionist());
			mainPanel.add(getBtnManagers());

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

	private JButton getBtnDoctors() {
		if (btnDoctors == null) {
			btnDoctors = createMenuButton("Doctors Portal",
					"Access doctors management system", PRIMARY_COLOR);
		}
		return btnDoctors;
	}

	private JButton getBtnreceptionist() {
		if (btnreceptionist == null) {
			btnreceptionist = createMenuButton("Medical receptionist Portal",
					"Access assistants management system", PRIMARY_COLOR);
		}

		return btnreceptionist;
	}

	private JButton getBtnManagers() {
		if (btnManagers == null) {
			btnManagers = createMenuButton("Managers Portal",
					"Access managers management system", PRIMARY_COLOR);
		}

		return btnManagers;
	}

	private JLabel getTitleLabel() {
		if (titleLabel == null) {
			titleLabel = new JLabel("Hospital Management System",
					SwingConstants.CENTER);
			titleLabel.setFont(TITLE_FONT);
			titleLabel.setForeground(PRIMARY_COLOR);
			titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		}
		return titleLabel;
	}

	private JLabel getSubtitleLabel() {
		if (subtitleLabel == null) {
			subtitleLabel = new JLabel("Access Portal", SwingConstants.CENTER);
			subtitleLabel.setFont(HEADER_FONT);
			subtitleLabel.setForeground(Color.DARK_GRAY);
			subtitleLabel
					.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		}
		return subtitleLabel;
	}

	private JLabel getHospitalLabel() {
		if (HospitalLabel == null) {
			HospitalLabel = new JLabel("Hospital of Oviedo ");
			HospitalLabel.setFont(LABEL_FONT);
			HospitalLabel.setForeground(Color.GRAY);
		}
		return HospitalLabel;
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
		getBtnDoctors().addActionListener(e -> {
//            JOptionPane.showMessageDialog(this, 
//                "Doctors portal functionality to be implemented", 
//                "Doctors Portal", 
//                JOptionPane.INFORMATION_MESSAGE);
//           
			// MARIA
			try {
				// Initialize MVC components
				AppointmentModel model = new AppointmentModel(db);

				DoctorLoginWindow loginDialog = new DoctorLoginWindow(
						MainMenuWindow.this, model);
				loginDialog.setVisible(true);

				DoctorDTO selectedDoctor = loginDialog.getSelectedDoctor();
				if (selectedDoctor != null) {
					DoctorMenu doctorMenu = new DoctorMenu(model,
							selectedDoctor);
					doctorMenu.setVisible(true);
					/// TODO abrir menu
//					DoctorMainWindow view = new DoctorMainWindow(model);
//					DoctorController controller = new DoctorController(model,
//							view, selectedDoctor);
//
//					view.setVisible(true);
					///
					System.out.println("Doctor portal opened for: "
							+ selectedDoctor.getFullName());
				} else {
					JOptionPane.showMessageDialog(MainMenuWindow.this,
							"Login cancelled. Doctor portal not opened.",
							"Information", JOptionPane.INFORMATION_MESSAGE);
				}

			} catch (Exception e2) {
				e2.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Error initializing application: " + e2.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		getBtnreceptionist().addActionListener(e -> {
			// ADRIANA JOEL
			ReceptionistMenu menuReceptionist = new ReceptionistMenu();

		});

		getBtnManagers().addActionListener(e -> {
			ManagerDashboardWindow managerDashboard = new ManagerDashboardWindow();
			managerDashboard.setVisible(true);
		});

	}
}