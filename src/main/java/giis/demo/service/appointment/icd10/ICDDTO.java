package giis.demo.service.appointment.icd10;

public class ICDDTO {
	private String code;
	private String description;
	private String category;
	private String section; // Nueva propiedad

	// Constructor
	public ICDDTO(String code, String description, String category, String section) {
		this.code = code;
		this.description = description;
		this.category = category;
		this.section = section;
	}

	// Getters
	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public String getSection() {
		return section;
	}

	// Setters
	public void setCode(String code) {
		this.code = code;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setSection(String section) {
		this.section = section;
	}

	@Override
	public String toString() {
		return code + " - " + description + " (" + category + ") [" + section + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ICDDTO icddto = (ICDDTO) obj;
		return code.equals(icddto.code);
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}
}