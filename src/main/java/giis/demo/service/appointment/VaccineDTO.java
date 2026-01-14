package giis.demo.service.appointment;

public class VaccineDTO {
	private int id;
	private String name;
	private String doseType;
	private int recommendedDoses;
	private String administeredDate; // NEW FIELD

	// Constructors
	public VaccineDTO() {
	}

	public VaccineDTO(String name, String doseType, int recommendedDoses) {
		this.name = name;
		this.doseType = doseType;
		this.recommendedDoses = recommendedDoses;
	}

	public VaccineDTO(String vaccineName, Integer recommendedDoses2) {
		this.name = vaccineName;
		this.recommendedDoses = recommendedDoses2;
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

	public String getDoseType() {
		return doseType;
	}

	public void setDoseType(String doseType) {
		this.doseType = doseType;
	}

	public int getRecommendedDoses() {
		return recommendedDoses;
	}

	public void setRecommendedDoses(int recommendedDoses) {
		this.recommendedDoses = recommendedDoses;
	}

	// NEW: Administered date getter and setter
	public String getAdministeredDate() {
		return administeredDate;
	}

	public void setAdministeredDate(String administeredDate) {
		this.administeredDate = administeredDate;
	}

	@Override
	public String toString() {
		return name + "( " + doseType + " ) " + ", recommendedDoses="
				+ recommendedDoses;
	}
}