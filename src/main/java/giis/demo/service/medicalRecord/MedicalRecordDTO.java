package giis.demo.service.medicalRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MedicalRecordDTO {
	private Integer id;
	private Integer patient_id;
	private Integer appointment_id;
	private Integer doctor_id;
	private String disease;
	private String diagnosis_date;
	private String notes;
	private String icd10_code;

	private String doctor_name;
	private String doctor_surname;
	private String appointment_date;
	private String icd10_description;
	private String icd10_chapter;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPatient_id() {
		return patient_id;
	}

	public void setPatient_id(Integer patient_id) {
		this.patient_id = patient_id;
	}

	public Integer getAppointment_id() {
		return appointment_id;
	}

	public void setAppointment_id(Integer appointment_id) {
		this.appointment_id = appointment_id;
	}

	public Integer getDoctor_id() {
		return doctor_id;
	}

	public void setDoctor_id(Integer doctor_id) {
		this.doctor_id = doctor_id;
	}

	public String getIcd10_code() {
		return icd10_code;
	}

	public void setIcd10_code(String icd10_code) {
		this.icd10_code = icd10_code;
	}

	public String getDisease() {
		return disease;
	}

	public void setDisease(String disease) {
		this.disease = disease;
	}

	public String getDiagnosis_date() {
		return diagnosis_date;
	}

	public void setDiagnosis_date(String diagnosis_date) {
		this.diagnosis_date = diagnosis_date;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getDoctor_name() {
		return doctor_name;
	}

	public void setDoctor_name(String doctor_name) {
		this.doctor_name = doctor_name;
	}

	public String getDoctor_surname() {
		return doctor_surname;
	}

	public void setDoctor_surname(String doctor_surname) {
		this.doctor_surname = doctor_surname;
	}

	public String getAppointment_date() {
		return appointment_date;
	}

	public void setAppointment_date(String appointment_date) {
		this.appointment_date = appointment_date;
	}

	public String getIcd10_description() {
		return icd10_description;
	}

	public void setIcd10_description(String icd10_description) {
		this.icd10_description = icd10_description;
	}

	public String getIcd10_chapter() {
		return icd10_chapter;
	}

	public void setIcd10_chapter(String icd10_category) {
		this.icd10_chapter = icd10_category;
	}

	public String getFormattedDiagnosisDate() {
		try {
			LocalDate date = LocalDate.parse(diagnosis_date);
			return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
		} catch (Exception e) {
			return diagnosis_date;
		}
	}

	public String getDoctorFullName() {
		if (doctor_name != null && doctor_surname != null) {
			return "Dr. " + doctor_name + " " + doctor_surname;
		}
		return "Doctor ID: " + doctor_id;
	}

	public String getFullIcd10Description() {
		if (icd10_code != null && icd10_description != null) {
			return icd10_code + " - " + icd10_description;
		}
		return icd10_code != null ? icd10_code : "Unknown";
	}

	@Override
	public String toString() {
		return "MedicalRecordDTO{id=" + id + ", disease=" + disease + ", date="
				+ diagnosis_date + "}";
	}
}