package giis.demo.service.appointment.prescription;

import java.util.Objects;

public class MedicationDTO {
	private int id;
	private String medication_name;
	private int amount;
	private int duration;
	private int interval_hours;
	private String notes;

	public MedicationDTO() {
	}

	public MedicationDTO(int id, String medication_name, int amount, int duration, int interval_hours, String notes) {
		this.id = id;
		this.medication_name = medication_name;
		this.amount = amount;
		this.duration = duration;
		this.interval_hours = interval_hours;
		this.notes = notes;
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMedication_name() {
		return medication_name;
	}

	public void setMedication_name(String medication_name) {
		this.medication_name = medication_name;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getInterval_hours() {
		return interval_hours;
	}

	public void setInterval_hours(int interval_hours) {
		this.interval_hours = interval_hours;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		return medication_name + " - " + notes;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		MedicationDTO that = (MedicationDTO) obj;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}