package giis.demo.service.appointment.prescription;

import java.util.Objects;

public class PrescriptionDTO {
	private int id;
	private String name;
	private String notes;

	public PrescriptionDTO() {
	}

	public PrescriptionDTO(int id, String name, String notes) {
		this.id = id;
		this.name = name;
		this.notes = notes;
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		return name + (notes != null && !notes.isEmpty() ? " - " + notes : "");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PrescriptionDTO that = (PrescriptionDTO) obj;
		return id == that.id && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}
}