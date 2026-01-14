package giis.demo.ui.receptionist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import giis.demo.ui.scheduler.SchedulerWindow;

public class ReceptionistMenu extends JFrame {
	private static final long serialVersionUID = 1L;

	// Colors
	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color SECONDARY_COLOR = new Color(52, 152, 219);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;

	// Fonts
	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

	private JPanel contentPane;
	private JButton btnNewAppointment;
	private JButton btnScheduleWorkers;
	private JButton btnVaccineAppointment;

	public ReceptionistMenu() {
		initializeWindow();
		initializeComponents();
	}

	private void initializeWindow() {
		setTitle("Receptionist Panel");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(300, 200, 900, 700);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(30, 30, 30, 30));
		contentPane.setBackground(BACKGROUND_COLOR);
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		contentPane.add(getHeaderPanel(), BorderLayout.NORTH);
		contentPane.add(getMainPanel(), BorderLayout.CENTER);
		setVisible(true);
	}

	private void initializeComponents() {
		styleButton(getBtnNewAppointment(), PRIMARY_COLOR);
		styleButton(getBtnScheduleWorkers(), PRIMARY_COLOR);
		styleButton(getBtnVaccineAppointment(), PRIMARY_COLOR);
	}

	private JPanel getHeaderPanel() {
		JPanel headerPanel = createSectionPanel(null);
		headerPanel.setLayout(new BorderLayout());

		JLabel titleLabel = new JLabel("Receptionist Panel",
				SwingConstants.CENTER);
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(PRIMARY_COLOR);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		JLabel subtitleLabel = new JLabel("Appointment Management System",
				SwingConstants.CENTER);
		subtitleLabel.setFont(HEADER_FONT);
		subtitleLabel.setForeground(Color.DARK_GRAY);
		subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(PANEL_BACKGROUND);
		centerPanel.add(titleLabel, BorderLayout.NORTH);
		centerPanel.add(subtitleLabel, BorderLayout.CENTER);

		headerPanel.add(centerPanel, BorderLayout.CENTER);

		return headerPanel;
	}

	/**
	 * Main panel with two centered option cards side-by-side.
	 */
	private JPanel getMainPanel() {
		JPanel wrapper = createSectionPanel("Quick actions");
		wrapper.setLayout(new BorderLayout());

		// Grid with two columns for the buttons
		JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
		grid.setOpaque(false);

		grid.add(getBtnNewAppointment());
		grid.add(getBtnScheduleWorkers());
		grid.add(getBtnVaccineAppointment());

		// Center the grid nicely
		JPanel center = new JPanel();
		center.setBackground(PANEL_BACKGROUND);
		center.add(grid); // FlowLayout centers it

		wrapper.add(center, BorderLayout.CENTER);

		setupButtonActions();
		return wrapper;
	}

	private JButton getBtnNewAppointment() {
		if (btnNewAppointment == null) {
			btnNewAppointment = createMenuButton("➕ New Appointment",
					"Schedule a new appointment for a patient", PRIMARY_COLOR);
			btnNewAppointment.setPreferredSize(new Dimension(350, 160));
		}
		return btnNewAppointment;
	}

	private JButton getBtnScheduleWorkers() {
		if (btnScheduleWorkers == null) {
			btnScheduleWorkers = createMenuButton("📅 Create new schedule",
					"Create a new schedule for any worker", PRIMARY_COLOR);
			btnScheduleWorkers.setPreferredSize(new Dimension(350, 160));
		}
		return btnScheduleWorkers;
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

		JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
		titleLabel.setFont(BUTTON_FONT);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		JLabel descLabel = new JLabel(description, SwingConstants.CENTER);
		descLabel.setFont(LABEL_FONT);
		descLabel.setForeground(Color.WHITE);

		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.setBackground(color);
		textPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		textPanel.add(titleLabel, BorderLayout.NORTH);
		textPanel.add(descLabel, BorderLayout.CENTER);

		button.add(textPanel, BorderLayout.CENTER);

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
		getBtnNewAppointment().addActionListener(e -> new NewApptWindow());

		getBtnScheduleWorkers().addActionListener(e -> {
			SchedulerWindow scheduler = new SchedulerWindow();
			scheduler.setVisible(true);
		});

		getBtnVaccineAppointment().addActionListener(e -> {
			NewVaccineAppointmentWindow vaccineWindow = new NewVaccineAppointmentWindow();
			vaccineWindow.setVisible(true);
		});
	}

	private JButton getBtnVaccineAppointment() {
		if (btnVaccineAppointment == null) {
			btnVaccineAppointment = createMenuButton("Vaccine Appointment",
					"Schedule new vaccine appointments for patients",
					PRIMARY_COLOR // Green color for distinction
			);
			btnVaccineAppointment.setPreferredSize(new Dimension(350, 160));
		}
		return btnVaccineAppointment;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				ReceptionistMenu frame = new ReceptionistMenu();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
