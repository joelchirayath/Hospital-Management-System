package giis.demo.service.doctor;

import java.time.LocalDate;
import java.util.List;

import giis.demo.service.appointment.AppointmentDTO;
import giis.demo.service.appointment.AppointmentModel;
import giis.demo.ui.doctor.DoctorMainWindow;

public class DoctorController {
	private AppointmentModel model;
	private DoctorMainWindow view;
	private DoctorDTO currentDoctor;

	public DoctorController(AppointmentModel model, DoctorMainWindow view) {
		this.model = model;
		this.view = view;
		this.view.setController(this);
	}

	public DoctorController(AppointmentModel model, DoctorMainWindow view,
		DoctorDTO doctor) {
		this.model = model;
		this.view = view;
		this.view.setController(this);
		this.currentDoctor = doctor;
		initializeView();
	}

	private void initializeView() {
		if (currentDoctor != null) {
			loadAppointmentsForDate(LocalDate.now());
			view.setCurrentDoctor(currentDoctor);
		}
	}

	public void loadAppointmentsForDate(LocalDate date) {
		if (currentDoctor == null) {
			System.out.println(
				"No doctor logged in. Cannot load appointments.");
			return;
		}

		List<AppointmentDTO> appointments = model.getAppointmentsForDoctorAndDate(
			currentDoctor.getId(), date);
		view.displayAppointments(appointments);
		view.updateDateLabel(date);
	}

	public void setCurrentDoctor(DoctorDTO doctor) {
		this.currentDoctor = doctor;
		if (view != null) {
			view.setCurrentDoctor(doctor);
		}
		if (doctor != null) {
			loadAppointmentsForDate(LocalDate.now());
		}
	}

	public DoctorDTO getCurrentDoctor() {
		return currentDoctor;
	}

	public boolean isDoctorLoggedIn() {
		return currentDoctor != null;
	}
}