package giis.demo.ui.doctor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.toedter.calendar.JCalendar;

import giis.demo.service.appointment.AppointmentDTO;
import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.doctor.DoctorController;
import giis.demo.service.doctor.DoctorDTO;
import giis.demo.ui.appointment.AppointmentDetailWindow;

public class DoctorMainWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	// Colores y fuentes consistentes con DoctorMenu
	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Color LOGOUT_COLOR = new Color(192, 57, 43);

	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font WELCOME_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final Font APPOINTMENT_TITLE_FONT = new Font("Segoe UI", Font.BOLD,
			14);
	private final Font APPOINTMENT_INFO_FONT = new Font("Segoe UI", Font.PLAIN,
			12);

	private JPanel contentPane;
	private JPanel appointmentsPanel;
	private JLabel dateLabel;
	private JButton todayButton;
	private JCalendar calendar;
	private DoctorController controller;
	private AppointmentModel model;
	private boolean updatingCalendar = false;
	private DoctorDTO currentDoctor;

	public DoctorMainWindow(AppointmentModel model) {
		this.model = model;
		initializeUI();
	}

	public void setCurrentDoctor(DoctorDTO doctor) {
		this.currentDoctor = doctor;
		if (doctor != null) {
			setTitle("Doctor Appointment System - " + doctor.getFullName());

			String doctorInfo = String.format("Dr. %s - %s",
					doctor.getFullName(), doctor.getSpecialization());
			System.out.println("Logged in as : " + doctorInfo);

		}
	}

	public void setController(DoctorController controller) {
		this.controller = controller;
	}

	private void initializeUI() {
		setTitle("Doctor Appointment System");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 900, 600);

		contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		contentPane.setBackground(BACKGROUND_COLOR);
		setContentPane(contentPane);

		initializeHeaderPanel();
		initializeCalendarPanel();
		initializeAppointmentsPanel();
	}

	private void initializeHeaderPanel() {
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(PANEL_BACKGROUND);
		headerPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(15, 20, 15, 20)));

		dateLabel = new JLabel("Today's Appointments", JLabel.CENTER);
		dateLabel.setFont(HEADER_FONT);
		dateLabel.setForeground(PRIMARY_COLOR);
		headerPanel.add(dateLabel, BorderLayout.CENTER);

		todayButton = createStyledButton("Today", PRIMARY_COLOR);
		todayButton.addActionListener(e -> {
			LocalDate today = LocalDate.now();
			updateCalendarSelection(today);
			controller.loadAppointmentsForDate(today);
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBackground(PANEL_BACKGROUND);
		buttonPanel.add(todayButton);
		headerPanel.add(buttonPanel, BorderLayout.SOUTH);

		contentPane.add(headerPanel, BorderLayout.NORTH);
	}

	private void initializeCalendarPanel() {
		JPanel calendarPanel = new JPanel(new BorderLayout());
		calendarPanel.setBackground(PANEL_BACKGROUND);
		calendarPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createLineBorder(BORDER_COLOR, 1),
						"Select Date",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						HEADER_FONT, PRIMARY_COLOR),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		calendarPanel.setPreferredSize(new Dimension(250, 0));

		calendar = new JCalendar();
		calendar.setBackground(PANEL_BACKGROUND);
		calendar.setDecorationBackgroundColor(PANEL_BACKGROUND);
		calendar.addPropertyChangeListener("calendar", evt -> {
			// Prevent infinite recursion
			if (!updatingCalendar) {
				java.util.Calendar selectedCalendar = calendar.getCalendar();
				LocalDate selectedDate = LocalDate.of(
						selectedCalendar.get(java.util.Calendar.YEAR),
						selectedCalendar.get(java.util.Calendar.MONTH) + 1,
						selectedCalendar.get(java.util.Calendar.DAY_OF_MONTH));
				controller.loadAppointmentsForDate(selectedDate);
			}
		});

		calendarPanel.add(calendar, BorderLayout.CENTER);
		contentPane.add(calendarPanel, BorderLayout.WEST);
	}

	private void initializeAppointmentsPanel() {
		JPanel mainAppointmentsPanel = new JPanel(new BorderLayout());
		mainAppointmentsPanel.setBackground(BACKGROUND_COLOR);
		mainAppointmentsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createLineBorder(BORDER_COLOR, 1),
						"Appointments",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						HEADER_FONT, PRIMARY_COLOR),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		appointmentsPanel = new JPanel();
		appointmentsPanel
				.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
		appointmentsPanel.setBackground(PANEL_BACKGROUND);
		appointmentsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
		scrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getViewport().setBackground(PANEL_BACKGROUND);

		mainAppointmentsPanel.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(mainAppointmentsPanel, BorderLayout.CENTER);
	}

	public void displayAppointments(List<AppointmentDTO> appointments) {
		appointmentsPanel.removeAll();

		if (appointments.isEmpty()) {
			JLabel noAppointmentsLabel = new JLabel(
					"No appointments for this date", JLabel.CENTER);
			noAppointmentsLabel.setFont(LABEL_FONT);
			noAppointmentsLabel.setForeground(Color.GRAY);
			appointmentsPanel.add(noAppointmentsLabel);
		} else {
			for (AppointmentDTO appointment : appointments) {
				appointmentsPanel.add(createAppointmentPanel(appointment));
				appointmentsPanel
						.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing
			}
		}

		appointmentsPanel.revalidate();
		appointmentsPanel.repaint();
	}

	private JPanel createAppointmentPanel(AppointmentDTO appointment) {
		JPanel appointmentPanel = new JPanel(new BorderLayout());
		appointmentPanel.setBackground(PANEL_BACKGROUND);
		appointmentPanel.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(10, 15, 10, 15)));
		appointmentPanel.setPreferredSize(new Dimension(500, 60));
		appointmentPanel.setMaximumSize(new Dimension(500, 60));
		appointmentPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Efectos hover
		appointmentPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				appointmentPanel.setBackground(new Color(245, 245, 245));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				appointmentPanel.setBackground(PANEL_BACKGROUND);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				openAppointmentDetail(appointment);
			}
		});

		JPanel mainInfoPanel = new JPanel(new BorderLayout());
		mainInfoPanel.setBackground(appointmentPanel.getBackground());

		JPanel timeOfficePanel = new JPanel();
		timeOfficePanel
				.setLayout(new BoxLayout(timeOfficePanel, BoxLayout.Y_AXIS));
		timeOfficePanel.setBackground(appointmentPanel.getBackground());

		JLabel timeLabel = new JLabel(appointment.getTimeSlot());
		timeLabel.setFont(APPOINTMENT_TITLE_FONT);
		timeLabel.setForeground(PRIMARY_COLOR);

		JLabel officeLabel = new JLabel(appointment.getOffice());
		officeLabel.setFont(APPOINTMENT_INFO_FONT);
		officeLabel.setForeground(Color.GRAY);

		timeOfficePanel.add(timeLabel);
		timeOfficePanel.add(officeLabel);

		JPanel patientPanel = new JPanel();
		patientPanel.setLayout(new BoxLayout(patientPanel, BoxLayout.Y_AXIS));
		patientPanel.setBackground(appointmentPanel.getBackground());

		JLabel patientNameLabel = new JLabel(appointment.getFullPatientName());
		patientNameLabel.setFont(APPOINTMENT_TITLE_FONT);
		patientNameLabel.setForeground(Color.BLACK);

		JLabel dniLabel = new JLabel("DNI: " + appointment.getPatient_dni());
		dniLabel.setFont(APPOINTMENT_INFO_FONT);
		dniLabel.setForeground(Color.DARK_GRAY);

		patientPanel.add(patientNameLabel);
		patientPanel.add(dniLabel);

		JLabel attendanceIndicator = new JLabel();
		attendanceIndicator.setFont(new Font("Segoe UI", Font.BOLD, 14));
		attendanceIndicator.setHorizontalAlignment(SwingConstants.RIGHT);

		if (appointment.getAttended() != null) {
			if (appointment.getAttended() == 1) {
				attendanceIndicator.setText("✓ Attended");
				attendanceIndicator.setForeground(new Color(39, 174, 96));
			} else {
				attendanceIndicator.setText("✗ Not Attended");
				attendanceIndicator.setForeground(LOGOUT_COLOR);
			}
		} else {
			attendanceIndicator.setText("● Pending");
			attendanceIndicator.setForeground(new Color(243, 156, 18));
		}

		mainInfoPanel.add(timeOfficePanel, BorderLayout.WEST);
		mainInfoPanel.add(patientPanel, BorderLayout.CENTER);
		mainInfoPanel.add(attendanceIndicator, BorderLayout.EAST);

		appointmentPanel.add(mainInfoPanel, BorderLayout.CENTER);

		return appointmentPanel;
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

		return button;
	}

	private void openAppointmentDetail(AppointmentDTO appointment) {
		System.out.println(
				"Opening appointment details for: " + appointment.toString());

		AppointmentDetailWindow detailWindow = new AppointmentDetailWindow(
				model, appointment);
		detailWindow.setVisible(true);
	}

	public void updateDateLabel(LocalDate date) {
		String formattedDate = date
				.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
		dateLabel.setText("Appointments for: " + formattedDate);
	}

	private void updateCalendarSelection(LocalDate date) {
		updatingCalendar = true;
		try {
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.set(date.getYear(), date.getMonthValue() - 1,
					date.getDayOfMonth());
			calendar.setCalendar(cal);
		} finally {
			updatingCalendar = false;
		}
	}

//    public static void main(String[] args) {
//        EventQueue.invokeLater(() -> {
//            try {
//                // Initialize MVC components
//                AppointmentModel model = new AppointmentModel();
//                DoctorMainWindow view = new DoctorMainWindow(model);
//                DoctorController controller = new DoctorController(model, view);
//                
//                view.setVisible(true);
//            } catch (Exception e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(null, 
//                    "Error initializing application: " + e.getMessage(), 
//                    "Error", 
//                    JOptionPane.ERROR_MESSAGE);
//            }
//        });
//    }
}