package giis.demo.service.appointment;

public class VaccineAppointmentDTO {
	private Integer id;
	private Integer doctorId;
	private Integer patientId;
	private String date;
	private String hour;
	private String doctorName;
	private String doctorSpecialization;
	private String patientName;
	private String patientDni;
	private String vaccines;

	// Getters y Setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Integer doctorId) {
		this.doctorId = doctorId;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public String getDoctorSpecialization() {
		return doctorSpecialization;
	}

	public void setDoctorSpecialization(String doctorSpecialization) {
		this.doctorSpecialization = doctorSpecialization;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getPatientDni() {
		return patientDni;
	}

	public void setPatientDni(String patientDni) {
		this.patientDni = patientDni;
	}

	public String getVaccines() {
		return vaccines;
	}

	public void setVaccines(String vaccines) {
		this.vaccines = vaccines;
	}

	// Métodos de utilidad
	public String getFormattedTime() {
		if (hour == null || hour.isEmpty()) {
			return "";
		}
		try {
			String[] parts = hour.split(":");
			if (parts.length >= 2) {
				int h = Integer.parseInt(parts[0]);
				int m = Integer.parseInt(parts[1]);
				return String.format("%02d:%02d", h, m);
			}
		} catch (Exception e) {
			// Ignore format errors
		}
		return hour;
	}

	public String getVaccinesSummary() {
		if (vaccines == null || vaccines.isEmpty()) {
			return "No vaccines specified";
		}
		return vaccines;
	}

	@Override
	public String toString() {
		return String.format(
				"VaccineAppointment [ID=%d, Date=%s, Time=%s, Doctor=%s, Patient=%s, Vaccines=%s]",
				id, date, hour, doctorName, patientName, getVaccinesSummary());
	}
}