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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.toedter.calendar.JCalendar;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.VaccineAppointmentDTO;
import giis.demo.service.doctor.DoctorDTO;
import giis.demo.ui.vaccine.VaccineAppointmentDetailWindow;

public class ScheduledVaccineWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	// Colores y fuentes consistentes con DoctorMainWindow (AZUL como color
	// principal)
	private final Color PRIMARY_COLOR = new Color(41, 128, 185); // AZUL
																	// principal
	private final Color VACCINE_ACCENT_COLOR = new Color(39, 174, 96); // Verde
																		// para
																		// acentos
																		// de
																		// vacunas
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Color HIGHLIGHT_COLOR = new Color(240, 248, 255); // Azul
																	// claro
																	// para
																	// hover

	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
	private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font VACCINE_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font VACCINE_INFO_FONT = new Font("Segoe UI", Font.PLAIN, 12);
	private final Font VACCINE_DETAIL_FONT = new Font("Segoe UI", Font.ITALIC,
			11);
	private final Font VACCINE_LABEL_FONT = new Font("Segoe UI", Font.BOLD, 11);

	private JPanel contentPane;
	private JPanel appointmentsPanel;
	private JLabel dateLabel;
	private JButton todayButton;
	private JCalendar calendar;
	private AppointmentModel model;
	private boolean updatingCalendar = false;
	private DoctorDTO currentDoctor;
	private boolean showAllAppointments = false;

	public ScheduledVaccineWindow(AppointmentModel model) {
		this.model = model;
		initializeUI();
	}

	public void setCurrentDoctor(DoctorDTO doctor) {
		this.currentDoctor = doctor;
		if (doctor != null) {
			setTitle("Vaccine Schedule - Dr. " + doctor.getFullName());
			System.out.println("Logged in as : Dr. " + doctor.getFullName()
					+ " - " + doctor.getSpecialization());
		} else {
			setTitle("Vaccine Schedule - All Appointments");
			showAllAppointments = true;
		}
	}

	private void initializeUI() {
		setTitle("Vaccine Schedule");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 900, 600);

		contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		contentPane.setBackground(BACKGROUND_COLOR);
		setContentPane(contentPane);

		initializeHeaderPanel();
		initializeCalendarPanel();
		initializeAppointmentsPanel();

		// Cargar citas del día actual
		LocalDate today = LocalDate.now();
		updateCalendarSelection(today);
		loadVaccineAppointments(today);
	}

	private void initializeHeaderPanel() {
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(PANEL_BACKGROUND);
		headerPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(15, 20, 15, 20)));

		dateLabel = new JLabel("Today's Vaccine Appointments", JLabel.CENTER);
		dateLabel.setFont(HEADER_FONT);
		dateLabel.setForeground(PRIMARY_COLOR); // AZUL como en DoctorMainWindow
		headerPanel.add(dateLabel, BorderLayout.CENTER);

		todayButton = createStyledButton("Today", PRIMARY_COLOR); // Botón AZUL
		todayButton.addActionListener(e -> {
			LocalDate today = LocalDate.now();
			updateCalendarSelection(today);
			loadVaccineAppointments(today);
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
						HEADER_FONT, PRIMARY_COLOR), // Título AZUL
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		calendarPanel.setPreferredSize(new Dimension(250, 0));

		calendar = new JCalendar();
		calendar.setBackground(PANEL_BACKGROUND);
		calendar.setDecorationBackgroundColor(PANEL_BACKGROUND);
		calendar.addPropertyChangeListener("calendar", evt -> {
			if (!updatingCalendar) {
				java.util.Calendar selectedCalendar = calendar.getCalendar();
				LocalDate selectedDate = LocalDate.of(
						selectedCalendar.get(java.util.Calendar.YEAR),
						selectedCalendar.get(java.util.Calendar.MONTH) + 1,
						selectedCalendar.get(java.util.Calendar.DAY_OF_MONTH));
				loadVaccineAppointments(selectedDate);
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
						"Vaccine Appointments",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						HEADER_FONT, PRIMARY_COLOR),
				BorderFactory.createEmptyBorder(5, 5, 5, 5))); // Título AZUL

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

	private void loadVaccineAppointments(LocalDate date) {
		// DEBUG: Verificar datos primero
		model.debugVaccineAppointmentData();

		List<VaccineAppointmentDTO> appointments;

		if (showAllAppointments || currentDoctor == null) {
			appointments = model.getAllVaccineAppointmentsWithDetails();
		} else {
			appointments = model.getVaccineAppointmentsForDoctorAndDate(
					currentDoctor.getId(), date);
		}

		// DEBUG: Mostrar lo que se obtuvo
		System.out.println(
				"=== Citas obtenidas: " + appointments.size() + " ===");
		for (VaccineAppointmentDTO appt : appointments) {
			System.out.println(appt.toString());
		}

		displayVaccineAppointments(appointments);
		updateDateLabel(date);
	}

	public void displayVaccineAppointments(
			List<VaccineAppointmentDTO> appointments) {
		appointmentsPanel.removeAll();

		if (appointments.isEmpty()) {
			JLabel noAppointmentsLabel = new JLabel(
					"No vaccine appointments for this date", JLabel.CENTER);
			noAppointmentsLabel.setFont(LABEL_FONT);
			noAppointmentsLabel.setForeground(Color.GRAY);
			appointmentsPanel.add(noAppointmentsLabel);
		} else {
			for (VaccineAppointmentDTO appointment : appointments) {
				appointmentsPanel
						.add(createVaccineAppointmentPanel(appointment));
				appointmentsPanel
						.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing
			}
		}

		appointmentsPanel.revalidate();
		appointmentsPanel.repaint();
	}

	private JPanel createVaccineAppointmentPanel(
			VaccineAppointmentDTO appointment) {
		JPanel appointmentPanel = new JPanel(new BorderLayout());
		appointmentPanel.setBackground(PANEL_BACKGROUND);
		appointmentPanel.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(10, 15, 10, 15)));
		appointmentPanel.setPreferredSize(new Dimension(500, 80));
		appointmentPanel.setMaximumSize(new Dimension(500, 80));
		appointmentPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Efectos hover - Azul claro como en DoctorMainWindow
		appointmentPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				appointmentPanel.setBackground(HIGHLIGHT_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				appointmentPanel.setBackground(PANEL_BACKGROUND);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				openVaccineAppointmentDetail(appointment);
			}
		});

		JPanel mainInfoPanel = new JPanel(new BorderLayout());
		mainInfoPanel.setBackground(appointmentPanel.getBackground());

		// Panel izquierdo: Hora y Doctor
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBackground(appointmentPanel.getBackground());

		JLabel timeLabel = new JLabel(appointment.getFormattedTime());
		timeLabel.setFont(VACCINE_TITLE_FONT);
		timeLabel.setForeground(PRIMARY_COLOR); // AZUL para la hora

		JLabel doctorLabel = new JLabel("Dr. " + appointment.getDoctorName());
		doctorLabel.setFont(VACCINE_INFO_FONT);
		doctorLabel.setForeground(Color.DARK_GRAY);

		JLabel specializationLabel = new JLabel(
				appointment.getDoctorSpecialization());
		specializationLabel.setFont(VACCINE_DETAIL_FONT);
		specializationLabel.setForeground(Color.GRAY);

		leftPanel.add(timeLabel);
		leftPanel.add(doctorLabel);
		leftPanel.add(specializationLabel);

		// Panel central: Información del paciente
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBackground(appointmentPanel.getBackground());

		JLabel patientNameLabel = new JLabel(appointment.getPatientName());
		patientNameLabel.setFont(VACCINE_TITLE_FONT);
		patientNameLabel.setForeground(Color.BLACK);

		JLabel dniLabel = new JLabel("DNI: " + appointment.getPatientDni());
		dniLabel.setFont(VACCINE_INFO_FONT);
		dniLabel.setForeground(Color.DARK_GRAY);

		JLabel dateLabel = new JLabel("Date: " + appointment.getDate());
		dateLabel.setFont(VACCINE_INFO_FONT);
		dateLabel.setForeground(Color.DARK_GRAY);

		centerPanel.add(patientNameLabel);
		centerPanel.add(dniLabel);
		centerPanel.add(dateLabel);

		// Panel derecho: Vacunas (con etiqueta verde para mantener la identidad
		// de vacunas)
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBackground(appointmentPanel.getBackground());

		JLabel vaccinesLabel = new JLabel("Vaccines:");
		vaccinesLabel.setFont(VACCINE_LABEL_FONT);
		vaccinesLabel.setForeground(VACCINE_ACCENT_COLOR); // Verde solo para la
															// etiqueta
															// "Vaccines:"

		String vaccinesText = appointment.getVaccinesSummary();
		JLabel vaccinesValueLabel = new JLabel(
				"<html><div style='width:150px;'>" + vaccinesText
						+ "</div></html>");
		vaccinesValueLabel.setFont(VACCINE_DETAIL_FONT);
		vaccinesValueLabel.setForeground(Color.BLACK); // Texto de vacunas en
														// negro

		rightPanel.add(vaccinesLabel);
		rightPanel.add(vaccinesValueLabel);

		// Agregar paneles al panel principal
		mainInfoPanel.add(leftPanel, BorderLayout.WEST);
		mainInfoPanel.add(centerPanel, BorderLayout.CENTER);
		mainInfoPanel.add(rightPanel, BorderLayout.EAST);

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

	private void openVaccineAppointmentDetail(
			VaccineAppointmentDTO appointment) {
		System.out.println("Opening vaccine appointment details: "
				+ appointment.toString());

		// Aquí puedes abrir una ventana de detalle para la cita de vacunación
		// Por ahora solo mostramos en consola
		System.out.println("Vaccine Appointment Details:");
		System.out.println("  ID: " + appointment.getId());
		System.out.println("  Date: " + appointment.getDate());
		System.out.println("  Time: " + appointment.getHour());
		System.out.println("  Doctor: " + appointment.getDoctorName());
		System.out.println("  Patient: " + appointment.getPatientName()
				+ " (DNI: " + appointment.getPatientDni() + ")");
		System.out.println("  Vaccines: " + appointment.getVaccinesSummary());

		VaccineAppointmentDetailWindow detailWindow = new VaccineAppointmentDetailWindow(
				model, appointment, currentDoctor);
		detailWindow.setLocationRelativeTo(this); // Center relative to parent
		detailWindow.setVisible(true);
	}

	public void updateDateLabel(LocalDate date) {
		String formattedDate = date
				.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
		dateLabel.setText("Vaccine Appointments for: " + formattedDate);
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

	// Método principal para pruebas
	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(() -> {
			try {
				AppointmentModel model = new AppointmentModel();
				ScheduledVaccineWindow window = new ScheduledVaccineWindow(
						model);

				// Para pruebas, podemos simular un doctor o mostrar todas las
				// citas
				DoctorDTO testDoctor = new DoctorDTO();
				testDoctor.setId(1);
				testDoctor.setName("John");
				testDoctor.setSurname("Doe");
				testDoctor.setSpecialization("Pediatrics");
				window.setCurrentDoctor(testDoctor);

				window.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
				javax.swing.JOptionPane.showMessageDialog(null,
						"Error initializing vaccine schedule: "
								+ e.getMessage(),
						"Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		});
	}
}