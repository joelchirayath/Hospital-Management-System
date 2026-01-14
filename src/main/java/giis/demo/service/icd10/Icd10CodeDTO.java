package giis.demo.service.icd10;

public class Icd10CodeDTO {
	private Integer id;
	private String code;
	private String description;
	private String category;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getFullDescription() {
		return code + " - " + description;
	}

	@Override
	public String toString() {
		return "Icd10CodeDTO{code=" + code + ", description=" + description
			+ "}";
	}
}